# Plan: VS Code-like Android IDE for Foldables (Project "AndroCode")

## 1. Vision & Goals

*   **Goal:** Develop a native Android application providing a robust code editing
    and development environment, mirroring key functionalities of Visual Studio Code.
*   **Target Device:** Optimized for large-screen and foldable Android devices,
    specifically targeting the Google Pixel Fold Pro 9 form factor (adaptable UI).
*   **Core Differentiator:** Seamless, built-in remote development capabilities
    (SSH) allowing users to connect to and work on remote servers directly from
    the device.
*   **Target Users:** Developers needing a powerful mobile coding solution,
    especially for remote work, quick edits, or tablet-based development setups.

## 2. Core Features

*   **High-Fidelity Code Editor:**
    *   Syntax highlighting for major languages (JS, Python, Java, Kotlin, HTML,
        CSS, C/C++, etc.)
    *   Code completion & basic IntelliSense (expandable via LSP)
    *   Find & Replace (in file, across files)
    *   Multi-cursor support
    *   Code folding
    *   Line numbers, bracket matching, indentation guides
*   **File Explorer:**
    *   Browse local device storage (using Storage Access Framework)
    *   Browse remote server filesystem (via SSH/SFTP)
    *   File operations (create, delete, rename, move) for both local and remote
*   **Integrated Terminal:**
    *   Local shell access on the Android device
    *   Remote shell access via SSH connection
    *   Multiple terminal instances
*   **Remote Development (SSH):**
    *   Secure SSH connection management (save hosts, credentials/keys)
    *   Remote file editing and management
    *   Remote terminal execution
    *   Port forwarding (optional, advanced feature)
*   **Version Control (Git):**
    *   Basic Git operations (status, add, commit, push, pull, branch, log) for
        local and remote (via SSH) repositories
    *   Diff viewing
*   **Adaptable UI:**
    *   Layout dynamically adjusts for different screen sizes and folded/unfolded
        states.
    *   Optimized for touch input and physical keyboard usage.
*   **Extensibility (Future Goal):**
    *   A simplified extension system for themes, keymaps, or basic language tools.
*   **Settings & Configuration:**
    *   Customizable editor settings, themes, keybindings.

## 3. Technology Stack & Architecture

*   **Platform:** Android (Targeting modern API levels)
*   **Language:** Kotlin (Leveraging coroutines for async operations)
*   **UI Toolkit:** Jetpack Compose (Ideal for declarative, adaptable UIs)
*   **Architecture:** MVVM (Model-View-ViewModel) or MVI (Model-View-Intent) with
    Clean Architecture principles.
*   **Core Editor:** Likely requires a custom implementation using Compose Canvas or
    investigating specialized Android text editor components. Libraries like
    CodeView might offer starting points but may need heavy customization.
*   **Syntax Highlighting:** Integrate a robust library (e.g., adapt TextMate
    grammars, investigate `highlightjs-android`, or build custom parser).
*   **SSH/SFTP:** `sshj` (preferred) or `JSch` library.
*   **Terminal Emulation:** Libraries like `termux-app`'s terminal view components
    (if adaptable) or `connectbot`'s implementation for inspiration.
*   **Git:** `JGit` library.
*   **LSP Client (Advanced):** `lsp4j` (might need adaptation for Android runtime).
*   **Dependency Injection:** Hilt or Koin.
*   **Build System:** Gradle with Kotlin DSL.
*   **Linting/Formatting:** `ktlint`, `detekt`.

## 4. Development Phases

### Phase 1: Foundation & Core Editor (Est: 3-4 months)

*   **[1.1] Project Setup:** Gradle config, dependencies, base architecture, CI/CD
    pipeline.
*   **[1.2] UI Shell:** Basic Jetpack Compose layout (Editor Pane, File Explorer
    Stub, Terminal Stub), adaptable stubs for fold states.
*   **[1.3] Core Text View:** Implement basic text rendering, input handling (touch
    & keyboard), scrolling within Compose.
*   **[1.4] Basic Syntax Highlighting:** Integrate initial library/mechanism for 1-2
    languages.
*   **[1.5] Local File Access:** Implement SAF integration for browsing and opening/
    saving local files. Basic file explorer UI.

### Phase 2: Enhancing the Editor & Local Tools (Est: 2-3 months)

*   **[2.1] Advanced Editor Features:** Multi-cursor, find/replace, code folding,
    bracket matching.
