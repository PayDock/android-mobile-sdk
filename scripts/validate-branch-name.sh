#!/bin/bash

# Git Branch Name Validation Script
# Restricts branch creation to specific prefixes: bug/, task/, feature/, deploy/, spike/

set -e

# Define allowed branch prefixes
ALLOWED_PREFIXES=("bug/" "task/" "feature/" "deploy/" "spike/")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_error() {
    echo -e "${RED}ERROR: $1${NC}" >&2
}

print_success() {
    echo -e "${GREEN}SUCCESS: $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

# Function to validate branch name
validate_branch_name() {
    local branch_name="$1"
    
    # Skip validation for protected base branches
    if [[ "$branch_name" == "main" || "$branch_name" == "release" ]]; then
        return 0
    fi
    
    # Check if branch name starts with any allowed prefix
    for prefix in "${ALLOWED_PREFIXES[@]}"; do
        if [[ "$branch_name" == "$prefix"* ]]; then
            # For bug/, task/, feature/, spike/ - require SDK- ticket number
            if [[ "$prefix" == "bug/" || "$prefix" == "task/" || "$prefix" == "feature/" || "$prefix" == "spike/" ]]; then
                # Extract the part after the prefix
                local suffix="${branch_name#$prefix}"
                
                # Check if it starts with SDK- followed by numbers
                if [[ "$suffix" =~ ^SDK-[0-9]+.* ]]; then
                    return 0
                else
                    return 1
                fi
            elif [[ "$prefix" == "deploy/" ]]; then
                # For deploy/ branches, enforce semantic version: deploy/v<major>.<minor>.<patch>
                local suffix="${branch_name#$prefix}"
                if [[ "$suffix" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
                    return 0
                else
                    return 1
                fi
            fi
        fi
    done
    
    return 1
}

# Function to display usage
show_usage() {
    echo "Usage: $0 [branch-name]"
    echo ""
    echo "Validates git branch names against allowed prefixes."
    echo ""
    echo "Allowed prefixes:"
    for prefix in "${ALLOWED_PREFIXES[@]}"; do
        echo "  - $prefix"
    done
    echo ""
    echo "Examples of valid branch names:"
    echo "  - bug/SDK-1234-fix-login-issue"
    echo "  - task/SDK-5678-update-dependencies"
    echo "  - feature/SDK-9012-user-authentication"
    echo "  - deploy/v1.2.0"
    echo "  - spike/SDK-3456-investigate-performance"
    echo ""
    echo "If no branch name is provided, validates the current branch."
}

# Main execution
main() {
    local branch_name=""
    
    # Parse command line arguments
    case "${1:-}" in
        -h|--help)
            show_usage
            exit 0
            ;;
        "")
            # No argument provided, get current branch name
            if ! branch_name=$(git rev-parse --abbrev-ref HEAD 2>/dev/null); then
                print_error "Not in a git repository or unable to determine current branch"
                exit 1
            fi
            ;;
        *)
            branch_name="$1"
            ;;
    esac
    
    echo "Validating branch name: $branch_name"
    
    if validate_branch_name "$branch_name"; then
        print_success "Branch name '$branch_name' is valid"
        exit 0
    else
        print_error "Branch name '$branch_name' does not follow naming conventions"
        echo ""
        echo "Branch naming requirements:"
        echo "  • bug/, task/, feature/, spike/ branches must include Jira ticket (SDK-####)"
        echo "  • deploy/ branches must be in the form deploy/v<major>.<minor>.<patch>"
        echo ""
        echo "Required format:"
        echo "  • bug/SDK-####-description"
        echo "  • task/SDK-####-description"  
        echo "  • feature/SDK-####-description"
        echo "  • spike/SDK-####-description"
        echo "  • deploy/v<major>.<minor>.<patch>"
        echo ""
        echo "Examples:"
        echo "  git checkout -b bug/SDK-1234-fix-payment-issue"
        echo "  git checkout -b task/SDK-5678-update-readme"
        echo "  git checkout -b feature/SDK-9012-new-payment-method"
        echo "  git checkout -b deploy/v2.1.0"
        echo "  git checkout -b spike/SDK-3456-investigate-memory-usage"
        exit 1
    fi
}

# Run main function with all arguments
main "$@"
