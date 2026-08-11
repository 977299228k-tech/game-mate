import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import { spawn } from 'node:child_process'
import { build } from 'esbuild'
import JavaScriptObfuscator from 'javascript-obfuscator'

const projectRoot = path.resolve(import.meta.dirname, '..')
const desktopOutput = path.join(projectRoot, 'desktop-dist')
const rendererOutput = path.join(projectRoot, 'dist')
const viteEntry = path.join(projectRoot, 'node_modules', 'vite', 'bin', 'vite.js')

function run(command, args, options = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, {
      cwd: projectRoot,
      stdio: 'inherit',
      shell: false,
      ...options
    })
    child.on('error', reject)
    child.on('exit', (code) => {
      if (code === 0) resolve()
      else reject(new Error(`${command} exited with code ${code}`))
    })
  })
}

function normalizeUrl(value) {
  return String(value || '').trim().replace(/\/+$/, '')
}

function validateEndpoint(name, value, required) {
  if (!value && !required) return
  if (!value) throw new Error(`${name} is required`)

  const parsed = new URL(value)
  const isLocal = ['localhost', '127.0.0.1'].includes(parsed.hostname)
  if (parsed.protocol !== 'https:' && !isLocal) {
    throw new Error(`${name} must use HTTPS outside localhost`)
  }
}

async function collectJavaScriptFiles(directory) {
  const entries = await fs.readdir(directory, { withFileTypes: true })
  const files = []
  for (const entry of entries) {
    const fullPath = path.join(directory, entry.name)
    if (entry.isDirectory()) files.push(...await collectJavaScriptFiles(fullPath))
    else if (entry.isFile() && entry.name.endsWith('.js')) files.push(fullPath)
  }
  return files
}

async function obfuscateRenderer() {
  const files = await collectJavaScriptFiles(rendererOutput)
  for (const file of files) {
    const source = await fs.readFile(file, 'utf8')
    // Large chunks are third-party frameworks. The application's own chunks remain protected.
    if (Buffer.byteLength(source, 'utf8') > 650_000) continue

    const result = JavaScriptObfuscator.obfuscate(source, {
      target: 'browser',
      compact: true,
      simplify: true,
      identifierNamesGenerator: 'hexadecimal',
      renameGlobals: false,
      stringArray: true,
      stringArrayEncoding: ['base64'],
      stringArrayThreshold: 0.65,
      splitStrings: true,
      splitStringsChunkLength: 8,
      transformObjectKeys: true,
      unicodeEscapeSequence: false,
      sourceMap: false,
      disableConsoleOutput: true
    })
    await fs.writeFile(file, result.getObfuscatedCode(), 'utf8')
  }
}

const apiBaseUrl = normalizeUrl(process.env.DESKTOP_API_BASE_URL || 'http://localhost:8080')
const updateUrl = normalizeUrl(process.env.DESKTOP_UPDATE_URL || '')
const channel = String(process.env.DESKTOP_UPDATE_CHANNEL || 'latest').trim() || 'latest'

validateEndpoint('DESKTOP_API_BASE_URL', apiBaseUrl, true)
validateEndpoint('DESKTOP_UPDATE_URL', updateUrl, false)

await fs.rm(desktopOutput, { recursive: true, force: true })
await fs.mkdir(desktopOutput, { recursive: true })

await run(process.execPath, [viteEntry, 'build'], {
  env: {
    ...process.env,
    VITE_API_BASE_URL: apiBaseUrl,
    VITE_DESKTOP_BUILD: 'true'
  }
})

await Promise.all([
  build({
    entryPoints: [path.join(projectRoot, 'electron', 'main.mjs')],
    outfile: path.join(desktopOutput, 'main.cjs'),
    bundle: true,
    minify: true,
    platform: 'node',
    format: 'cjs',
    sourcemap: false,
    external: ['electron']
  }),
  build({
    entryPoints: [path.join(projectRoot, 'electron', 'preload.mjs')],
    outfile: path.join(desktopOutput, 'preload.cjs'),
    bundle: true,
    minify: true,
    platform: 'node',
    format: 'cjs',
    sourcemap: false,
    external: ['electron']
  })
])

await obfuscateRenderer()

await fs.writeFile(
  path.join(desktopOutput, 'runtime-config.json'),
  `${JSON.stringify({ apiBaseUrl, updateUrl, channel }, null, 2)}\n`,
  'utf8'
)

console.log(`Desktop build ready. API: ${apiBaseUrl}`)
console.log(`Automatic updates: ${updateUrl || 'disabled until DESKTOP_UPDATE_URL is configured'}`)
