# AndroCode Task Checklist

Based on the development plan in `plan.md`.

## Phase 1: Foundation & Core Editor

*   [x] **[1.1] Project Setup:**
    *   [x] Configure Gradle build files (Kotlin DSL).
    *   [x] Add initial core dependencies (Kotlin, Compose, Coroutines, Hilt).
    *   [x] Set up base project structure (directories, packages).
    *   [x] Define base architecture (MVVM).
    *   [x] Implement basic dependency injection (Hilt).
*   [x] **[1.2] UI Shell:**
    *   [x] Create basic Jetpack Compose layout structure (Row/Column/SplitPane).
    *   [x] Define main UI areas (Editor Pane, File Explorer, Terminal).
    *   [x] Implement stubs/placeholders for core components.
    *   [x] All panes should remain visible like VSCode.
    *   [x] Create initial adaptable layout logic (basic).
*   [x] **[1.3] Core Text View:**
    *   [x] Implement basic text rendering (using placeholder `Text`).
    *   [x] Handle touch input for cursor placement and selection (basic via TextField).
    *   [x] Implement vertical and horizontal scrolling (basic via Compose).
    *   [x] Add line numbering display.
*   [x] **[1.4] Basic Syntax Highlighting:**
    *   [x] Choose and integrate a syntax highlighting library/mechanism.
    *   [x] Implement highlighting for 1-2 initial languages (e.g., Kotlin, Java).
*   [x] **[1.5] Local File Access:**
    *   [x] Integrate Android Storage Access Framework (SAF).
    *   [x] Implement logic to request storage permissions (via SAF picker).
    *   [x] Build UI for browsing local directories (`LazyColumn`).
    *   [x] Implement file opening action (load content into editor view).
    *   [x] Implement file saving action (write editor content to local file).
    *   [x] Implement basic file operations (create folder/file via SAF).

## Phase 2: Enhancing the Editor & Local Tools

*   [ ] **[2.1] Advanced Editor Features:**
    *   [x] Implement multi-cursor support.
    *   [x] Implement find functionality (within the current file).
    *   [x] Implement replace functionality (within the current file).
    *   [x] Implement code folding mechanism (brace-based).
    *   [x] Add bracket matching visualization.
    *   [ ] Add indentation guides.
*   [ ] **[2.2] Robust Syntax Highlighting:**
    *   [ ] Add support for more languages (JS, Python, HTML, CSS, C/C++).
    *   [ ] Refine highlighting rules and accuracy.
    *   [ ] Investigate TextMate grammar compatibility if applicable.
*   [ ] **[2.3] Local Terminal:**
    *   [ ] Choose and integrate a terminal emulation view component.
    *   [ ] Connect terminal view to the Android local shell.
    *   [ ] Handle input/output streams.
    *   [ ] Support basic terminal commands.
*   [ ] **[2.4] Basic Local Git:**
    *   [ ] Integrate JGit library.
    *   [ ] Implement `git status` functionality for local repo.
    *   [ ] Implement `git add` functionality.
    *   [ ] Implement `git commit` functionality.
    *   [ ] Create a basic diff viewing UI.

## Phase 3: Remote Development Core

*   [ ] **[3.1] SSH Integration:**
    *   [ ] Add SSH/SFTP library (e.g., `sshj`).
    *   [ ] Implement core SSH connection logic.
    *   [ ] Handle different authentication methods (password, key).
    *   [ ] Implement secure storage for SSH credentials/keys.
*   [ ] **[3.2] Connection Manager UI:**
    *   [ ] Create UI for listing saved SSH hosts.
    *   [ ] Create UI for adding/editing SSH host configurations.
    *   [ ] Implement connect/disconnect actions.
*   [ ] **[3.3] Remote File Explorer:**
    *   [ ] Extend file explorer UI to show remote files/directories via SFTP.
    *   [ ] Implement directory listing for remote hosts.
    *   [ ] Implement remote file operations (create, delete, rename, move) via
        SFTP.
*   [ ] **[3.4] Remote File Editing:**
    *   [ ] Enable opening remote files in the editor (download via SFTP).
    *   [ ] Implement saving changes back to the remote server (upload via SFTP).
    *   [ ] Handle potential synchronization conflicts/issues.
