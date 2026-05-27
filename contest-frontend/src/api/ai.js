const BASE_URL = '/api/ai'

export function createChatStream(conversationId, message, onMessage, onDone, onError, onStart) {
  const token = localStorage.getItem('token')

  fetch(`${BASE_URL}/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify({ conversationId, message })
  }).then(async response => {
    if (!response.ok) {
      const body = await response.text().catch(() => '')
      onError(new Error(response.status + ': ' + body))
      return
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed.startsWith('data:')) continue
        const jsonStr = trimmed[5] === ' ' ? trimmed.slice(6) : trimmed.slice(5)
        if (!jsonStr) continue
        try {
          const event = JSON.parse(jsonStr)
          if (event.eventType === 'start' && event.eventData && onStart) {
            onStart(event.eventData)
          } else if (event.eventType === 'data' && event.eventData) {
            onMessage(event.eventData)
          } else if (event.eventType === 'error') {
            onError(new Error(event.eventData || '服务器错误'))
          } else if (event.eventType === 'stop') {
            onDone()
          }
        } catch (e) {
          // ignore parse errors for partial lines
        }
      }
    }
  }).catch(err => {
    onError(err)
  })
}

export function stopChatStream(conversationId) {
  const token = localStorage.getItem('token')
  if (!token) return
  fetch(`${BASE_URL}/stop/${conversationId}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  }).catch(() => {})
}
