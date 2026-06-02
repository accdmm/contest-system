export function sanitizeHtml(html) {
  if (!html) return ''
  let s = html
  s = s.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
  s = s.replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, '')
  s = s.replace(/<object\b[^<]*(?:(?!<\/object>)<[^<]*)*<\/object>/gi, '')
  s = s.replace(/<embed\b[^<]*(?:(?!<\/embed>)<[^<]*)*<\/embed>/gi, '')
  s = s.replace(/\son\w+\s*=\s*["'][^"']*["']/gi, '')
  s = s.replace(/\son\w+\s*=\s*[^\s>]+/gi, '')
  s = s.replace(/href\s*=\s*["']\s*javascript\s*:[^"']*["']/gi, 'href="#"')
  s = s.replace(/src\s*=\s*["']\s*javascript\s*:[^"']*["']/gi, 'src="#"')
  return s
}
