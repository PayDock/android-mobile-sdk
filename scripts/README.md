# Git Branch Naming Validation

Automatic enforcement of branch naming conventions for the Mobile SDK Android project.

## Quick start

```bash
# Ensure hooks are installed and validation is active
./scripts/ensure-validation.sh

# Check status
./scripts/ensure-validation.sh --status
```

If validation fails, rename your branch to include a Jira ticket for non-deploy branches (for example `task/SDK-1234-description`).

## Branch Naming Conventions

All branches must follow specific naming patterns:

### Required Formats

- `bug/SDK-####-description` - For bug fixes (requires Jira ticket)
- `task/SDK-####-description` - For general tasks and maintenance (requires Jira ticket)
- `feature/SDK-####-description` - For new features (requires Jira ticket)
- `spike/SDK-####-description` - For research and investigation work (requires Jira ticket)
- `deploy/v<major>.<minor>.<patch>` - For deployment/release branches (semantic version required)

### Jira Ticket Requirement

Bug, task, feature, and spike branches **must** include a Jira ticket number in the format `SDK-` followed by numbers (e.g., `SDK-1234`). Release branches are exempt from this requirement.

### Valid Branch Name Examples

```bash
bug/SDK-1234-fix-payment-processing
bug/SDK-5678-resolve-crash-on-startup
task/SDK-9012-update-dependencies
task/SDK-3456-refactor-networking
feature/SDK-7890-apple-pay-integration
feature/SDK-2468-new-payment-method
deploy/v1.2.0
spike/SDK-1357-investigate-memory-leaks
spike/SDK-8642-research-new-payment-api
```

### Invalid Branch Names

```bash
bug/fix-login              # Missing SDK- ticket number
task/update-deps           # Missing SDK- ticket number  
feature/new-ui             # Missing SDK- ticket number
spike/research             # Missing SDK- ticket number
hotfix/urgent-fix          # Invalid prefix (should be bug/)
improvement/ui-updates     # Invalid prefix (should be feature/ or task/)
my-branch                  # Missing required prefix and ticket
fix-login                  # Missing prefix and ticket
bug/PROJ-123-fix           # Wrong ticket format (should be SDK-####)
```

## How It Works

Branch validation is **automatically enforced** with zero setup required:

1. **First Commit/Push**: When you first try to commit or push, validation hooks are automatically installed
2. **Always Active**: The system self-checks and auto-repairs if hooks are missing or broken
3. **Multi-Layer Protection**: Validates on both commit and push to prevent invalid branches
4. **Zero Maintenance**: No manual setup, updates, or maintenance required

### What You'll See

When working with an invalid branch name:

```bash
$ git commit -m "Fix bug"
Ensuring branch validation is active...
COMMIT REJECTED: Branch validation failed

Your current branch name does not follow the required naming conventions.

Branch naming requirements:
  • bug/, task/, feature/, spike/ branches must include Jira ticket (SDK-####)
  • deploy/ branches must be named deploy/v<major>.<minor>.<patch>

Examples of valid branch names:
  • bug/SDK-1234-fix-payment-issue
  • task/SDK-5678-update-dependencies
  • feature/SDK-9012-new-payment-method
  • deploy/v1.2.0
  • spike/SDK-3456-investigate-performance

To fix this:
1. Create a new branch with a valid name:
   git checkout -b feature/SDK-####-your-feature-name

2. Cherry-pick your changes:
   git cherry-pick HEAD

3. Delete the old branch:
   git branch -D old-branch-name
```

## Commands

### Common commands

```bash
# Validate current branch
./scripts/validate-branch-name.sh

# Validate specific branch name
./scripts/validate-branch-name.sh feature/SDK-1234-my-feature

# Check validation status
./scripts/ensure-validation.sh --status

# Ensure/force validation setup (auto-installs hooks if missing)
./scripts/ensure-validation.sh
```

### Hook installation

Hooks are auto-installed the first time you commit or push. To install or repair hooks explicitly:

```bash
./scripts/ensure-validation.sh
```

This configures both hooks:

- `pre-commit`: validates branch naming first, then runs the repo helper `scripts/pre-commit` (Detekt format + Detekt).
- `pre-push`: validates branch naming before pushing.

## Status Check

You can check the current validation status:

```bash
$ ./scripts/ensure-validation.sh --status
Branch Validation Status Check
==============================

SUCCESS: Validation script: OK
SUCCESS: Pre-push hook: OK
SUCCESS: Pre-commit hook: OK
SUCCESS: Setup flag: OK

SUCCESS: Current branch validation: PASSED
```

## Troubleshooting

### Validation Not Working

