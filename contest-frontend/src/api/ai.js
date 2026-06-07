const BASE_URL = '/api/ai'

/**
 * 创建 SSE 流式对话
 * @param {number|null} conversationId 会话ID（null=新会话）
 * @param {string} message 用户消息
 * @param {function(string)} onMessage 流式内容回调
 * @param {function()} onDone 完成回调
 * @param {function(Error, number)} onError 错误回调 (error, httpStatus)
 * @param {function(number)} onStart 开始回调 (conversationId)
 */
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
    // HTTP 级错误（非 200）
    if (!response.ok) {
      const body = await response.text().catch(() => '')
      onError(new Error(body || '请求失败'), response.status)
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
            onError(new Error(event.eventData || '服务器错误'), 0)
          } else if (event.eventType === 'stop') {
            onDone()
          }
        } catch (e) {
          // ignore parse errors for partial lines
        }
      }
    }
  }).catch(err => {
    onError(err, 0)
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

import request from './request'

export function listConversations() {
  return request.get('/ai/conversations')
}

export function deleteConversation(id) {
  return request.post(`/ai/conversations/${id}`)
}

export function getConversationMessages(id) {
  return request.get(`/ai/conversations/${id}/messages`)
}

export function batchDeleteConversations(ids) {
  return request.post('/ai/conversations/batch-delete', ids)
}
