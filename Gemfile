source "https://rubygems.org"

gem "fastlane"
gem "octokit"
gem "rexml", ">= 3.4.2"
gem "aws-sdk-s3", ">= 1.208.0"  # Security fix for CVE-2025-14762
gem "faraday", ">= 1.10.6"  # Security fix for CVE-2026-54297
plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)