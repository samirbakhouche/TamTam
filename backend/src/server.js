const express = require('express');
const http = require('http');
const path = require('path');
const WebSocket = require('ws');
const cors = require('cors');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: '/ws' });

const PORT = process.env.PORT || 5000;
const JWT_SECRET = process.env.JWT_SECRET || 'tamtam_super_secure_jwt_secret_key_2026';

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve static admin portal
app.use('/admin', express.static(path.join(__dirname, '../../admin')));
app.get('/', (req, res) => {
    res.redirect('/admin');
});

// In-Memory store for active WebSocket clients
// Map<userId, Set<WebSocket>>
const activeClients = new Map();

// Helper: Broadcast to specific user IDs
function broadcastToUsers(userIds, event, payload) {
    const data = JSON.stringify({ event, payload });
    userIds.forEach(uid => {
        const clientSet = activeClients.get(String(uid));
        if (clientSet) {
            clientSet.forEach(ws => {
                if (ws.readyState === WebSocket.OPEN) {
                    ws.send(data);
                }
            });
        }
    });
}

// Global broadcast (for presence / stories)
function broadcastAll(event, payload) {
    const data = JSON.stringify({ event, payload });
    wss.clients.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
            client.send(data);
        }
    });
}

// WebSocket Connection Handling
wss.on('connection', (ws, req) => {
    let authenticatedUserId = null;

    ws.on('message', (message) => {
        try {
            const data = JSON.parse(message);
            const { event, payload } = data;

            switch (event) {
                case 'auth:handshake': {
                    const token = payload?.token;
                    if (!token) {
                        ws.send(JSON.stringify({ event: 'error', message: 'Token required' }));
                        return;
                    }
                    try {
                        const decoded = jwt.verify(token, JWT_SECRET);
                        authenticatedUserId = String(decoded.id);

                        if (!activeClients.has(authenticatedUserId)) {
                            activeClients.set(authenticatedUserId, new Set());
                        }
                        activeClients.get(authenticatedUserId).add(ws);

                        ws.send(JSON.stringify({
                            event: 'auth:success',
                            payload: { user_id: authenticatedUserId, status: 'connected' }
                        }));

                        // Broadcast user is online
                        broadcastAll('user:presence', {
                            user_id: authenticatedUserId,
                            is_online: true,
                            last_seen: new Date().toISOString()
                        });
                    } catch (err) {
                        ws.send(JSON.stringify({ event: 'error', message: 'Invalid token' }));
                    }
                    break;
                }

                case 'typing:start':
                case 'typing:stop': {
                    if (!authenticatedUserId) return;
                    const { conversation_id, recipient_ids } = payload || {};
                    if (recipient_ids && Array.isArray(recipient_ids)) {
                        broadcastToUsers(recipient_ids, event, {
                            user_id: authenticatedUserId,
                            conversation_id,
                            is_typing: event === 'typing:start'
                        });
                    }
                    break;
                }

                case 'message:send': {
                    if (!authenticatedUserId) return;
                    const { conversation_id, recipient_ids, content, type, temp_id } = payload || {};
                    const newMsg = {
                        id: Date.now(),
                        conversation_id,
                        sender_id: Number(authenticatedUserId),
                        type: type || 'text',
                        content,
                        temp_id,
                        created_at: new Date().toISOString(),
                        delivery_status: 'delivered'
                    };

                    // Broadcast to recipients and sender
                    const targetUsers = recipient_ids ? [...recipient_ids, authenticatedUserId] : [authenticatedUserId];
                    broadcastToUsers(targetUsers, 'message:new', newMsg);
                    break;
                }

                case 'presence:ping': {
                    ws.send(JSON.stringify({ event: 'presence:pong', timestamp: Date.now() }));
                    break;
                }
            }
        } catch (err) {
            console.error('WS Error:', err);
        }
    });

    ws.on('close', () => {
        if (authenticatedUserId && activeClients.has(authenticatedUserId)) {
            const userSet = activeClients.get(authenticatedUserId);
            userSet.delete(ws);
            if (userSet.size === 0) {
                activeClients.delete(authenticatedUserId);
                // Broadcast offline status
                broadcastAll('user:presence', {
                    user_id: authenticatedUserId,
                    is_online: false,
                    last_seen: new Date().toISOString()
                });
            }
        }
    });
});