The system is self-healing, but you can force a check:

```bash
./scripts/ensure-validation.sh
```

### Disabling Validation Temporarily

**Not recommended**, but if you need to bypass validation:

```bash
# Disable hooks temporarily
mv .git/hooks/pre-push .git/hooks/pre-push.disabled
mv .git/hooks/pre-commit .git/hooks/pre-commit.disabled

# Re-enable hooks
mv .git/hooks/pre-push.disabled .git/hooks/pre-push
mv .git/hooks/pre-commit.disabled .git/hooks/pre-commit
```

### Fixing Invalid Branch Names

If you're already on a branch with an invalid name:

```bash
# Create a new branch with valid name (include Jira ticket)
git checkout -b feature/SDK-1234-your-feature-name

# Cherry-pick your commits (if needed)
git cherry-pick old-branch-name

# Delete the old branch
git branch -D old-branch-name

# Push the new branch
git push origin feature/SDK-1234-your-feature-name
```

### Removing All Validation

To completely remove branch validation:

```bash
rm -f .git/hooks/pre-push .git/hooks/pre-commit .git/hooks/.hooks-setup-complete
```

### GUI clients (e.g., SourceTree) not running hooks

Some GUI clients can appear to bypass hooks if they were not installed yet. Run:

```bash
./scripts/ensure-validation.sh --status
```

If hooks are missing, install them with:

```bash
./scripts/ensure-validation.sh
```

Then retry your commit/push from the GUI.

## Technical Details

### Files

- `scripts/validate-branch-name.sh` - Core branch-name validation logic
- `scripts/ensure-validation.sh` - Auto-enforcement and hook setup
- `scripts/pre-commit` - Runs Detekt formatting and analysis before commit
- `scripts/convert_detekt_to_gitlab_cq.py` - Converts Detekt SARIF to GitLab Code Quality format
- `scripts/setup_bundler.sh` - Installs Bundler and updates Ruby gems (Fastlane)
- `.git/hooks/pre-commit` - Validates before commits (auto-installed)
- `.git/hooks/pre-push` - Validates before pushes (auto-installed)

### How Auto-Setup Works

1. **Detection**: When you commit/push, hooks check if validation is properly set up
2. **Auto-Repair**: If anything is missing or broken, it's automatically fixed
3. **Validation**: Your branch name is validated against the conventions
4. **Enforcement**: Invalid branches are rejected with helpful error messages

### Zero Configuration

- **No manual setup required** - everything happens automatically
- **Self-healing** - automatically fixes missing or broken components
- **Always up-to-date** - hooks are regenerated if they become outdated
- **Team-friendly** - works the same for everyone, no coordination needed

## Integration with CI/CD

The validation can be used in CI/CD pipelines:

```bash
# In your CI script
./scripts/validate-branch-name.sh "$BRANCH_NAME" || exit 1
```

## Customization

To modify allowed prefixes, edit the `ALLOWED_PREFIXES` array in `validate-branch-name.sh`:

```bash
ALLOWED_PREFIXES=("bug/" "task/" "feature/" "deploy/" "spike/")
```

## Pre-commit: Detekt formatting and static analysis

The repository provides a pre-commit helper at `scripts/pre-commit` that:

- Formats code with Detekt (`detektFormat`)
- Runs Detekt analysis (`detektAll`) and blocks the commit on failures

Install it as your local Git pre-commit hook:

```bash
ln -sf "$PWD/scripts/pre-commit" .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

Note: when `scripts/ensure-validation.sh` (re)installs `pre-commit`, it will chain to this repo helper automatically after branch validation.

## Detekt SARIF → GitLab Code Quality converter

Use `scripts/convert_detekt_to_gitlab_cq.py` to convert a Detekt SARIF report into GitLab Code Quality JSON for the Merge Request widget.

```bash
python3 scripts/convert_detekt_to_gitlab_cq.py \
  mobile-sdk/build/reports/detekt-all.sarif \
  build/reports/gitlab-code-quality.json
```

Example CI usage:

```bash
./gradlew :mobile-sdk:detektAll
python3 scripts/convert_detekt_to_gitlab_cq.py \
  mobile-sdk/build/reports/detekt-all.sarif \
  build/reports/gitlab-code-quality.json
```

## Bundler setup (Fastlane)

The helper `scripts/setup_bundler.sh` installs a pinned Bundler version and updates gems, which is useful for Fastlane tasks in `fastlane/`.

```bash
bash scripts/setup_bundler.sh
```

Prerequisites: a working Ruby toolchain with `gem` and `bundle` available.

The system will automatically use the updated rules on the next commit/push.
