# AGENTS.md

Blood donation appointment monitoring Telegram bot. Polls donor-mos.ru sites for available slots and notifies subscribed users.

## Build & Run

```bash
./gradlew bootJar          # Build JAR
./gradlew test             # Run tests
./gradlew bootJar -x test  # Build without tests (Docker uses this)
```

**Runtime requirements:**
- Java 21
- PostgreSQL database
- All env vars from `.env.example` must be set

**Important:** `DB_URL` in `.env` should NOT have quotes around the entire value. Docker compose mounts `.env` directly, and quoting breaks JDBC.

## Architecture

**Entry point:** `BloodDonationBot` → `UpdateDispatcher` → `UpdateHandler` implementations

**Patterns:**
- **Command pattern:** `BotCommand` interface with `command()`, `supports()`, `process()` for routing Telegram commands
- **Strategy pattern:** `UpdateHandler` implementations selected by `supports()` method
- **Scheduled jobs:** `@Scheduled` methods in `service/jobs/` (SpotDonationJob runs every 5 min)

**Package structure:**
```
bot/
  command/     # BotCommand implementations (StartCommand, AuthCommand, etc.)
  handler/     # UpdateHandler implementations (CommandUpdateHandler, AuthUpdateHandler)
  keyboard/    # Inline keyboard builder
  client/      # TelegramClientWrapper
client/donormos/  # External API client for donor-mos sites
config/        # @ConfigurationProperties classes
model/         # JPA entities (User, Spot, SiteError, Admin)
repository/    # Spring Data JPA interfaces
service/       # Business logic
  jobs/        # Scheduled jobs
  session/     # UserStateStorage (ConcurrentHashMap-based state)
utils/         # EncryptionUtils, HtmlUtils, SpotUtils, FormUtils
```

## Key Conventions

**Lombok everywhere:** `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j` - expect generated getters/setters/constructors

**Menu system:** Bot menus defined in `application.yml` under `commands.menus.*` - text and buttons loaded via `MenuService`

**User state:** In-memory `ConcurrentHashMap` in `UserStateStorage` (not persistent) - states: `AWAITING_SITE_SELECTION`, `AWAITING_AUTH_CREDENTIALS`, `NONE`

**Password encryption:** AES/GCM/NoPadding with 32-char key from `ENCRYPTION_SECRET_KEY` env var - passwords stored encrypted in DB

**Site enum:** `UserSite` enum defines monitored locations: `DONOR_MOS` (Поликарпова), `DONOR_MOS_SAB` (Шаболовка), `DONOR_MOS_ZAR` (Царицыно), `ALL`

## Authentication Flow (Complex)

The `AuthService.getCookieHeader()` performs multi-step cookie collection:
1. Preflight GET requests to collect initial cookies
2. JS cookie extraction from HTML (`HtmlUtils.extractJsCookieFromHtml`)
3. JS redirect following (`HtmlUtils.extractJsRedirectFromHtml`)
4. POST login with form data
5. Verify auth by checking for `table-item__date` in account page HTML

**Retry mechanism:** Network failures retry with `auth.retry.max-attempts` and `auth.retry.delay-ms` config

## Environment Variables

Required (see `.env` / `.env.example`):
- `DB_URL` - PostgreSQL JDBC URL (use service name `db` for Docker)
- `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` - PostgreSQL credentials
- `BOT_TOKEN` - Telegram bot token (`.env.example` incorrectly uses `TELEGRAM_BOT_TOKEN`; use `BOT_TOKEN`)
- `BASE_URL`, `VALID_URL` - Donor site URLs (legacy, not used for per-user sites)
- `SUPPORT_CHAT_URL` - URL for support button (required in `application.yml`)
- `ENCRYPTION_SECRET_KEY` - 32+ character AES key
- `ADMIN_PASSWORD` - Password for `/admin-auth` command
- `DONATION_URL` - URL for donation/support button

## Docker

```bash
docker-compose up -d        # Start app + PostgreSQL
docker build -t blood-spot . # Build image
```

**Dockerfile:** Multi-stage build (Gradle 8.5.0-jdk21 → liberica-openjdk-alpine:21)

## Testing

- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- Tests mock repositories and services
- No integration tests requiring running database
- Tests run with `-Djava.net.preferIPv4Stack=true` (configured in `build.gradle`)

## Scheduled Jobs

`SpotDonationJob.pollAllUsers()` runs every 5 minutes (`0 */5 * * * *`):
1. Fetches all subscribed users
2. For each user/site: authenticate → fetch HTML → parse slots with Jsoup
3. Save new slots, notify users via `NewSpotHandler`

## Admin Commands

- `/admin-auth <password>` - Register as admin
- `/admin-stats` - Show user statistics
- `/admin-errors` - Show recent site errors
- `/admin-say <message>` - Broadcast message to all users

Admin check: `AdminService.isAdmin(chatId)` queries `admins` table

## Documentation

`docs/` contains PlantUML sequence diagrams for commands and jobs - useful for understanding flows.
