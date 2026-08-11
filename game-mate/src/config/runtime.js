function normalizeOrigin(value) {
  return String(value || '').trim().replace(/\/+$/, '')
}

export function getApiOrigin() {
  return normalizeOrigin(
    window.gameMateDesktop?.apiBaseUrl || import.meta.env.VITE_API_BASE_URL || ''
  )
}

export function buildServerUrl(path) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${getApiOrigin()}${normalizedPath}`
}

export function resolveServerAsset(value) {
  if (typeof value !== 'string' || !value.startsWith('/uploads/')) return value
  return buildServerUrl(value)
}

export function resolveServerAssets(value) {
  if (Array.isArray(value)) return value.map(resolveServerAssets)
  if (value && typeof value === 'object') {
    Object.keys(value).forEach((key) => {
      value[key] = resolveServerAssets(value[key])
    })
    return value
  }
  return resolveServerAsset(value)
}

