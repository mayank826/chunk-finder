# Chunk Finder — Fabric 1.21.1

This is a starter client-side Fabric mod project. It searches only already-loaded nearby chunks, and only within 24 blocks vertically of the player's current Y level. It reports amethyst-related blocks as indicators, not proof of a hidden base.

## Controls
- G: Toggle the HUD/scanning.
- H: Cycle scan radius from 1 to 4 chunks.
- Sensitivity is currently a source-code setting (`sensitivity = 1`): 0 = Strict, 1 = Normal, 2 = Broad. A proper in-game settings screen is not included in this starter.

## Build on Android without Termux
1. Download and extract this ZIP on your phone.
2. In your browser, open https://github.com and sign in.
3. Create a new repository named `chunk-finder`, set it to Public or Private, and create it without adding a README/license/gitignore.
4. Open the new repository, tap **Add file → Upload files**, and upload the *contents* of this extracted folder (including `.github`, `src`, `build.gradle`, `settings.gradle`, and `gradle.properties`). If GitHub's mobile page hides the upload option, turn on **Desktop site** in your browser.
5. Commit the uploaded files to the `main` branch.
6. Open the repository's **Actions** tab. If prompted, enable GitHub Actions.
7. Select **Build Chunk Finder** and tap **Run workflow** if it hasn't started automatically.
8. Wait for the run to finish with a green check. Open the successful run and scroll to **Artifacts**.
9. Download `chunk-finder-jar` and extract it. The `chunk-finder-1.0.0.jar` file inside `build/libs` is the mod. Do not use the `-sources.jar`.
10. Install it in a Minecraft Java 1.21.1 instance with Fabric Loader and Fabric API 0.102.1+1.21.1. If you play on Android using a Java launcher, put the JAR in that instance's `mods` folder.

## Limitations
- The mod does not reveal arbitrary hidden bases or inspect unloaded chunks.
- Amethyst-related blocks are clues only and can occur naturally.
- This first version has keybinds and a HUD, not a full click-GUI module/settings screen.
- If the build fails, open the failed Actions run, copy the first red error section, and share it for troubleshooting.
