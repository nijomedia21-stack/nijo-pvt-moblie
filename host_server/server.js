const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const crypto = require('crypto');

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  },
  maxHttpBufferSize: 1e8 // 100 MB payload buffer
});

const PORT = process.env.PORT || 4000;

// Centralized upload storage on Laptop (C:\Users\nijoj\PVT_Shared_Files)
const preferredDir = (process.platform === 'win32' || fs.existsSync('C:\\Users\\nijoj\\PVT_Shared_Files'))
  ? 'C:\\Users\\nijoj\\PVT_Shared_Files'
  : (process.env.SHARED_FILES_DIR || path.join(__dirname, 'uploads'));

let UPLOADS_DIR = preferredDir;
try {
  if (!fs.existsSync(UPLOADS_DIR)) {
    fs.mkdirSync(UPLOADS_DIR, { recursive: true });
  }
} catch (_) {
  UPLOADS_DIR = path.join(__dirname, 'uploads');
  if (!fs.existsSync(UPLOADS_DIR)) {
    fs.mkdirSync(UPLOADS_DIR, { recursive: true });
  }
}

// In-Memory Master State (Centralized Laptop Hub)
const state = {
  aiName: 'Nijo Copilot',
  connectedDevices: new Map(), // socketId -> { id, type, name, ip, connectedAt }
  clipboardHistory: [
    {
      id: 'clip_default_1',
      sender: 'Laptop',
      target: 'all',
      content: 'https://ais-dev-m4qogna3zkvcbvi3hhxavc-771692029145.asia-southeast1.run.app',
      type: 'url',
      timestamp: Date.now() - 3600000
    }
  ],
  tasks: [
    {
      id: 'task_1',
      title: 'Review AI&ML Semester 1 Syllabus',
      notes: 'Focus on Unit 1: Matrices, Python vectors, and Semiconductor Physics.',
      status: 'in-progress',
      priority: 'high',
      targetDevice: 'all',
      createdAt: Date.now() - 7200000
    },
    {
      id: 'task_2',
      title: 'Mathrubhumi Calendar Sync',
      notes: 'Verify Kollavarsham 1202 transition and Vishu / Thiruvonam dates.',
      status: 'completed',
      priority: 'medium',
      targetDevice: 'laptop',
      createdAt: Date.now() - 14400000
    },
    {
      id: 'task_3',
      title: 'TV File Transfer Test',
      notes: 'Stream study lecture clip to 42-inch Smart TV over home Wi-Fi.',
      status: 'todo',
      priority: 'high',
      targetDevice: 'tv',
      createdAt: Date.now() - 1800000
    }
  ],
  files: [
    {
      id: 'file_demo_1',
      name: 'syllabus_semester_1_aiml.pdf',
      size: 2457600,
      mimeType: 'application/pdf',
      sender: 'Laptop',
      target: 'all',
      hash: '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08',
      uploadTime: Date.now() - 5000000
    }
  ]
};

// Middleware
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));
app.use(express.static(path.join(__dirname, 'public')));
app.use('/uploads', express.static(UPLOADS_DIR));

// Multer Storage Configuration for High-Speed File Transfers
const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, UPLOADS_DIR),
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
    cb(null, `${uniqueSuffix}-${file.originalname}`);
  }
});
const upload = multer({ storage });

// REST API Endpoints
app.get('/api/state', (req, res) => {
  res.json({
    aiName: state.aiName,
    connectedDevices: Array.from(state.connectedDevices.values()),
    clipboardHistory: state.clipboardHistory,
    tasks: state.tasks,
    files: state.files
  });
});

// Upload handler (supports both /upload and /api/upload)
const handleUpload = (req, res) => {
  const file = req.file || (req.files && req.files.length > 0 ? req.files[0] : null);
  if (!file) {
    console.log('[Upload 400] No file parsed. Content-Type:', req.headers['content-type'], 'Body keys:', Object.keys(req.body));
    return res.status(400).json({ error: 'No file uploaded' });
  }

  const sender = req.body.sender || req.headers['x-sender'] || 'mobile';
  const target = req.body.target || req.headers['x-target'] || 'all';

  const fileData = {
    id: 'file_' + Date.now(),
    name: file.originalname,
    storedFilename: file.filename,
    size: file.size,
    mimeType: file.mimetype,
    sender,
    target,
    url: `/uploads/${file.filename}`,
    uploadTime: Date.now()
  };

  state.files.unshift(fileData);

  // Guarantee copy into C:\Users\nijoj\PVT_Shared_Files if path exists or on Windows
  try {
    const winDir = 'C:\\Users\\nijoj\\PVT_Shared_Files';
    if (UPLOADS_DIR !== winDir && fs.existsSync(winDir)) {
      const dest = path.join(winDir, file.originalname);
      fs.copyFileSync(file.path, dest);
      console.log(`[+] Mirrored uploaded file to ${dest}`);
    } else {
      console.log(`[+] Saved file to ${file.path}`);
    }
  } catch (err) {
    console.error('[-] Mirror copy error:', err.message);
  }

  // Broadcast to Web, Mobile, and TV
  io.emit('file:added', fileData);
  io.emit('file-transfer', fileData);

  console.log(`[+] Successfully received '${file.originalname}' (${file.size} bytes) from ${sender}`);
  res.json({ success: true, file: fileData });
};

