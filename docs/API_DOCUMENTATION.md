# TAMTAM REST API & WebSocket Real-Time Specification

## Base URL
- Production: `https://api.tamtam-messenger.com/api/v1`
- Development / Local: `http://localhost:5000/api/v1`
- WebSocket Server: `ws://localhost:5000/ws`

## Authentication
Protected endpoints require a Bearer token in the `Authorization` header:
```
Authorization: Bearer <access_token>
```

---

## 1. Authentication Endpoints (`/api/v1/auth`)

### `POST /auth/register`
Register new account with email/phone, password, username, and full name.
```json
{
  "username": "karim_dz",
  "phone": "+213555123456",
  "email": "karim@example.com",
  "password": "StrongPassword123!",
  "display_name": "Karim Benali"
}
```
**Response 201 Created**:
```json
{
  "status": "success",
  "data": {
    "user": {
      "id": 1,
      "uuid": "8e3bfa70-a81d-4eb2-8c11-fb45fbe2ef41",
      "username": "karim_dz",
      "phone": "+213555123456",
      "email": "karim@example.com",
      "display_name": "Karim Benali",
      "avatar_url": null
    },
    "token": "eyJhbGciOi...",
    "refresh_token": "def50200..."
  }
}
```

### `POST /auth/login`
Authenticate with username, email, or phone + password.
```json
{
  "identifier": "karim_dz",
  "password": "StrongPassword123!"
}
```

### `POST /auth/send-otp`
Request SMS / Email OTP verification code.
```json
{
  "phone": "+213555123456"
}
```

### `POST /auth/verify-otp`
Validate OTP code and obtain session token.
```json
{
  "phone": "+213555123456",
  "code": "482910"
}
```

---

## 2. Conversations & Messaging (`/api/v1/conversations`, `/api/v1/messages`)

### `GET /conversations`
List user's active conversations with unread counts and latest messages.

### `POST /conversations/direct`
Start or retrieve an existing 1:1 direct conversation with a recipient user.
```json
{
  "recipient_id": 2
}
```

### `GET /conversations/:conversationId/messages`
Paginated message history for a conversation.
Query Params: `limit=30&cursor=10492`

### `POST /conversations/:conversationId/messages`
Send text or attachment message.
```json
{
  "type": "text",
  "content": "السلام عليكم، كيف الحال؟",
  "reply_to_id": null
}
```

### `PUT /messages/:messageId`
Edit a previously sent message.

### `DELETE /messages/:messageId`
Delete message (for everyone or for current user).

### `POST /messages/:messageId/reactions`
Add emoji reaction (e.g. `❤️`, `👍`, `🔥`, `😂`, `👏`).

---

## 3. Real-Time WebSockets Engine (`/ws`)

### Client-to-Server Events:
- `auth:handshake`: Send `{ "token": "<jwt>" }` upon connection.
- `typing:start`: Send `{ "conversation_id": 12 }` when user types.
- `typing:stop`: Send `{ "conversation_id": 12 }` when user stops typing.
- `message:send`: Real-time instant delivery payload.
- `message:read`: Acknowledge read receipt for message IDs.
- `presence:ping`: Keep-alive heartbeat (every 30s).

### Server-to-Client Events:
- `message:new`: Broadcast new message payload to conversation members.
- `message:updated`: Message edit or reaction broadcast.
- `message:status`: Delivery status update (`delivered`, `read`).
- `user:typing`: `{ "user_id": 2, "conversation_id": 12, "is_typing": true }`.
- `user:presence`: `{ "user_id": 2, "is_online": true, "last_seen": "..." }`.
- `call:incoming`: WebRTC incoming call offer.
