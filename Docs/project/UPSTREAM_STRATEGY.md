# Long-Term Upstream Synchronization Strategy

**Upstream Repository:** `https://github.com/NuvioMedia/NuvioMobile.git`  
**Upstream Branch:** `cmp-rewrite`  
**Target Fork Branch:** `develop/cn-emby`  

---

## 1. Core Principles for Upstream Harmony

1. **Maximum Modular Isolation:**
   - All Emby server models, API clients, matchers, device profile builders, and scrobblers must reside strictly in `com.nuvio.app.features.emby`.
   - Core Nuvio screens (`MetaDetailsScreen.kt`, `HomeScreen.kt`, `StreamsScreen.kt`) should receive Emby streams via existing provider interfaces (`StreamsRepository`), with zero intrusive Emby-specific UI branches.
   - Emby stream items must gracefully coexist with **existing Nuvio Addon streams**, with no hardcoded dependencies on specific third-party providers.
2. **Localization Non-Invasiveness:**
   - Translations are housed entirely in `composeResources/values-zh-rCN/strings.xml`, `values-zh/strings.xml`, and `values-zh-rTW/strings.xml`.
   - The base English resource file (`values/strings.xml`) must NEVER be modified directly unless fixing an upstream typo.
   - Re-run resource generation tasks (`generateComposeResClass`) after updating string resources.
3. **Metadata Non-Invasiveness:**
   - Metadata enrichment is centralized within `TmdbMetadataService.kt` and `MetaDetailsRepository.kt`. Composable UI files do not contain locale-specific data fetching logic.
   - Prioritize existing built-in metadata mechanisms before considering additional proxies.
4. **Minimal Native Patching:**
   - Swift changes in `iosApp` are restricted to the MPV player bridge (`MPVPlayerBridge.swift` and `MPVSubtitleFontResolver.swift`), preserving Swift UI chrome and navigation untouched.
5. **Workflows & CI Scripts:**
   - Retain `.github/workflows/ios-test-build.yml` and related shell scripts identical to upstream.

---

## 2. Git Branching Model

```
upstream/cmp-rewrite ───────────────────●───────────────────● (Upstream commits)
                                        │                   │
                                   git fetch           git merge / rebase
                                        │                   │
develop/cn-emby   ──────────────────────●───────────────────● (Main development)
                    │               ▲
              branch│               │merge (squash or fast-forward)
                    ▼               │
              phase/01-zh-cn  ──────┘
```

- **`upstream/cmp-rewrite`:** Read-only tracking branch reflecting official Nuvio development.
- **`develop/cn-emby`:** Primary integration branch for Chinese localization and Emby features.
- **Feature Branches (`phase/*`):** Short-lived working branches dedicated to specific phases.

---

## 3. Upstream Synchronization Procedure

When upstream releases new commits or version tags:

### Step 1: Fetch and Inspect Upstream
```bash
git fetch upstream --prune --tags
git log develop/cn-emby..upstream/cmp-rewrite --oneline
```

### Step 2: Create a Sync Validation Branch
```bash
git checkout -b sync/upstream-$(date +%Y%m%d) develop/cn-emby
git merge upstream/cmp-rewrite -m "chore(upstream): sync upstream/cmp-rewrite $(date +%Y-%m-%d)"
```

### Step 3: Conflict Resolution Order
1. **`gradle/libs.versions.toml`:** Keep upstream dependency versions unless a fork-specific dependency is required.
2. **`composeApp/src/commonMain/composeResources/values/strings.xml`:** Accept upstream changes.
3. **`composeApp/src/commonMain/composeResources/values-zh-rCN/strings.xml`:** Run string difference audit to identify newly added English keys that require Chinese translation:
   ```bash
   python3 scripts/audit-missing-translations.py
   ```
   Apply standard Mainland software terminology normalization (e.g. 自订 -> 自定义).
4. **`composeApp/src/commonMain/kotlin/com/nuvio/app/features/streams/StreamsRepository.kt`:** Re-verify Emby stream provider injection hook.

### Step 4: Verification & Cloud Build
```bash
./gradlew compileCommonMainKotlinMetadata
git push origin sync/upstream-$(date +%Y%m%d)
# Trigger GitHub Actions: Build Test IPA (Debug)
```

### Step 5: Merge into `develop/cn-emby`
Once the cloud build succeeds and the unsigned IPA is verified:
```bash
git checkout develop/cn-emby
git merge --ff-only sync/upstream-$(date +%Y%m%d)
git push origin develop/cn-emby
git branch -d sync/upstream-$(date +%Y%m%d)
```

---

## 4. Conflict Prevention Guidelines

| Area | Danger Level | Conflict Prevention Rule |
|---|---|---|
| **Resource Files** | LOW | Never modify `values/strings.xml`. Add translations only in `values-zh-rCN/strings.xml` and `values-zh/strings.xml`. |
| **Settings Navigation** | MEDIUM | Add Emby settings through standard `SettingsDestinationRoute` in `Routes.kt` without changing existing route order. |
| **Streams Repository** | MEDIUM | Use a dedicated extension or decorator pattern on `StreamsRepository` rather than rewriting core loading loops. |
| **Player Engine** | HIGH | Do not modify `PlayerScreenContent.kt` layout structure; integrate through `PlayerScreenRuntimePlaybackActions.kt` and `PlayerLaunch`. |
| **Build Scripts** | LOW | Keep `scripts/build-ios-ipa.sh` and `scripts/prepare-ios-dependencies.sh` identical to upstream; only supply external environment variables. |
