# AGENTS.md - Blood Spot Bot API

## Project Overview
Spring Boot 3.4.4 application (Java 21) — Telegram bot for blood donation spot monitoring.
Uses WebFlux, Spring Data JPA, PostgreSQL, Telegram Bot API, and Jsoup for HTML scraping.

## Build/Test Commands

```bash
# Build
./gradlew build

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.pet_projects.bloodspotbotapi.service.UserServiceTest"

# Run a single test method
./gradlew test --tests "com.pet_projects.bloodspotbotapi.service.UserServiceTest.testSaveOrUpdate_NewUser"

# Run with verbose output
./gradlew test --info

# Run the application
./gradlew bootRun

# Clean build
./gradlew clean build

# Docker
docker build -t blood_spot .
```

## Architecture
Layered structure under `src/main/java/com/pet_projects/bloodspotbotapi/`:
- `bot/` — Telegram bot, commands, handlers, keyboards
- `service/` — Business logic (Auth, User, Spot), session state, scheduled jobs
- `model/` — JPA entities (User, Spot, UserSite enum)
- `repository/` — Spring Data JPA repositories
- `client/` — HTTP clients (DonorMosOnlineClient via RestClient)
- `utils/` — Static utilities (HtmlUtils, FormUtils, SpotUtils)
- `config/` — Spring configuration

## Code Style

### Imports
- No wildcard imports; all imports must be explicit
- Order: Java standard → third-party → internal package
- Use full qualified names sparingly (only for disambiguation)

### Formatting
- 4-space indentation (no tabs)
- Max line length: ~120 chars
- Braces on same line for methods/classes

### Naming Conventions
- Classes: `PascalCase` (AuthService, UpdateDispatcher)
- Methods/variables: `camelCase` (getCookieHeader, chatId)
- Constants: `UPPER_SNAKE_CASE` (USER_AGENT)
- Enums: `UPPER_SNAKE_CASE` (DONOR_MOS, AWAITING_AUTH_CREDENTIALS)
- Test methods: `testMethodName_Scenario` (testSaveOrUpdate_NewUser)

### Dependency Injection
- Use constructor injection exclusively via `@RequiredArgsConstructor` with `private final` fields
- Never use field `@Autowired`

### Lombok Annotations
- `@RequiredArgsConstructor` for DI
- `@Slf4j` or `@Log4j2` for logging
- `@Data`, `@Builder`, `@NoArgsConstructor` for DTOs/entities
- `@SneakyThrows` sparingly (only for checked exceptions in command handlers)

### Error Handling
- Create custom `RuntimeException` subclasses for domain errors (see `AuthFailedException`)
- Use `log.warn()` / `log.error()` — never `e.printStackTrace()`
- Catch specific exceptions when possible; avoid bare `catch (Exception e)` in new code
- Use `Optional` for nullable returns; avoid `.get()` without `isPresent()` check

### Logging
- Use `@Slf4j` and `log.info("msg: {}", value)` pattern
- Log messages may be in Russian (existing convention in bot handlers)
- Use `log.debug()` for detailed internal state

### Testing
- JUnit 5 (`org.junit.jupiter`) with Mockito
- Unit tests: `@ExtendWith(MockitoExtension.class)`, `@Mock`, manual constructor injection in `@BeforeEach`
- Use `ArgumentCaptor`, `verify()`, static imports for assertions
- Test class naming: `<ClassName>Test`
- Test resource files go in `src/test/resources/` (note: existing typo `recources/`)

### Spring Patterns
- `@Service` for business logic, `@Component` for utilities/handlers
- `@ConfigurationProperties(prefix = "...")` for externalized config
- `@EnableScheduling` + `@Scheduled` for cron jobs
- `@Value("${property}")` for simple config values

### Design Patterns
- Chain of Responsibility: `UpdateDispatcher` → `List<UpdateHandler>`
- Strategy: `BotCommand` interface with `supports()` / `process()`
- Builder: `CustomKeyBoardBuilder`, Lombok `@Builder`

## CI/CD
GitHub Actions: `.github/docker-image.yml` — builds and pushes Docker image on push to `main`.