// Mock/Real Hybrid Data Layer for Development & Immediate Testing
const users = [
    { id: 1, uuid: 'u1', username: 'samir_dev', phone: '+213550112233', email: 'samir@tamtam.dz', display_name: 'Samir Bakhouche', avatar_url: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200', bio: 'Senior Android & Backend Engineer', is_online: true },
    { id: 2, uuid: 'u2', username: 'amira_tech', phone: '+213550445566', email: 'amira@tamtam.dz', display_name: 'Amira Mansour', avatar_url: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200', bio: 'Product Designer & UI Specialist', is_online: true },
    { id: 3, uuid: 'u3', username: 'tamtam_team', phone: '+213550998877', email: 'support@tamtam.dz', display_name: 'TAMTAM Official Team', avatar_url: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200', bio: 'Welcome to TAMTAM Messenger!', is_online: true }
];

const conversations = [
    {
        id: 1,
        type: 'direct',
        title: 'Amira Mansour',
        avatar_url: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200',
        last_message: 'مرحباً سمير! هل التطبيق جاهز للتجربة؟',
        last_message_at: '10:45 AM',
        unread_count: 2,
        is_online: true,
        recipient_id: 2
    },
    {
        id: 2,
        type: 'group',
        title: 'TAMTAM Core Developers',
        avatar_url: 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=200',
        last_message: 'MySQL Schema & Realtime WebSockets deployed successfully! 🚀',
        last_message_at: '09:30 AM',
        unread_count: 0,
        is_online: false,
        recipient_id: null
    },
    {
        id: 3,
        type: 'direct',
        title: 'TAMTAM Official Team',
        avatar_url: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200',
        last_message: 'أهلاً بك في منصة طمطم للمراسلة الآمنة والسريعة!',
        last_message_at: 'Yesterday',
        unread_count: 0,
        is_online: true,
        recipient_id: 3
    }
];

const messages = [
    { id: 101, conversation_id: 1, sender_id: 2, type: 'text', content: 'السلام عليكم ورحمة الله!', created_at: '10:40 AM', delivery_status: 'read' },
    { id: 102, conversation_id: 1, sender_id: 1, type: 'text', content: 'وعليكم السلام يا أميرة! تم إعداد البنية التحتية بالكامل.', created_at: '10:42 AM', delivery_status: 'read' },
    { id: 103, conversation_id: 1, sender_id: 2, type: 'text', content: 'مرحباً سمير! هل التطبيق جاهز للتجربة؟', created_at: '10:45 AM', delivery_status: 'delivered' }
];

// --- REST API ENDPOINTS ---

// Health & Status
app.get('/api/v1/health', (req, res) => {
    res.json({
        status: 'online',
        service: 'TAMTAM Messaging Backend',
        version: '1.0.0',
        timestamp: new Date().toISOString(),
        active_websocket_connections: wss.clients.size
    });
});

// Authentication
app.post('/api/v1/auth/login', (req, res) => {
    const { identifier, password } = req.body;
    const user = users.find(u => u.username === identifier || u.phone === identifier || u.email === identifier) || users[0];
    const token = jwt.sign({ id: user.id, username: user.username }, JWT_SECRET, { expiresIn: '7d' });

    res.json({
        status: 'success',
        data: {
            user,
            token
        }
    });
});

app.post('/api/v1/auth/register', (req, res) => {
    const { username, phone, email, display_name } = req.body;
    const newUser = {
        id: users.length + 1,
        uuid: 'u' + (users.length + 1),
        username: username || 'user_' + Date.now(),
        phone: phone || '+21350000000',
        email: email || `${username}@tamtam.dz`,
        display_name: display_name || username || 'New User',
        avatar_url: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200',
        bio: 'Hey there! I am using TAMTAM.',
        is_online: true
    };
    users.push(newUser);
    const token = jwt.sign({ id: newUser.id, username: newUser.username }, JWT_SECRET, { expiresIn: '7d' });

    res.status(201).json({
        status: 'success',
        data: {
            user: newUser,
            token
        }
    });
});

// Conversations
app.get('/api/v1/conversations', (req, res) => {
    res.json({
        status: 'success',
        data: conversations
    });
});

// Messages in conversation
app.get('/api/v1/conversations/:id/messages', (req, res) => {
    const convId = parseInt(req.params.id, 10);
    const convMessages = messages.filter(m => m.conversation_id === convId);
    res.json({
        status: 'success',
        data: convMessages
    });
});

app.post('/api/v1/conversations/:id/messages', (req, res) => {
    const convId = parseInt(req.params.id, 10);
    const { content, type = 'text', sender_id = 1 } = req.body;

    const newMsg = {
        id: Date.now(),
        conversation_id: convId,
        sender_id,
        type,
        content,
        created_at: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        delivery_status: 'delivered'
    };
    messages.push(newMsg);

    // Update conversation last message
    const conv = conversations.find(c => c.id === convId);
    if (conv) {
        conv.last_message = content;
        conv.last_message_at = newMsg.created_at;
    }

    // Broadcast through WebSocket
    broadcastAll('message:new', newMsg);

    res.status(201).json({
        status: 'success',
        data: newMsg
    });
});

// Admin Analytics API
app.get('/api/v1/admin/stats', (req, res) => {
    res.json({
        total_users: users.length + 15420,
        active_users_24h: 8940,
        total_conversations: conversations.length + 32100,
        total_messages: messages.length + 420800,
        pending_reports: 3,
        system_uptime: '99.98%'
    });
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`[TAMTAM API] Server running on http://0.0.0.0:${PORT}`);
    console.log(`[TAMTAM WS] WebSocket running on ws://0.0.0.0:${PORT}/ws`);
});
