# CreateAI — Multimodal AI Creation Studio

A modern AI creation application and client-server architecture built for mobile, tablet, and desktop environments.

## Core Features

1. **AI Chat**: Conversational interface with streaming response UI, code blocks with quick-copy, Markdown styling, session management, and Gemini API integration.
2. **AI Image Studio**: Text-to-image prompt engine with aspect ratio selectors (1:1, 16:9, 9:16, 4:3, 3:4), resolutions (512x512, 1024x1024, 2048x2048), batch counts, negative prompting, and gallery viewer.
3. **AI Video Synthesizer**: Text-to-video and image-to-video synthesis pipeline with duration selection (3s, 5s, 10s), aspect ratios, progress tracker, video loop player, and provider status indicators.
4. **Honest Cost Control & Quotas**: Real-time daily usage counters with explicit "Free Plan" quotas (e.g. 50 chats, 5 images, 2 videos daily) without false unlimited claims.
5. **Provider Abstraction Architecture**: Modular adapters (`ChatProvider`, `ImageGenerationProvider`, `VideoGenerationProvider`) that clearly report `"Provider not configured"` when external credentials are not connected instead of generating fake responses.
6. **Local Persistence**: Room SQLite database on Android (and PostgreSQL schema for web) storing users, conversations, messages, image generations, video generations, usage records, and app settings.
7. **Admin Settings & Maintenance**: Live toggle for system maintenance mode, model selection (`gemini-3.5-flash`, `gemini-3.1-pro-preview`, `gemini-2.5-flash-image`), API key override, and custom daily quota configuration.

---

## Android Native Application

### Architecture
- **Framework**: Android Jetpack Compose (Material Design 3)
- **Architecture**: MVVM + Repository Pattern + Clean Provider Adapters
- **Concurrency**: Kotlin Coroutines & Reactive Flow (`StateFlow`, `collectAsStateWithLifecycle`)
- **Networking**: Retrofit 2 + OkHttp (60s timeout) + Moshi JSON
- **Database**: Room Database with KSP (`CreateAiDatabase`)
- **Image Rendering**: Coil Compose
- **Adaptive UI**: Scaffold with Bottom Navigation for phones, Navigation Rail for tablets and wide screens.

### Android Compilation
```bash
gradle assembleDebug
```

---

## Web & Backend Architecture (`/web`)

### Tech Stack
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Database**: PostgreSQL (see schema at `web/lib/db/schema.sql`)
- **Security**: Server-side credentials injection, no client-side secret exposure

### API Endpoints
- `POST /api/chat`: Handles conversational chat requests via `GeminiChatProvider`.
- `POST /api/images/generate`: Handles text-to-image synthesis with provider validation.
- `POST /api/videos/generate`: Initiates video generation pipeline with status checks.
- `GET /api/history`: Retrieves user generation activity.
- `GET /api/profile`: Retrieves user profile and live usage metrics.

### Web Setup & Running
```bash
cd web
npm install
cp .env.example .env.local
npm run dev
```

---

## Security & Secrets
- Never hardcode secrets in code or Gradle files.
- Inject `GEMINI_API_KEY` through the AI Studio Secrets panel or `.env` file.
- All unconfigured external providers report `"Provider not configured"` in compliance with the zero-fake policy.
