const fs = require('node:fs')
const path = require('node:path')

function readRuntimeConfig() {
  const file = path.join(__dirname, 'desktop-dist', 'runtime-config.json')
  if (!fs.existsSync(file)) return { updateUrl: '' }
  return JSON.parse(fs.readFileSync(file, 'utf8'))
}

const runtimeConfig = readRuntimeConfig()
const publish = runtimeConfig.updateUrl
  ? [{ provider: 'generic', url: runtimeConfig.updateUrl, channel: runtimeConfig.channel || 'latest' }]
  : null

const win = {
  target: [{ target: 'nsis', arch: ['x64'] }],
  artifactName: 'AI电竞经理-Setup-${version}-${arch}.${ext}',
  verifyUpdateCodeSignature: true
}
if (process.env.WINDOWS_PUBLISHER_NAME) {
  win.publisherName = process.env.WINDOWS_PUBLISHER_NAME
}

module.exports = {
  appId: 'com.gamemate.desktop',
  productName: 'AI电竞经理',
  copyright: 'Copyright © 2026 AI电竞经理',
  asar: true,
  compression: 'maximum',
  forceCodeSigning: process.env.REQUIRE_CODE_SIGNING === 'true',
  electronFuses: {
    runAsNode: false,
    enableCookieEncryption: true,
    enableNodeOptionsEnvironmentVariable: false,
    enableNodeCliInspectArguments: false,
    enableEmbeddedAsarIntegrityValidation: true,
    onlyLoadAppFromAsar: true
  },
  directories: {
    output: 'release',
    buildResources: 'build-resources'
  },
  files: [
    'dist/**/*',
    'desktop-dist/**/*',
    'package.json',
    '!src{,/**/*}',
    '!electron{,/**/*}',
    '!scripts{,/**/*}',
    '!*.config.*',
    '!**/*.map',
    '!*.env*'
  ],
  win,
  nsis: {
    oneClick: false,
    perMachine: false,
    allowToChangeInstallationDirectory: true,
    createDesktopShortcut: true,
    createStartMenuShortcut: true,
    shortcutName: 'AI电竞经理',
    uninstallDisplayName: 'AI电竞经理',
    deleteAppDataOnUninstall: false
  },
  publish
}
