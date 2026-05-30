#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./scripts/sonar-pr-status.sh <project_key> <pr_number>
# Example:
#   ./scripts/sonar-pr-status.sh christiandrochon_sma-eventsourcing-17 14

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <project_key> <pr_number>"
  exit 1
fi

PROJECT_KEY="$1"
PR_KEY="$2"
SONAR_HOST="${SONAR_HOST:-https://sonarcloud.io}"

python3 - <<'PY' "$SONAR_HOST" "$PROJECT_KEY" "$PR_KEY"
import json
import sys
import urllib.parse
import urllib.request

host = sys.argv[1].rstrip('/')
project_key = sys.argv[2]
pr_key = sys.argv[3]

# 1) Pull request status summary
pr_url = host + "/api/project_pull_requests/list?" + urllib.parse.urlencode({"project": project_key})
with urllib.request.urlopen(pr_url, timeout=30) as r:
    pr_data = json.load(r)

pr_info = None
for pr in pr_data.get("pullRequests", []):
    if str(pr.get("key")) == str(pr_key):
        pr_info = pr
        break

if pr_info is None:
    print(f"[ERROR] PR {pr_key} introuvable pour le projet {project_key}")
    sys.exit(2)

status = pr_info.get("status", {})
print("=== PR Summary ===")
print("project:", project_key)
print("pr:", pr_key)
print("qualityGate:", status.get("qualityGateStatus", "UNKNOWN"))
print("bugs:", status.get("bugs", "?"))
print("vulnerabilities:", status.get("vulnerabilities", "?"))
print("codeSmells:", status.get("codeSmells", "?"))
print("analysisDate:", pr_info.get("analysisDate", "?"))
print("commit:", pr_info.get("commit", {}).get("sha", "?"))

# 2) Open issues only
issues_params = {
    "projects": project_key,
    "pullRequest": pr_key,
    "resolved": "false",
    "statuses": "OPEN,CONFIRMED,REOPENED",
    "ps": "100",
}
issues_url = host + "/api/issues/search?" + urllib.parse.urlencode(issues_params)
with urllib.request.urlopen(issues_url, timeout=30) as r:
    issues_data = json.load(r)

issues = issues_data.get("issues", [])
print("\n=== Open Issues ===")
print("count:", issues_data.get("total", 0))
for idx, issue in enumerate(issues, start=1):
    print(
        f"{idx}. {issue.get('type')} | {issue.get('severity')} | {issue.get('rule')} | "
        f"{issue.get('component')}:{issue.get('line')} | {issue.get('message')}"
    )

# Non-zero exit if gate is not OK or open issues remain
quality_gate = status.get("qualityGateStatus", "UNKNOWN")
if quality_gate != "OK" or issues_data.get("total", 0) > 0:
    sys.exit(3)
PY

