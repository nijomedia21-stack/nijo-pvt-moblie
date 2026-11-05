/**
 * NIJO PVT - Service Worker
 * Enables Hotspot Direct Mode offline file persistence and caching via IndexedDB & Cache API.
 * Guarantees shared files persist across browser session restarts, reloads, and network drops.
 */

const CACHE_NAME = 'nijo-pvt-cache-v1';
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/manifest.json'
];

const DB_NAME = 'nijo_pvt_files_db';
const DB_VERSION = 1;
const STORE_NAME = 'cached_files';

// IndexedDB Helper inside Service Worker
function openDb() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = (event) => {
      const db = event.target.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        const store = db.createObjectStore(STORE_NAME, { keyPath: 'id' });
        store.createIndex('name', 'name', { unique: false });
        store.createIndex('url', 'url', { unique: false });
        store.createIndex('timestamp', 'timestamp', { unique: false });
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

async function getFileFromDb(urlOrId) {
  try {
    const db = await openDb();
    return new Promise((resolve) => {
      const tx = db.transaction(STORE_NAME, 'readonly');
      const store = tx.objectStore(STORE_NAME);
      const req = store.getAll();
      req.onsuccess = () => {
        const all = req.result || [];
        const match = all.find(item => item.url === urlOrId || item.id === urlOrId || item.storedFilename === urlOrId || (item.url && item.url.endsWith(urlOrId)));
        resolve(match || null);
      };
      req.onerror = () => resolve(null);
    });
  } catch (err) {
    console.warn('[SW DB] Error getting file:', err);
    return null;
  }
}

async function putFileInDb(fileRecord) {
  try {
    const db = await openDb();
    return new Promise((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readwrite');
      const store = tx.objectStore(STORE_NAME);
      store.put(fileRecord);
      tx.oncomplete = () => resolve(true);
      tx.onerror = () => reject(tx.error);
    });
  } catch (err) {
    console.warn('[SW DB] Error saving file:', err);
    return false;
  }
}

// Service Worker Lifecycle: Install
self.addEventListener('install', (event) => {
  console.log('[SW] Installing NIJO PVT Service Worker with IndexedDB caching...');
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(STATIC_ASSETS).catch(err => {
        console.warn('[SW] Cache addAll skipped for some assets:', err);
      });
    }).then(() => self.skipWaiting())
  );
});

// Service Worker Lifecycle: Activate
self.addEventListener('activate', (event) => {
  console.log('[SW] Activating NIJO PVT Service Worker...');
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
      );
    }).then(() => self.clients.claim())
  );
});

// Service Worker Fetch Interceptor
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // If requesting an uploaded file (/uploads/...), prioritize network, fallback to IndexedDB
  if (url.pathname.startsWith('/uploads/')) {
    event.respondWith(
      fetch(event.request)
        .then(async (response) => {
          if (response && response.ok) {
            // Clone and cache file blob into IndexedDB for Hotspot Direct Mode offline access
            const clone = response.clone();
            clone.blob().then((blob) => {
              const filename = url.pathname.split('/').pop();
              putFileInDb({
                id: 'file_cached_' + filename,
                url: url.pathname,
                name: filename,
                storedFilename: filename,
                blob: blob,
                size: blob.size,
                mimeType: blob.type || 'application/octet-stream',
                timestamp: Date.now(),
                hotspotPersisted: true
              });
            }).catch(() => {});
            return response;
          }
          throw new Error('Network response not ok');
        })
        .catch(async () => {
          // Offline / Hotspot Direct fallback: look up in IndexedDB
          const filename = url.pathname.split('/').pop();
          const cached = await getFileFromDb(url.pathname) || await getFileFromDb(filename);
          if (cached && cached.blob) {
            console.log('[SW] Serving file from IndexedDB cache:', cached.name);
            return new Response(cached.blob, {
              status: 200,
              headers: {
                'Content-Type': cached.mimeType || 'application/octet-stream',
                'Content-Length': cached.size ? cached.size.toString() : cached.blob.size.toString(),
                'X-Cached-By': 'NIJO-PVT-IndexedDB-ServiceWorker'
              }
            });
          }
          return new Response('File not found in offline cache', { status: 404, statusText: 'Not Found' });
        })
    );
    return;
  }

  // App shell & static assets: Cache first with network fallback
  if (event.request.method === 'GET' && (url.pathname === '/' || url.pathname.endsWith('.html') || url.pathname.endsWith('.json'))) {
    event.respondWith(
      caches.match(event.request).then((cachedResponse) => {
        if (cachedResponse) {
          // Fetch fresh in background
          fetch(event.request).then(networkResponse => {
            if (networkResponse && networkResponse.ok) {
              caches.open(CACHE_NAME).then(c => c.put(event.request, networkResponse));
            }
          }).catch(() => {});
          return cachedResponse;
        }
        return fetch(event.request);
      })
    );
  }
});

// Handle messages from client window
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'CACHE_FILE') {
    putFileInDb(event.data.payload)
      .then(() => {
        event.ports[0]?.postMessage({ success: true, id: event.data.payload.id });
      })
      .catch((err) => {
        event.ports[0]?.postMessage({ success: false, error: err.message });
      });
  } else if (event.data && event.data.type === 'GET_ALL_FILES') {
    openDb().then(db => {
      const tx = db.transaction(STORE_NAME, 'readonly');
      const req = tx.objectStore(STORE_NAME).getAll();
      req.onsuccess = () => {
        event.ports[0]?.postMessage({ success: true, files: req.result || [] });
      };
      req.onerror = () => {
        event.ports[0]?.postMessage({ success: false, files: [] });
      };
    });
  }
});