app.post('/upload', upload.any(), handleUpload);
app.post('/api/upload', upload.any(), handleUpload);

// Health check endpoint for Mobile & TV connection probes
app.get('/status', (req, res) => {
  res.json({ status: 'ok', online: true, port: PORT, server: 'NIJO PVT Hub' });
});

// SSE event stream for live incoming file push
app.get('/events', (req, res) => {
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.write(`data: {"type":"connected","time":${Date.now()}}\n\n`);
  const keepAlive = setInterval(() => {
    res.write(`data: {"type":"ping"}\n\n`);
  }, 15000);
  req.on('close', () => clearInterval(keepAlive));
});

// Real-Time Socket.io Synchronization Engine
io.on('connection', (socket) => {
  const clientIp = socket.handshake.address;

  socket.on('device:register', (deviceInfo) => {
    const deviceRecord = {
      socketId: socket.id,
      id: deviceInfo.id || socket.id,
      type: deviceInfo.type || 'laptop', // 'laptop', 'mobile', 'tv'
      name: deviceInfo.name || `${deviceInfo.type.toUpperCase()} Client`,
      ip: clientIp,
      connectedAt: Date.now()
    };

    socket.deviceType = deviceRecord.type;
    socket.join(`device_${deviceRecord.type}`);
    state.connectedDevices.set(socket.id, deviceRecord);

    // Send initial snapshot to newly joined device
    socket.emit('state:sync', {
      aiName: state.aiName,
      devices: Array.from(state.connectedDevices.values()),
      clipboardHistory: state.clipboardHistory,
      tasks: state.tasks,
      files: state.files
    });

    // Notify all devices of updated fleet
    io.emit('devices:updated', Array.from(state.connectedDevices.values()));
  });

  // Universal Clipboard Sync Event
  socket.on('clipboard:push', (payload) => {
    const clipItem = {
      id: 'clip_' + Date.now(),
      sender: payload.sender || socket.deviceType || 'Unknown',
      target: payload.target || 'all',
      content: payload.content,
      type: payload.type || 'text',
      timestamp: Date.now()
    };

    state.clipboardHistory.unshift(clipItem);
    if (state.clipboardHistory.length > 50) state.clipboardHistory.pop();

    if (clipItem.target === 'all') {
      io.emit('clipboard:new', clipItem);
    } else {
      io.to(`device_${clipItem.target}`).emit('clipboard:new', clipItem);
      socket.emit('clipboard:new', clipItem); // Echo back to sender
    }
  });

  // Task & Notion Workspace Sync
  socket.on('task:create', (taskData) => {
    const newTask = {
      id: 'task_' + Date.now(),
      title: taskData.title,
      notes: taskData.notes || '',
      status: taskData.status || 'todo',
      priority: taskData.priority || 'medium',
      targetDevice: taskData.targetDevice || 'all',
      createdAt: Date.now()
    };

    state.tasks.unshift(newTask);
    io.emit('task:added', newTask);
  });

  socket.on('task:update', (updatedTask) => {
    const index = state.tasks.findIndex(t => t.id === updatedTask.id);
    if (index !== -1) {
      state.tasks[index] = { ...state.tasks[index], ...updatedTask };
      io.emit('task:updated', state.tasks[index]);
    }
  });

  socket.on('task:delete', (taskId) => {
    state.tasks = state.tasks.filter(t => t.id !== taskId);
    io.emit('task:deleted', taskId);
  });

  // Custom-Named AI Assistant Settings Sync
  socket.on('ai:rename', (newName) => {
    if (newName && newName.trim()) {
      state.aiName = newName.trim();
      io.emit('ai:renamed', state.aiName);
    }
  });

  // QuickShare Direct Beam Sync Event
  socket.on('quickshare:beam', (beamPayload) => {
    const beamRecord = {
      id: 'beam_' + Date.now(),
      pin: beamPayload.pin || '849-201',
      file: beamPayload.file || 'syllabus_sem1_aiml.pdf',
      notes: beamPayload.notes || '',
      sender: beamPayload.sender || socket.deviceType || 'Phone',
      timestamp: Date.now()
    };
    // Broadcast beam to Smart TV and other receivers
    io.emit('quickshare:received', beamRecord);
  });

  // Device Disconnection
  socket.on('disconnect', () => {
    state.connectedDevices.delete(socket.id);
    io.emit('devices:updated', Array.from(state.connectedDevices.values()));
  });
});

// Network IP helper to display local URLs in terminal
function getLocalIpAddresses() {
  const os = require('os');
  const interfaces = os.networkInterfaces();
  const addresses = [];
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        addresses.push(iface.address);
      }
    }
  }
  return addresses;
}

server.listen(PORT, '0.0.0.0', () => {
  const ips = getLocalIpAddresses();
  console.log(`=======================================================`);
  console.log(`   NIJO PVT - Cross-Platform Host Server & PWA Hub    `);
  console.log(`=======================================================`);
  console.log(`[*] Laptop Host Server active on Port ${PORT}`);
  console.log(`[*] Localhost access:   http://localhost:${PORT}`);
  ips.forEach(ip => {
    console.log(`[*] LAN / Wi-Fi access: http://${ip}:${PORT}`);
  });
  console.log(`[*] Open URL on Phone or Smart TV to pair instantly!`);
  console.log(`=======================================================`);
});
