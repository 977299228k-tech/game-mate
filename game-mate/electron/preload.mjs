import { contextBridge, ipcRenderer } from 'electron'

function readArgument(name) {
  const prefix = `--${name}=`
  const value = process.argv.find((item) => item.startsWith(prefix))
  return value ? value.slice(prefix.length) : ''
}

const apiBaseUrl = decodeURIComponent(readArgument('gamemate-api-base') || '')
const updateEnabled = readArgument('gamemate-update-enabled') === '1'

contextBridge.exposeInMainWorld('gameMateDesktop', Object.freeze({
  isDesktop: true,
  platform: process.platform,
  apiBaseUrl,
  updateEnabled,
  checkForUpdates: () => ipcRenderer.invoke('desktop:check-for-updates'),
  installUpdate: () => ipcRenderer.invoke('desktop:install-update'),
  onUpdateStatus: (listener) => {
    if (typeof listener !== 'function') return () => {}
    const handler = (_event, payload) => listener(payload)
    ipcRenderer.on('desktop:update-status', handler)
    return () => ipcRenderer.removeListener('desktop:update-status', handler)
  }
}))

