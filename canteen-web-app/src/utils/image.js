const PRODUCT_PLACEHOLDER = '/images/products/placeholder.svg'

export const normalizeImageUrl = (imageUrl, fallback = PRODUCT_PLACEHOLDER) => {
  if (!imageUrl) return fallback

  const url = String(imageUrl).trim()
  if (!url) return fallback
  if (/^https?:\/\//i.test(url)) return url
  if (url.startsWith('/uploads/')) return url
  if (url.startsWith('/images/')) return url
  if (url.startsWith('uploads/')) return `/${url}`
  if (url.startsWith('images/')) return `/${url}`

  return fallback
}

export const setImageFallback = (event, fallback = PRODUCT_PLACEHOLDER) => {
  if (event?.target) {
    event.target.src = fallback
  }
}

export { PRODUCT_PLACEHOLDER }