*   **[2.2] Robust Syntax Highlighting:** Expand language support, refine
    highlighting accuracy.
*   **[2.3] Local Terminal:** Integrate basic local terminal emulator view.
*   **[2.4] Basic Local Git:** Integrate JGit for status, add, commit operations on
    local repos. Diff view.

### Phase 3: Remote Development Core (Est: 3-4 months)

*   **[3.1] SSH Integration:** Add SSH library, implement connection logic, secure
    credential/key storage.
*   **[3.2] Connection Manager UI:** UI for adding, editing, connecting to SSH hosts.
*   **[3.3] Remote File Explorer:** Extend file explorer to browse remote SFTP
    directories. Implement remote file operations.
*   **[3.4] Remote File Editing:** Enable opening, editing, saving files directly on
    the remote server via SFTP/SSH. Handle synchronization.
*   **[3.5] Remote Terminal:** Connect terminal view to remote SSH shell.
*   **[3.6] Remote Git:** Adapt Git operations to work on remote repositories via
    SSH.

### Phase 4: Feature Parity & Polish (Est: 4-6 months+)

*   **[4.1] Language Server Protocol (LSP):** Implement LSP client for richer
    IntelliSense, diagnostics, go-to-definition for supported languages (requires
    running language servers, potentially remotely).
*   **[4.2] Debugger Integration (Highly Complex):** Investigate Debug Adapter
    Protocol (DAP). This is a significant undertaking.
*   **[4.3] UI/UX Refinement:** Polish foldable state transitions, optimize touch
    controls, improve themes, add command palette.
*   **[4.4] Settings System:** Implement comprehensive settings UI.
*   **[4.5] Performance Optimization:** Profile CPU, memory, battery usage. Optimize
    rendering and background tasks.
*   **[4.6] Extensibility Foundation:** Design and implement a basic plugin API
    (e.g., for themes).

### Phase 5: Testing & Deployment (Ongoing)

*   **[5.1] Unit & Integration Tests:** Write tests for core logic, view models,
    SSH/Git integrations.
*   **[5.2] UI Tests:** Use Compose testing tools and Espresso for UI/flow testing,
    including foldable states.
*   **[5.3] Performance Testing:** Measure startup time, editor responsiveness,
    connection stability, resource usage.
*   **[5.4] Beta Program:** Gather user feedback through internal/external beta
    testing.
*   **[5.5] Deployment:** Prepare for Google Play Store release (listing, policies,
    signing).

## 5. UI/UX Considerations

*   **Foldable First:** Design layouts that seamlessly adapt between compact and
    expanded views. Consider multi-pane layouts for larger screens.
*   **Touch & Keyboard:** Ensure all core actions are accessible via both touch
    gestures and physical keyboard shortcuts (crucial for developers).
*   **Information Density:** Balance showing enough information (like VS Code)
    without overwhelming a mobile screen.
*   **Performance:** UI must remain responsive, especially during typing and
    scrolling in large files or over remote connections.

## 6. Key Challenges

*   **Editor Performance:** Building a performant, feature-rich text editor view
    on Android is non-trivial.
*   **Remote Connection Stability:** Handling network interruptions, latency, and
    secure SSH connections reliably.
*   **Resource Consumption:** Managing CPU, memory, and battery usage effectively on
    mobile.
*   **Feature Complexity:** Replicating the depth of VS Code features (LSP, DAP,
    Extensions) is a massive effort. Prioritization is key.
*   **UI/UX Adaptation:** Designing an intuitive interface that works well on both
    touchscreens and with keyboards across different screen sizes/states.

## 7. Monetization Strategy (Optional)

*   **Freemium:** Core local editor free; Remote development features and advanced
    capabilities (LSP, Debugging) under a one-time purchase or subscription.
*   **Subscription:** Monthly/annual subscription for full access, potentially
    tiered.

## 8. Assumptions & Risks

*   **Feasibility:** Assumes building a performant editor and stable remote tools
    is technically achievable on Android within reasonable constraints.
*   **Library Suitability:** Relies on the chosen libraries (SSH, Git, etc.) being
    robust and adaptable.
*   **User Adoption:** Depends on developers finding value in a mobile-first,
    remote-capable IDE.

## 9. Next Steps

*   Refine feature prioritization for MVP (Minimum Viable Product).
*   Deep-dive research into core editor implementation options.
*   Set up project structure and initial dependencies.
*   Begin Phase 1 development.
