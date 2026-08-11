import fs from 'node:fs'
import path from 'node:path'
import { pathToFileURL } from 'node:url'
import { app, BrowserWindow, ipcMain, net, protocol, session, shell } from 'electron'
import { autoUpdater } from 'electron-updater'

const APP_ID = 'com.gamemate.desktop'
const APP_PROTOCOL = 'gamemate'
const APP_HOST = 'app'

protocol.registerSchemesAsPrivileged([
  {
    scheme: APP_PROTOCOL,
    privileges: {
      standard: true,
      secure: true,
      supportFetchAPI: true,
      corsEnabled: true,
      stream: true
    }
  }
])

let mainWindow = null

function loadRuntimeConfig() {
  const configPath = path.join(__dirname, 'runtime-config.json')
  try {
    return JSON.parse(fs.readFileSync(configPath, 'utf8'))
  } catch {
    return {
      apiBaseUrl: 'http://localhost:8080',
      updateUrl: '',
      channel: 'latest'
    }
  }
}

const runtimeConfig = loadRuntimeConfig()

function isAllowedExternalUrl(rawUrl) {
  try {
    return new URL(rawUrl).protocol === 'https:'
  } catch {
    return false
  }
}

function registerApplicationProtocol() {
  const rendererRoot = path.resolve(__dirname, '..', 'dist')

  protocol.handle(APP_PROTOCOL, (request) => {
    const requestUrl = new URL(request.url)
    const requestedPath = decodeURIComponent(requestUrl.pathname || '/').replace(/^[/\\]+/, '')
    const relativePath = requestedPath || 'index.html'
    let targetPath = path.resolve(rendererRoot, relativePath)

    if (!targetPath.startsWith(`${rendererRoot}${path.sep}`) && targetPath !== rendererRoot) {
      targetPath = path.join(rendererRoot, 'index.html')
    }
    if (!fs.existsSync(targetPath) || fs.statSync(targetPath).isDirectory()) {
      targetPath = path.join(rendererRoot, 'index.html')
    }

    return net.fetch(pathToFileURL(targetPath).toString())
  })
}

function configurePermissions() {
  const appSession = session.defaultSession
  const isTrustedPage = (webContents) => webContents === mainWindow?.webContents

  appSession.setPermissionCheckHandler((webContents, permission) => {
    return isTrustedPage(webContents) && permission === 'media'
  })
  appSession.setPermissionRequestHandler((webContents, permission, callback) => {
    callback(isTrustedPage(webContents) && permission === 'media')
  })
}

function sendUpdaterStatus(status, payload = {}) {
  if (!mainWindow || mainWindow.isDestroyed()) return
  mainWindow.webContents.send('desktop:update-status', { status, ...payload })
}

function configureAutoUpdater() {
  if (!app.isPackaged || !runtimeConfig.updateUrl) return

  autoUpdater.autoDownload = true
  autoUpdater.autoInstallOnAppQuit = true
  autoUpdater.allowDowngrade = false
  autoUpdater.setFeedURL({
    provider: 'generic',
    url: runtimeConfig.updateUrl,
    channel: runtimeConfig.channel || 'latest'
  })

  autoUpdater.on('checking-for-update', () => sendUpdaterStatus('checking'))
  autoUpdater.on('update-available', (info) => sendUpdaterStatus('available', { version: info.version }))
  autoUpdater.on('update-not-available', () => sendUpdaterStatus('current'))
  autoUpdater.on('download-progress', (progress) => {
    sendUpdaterStatus('downloading', { percent: Math.round(progress.percent || 0) })
  })
  autoUpdater.on('update-downloaded', (info) => sendUpdaterStatus('downloaded', { version: info.version }))
  autoUpdater.on('error', (error) => sendUpdaterStatus('error', { message: error.message }))

  setTimeout(() => {
    autoUpdater.checkForUpdates().catch((error) => sendUpdaterStatus('error', { message: error.message }))
  }, 8000)
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1440,
    height: 900,
    minWidth: 1100,
    minHeight: 700,
    show: false,
    backgroundColor: '#0b1020',
    title: 'AI电竞经理',
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(__dirname, 'preload.cjs'),
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: true,
      webSecurity: true,
      devTools: !app.isPackaged,
      additionalArguments: [
        `--gamemate-api-base=${encodeURIComponent(runtimeConfig.apiBaseUrl || '')}`,
        `--gamemate-update-enabled=${runtimeConfig.updateUrl ? '1' : '0'}`
      ]
    }
  })

  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    if (isAllowedExternalUrl(url)) shell.openExternal(url)
    return { action: 'deny' }
  })
  mainWindow.webContents.on('will-navigate', (event, url) => {
    if (!url.startsWith(`${APP_PROTOCOL}://${APP_HOST}/`)) event.preventDefault()
  })
  mainWindow.once('ready-to-show', () => mainWindow?.show())
  mainWindow.on('closed', () => {
    mainWindow = null
  })

  return mainWindow.loadURL(`${APP_PROTOCOL}://${APP_HOST}/index.html`)
}

ipcMain.handle('desktop:check-for-updates', async () => {
  if (!app.isPackaged || !runtimeConfig.updateUrl) return { enabled: false }
  await autoUpdater.checkForUpdates()
  return { enabled: true }
})

ipcMain.handle('desktop:install-update', () => {
  if (app.isPackaged && runtimeConfig.updateUrl) autoUpdater.quitAndInstall(false, true)
})

const hasSingleInstanceLock = app.requestSingleInstanceLock()
if (!hasSingleInstanceLock) {
  app.quit()
} else {
  app.on('second-instance', () => {
    if (!mainWindow) return
    if (mainWindow.isMinimized()) mainWindow.restore()
    mainWindow.show()
    mainWindow.focus()
  })

  app.whenReady().then(async () => {
    app.setAppUserModelId(APP_ID)
    registerApplicationProtocol()
    await createWindow()
    configurePermissions()
    configureAutoUpdater()
  })
}

app.on('window-all-closed', () => app.quit())

