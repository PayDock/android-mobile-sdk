#!/bin/bash

# Branch Validation Enforcement Script
# This script ensures branch validation is always enabled by checking current state
# and automatically setting up validation if it's missing or broken.

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_error() {
    echo -e "${RED}ERROR: $1${NC}" >&2
}

print_success() {
    echo -e "${GREEN}SUCCESS: $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

print_info() {
    echo -e "${BLUE}INFO: $1${NC}"
}

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Paths
HOOKS_DIR="$PROJECT_ROOT/.git/hooks"
PRE_PUSH_HOOK="$HOOKS_DIR/pre-push"
PRE_COMMIT_HOOK="$HOOKS_DIR/pre-commit"
VALIDATE_SCRIPT="$SCRIPT_DIR/validate-branch-name.sh"
HOOKS_SETUP_FLAG="$HOOKS_DIR/.hooks-setup-complete"

# Quiet mode flag
QUIET_MODE=false
if [[ "${1:-}" == "--quiet" ]]; then
    QUIET_MODE=true
fi

# Function to check if validation is properly set up
check_validation_status() {
    local issues=0
    
    # Check if validation script exists and is executable
    if [[ ! -f "$VALIDATE_SCRIPT" ]]; then
        if [[ "$QUIET_MODE" != "true" ]]; then
            print_error "Validation script missing: $VALIDATE_SCRIPT"
        fi
        ((issues++))
    elif [[ ! -x "$VALIDATE_SCRIPT" ]]; then
        if [[ "$QUIET_MODE" != "true" ]]; then
            print_warning "Validation script not executable: $VALIDATE_SCRIPT"
        fi
        chmod +x "$VALIDATE_SCRIPT" 2>/dev/null || ((issues++))
    fi
    
    # Check if pre-push hook exists and is executable
    if [[ ! -f "$PRE_PUSH_HOOK" ]]; then
        if [[ "$QUIET_MODE" != "true" ]]; then
            print_warning "Pre-push hook missing: $PRE_PUSH_HOOK"
        fi
        ((issues++))
    elif [[ ! -x "$PRE_PUSH_HOOK" ]]; then
        if [[ "$QUIET_MODE" != "true" ]]; then
            print_warning "Pre-push hook not executable: $PRE_PUSH_HOOK"
        fi
        chmod +x "$PRE_PUSH_HOOK" 2>/dev/null || ((issues++))
    else
        # Check if pre-push hook contains validation logic
        if ! grep -q "validate-branch-name.sh" "$PRE_PUSH_HOOK" 2>/dev/null; then
            if [[ "$QUIET_MODE" != "true" ]]; then
                print_warning "Pre-push hook doesn't contain validation logic"
            fi
            ((issues++))
        fi
    fi
    
    # Check if pre-commit hook exists and is executable and contains validation logic
    if [[ ! -f "$PRE_COMMIT_HOOK" ]]; then
        if [[ "$QUIET_MODE" != "true" ]]; then
            print_warning "Pre-commit hook missing: $PRE_COMMIT_HOOK"
        fi
        ((issues++))
    elif [[ ! -x "$PRE_COMMIT_HOOK" ]]; then
        if [[ "$QUIET_MODE" != "true" ]]; then
            print_warning "Pre-commit hook not executable: $PRE_COMMIT_HOOK"
        fi
        chmod +x "$PRE_COMMIT_HOOK" 2>/dev/null || ((issues++))
    else
        if ! grep -q "ensure-validation.sh\|validate-branch-name.sh" "$PRE_COMMIT_HOOK" 2>/dev/null; then
            if [[ "$QUIET_MODE" != "true" ]]; then
                print_warning "Pre-commit hook exists but lacks branch validation enforcement"
            fi
            ((issues++))
        fi
    fi
    
    # Check if we're in a git repository
    if [[ ! -d "$PROJECT_ROOT/.git" ]]; then
        if [[ "$QUIET_MODE" != "true" ]]; then
            print_error "Not in a Git repository"
        fi
        ((issues++))
    fi
    
    return $issues
}

# Function to automatically fix validation setup
fix_validation_setup() {
    if [[ "$QUIET_MODE" != "true" ]]; then
        print_info "Automatically setting up branch validation..."
    fi
    
    # Create hooks directory if it doesn't exist
    mkdir -p "$HOOKS_DIR"
    
    # Make validation script executable
    if [[ -f "$VALIDATE_SCRIPT" ]]; then
        chmod +x "$VALIDATE_SCRIPT"
    fi
    
    # Install pre-push hook if missing
    if [[ ! -f "$PRE_PUSH_HOOK" ]]; then
        cat > "$PRE_PUSH_HOOK" << 'EOF'
#!/bin/bash

# Git pre-push hook to validate branch naming conventions
# This hook is called by "git push" and can be used to prevent pushes
# that don't follow the project's branch naming conventions.

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Path to the branch validation script
VALIDATE_SCRIPT="$PROJECT_ROOT/scripts/validate-branch-name.sh"

# Colors for output
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_error() {
    echo -e "${RED}PUSH REJECTED: $1${NC}" >&2
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

# Check if validation script exists
if [[ ! -f "$VALIDATE_SCRIPT" ]]; then
    print_warning "Branch validation script not found at $VALIDATE_SCRIPT"
    print_warning "Skipping branch name validation"
    exit 0
fi

# Make sure the validation script is executable
if [[ ! -x "$VALIDATE_SCRIPT" ]]; then
    chmod +x "$VALIDATE_SCRIPT"
fi

# Get current branch name
current_branch=$(git rev-parse --abbrev-ref HEAD)

echo "Validating branch name before push..."

# Run the validation script
if ! "$VALIDATE_SCRIPT" "$current_branch"; then
    print_error "Push rejected due to invalid branch name"
    echo ""
    echo "To fix this issue:"
    echo "1. Create a new branch with a valid name:"
    echo "   git checkout -b bug/SDK-####-your-fix-name"
    echo "   git checkout -b task/SDK-####-your-task-name"
    echo "   git checkout -b feature/SDK-####-your-feature-name"
    echo "   git checkout -b deploy/v<major>.<minor>.<patch>"
    echo "   git checkout -b spike/SDK-####-your-investigation-name"
    echo ""
    echo "2. Cherry-pick your commits to the new branch:"
    echo "   git cherry-pick <commit-hash>"
    echo ""
    echo "3. Push the new branch:"
    echo "   git push origin <new-branch-name>"
    echo ""
    exit 1
fi

echo "Branch name validation passed ✓"
exit 0
EOF
        chmod +x "$PRE_PUSH_HOOK"
    fi
    
    # Install or repair pre-commit hook to enforce validation and then run repo helper if present
    if [[ ! -f "$PRE_COMMIT_HOOK" ]] || ! grep -q "ensure-validation.sh\|validate-branch-name.sh" "$PRE_COMMIT_HOOK" 2>/dev/null; then
        # Backup an existing user hook if present
        if [[ -f "$PRE_COMMIT_HOOK" ]]; then
            mv "$PRE_COMMIT_HOOK" "$PRE_COMMIT_HOOK.user" 2>/dev/null || true
        fi
        cat > "$PRE_COMMIT_HOOK" << 'EOF'
#!/bin/bash

# Git pre-commit hook with branch validation enforcement
# This hook ensures branch validation is always active and validates
# the current branch before allowing commits. If a repository helper
# exists at scripts/pre-commit, it is executed after validation to
# run Detekt formatting and analysis.

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Path to the enforcement script
ENSURE_SCRIPT="$PROJECT_ROOT/scripts/ensure-validation.sh"
# Optional repository pre-commit helper (Detekt)
REPO_PRE_COMMIT_HELPER="$PROJECT_ROOT/scripts/pre-commit"

# Colors for output
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_error() {
    echo -e "${RED}COMMIT REJECTED: $1${NC}" >&2
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

# Check if enforcement script exists
if [[ ! -f "$ENSURE_SCRIPT" ]]; then
    print_warning "Branch validation enforcement script not found at $ENSURE_SCRIPT"
    print_warning "Skipping branch name validation"
    exit 0
fi

# Make sure the enforcement script is executable
if [[ ! -x "$ENSURE_SCRIPT" ]]; then
    chmod +x "$ENSURE_SCRIPT" 2>/dev/null || {
        print_warning "Cannot make enforcement script executable"
        exit 0
    }
fi

echo "Ensuring branch validation is active..."

# Run the enforcement script in quiet mode
if ! "$ENSURE_SCRIPT" --quiet; then
    print_error "Branch validation failed"
    echo ""
    echo "Your current branch name does not follow the required naming conventions."
    echo ""
    echo "Branch naming requirements:"
    echo "  • bug/, task/, feature/, spike/ branches must include Jira ticket (SDK-####)"
    echo "  • deploy/ branches must be in the form deploy/v<major>.<minor>.<patch>"
    echo ""
    echo "Examples of valid branch names:"
    echo "  • bug/SDK-1234-fix-payment-issue"
    echo "  • task/SDK-5678-update-dependencies"
    echo "  • feature/SDK-9012-new-payment-method"
    echo "  • deploy/v1.2.0"
    echo "  • spike/SDK-3456-investigate-performance"
    echo ""
    echo "To fix this:"
    echo "1. Create a new branch with a valid name:"
    echo "   git checkout -b feature/SDK-####-your-feature-name"
    echo ""
    echo "2. Cherry-pick your changes:"
    echo "   git cherry-pick HEAD"
    echo ""
    echo "3. Delete the old branch:"
    echo "   git branch -D old-branch-name"
    echo ""
    exit 1
fi

# If present, run repository pre-commit helper (Detekt)
if [[ -f "$REPO_PRE_COMMIT_HELPER" ]]; then
    if [[ ! -x "$REPO_PRE_COMMIT_HELPER" ]]; then
        chmod +x "$REPO_PRE_COMMIT_HELPER" 2>/dev/null || true
    fi
    "$REPO_PRE_COMMIT_HELPER"
    helper_status=$?
    if [[ $helper_status -ne 0 ]]; then
        exit $helper_status
    fi
fi

echo "Branch validation passed ✓"
exit 0
EOF
        chmod +x "$PRE_COMMIT_HOOK"
    fi
    
    # Mark as set up
    touch "$HOOKS_SETUP_FLAG"
    
    if [[ "$QUIET_MODE" != "true" ]]; then
        print_success "Branch validation setup completed automatically"
    fi
    return 0
}

# Function to validate current branch
validate_current_branch() {
    if [[ -f "$VALIDATE_SCRIPT" && -x "$VALIDATE_SCRIPT" ]]; then
        local current_branch
        if current_branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null); then
            if [[ "$QUIET_MODE" != "true" ]]; then
                "$VALIDATE_SCRIPT" "$current_branch"
            else
                "$VALIDATE_SCRIPT" "$current_branch" >/dev/null 2>&1
            fi
            return $?
        fi
    fi
    return 1
}

# Function to show validation status
show_status() {
    echo "Branch Validation Status Check"
    echo "=============================="
    echo ""
    
    # Check validation script
    if [[ -f "$VALIDATE_SCRIPT" && -x "$VALIDATE_SCRIPT" ]]; then
        print_success "Validation script: OK"
    else
        print_error "Validation script: MISSING or NOT EXECUTABLE"
    fi
    
    # Check pre-push hook
    if [[ -f "$PRE_PUSH_HOOK" && -x "$PRE_PUSH_HOOK" ]]; then
        if grep -q "validate-branch-name.sh" "$PRE_PUSH_HOOK" 2>/dev/null; then
            print_success "Pre-push hook: OK"
        else
            print_warning "Pre-push hook: EXISTS but NO VALIDATION"
        fi
    else
        print_error "Pre-push hook: MISSING or NOT EXECUTABLE"
    fi
    
    # Check pre-commit hook
    if [[ -f "$PRE_COMMIT_HOOK" && -x "$PRE_COMMIT_HOOK" ]]; then
        if grep -q "ensure-validation.sh\|validate-branch-name.sh" "$PRE_COMMIT_HOOK" 2>/dev/null; then
            print_success "Pre-commit hook: OK"
        else
            print_warning "Pre-commit hook: EXISTS but NO VALIDATION"
        fi
    else
        print_warning "Pre-commit hook: MISSING"
    fi
    
    # Check setup flag
    if [[ -f "$HOOKS_SETUP_FLAG" ]]; then
        print_success "Setup flag: OK"
    else
        print_warning "Setup flag: MISSING"
    fi
    
    echo ""
    
    # Test current branch
    if validate_current_branch; then
        print_success "Current branch validation: PASSED"
    else
        print_error "Current branch validation: FAILED"
    fi
}

# Main execution
main() {
    case "${1:-}" in
        --status)
            show_status
            exit 0
            ;;
        --quiet)
            # Quiet mode handled above
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Ensures branch validation is always enabled by checking and fixing setup."
            echo ""
            echo "Options:"
            echo "  --status    Show detailed validation status"
            echo "  --quiet     Run silently (no output unless errors)"
            echo "  --help      Show this help message"
            echo ""
            echo "This script will:"
            echo "1. Check if branch validation is properly set up"
            echo "2. Automatically fix any issues found"
            echo "3. Validate the current branch name"
            exit 0
            ;;
    esac
    
    # Check current validation status
    if check_validation_status; then
        # Validation is properly set up
        if [[ "$QUIET_MODE" != "true" ]]; then
            print_success "Branch validation is properly configured"
        fi
        
        # Validate current branch
        if ! validate_current_branch; then
            exit 1  # Current branch is invalid
        fi
    else
        # Validation needs to be set up or fixed
        if [[ "$QUIET_MODE" != "true" ]]; then
            print_warning "Branch validation setup issues detected"
        fi
        
        # Attempt to fix automatically
        if fix_validation_setup; then
            if [[ "$QUIET_MODE" != "true" ]]; then
                print_success "Branch validation is now properly configured"
            fi
            
            # Validate current branch after setup
            if ! validate_current_branch; then
                exit 1  # Current branch is invalid
            fi
        else
            if [[ "$QUIET_MODE" != "true" ]]; then
                print_error "Failed to set up branch validation automatically"
                echo ""
                echo "Please run manually:"
                echo "  make setup-hooks"
                echo "  # OR"
                echo "  ./scripts/setup-git-hooks.sh"
            fi
            exit 1
        fi
    fi
}

# Run main function with all arguments
main "$@"
