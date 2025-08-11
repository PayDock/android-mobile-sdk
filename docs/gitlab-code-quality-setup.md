# GitLab Code Quality Integration with Detekt

This document explains the GitLab Code Quality integration setup for the Mobile SDK Android project using Detekt.

## Overview

The code quality pipeline integrates Detekt static analysis with GitLab's Code Quality feature to provide:
- Automatic code quality checks on every commit and merge request
- Visual code quality reports in GitLab's interface
- SARIF and GitLab Code Quality format reports
- HTML reports for detailed analysis

## Components

### 1. GitLab CI Configuration (`.gitlab-ci.yml`)

The `code_quality` job:
- Runs in the `code_quality` stage
- Executes the Fastlane `code_quality` lane
- Collects artifacts (reports) that persist for 1 week
- Uploads reports to GitLab's Code Quality tab
- Runs on all branch pushes and merge requests

```yaml
code_quality:
  stage: code_quality
  extends: .setup_bundler
  script:
    - bundle exec fastlane code_quality
  artifacts:
    expire_in: 1 week
    when: always
    paths:
      - reports/code-quality/
    reports:
      codequality: reports/code-quality/gl-code-quality-report.json
    expose_as: 'Code Quality Reports'
```

### 2. Fastlane Configuration (`fastlane/Fastfile`)

The `code_quality` lane:
- Cleans previous builds
- Runs Detekt formatting (auto-fixes)
- Runs Detekt analysis on the entire codebase
- Generates comprehensive reports
- Copies reports to a central location
- Converts SARIF to GitLab Code Quality format

### 3. Detekt Configuration (`convention-plugins/src/main/kotlin/detekt-convention.gradle.kts`)

Enhanced to:
- Generate SARIF reports (GitLab compatible)
- Generate XML, HTML, and Markdown reports
- Not fail builds on code quality issues (for reporting)
- Enable reports for the `detektAll` task

### 4. SARIF to GitLab Code Quality Converter (`scripts/convert_detekt_to_gitlab_cq.py`)

Python script that:
- Converts Detekt SARIF output to GitLab's preferred Code Quality JSON format
- Maps severity levels appropriately
- Categorizes issues by type (Style, Complexity, Bug Risk, Security)
- Generates fingerprints for issue tracking

## Report Formats Generated

1. **SARIF Report** (`detekt.sarif`) - Standard format, GitLab compatible
2. **GitLab Code Quality Report** (`gl-code-quality-report.json`) - Optimized for GitLab
3. **HTML Report** (`detekt.html`) - Human-readable detailed report
4. **XML Report** (`detekt.xml`) - Machine-readable format
5. **Text Report** (`detekt.txt`) - Console-style output

## How to View Results

### In GitLab Interface

1. **Code Quality Tab**: Navigate to your merge request → Code Quality tab
2. **Pipeline Artifacts**: CI/CD → Pipelines → Job artifacts → Download reports
3. **Merge Request Widget**: Shows code quality changes directly in MR interface

### Local Development

Run the code quality checks locally:
```bash
bundle exec fastlane code_quality
```

Reports will be generated in `reports/code-quality/` directory.

## Severity Mapping

| Detekt Level | GitLab Severity | Description |
|--------------|-----------------|-------------|
| error        | critical        | Must fix issues |
| warning      | major           | Should fix issues |
| note         | minor           | Optional improvements |
| info         | info            | Informational |

## Issue Categories

- **Style**: Code formatting and style issues
- **Complexity**: Complex code that might be hard to maintain
- **Bug Risk**: Potential bugs or problematic patterns
- **Security**: Security-related issues

## Configuration

### Adjusting Detekt Rules

Edit `config/detekt/detekt.yml` to:
- Enable/disable specific rules
- Adjust thresholds
- Configure rule-specific settings

### Modifying Report Generation

Edit the Fastlane `code_quality` lane to:
- Change report locations
- Add additional processing
- Modify conversion settings

## Troubleshooting

### No Reports Generated
- Check that Detekt tasks complete successfully
- Verify `reports/code-quality/` directory is created
- Check GitLab CI job logs for errors

### Reports Not Showing in GitLab
- Ensure `codequality` artifact path is correct
- Verify JSON format is valid GitLab Code Quality format
- Check that job completes successfully (even with code issues)

### Python Script Issues
- Ensure Python 3 is available in CI environment
- Check script permissions (`chmod +x`)
- Verify SARIF input file exists

## Benefits

1. **Continuous Quality Monitoring**: Automatic checks on every change
2. **Visual Feedback**: Clear interface showing code quality trends
3. **Merge Request Integration**: Quality gates before code merges
4. **Multiple Report Formats**: Different views for different stakeholders
5. **Non-blocking Pipeline**: Reports generated without failing builds
6. **Historical Tracking**: Track quality improvements over time

## Next Steps

- Configure quality gates (fail MR if quality decreases)
- Add custom Detekt rules specific to your project
- Integrate with IDE for local development feedback
- Set up notifications for quality threshold breaches