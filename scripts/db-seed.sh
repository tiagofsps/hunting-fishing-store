#!/usr/bin/env bash
#
# Resets the database and starts the app so the dev seeders run.
#
# Seeding happens automatically on startup against an empty database:
# 20 demo users, the fixed admin@huntingfishing.com / user@huntingfishing.com
# accounts (password Password123!), and the catalog data. The app stays
# running until you press Ctrl+C.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

"${SCRIPT_DIR}/db-reset.sh"

echo "Starting the app (dev profile) to seed the database..."
cd "${REPO_ROOT}"
./mvnw spring-boot:run