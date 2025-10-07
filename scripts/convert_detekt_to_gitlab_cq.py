#!/usr/bin/env python3
"""
Convert Detekt SARIF report to GitLab Code Quality format.
This script ensures maximum compatibility with GitLab's Code Quality tab.
"""

import json
import sys
import os
from pathlib import Path

def convert_sarif_to_gitlab_cq(sarif_file_path, output_file_path):
    """Convert SARIF format to GitLab Code Quality format."""
    
    if not os.path.exists(sarif_file_path):
        print(f"SARIF file not found: {sarif_file_path}")
        return False
    
    try:
        with open(sarif_file_path, 'r') as f:
            sarif_data = json.load(f)
        
        gitlab_cq_issues = []
        
        # Process SARIF results
        for run in sarif_data.get('runs', []):
            rules = {}
            
            # Extract rule information
            for rule in run.get('tool', {}).get('driver', {}).get('rules', []):
                rules[rule['id']] = {
                    'description': rule.get('shortDescription', {}).get('text', ''),
                    'help': rule.get('help', {}).get('text', ''),
                    'severity': rule.get('defaultConfiguration', {}).get('level', 'warning')
                }
            
            # Process results/issues
            for result in run.get('results', []):
                rule_id = result.get('ruleId', 'unknown')
                rule_info = rules.get(rule_id, {})
                
                for location in result.get('locations', []):
                    physical_location = location.get('physicalLocation', {})
                    artifact_location = physical_location.get('artifactLocation', {})
                    region = physical_location.get('region', {})
                    
                    # Map SARIF severity to GitLab severity
                    sarif_level = result.get('level', 'warning')
                    severity_mapping = {
                        'error': 'critical',
                        'warning': 'major',
                        'note': 'minor',
                        'info': 'info'
                    }
                    severity = severity_mapping.get(sarif_level, 'major')
                    
                    issue = {
                        "description": result.get('message', {}).get('text', rule_info.get('description', 'Detekt issue')),
                        "check_name": rule_id,
                        "fingerprint": f"detekt_{rule_id}_{artifact_location.get('uri', '')}_L{region.get('startLine', 0)}",
                        "severity": severity,
                        "location": {
                            "path": artifact_location.get('uri', '').replace('file://', ''),
                            "lines": {
                                "begin": region.get('startLine', 0),
                                "end": region.get('endLine', region.get('startLine', 0))
                            }
                        }
                    }
                    
                    # Add categories for better GitLab integration
                    categories = ["Style"]
                    if "complexity" in rule_id.lower():
                        categories = ["Complexity"]
                    elif "bug" in rule_id.lower() or "potential" in rule_id.lower():
                        categories = ["Bug Risk"]
                    elif "security" in rule_id.lower():
                        categories = ["Security"]
                    
                    issue["categories"] = categories
                    
                    gitlab_cq_issues.append(issue)
        
        # Write GitLab Code Quality format
        with open(output_file_path, 'w') as f:
            json.dump(gitlab_cq_issues, f, indent=2)
        
        print(f"Converted {len(gitlab_cq_issues)} issues from SARIF to GitLab Code Quality format")
        print(f"Output written to: {output_file_path}")
        return True
        
    except Exception as e:
        print(f"Error converting SARIF to GitLab Code Quality format: {e}")
        return False

def main():
    if len(sys.argv) != 3:
        print("Usage: python3 convert_detekt_to_gitlab_cq.py <sarif_input_file> <gitlab_cq_output_file>")
        sys.exit(1)
    
    sarif_file = sys.argv[1]
    output_file = sys.argv[2]
    
    # Ensure output directory exists
    os.makedirs(os.path.dirname(output_file), exist_ok=True)
    
    if convert_sarif_to_gitlab_cq(sarif_file, output_file):
        sys.exit(0)
    else:
        sys.exit(1)

if __name__ == "__main__":
    main()