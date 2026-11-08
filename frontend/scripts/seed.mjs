// Fills a local Gamergram API with demo content through its public endpoints.
// Usage: npm run seed            (API_URL overrides http://localhost:8080)
const API = process.env.API_URL ?? 'http://localhost:8080'
const PASSWORD = 'demo-password'

const USERS = [
  ['pixelvex', 'Ranked grinder. Support main.'],
  ['nightowl', 'Soulslike completionist.'],
  ['tinkerbyte', 'Factory games until sunrise.'],
  ['ghostframe', 'Virtual photography.'],
  ['mira_gg', 'Speedruns and bad puns.'],
]
const GAMES = ['Elden Ring', 'Counter-Strike 2', 'Hades II', 'Factorio', 'Cyberpunk 2077', 'Stardew Valley']
const CAPTIONS = [
  'Took me forty tries, worth it.',
  'Photo mode never disappoints.',
  'Clutched the round 1v4.',
  'Tonight’s build. Do not ask about the spaghetti.',
  'This view stopped me mid-quest.',
  'New personal best!',
  'Sunday session.',
  null,
]
const COMMENTS = ['Clean!', 'How long did that take?', 'That lighting though', 'Teach me', 'GG']
const SIZES = [[600, 800], [600, 600], [600, 900], [600, 450], [600, 750]]
const POSTS_PER_USER = 9

async function call(path, { method = 'GET', body, token } = {}) {
  const response = await fetch(API + path, {
    method,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  })
  if (!response.ok && response.status !== 409) {
    throw new Error(`${method} ${path}: ${response.status} ${await response.text()}`)
  }
  const text = await response.text()
  return text ? JSON.parse(text) : undefined
}

const accounts = []
for (const [username, bio] of USERS) {
  await call('/api/auth/register', {
    method: 'POST',
    body: { username, email: `${username}@example.com`, password: PASSWORD },
  })
  const { token } = await call('/api/auth/login', { method: 'POST', body: { username, password: PASSWORD } })
  const me = await call('/api/users/me', { token })
  await call('/api/users/me', { method: 'PATCH', token, body: { bio } })
  accounts.push({ ...me, token })
}

const [first] = accounts
const existing = await call(`/api/posts/user/${first.id}?size=1`, { token: first.token })
if (existing.totalElements > 0) {
  console.log('Demo posts already exist, nothing to add.')
  process.exit(0)
}

const games = []
for (const title of GAMES) {
  const [found] = await call(`/api/games?title=${encodeURIComponent(title)}`, { token: first.token })
  games.push(found ?? (await call('/api/games', { method: 'POST', token: first.token, body: { title } })))
}

let serial = 0
for (const [index, account] of accounts.entries()) {
  for (const other of accounts) {
    if (other.id !== account.id) await call(`/api/users/${other.id}/follow`, { method: 'POST', token: account.token })
  }
  await call('/api/me/games', { method: 'POST', token: account.token, body: { gameId: games[index].id, relationType: 'FAVORITE' } })
  await call('/api/me/games', { method: 'POST', token: account.token, body: { gameId: games[index + 1].id, relationType: 'WISHLIST' } })

  for (let i = 0; i < POSTS_PER_USER; i++) {
    serial++
    const [width, height] = SIZES[serial % SIZES.length]
    const post = await call('/api/posts', {
      method: 'POST',
      token: account.token,
      body: {
        mediaUrl: `https://picsum.photos/seed/gamergram${serial}/${width}/${height}`,
        mediaType: 'PHOTO',
        caption: CAPTIONS[serial % CAPTIONS.length] ?? undefined,
        gameId: games[serial % games.length].id,
      },
    })
    const reader = accounts[(index + 1 + i) % accounts.length]
    await call(`/api/posts/${post.id}/likes`, { method: 'POST', token: reader.token })
    await call(`/api/posts/${post.id}/comments`, {
      method: 'POST',
      token: reader.token,
      body: { body: COMMENTS[serial % COMMENTS.length] },
    })
  }
}

console.log(`Seeded ${accounts.length} users and ${serial} posts. Sign in as ${first.username} / ${PASSWORD}`)
