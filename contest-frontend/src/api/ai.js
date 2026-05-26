const BASE_URL = '/api/ai'

export function createChatStream(conversationId, message, onMessage, onDone, onError) {
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
    let currentEvent = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEvent = line[6] === ' ' ? line.slice(7).trim() : line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          const data = line[5] === ' ' ? line.slice(6) : line.slice(5)
          if (currentEvent === 'done') {
            onDone()
          } else if (currentEvent === 'message' || currentEvent === '') {
            if (data) onMessage(data)
          } else if (currentEvent === 'error') {
            if (data) onError(new Error(data))
          }
        }
      }
    }
    onDone()
  }).catch(err => {
    onError(err)
  })
}
