WebSocket / STOMP Test Client

Purpose
- Simple static page to test your Spring WebSocket + STOMP notification flow using JWT authentication.

Files
- `index.html` — browser UI that:
  - Logs in via `POST /api/auth/login` to obtain `accessToken` and `userId`.
  - Connects to SockJS endpoint `/ws` and performs STOMP `CONNECT` with header `Authorization: Bearer <token>`.
  - Subscribes to `/topic/user/{id}`, `/topic/worker/{id}`, or `/topic/department/{id}`.
  - Triggers notification by calling `POST /api/departments/complaints/assign` (requires a complaintId and workerId).

How to use
1. Start your Spring Boot app (project root `spring/civic`):

```powershell
cd 'C:\Users\dhane\Documents\SIH\spring\civic'
.\mvnw.cmd spring-boot:run
```

2. Open `ws-test/index.html` in your browser (double-click the file or open via `file://` path). If you prefer, serve it over a simple static server (for example `npx serve`), but `file://` usually works.

3. In the page:
- Enter your `loginId` (mobile or email) and `password`, click `Login`.
- Click `Connect STOMP` to connect to the WebSocket.
- Click `Subscribe` (use `user` and your shown `userId` or provide a worker/department id).
- Fill `complaintId` and `workerId` then click `Trigger` to call the assign endpoint — you should receive a STOMP message in the Messages box.

Notes/Troubleshooting
- The page assumes server runs at `http://localhost:8080` and SockJS endpoint at `/ws`.
- If your WebSocket handshake requires custom HTTP headers (some clients don't allow), this page uses the STOMP CONNECT headers approach and SockJS, which Spring accepts.
- If you get CORS problems when using `file://`, serve the file from a simple local HTTP server so the browser sends the correct Origin header.

Next steps
- I can add a tiny unauthenticated endpoint if you prefer a no-auth quick test.
- I can also create a simple Node-based static server script to serve the page if you need it.
