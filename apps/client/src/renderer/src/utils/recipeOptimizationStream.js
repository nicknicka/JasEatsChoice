const EVENT_BOUNDARY = /\r?\n\r?\n/

const parseEventBlock = (block, onEvent) => {
  let eventName = 'message'
  const dataLines = []

  for (const line of block.split(/\r?\n/)) {
    if (!line || line.startsWith(':')) continue

    const separatorIndex = line.indexOf(':')
    const field = separatorIndex === -1 ? line : line.slice(0, separatorIndex)
    let value = separatorIndex === -1 ? '' : line.slice(separatorIndex + 1)
    if (value.startsWith(' ')) value = value.slice(1)

    if (field === 'event') {
      eventName = value || 'message'
    } else if (field === 'data') {
      dataLines.push(value)
    }
  }

  if (dataLines.length === 0) return

  const rawData = dataLines.join('\n')
  let data
  try {
    data = JSON.parse(rawData)
  } catch {
    throw new Error('SSE 事件数据不是合法 JSON')
  }

  onEvent({ event: eventName, data })
}

export function createRecipeSSEParser(onEvent) {
  let buffer = ''

  const drain = () => {
    let boundary = buffer.match(EVENT_BOUNDARY)
    while (boundary?.index !== undefined) {
      const block = buffer.slice(0, boundary.index)
      buffer = buffer.slice(boundary.index + boundary[0].length)
      if (block.trim()) parseEventBlock(block, onEvent)
      boundary = buffer.match(EVENT_BOUNDARY)
    }
  }

  return {
    feed(chunk) {
      buffer += chunk
      drain()
    },
    finish() {
      if (buffer.trim()) parseEventBlock(buffer, onEvent)
      buffer = ''
    }
  }
}

export async function readRecipeEventStream(reader, onEvent) {
  const decoder = new TextDecoder()
  const parser = createRecipeSSEParser(onEvent)

  // eslint-disable-next-line no-constant-condition
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    parser.feed(decoder.decode(value, { stream: true }))
  }

  parser.feed(decoder.decode())
  parser.finish()
}
