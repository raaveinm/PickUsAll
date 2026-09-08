# PickUsAll (Picasso)

KMP + Compose Multiplatform client (Android / Desktop / iOS). Backend (`picassobackend`, C++/Drogon/Postgres) is a separate repo, not part of this codebase.

Product context, terminology (Artist/Palette/Color/...), business model, and roadmap live in [brainstorm/brainstorm.md](brainstorm/brainstorm.md) and [brainstorm/pipeline.md](brainstorm/pipeline.md) — read those first, don't duplicate them here. Keep `pipeline.md`'s "Status snapshot" table updated at the end of a work session; it's the source of truth for "what's actually done" vs the stage plan.

## Module map

- `core/model` — pure Kotlin DTOs/domain models (`User` — brainstorm.md's "Artist" terminology, no separate `Artist` type exists — `Chat`, `Palette`, `Conversation` sealed interface, ...). No Room, no platform code.
- `core/database` — **client-side Room/SQLite cache only.** Not the backend schema. See below.
- `core/designsystem` — UI kit (colors, typography, `GameCard`, `ChatPreview`, `NavBar`, ...).
- `features/colorpicker` — empty scaffold, not started. **Not where the Color Picker feature actually lives** — that got built directly in `shared/ui/canvas` instead (see `pipeline.md`'s status snapshot). Same story for chat: no `features/chat` module exists, it's `shared/ui/chat` + `shared/data/repository/ChatRepository`. Don't assume feature code lives under `features/*` just because brainstorm.md's module layout says so — check `shared` first.
- `shared` — the actual app (screens, navigation, viewmodels, Koin wiring, and in practice most feature logic too). Depends on `core:*`.
- `androidApp` / `desktopApp` / iOS (`iosApp/` Xcode project, Swift entry calling into `shared`'s `MainViewController`) — thin platform entry points.

## Database: client cache, not backend

`core/database`'s tables (`Servers`, `Conversations`, `Chats`, `Palettes`, `PaletteMembers`, `MessageData`, plus the Steam catalog cache tables) are the **local Room cache on the client**, per [multi-server client & sync](brainstorm/brainstorm.md#multi-server-client--sync) — a client can hold connections to several backend servers at once. This is why:

- `Servers` table = the client's list of known backend URLs (`url`, `name`, `added`), not a backend concept.
- `Conversations.serverId` = FK to which known server a cached conversation belongs to.
- `Conversations.remoteId` = the conversation's id **on that server** (server-side PK space), distinct from `id` (local Room autoincrement PK). Needed because the local and remote id spaces are independent — without it, `Index(["serverId","remoteId"], unique=true)` (the sync/dedup key) can't work, and there'd be no way to map an incoming server update to the right local row. Don't remove it.
- `Chats.chatTitleSteamId` stores only the *other* DM participant — valid for a client cache (the local Artist is implicit from the session), would NOT be valid as a multi-tenant backend schema (a backend needs both participants).

The Postgres backend (`picassobackend`) is expected to have its own, separately-designed schema; don't assume 1:1 parity with these Room entities.

## Cache rotation

The cache exists to economize Steam API calls, not to hoard data forever — most of what it holds about *other* people is only there because it was fetched once, not because there's an ongoing reason to keep it. Only two things are genuinely permanent: the local Artist's own `Users` row and their own `OwnedGames`/`UserAchievements`. Everything about other people (friends, friends-of-friends surfaced in a Palette, a stranger's profile viewed once) is a rotation candidate.

- `Users`, `OwnedGames`, `UserAchievements` each carry a required `fetchedAt: Long` (epoch, no DB default) — it's caching metadata, not part of the Steam API shape, which is why `User.toEntity(fetchedAt: Long)` takes it as a parameter instead of deriving it. Whoever fetches from the API is the only one who knows "when".
- `UserDao.pruneStaleUsers(selfSteamId, cutoff)` deletes a `Users` row only if it's both stale (`fetchedAt < cutoff`) **and** unreachable — not self, not a friend, not a participant in any DM/Palette. Existing `ON DELETE CASCADE` FKs already clean up that user's `OwnedGames`/`UserAchievements`/`PaletteMembers` rows — don't add separate deletes for those.
- `SteamFriends` is never a pruning target: it can only ever hold rows for the local user (Steam's API doesn't expose a friend's friend list), so a friend can never become "unreachable" through it.
- Not wired to a call site yet. It doesn't strictly need `features/auth` to compile — `AppConfig.USER_ID` could pass as `selfSteamId` today — but it needs `features/auth` to be *meaningful*: right now every install has the same one hardcoded identity, so "self" isn't really a per-user concept yet. Once real auth lands, call it once per app session start with the actual logged-in user's steamId.
- A friend's `Users` row is *never* deleted by this sweep (breaks the friend list if it's gone), but their `OwnedGames`/`UserAchievements` still carry their own `fetchedAt` — that's for the repository layer (see below) to decide "stale, re-fetch" without needing eviction.

## Repository layer

One repository per feature, all in `shared/data/repository/`: `OwnedGamesRepository`, `GameStoreRepository`, `FriendsRepository`, `ChatRepository`. They sit between DAOs and ViewModels and decide cache-vs-network — this is NOT "check DB, if empty call API, else return DB" as two competing paths. It's single-source-of-truth: **the DAO's `Flow` is always what the ViewModel observes; a repository's only job is writing into Room** (deciding, using `fetchedAt` where relevant, whether to kick off a network refresh) — the existing `Flow` re-emits on its own once the write lands. A repository method has no "return the data" path; if it's not coming from Room, the ViewModel shouldn't be observing it. `FriendsRepository.refresh()` is the cleanest example of the pattern (see its own doc comment). `ChatRepository` extends the same idea to writes: `sendMessage()` always durably inserts into Room first (`MessageStatus.PENDING`) before attempting anything over the network — see "Outbox pattern" below.

## Outbox pattern (chat sending)

Sending a message never talks to the network directly — see [decisions.md](brainstorm/decisions.md) for the full reasoning (durability across dropped connections, why immediate-attempt beats poll-only, why per-message idempotency is a separate concern from `Conversations.remoteId`). Mechanically, in `ChatRepository`:

1. `sendMessage()` inserts a `MessageData` row with `status = MessageStatus.PENDING` — this is what makes the message appear instantly (the screen observes `ChatDao`'s `Flow`, not the network call).
2. `attemptSend()` then tries `sendToServer()`, and updates the row to `SENT` or `FAILED`.
3. `sendToServer()` is currently a `TODO()` stub — no chat wire protocol exists yet (`features/chat` was never built as a module, and `picassobackend` doesn't exist). Filling it in needs: resolving `conversationId` → the owning server + `Conversations.remoteId`, sending the local message id as an idempotency key (so a retried send after a lost ack doesn't create a server-side duplicate), and **never** trusting a client-supplied `senderSteamId` — the server must derive the sender from the authenticated connection.

**Gotcha that already cost a bug:** `attemptSend()` catches `Throwable`, not `Exception`. Kotlin's `TODO()` throws `NotImplementedError`, which is an `Error`, not an `Exception` — a plain `catch (_: Exception)` lets it straight through and crashes the app instead of landing the message as `FAILED`. `CancellationException` is still re-thrown explicitly before the catch-all, so this doesn't swallow coroutine cancellation. Keep both when `sendToServer()` gets a real implementation and starts throwing real network exceptions.

## Room3 gotchas (androidx.room3, KSP-based)

This project uses the newer `androidx.room3` KMP artifact, which differs from classic Room in a few ways that cost real debugging time — check these before assuming a Room3 API mirrors classic Room from memory:

- **KSP must be wired explicitly per target.** Applying the `room3`/`ksp` Gradle plugins is not enough — you need `dependencies { add("kspAndroid", libs.androidx.room3.compiler); add("kspIosArm64", ...); add("kspJvm", ...) }` etc. (see [core/database/build.gradle.kts](core/database/build.gradle.kts)) for every target the module declares. Without this, `expect object XConstructor : RoomDatabaseConstructor<T>` never gets its `actual` generated and compilation fails with "no actual declaration" — that's a missing-KSP-wiring symptom, not a "write the actual by hand" situation. Room really does generate it.
- **Don't name the `@Database`-annotated class `Database`.** It collides with the imported `androidx.room3.Database` annotation. Plain `kotlinc` tolerates it, but KSP's Room processor gets confused resolving `RoomDatabaseConstructor<Database>` and fails with "must implement a single interface of type ...". Ours is named `PicassoDatabase`. Google's own sample uses `AppDatabase` for the same reason.
- **`@Relation`/`@Junction` use plural, array-typed params**: `parentColumns`/`entityColumns` (`Array<String>`), not the classic Room singular `parentColumn`/`entityColumn`.
- **Never hand-seed `room_master_table`.** Room owns this table (schema identity hash) entirely; it's created/populated by `createAllTables()` on first open. If you need to inspect the current expected hash, it's in the generated `*_Impl.kt` (`core/database/build/generated/ksp/.../PicassoDatabase_Impl.kt`) and in `core/database/schemas/.../1.json` — don't invent a value.
- **On iOS/Native, `Dispatchers.IO` needs `import kotlinx.coroutines.IO`** in addition to `import kotlinx.coroutines.Dispatchers` — it's an internal member on Native without that extension import.
- **`DatabaseFactory` (expect/actual, in `core/database`) has a different constructor per platform** (Android needs `Context`, iOS/JVM don't) — it can't be constructed from common code. Each platform entry point builds its own `DatabaseFactory` and feeds it into `databaseModule(factory)` (a Koin module providing `PicassoDatabase` + all DAOs). Pattern verified against Google's official [Fruitties KMP Room sample](https://github.com/android/kotlin-multiplatform-samples/tree/main/Fruitties).

## Koin DI

Platform-level init, not the composable-scoped `KoinApplication`:
- `initKoin(config)` in `shared`'s `di/KoinModules.kt` is idempotent (guards via `KoinPlatformTools.defaultContext().getOrNull()`).
- Called once per platform entry point: `PicassoApplication.onCreate()` (Android, needs its own `Application` class registered in the manifest — not `MainActivity.onCreate`, which can rerun on config changes), `main()` (Desktop), `MainViewController()` (iOS).
- Each platform's `initKoin { ... }` call also loads `databaseModule(DatabaseFactory(...))` on top of the shared `sharedModule`.
- ViewModels are resolved via `koinViewModel<X>()` in composables, never constructed manually (`X()`) — that silently bypasses DI and breaks the moment a ViewModel needs an injected dependency.

## Useful build/verify commands

Module/task names are non-obvious in this AGP/KMP setup — a few that come up often:

```
./gradlew :core:database:compileAndroidMain      # NOT compileDebugKotlinAndroid
./gradlew :core:database:compileKotlinJvm
./gradlew :core:database:compileIosMainKotlinMetadata
./gradlew :shared:compileKotlinJvm :desktopApp:compileKotlin
./gradlew :shared:compileAndroidMain :androidApp:compileDebugKotlin
```

Compiling `core:database` alone is the fastest way to check an entity/DAO/migration change before touching the app modules.

## Known WIP / gaps

- No auth (`features/auth` not started) — every install shares one hardcoded identity from `local.properties`/`AppConfig`. See "Cache rotation" above for one concrete thing this blocks.
- `ChatRepository.sendToServer()` is a `TODO()` — chat messages persist locally (outbox, see above) but never actually leave the device.
- No `picassobackend` integration at all — the client currently only talks to Steam's public Web API and its own local Room cache. This is what `sendToServer()`, real multi-server support, and the self-host tier are all waiting on.
- `SettingsScreen`/`SettingsViewModel` are empty placeholders (tab exists in nav, does nothing).
- `features:colorpicker`, `features:chat`, `features:audio` modules were never built — the Color Picker and Chat *features* exist and work, just live in `shared` instead (see Module map above). Full status: [pipeline.md](brainstorm/pipeline.md)'s status snapshot.
