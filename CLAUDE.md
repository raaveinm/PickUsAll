# PickUsAll (Picasso)

KMP + Compose Multiplatform client (Android / Desktop / iOS). Backend (`picassobackend`, C++/oat++/Postgres — see [decisions.md](brainstorm/decisions.md#server-stack-picassobackend) for the Drogon→oat++ switch) is a separate repo, not part of this codebase.

Product context, terminology (Artist/Palette/Color/...), business model, and roadmap live in [brainstorm/brainstorm.md](brainstorm/brainstorm.md) and [brainstorm/pipeline.md](brainstorm/pipeline.md) — read those first, don't duplicate them here. Keep `pipeline.md`'s "Status snapshot" table updated at the end of a work session; it's the source of truth for "what's actually done" vs the stage plan.

## Module map

- `core/model` — pure Kotlin DTOs/domain models (`User` — brainstorm.md's "Artist" terminology, no separate `Artist` type exists — `Chat`, `Palette`, `Conversation` sealed interface, ...). No Room, no platform code.
- `core/database` — **client-side Room/SQLite cache only.** Not the backend schema. See below.
- `core/designsystem` — UI kit (colors, typography, `GameCard`, `ChatPreview`, `NavBar`, ...).
- `features/colorpicker` — empty scaffold, not started. **Not where the Color Picker feature actually lives** — that got built directly in `shared/ui/canvas` instead (see `pipeline.md`'s status snapshot). Same story for chat: no `features/chat` module exists, it's `shared/ui/chat` + `shared/data/repository/ChatRepository`. Don't assume feature code lives under `features/*` just because brainstorm.md's module layout says so — check `shared` first.
- `shared` — the actual app (screens, navigation, viewmodels, Koin wiring, and in practice most feature logic too). Depends on `core:*`.
- `androidApp` / `desktopApp` / iOS (`iosApp/` Xcode project, Swift entry calling into `shared`'s `MainViewController`) — thin platform entry points.

## Database: client cache, not backend

`core/database`'s tables (`Servers`, `Conversations`, `Chats`, `Palettes`, `PaletteMembers`, `MessageData`, plus the Steam catalog cache tables) are the **local Room cache on the client**, per [multi-server client & sync](brainstorm/brainstorm.md#multi-server-client--sync) — a client can *know about* several backend servers, but connects to exactly one **active** one at a time (switchable in settings; switching resyncs against the new active server — see [decisions.md](brainstorm/decisions.md#architecture) for why simultaneous live connections were dropped). This is why:

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
- Not wired to a call site yet. Real auth has now landed (see "Auth" below), so "self" *is* a per-user concept and `selfSteamId` has a real value to take — call it once per app session start with `AuthRepository.session`'s `userId`.
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

## Auth (Steam OpenID via picassobackend)

Identity is a **session**, not a build constant. `AppConfig.USER_ID` is gone; `AppConfig` now only carries `STEAM_API_KEY`.

- `core/datastore` persists the session (`UserAuthToken`: opaque token + steamId + expiry) in a Preferences DataStore. `AuthTokenStore.session: Flow<UserAuthToken?>` is the single source of truth — **null emission is the logged-out state**, there's no separate boolean. `AuthDataStoreFactory` is expect/actual with a per-platform constructor for exactly the same reason `DatabaseFactory` is, and is wired in per entry point via `authDataStoreModule(...)`.
- Every viewmodel `flatMapLatest`es its DAO queries over that flow rather than reading an id once — logging in or out has to *re-subscribe* the queries, not just re-run them. Repositories (`FriendsRepository.refresh(self)`, `OwnedGamesRepository.refresh(selfSteamId)`) take the id as a parameter; same rationale as `User.toEntity(fetchedAt)`, only the caller knows.

**Why the login is three-legged.** Steam's consent page is a browser flow and its redirect lands in the *browser*, a different process — the app can never read the response to `/auth/steam/return`. So: the app mints a one-time `state` nonce and opens `/auth/steam/begin?state=<nonce>`; the backend parks the minted token under that nonce; the app claims it from `/auth/steam/poll?state=<nonce>`. Consequences worth knowing:

- The nonce is the *only* thing guarding a parked token, hence `secureNonce()` is expect/actual over a real CSPRNG (`SecureRandom` / `SecRandomCopyBytes`) — **not** `kotlin.random.Random`. It's hex so it survives a query string unescaped.
- Parked tokens are single-use and expire in 5 min server-side (`PENDING_LOGIN_TTL_MS`); `LOGIN_POLL_TIMEOUT` is deliberately under that. A 204 from `poll` means "not ready", "already claimed" and "expired" indistinguishably, by design.
- `logout()` clears locally *first*, then tells the server — a logout the user asked for must stick even with no network.
- The WS upgrade carries the **session token**, not the steamId. `SignalingClient` used to send `Bearer <steamId>` matching an older dev-mode stand-in on the backend; that is now rejected. `CallManager.disconnectSignaling()` exists because `connectSignaling` self-guards against reconnects, so without an explicit teardown a logout would leave the socket authenticated as the previous user and silently refuse to reconnect as the next one.

## Key bindings (hotkeys)

Desktop shortcuts are user-rebindable and persisted. Three commands exist: `Commands.REFRESH` (Ctrl/⌘R, consumed by the JVM `RefreshBox`), `QUIT_APPLICATION` (Ctrl/⌘Q) and `MINIMIZE_APPLICATION` (Ctrl/⌘W, iconifies the window — **not** hide-to-tray; it stays in the taskbar and the OS owns restoring it). Android/iOS use pull-to-refresh and ignore the keymap.

- `core/designsystem/.../keybinding/` — the model. `KeyMap` is an **immutable snapshot**; a rebind produces a new one via `KeyMap.withOverrides(...)`. Read it in composables through `LocalKeyMap` (defaults to factory bindings if nothing provides it).
- `KeyMap`'s constructor `require`s consistency and **throws** — that's for hardcoded lists (`DEFAULT_BINDINGS`) only. Anything user-supplied goes through `check()` (returns a `RebindProblem?`) or `withOverrides()` (silently drops invalid overrides → command keeps its default). Never feed persisted data straight into the constructor: a stale/corrupt file would crash the app at startup.
- `core/datastore/.../settings/` — `BehaviourSettingsStore` persists only the chords the user *changed*, as `key_binding_<Commands.name>` → `"keyCode,primary,shift,alt,control"`. Datastore can't see Compose, hence the primitive `StoredChord`; `KeyBindingRepository` (in `shared/data/repository`) maps to/from `KeyChord`. **`Commands.name` is the persistence key** — renaming a constant resets that binding (removing one is safe, unknown ids are ignored). Same for the encoding string: it's on users' disks.
- There are now **two** `DataStore<Preferences>` singletons (`auth.preferences_pb`, `settings.preferences_pb`), told apart by Koin qualifier in `DataStoreModule.kt`, not by type. Both paths come from the same per-platform `AuthDataStoreFactory`, so entry points are unchanged. Session logout only removes auth keys and never touches bindings.
- `ProvideKeyMap` (`shared/ui/app`, wraps `App`) collects `KeyBindingRepository.keyMap` into `LocalKeyMap`. Rebind UI lives in Settings → Behaviour: `KeyBindingsSection` is a single `LazyColumn` item; `SettingsViewModel` owns the recording state (`BehaviourState`).
- **`toChordOrNull()` reports modifiers verbatim and platform-independently**: Meta → `isPrimary` (Cmd on Apple, Super elsewhere), Ctrl → `isControl`. Nothing is folded or dropped, so **Ctrl+Q and Super+Q are two distinct bindable chords**. It takes no `isApple` parameter — don't re-add one. (It previously folded primary = Cmd-on-Apple/Ctrl-elsewhere, which made `isControl` unreachable from events and Super unrepresentable.)
- Because of that, **no single `SpecialKeys` value means "the OS shortcut modifier" on every platform** — so the platform difference lives in the hardcoded lists instead, via `SYSTEM_MODIFIER` (`= PRIMARY` on Apple, `CONTROL` elsewhere). `DEFAULT_BINDINGS` and `KeyMap.ReservedChords` are both built through it; `chord(Key.Q, SYSTEM_MODIFIER)` is ⌘Q on a Mac and Ctrl+Q on Linux/Windows. **Only for hardcoded lists** — a user-recorded chord is already concrete and must never be rewritten through it. `ToChordTest.systemModifierMatchesDefaultChordOfEveryCommand` guards the "default declared with a modifier no event ever reports" bug class, which is exactly how quit/minimize shipped broken the first time.
- `Binding.allowWhileTyping` defaults to `chord.hasShortcutModifier` (`isPrimary || isControl`), not `isPrimary` alone — otherwise the off-Apple Ctrl defaults would all count as plain typing. Still not honoured by `RefreshBox`.
- Window-level commands (quit/minimize) are dispatched in `App.kt`'s root `Box.onPreviewKeyEvent`, which sees events root-first even while a descendant holds focus. The actions themselves come in as `WindowActions` (`shared/ui/app`), a holder of nullable lambdas filled by the desktop entry point from `ApplicationScope.exitApplication` and `WindowState.isMinimized` (the window state is hoisted via `rememberWindowState` in `main.kt` so the flag survives recomposition — a fresh `WindowState(…)` per frame would discard it). **`null` means "unsupported on this platform", and the handler returns `false` for it** so the key event isn't swallowed on Android/iOS (`WindowActions.Unsupported`). Don't give the root `Box` an `onFocusChanged { requestFocus() }` loop — it fights every `TextField` in the app; `onPreviewKeyEvent` doesn't need that node to hold focus itself.
- Adding a command: add the enum constant, a default in `DEFAULT_BINDINGS`, title/description in `CommandText.kt`, and a branch in `App.kt`'s dispatch `when` (all three `when`s are exhaustive, so it won't compile until you do). No migration needed.
- Recording rejects: reserved system chords (clipboard/select-all/undo — Ctrl or ⌘ + C/V/X/A/Z), bare keys other than F1–F12, and chords another command owns. Quit/close are deliberately **not** reserved anymore: the app owns those actions and defaults to those chords, so reserving them would make `DEFAULT_BINDINGS` fail the `KeyMap` constructor check. Plain Esc cancels a recording and can't be bound.

## Koin DI

Platform-level init, not the composable-scoped `KoinApplication`:
- `initKoin(config)` in `shared`'s `di/KoinModules.kt` is idempotent (guards via `KoinPlatformTools.defaultContext().getOrNull()`).
- Called once per platform entry point: `PicassoApplication.onCreate()` (Android, needs its own `Application` class registered in the manifest — not `MainActivity.onCreate`, which can rerun on config changes), `main()` (Desktop), `MainViewController()` (iOS).
- Each platform's `initKoin { ... }` call also loads `databaseModule(DatabaseFactory(...))` on top of the shared `sharedModule`.
- ViewModels are resolved via `koinViewModel<X>()` in composables, never constructed manually (`X()`) — that silently bypasses DI and breaks the moment a ViewModel needs an injected dependency.

## Compose version pinning (material3 is NOT on the core's version line)

`material3` has its own version in `libs.versions.toml` and **must not** be switched to `version.ref = "composeMultiplatform"`. `org.jetbrains.compose.material3:material3` has published nothing but alphas since **1.9.0**, so there is no `1.11.x`/`1.12.x` to follow — and the Compose Gradle Plugin itself resolves `compose.material3` to `1.9.0` for both the 1.11 and 1.12 cores. 1.9.0 is the pairing JetBrains actually ships and tests; take it.

A material3 alpha compiles fine and then dies at runtime, because `foundation`/`ui`/`runtime` get resolved independently:

- Any `TextField` (e.g. `PicassoSearchBar` on the Friends tab) crashed with `AbstractMethodError: ... TextFieldDefaults$$Lambda ... applyStyle(CustomStyleScope) of interface CustomStyle`. Mechanism: material3 `1.11.0-alpha07` was compiled against foundation `1.11.0-beta03`, where `foundation.style.Style` was a standalone fun interface with `applyStyle(StyleScope)`; in foundation 1.12.x it became `Style : CustomStyle<StyleScope>`, whose JVM-level abstract method is the *erased* `applyStyle(CustomStyleScope)`. The old SAM lambda doesn't implement that descriptor. The follow-on `NoSuchElementException` in `TextFieldMeasurePolicy` is the same skew, one layer down.
- **`composenativetray` is why the core moves at all.** `2.1.7` requires `foundation-desktop:1.12.0`, so Gradle silently upgraded the whole core off the declared 1.11.1 while material3 stayed put. Keep `composeMultiplatform` at or above what the tray requires so declared == resolved; `./gradlew :desktopApp:dependencies --configuration runtimeClasspath` is how you check (look for `-> ` arrows on `org.jetbrains.compose.*`).

Diagnosing this class of bug: `AbstractMethodError`/`NoSuchMethodError` naming a `$$Lambda` of a Compose class is always artifact skew, never app code. `javap -v -p` the implicated class out of the two candidate jars and compare the SAM descriptor — it pins the mismatch down in a minute, whereas reading the stack trace does not.

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

- `ChatRepository.sendToServer()` is a `TODO()` — chat messages persist locally (outbox, see above) but never actually leave the device.
- Beyond auth, no `picassobackend` integration — the client otherwise only talks to Steam's public Web API and its own local Room cache. This is what `sendToServer()`, real multi-server support, and the self-host tier are all waiting on.
- `SettingsViewModel` does server CRUD + reachability pings (not the empty placeholder it once was), but there's still no **active server** selection — "active" is hardcoded as first-of-list in both `ChatViewModel`'s signaling and `AuthRepository.activeServerBaseUrl()`. Logging in therefore requires a server to already exist in Settings; `LoginFailure.NoServer` is the error surfaced when none does.
- `features:colorpicker`, `features:chat`, `features:audio` modules were never built — the Color Picker and Chat *features* exist and work, just live in `shared` instead (see Module map above). Full status: [pipeline.md](brainstorm/pipeline.md)'s status snapshot.