*   [ ] **[3.5] Remote Terminal:**
    *   [ ] Connect integrated terminal view to a remote SSH shell session.
    *   [ ] Manage multiple remote terminal sessions.
*   [ ] **[3.6] Remote Git:**
    *   [ ] Adapt JGit operations to execute over SSH connection.
    *   [ ] Implement remote `git status`, `add`, `commit`, `push`, `pull`,
        `branch`, `log`.

## Phase 4: Feature Parity & Polish

*   [ ] **[4.1] Language Server Protocol (LSP):**
    *   [ ] Integrate LSP client library (e.g., `lsp4j`, adapted).
    *   [ ] Implement communication channel between client and server (potentially
        over SSH for remote).
    *   [ ] Handle LSP messages for completion, diagnostics, hover, go-to-
        definition.
    *   [ ] Manage lifecycle of language servers.
*   [ ] **[4.2] Debugger Integration (DAP):**
    *   [ ] Research Debug Adapter Protocol integration on Android.
    *   [ ] Choose and integrate a DAP client library.
    *   [ ] Implement communication with debug adapters (potentially remote).
    *   [ ] Build basic debugging UI (breakpoints, stepping, variable inspection).
*   [ ] **[4.3] UI/UX Refinement:**
    *   [ ] Polish foldable state transitions and animations.
    *   [ ] Optimize touch controls and gestures.
    *   [ ] Add theme support (light/dark, custom themes).
    *   [ ] Implement a command palette (like VS Code's Ctrl+Shift+P).
    *   [ ] Refine keyboard shortcut support.
*   [ ] **[4.4] Settings System:**
    *   [ ] Design settings data structure.
    *   [ ] Implement persistent storage for settings.
    *   [ ] Create comprehensive Settings UI.
    *   [ ] Apply settings changes dynamically (editor font, theme, etc.).
*   [ ] **[4.5] Performance Optimization:**
    *   [ ] Profile CPU usage during editing, highlighting, remote ops.
    *   [ ] Profile memory allocation and identify leaks.
    *   [ ] Profile battery consumption.
    *   [ ] Optimize Compose rendering performance.
    *   [ ] Optimize background tasks and network usage.
*   [ ] **[4.6] Extensibility Foundation:**
    *   [ ] Design a simple plugin API.
    *   [ ] Implement initial extension points (e.g., themes, keymaps).
    *   [ ] Build basic mechanism for loading/managing extensions.

## Phase 5: Testing & Deployment

*   [ ] **[5.1] Unit & Integration Tests:**
    *   [ ] Write unit tests for ViewModels, UseCases, Repositories.
    *   [ ] Write integration tests for SSH/SFTP interactions.
    *   [ ] Write integration tests for Git operations.
    *   [ ] Write tests for core editor logic.
    *   [ ] Achieve target code coverage.
*   [ ] **[5.2] UI Tests:**
    *   [ ] Write Compose UI tests for individual components.
    *   [ ] Write Espresso tests for end-to-end user flows.
    *   [ ] Test UI adaptability on different screen sizes and fold states.
    *   [ ] Test both touch and keyboard interaction flows.
*   [ ] **[5.3] Performance Testing:**
    *   [ ] Establish performance benchmarks (startup time, editor responsiveness).
    *   [ ] Test performance with large files and complex projects.
    *   [ ] Test network performance and stability on various connections.
    *   [ ] Test resource usage over extended periods.
*   [ ] **[5.4] Beta Program:**
    *   [ ] Set up beta distribution channel (e.g., Play Store Internal/Closed
        Testing).
    *   [ ] Define feedback collection mechanism.
    *   [ ] Recruit beta testers.
    *   [ ] Iterate based on beta feedback.
*   [ ] **[5.5] Deployment:**
    *   [ ] Finalize app signing configuration.
    *   [ ] Prepare Google Play Store listing assets (screenshots, description).
    *   [ ] Ensure compliance with Play Store policies.
    *   [ ] Perform staged rollout.
    *   [ ] Monitor post-launch metrics and feedback.
