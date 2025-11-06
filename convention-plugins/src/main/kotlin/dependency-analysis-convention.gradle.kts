/**
 * Convention plugin for dependency analysis configuration
 * 
 * Configures the autonomousapps dependency-analysis plugin with strict rules:
 * - Fails on unused dependencies
 * - Fails on incorrect configurations (api vs implementation)
 * - Ignores transitive dependencies (critical ones declared explicitly)
 * 
 * Apply this to modules that should have strict dependency validation.
 * 
 * Usage in module build.gradle.kts:
 *   plugins {
 *       id("dependency-analysis-convention")
 *   }
 */

plugins {
    id("com.autonomousapps.dependency-analysis")
}

// Note: Main configuration is in root build.gradle.kts
// This convention plugin just applies the plugin to the module
// Individual modules can add specific exclusions as needed

