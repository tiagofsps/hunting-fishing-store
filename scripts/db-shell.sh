#!/usr/bin/env bash
#
# Opens an interactive psql session against the store database.
#
# Credentials default to the .env.example values and can be overridden via
# POSTGRES_* environment variables or a .env file.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Load optional .env overrides (POSTGRES_*)
if [[ -f "${REPO_ROOT}/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "${REPO_ROOT}/.env"
  set +a
fi

PGHOST="${POSTGRES_HOST:-localhost}"
PGPORT="${POSTGRES_PORT:-5432}"
PGUSER="${POSTGRES_USER:-postgres}"
export PGPASSWORD="${POSTGRES_PASSWORD:-postgres}"
DB="${POSTGRES_DB:-hunting_fishing_store}"

exec psql -h "${PGHOST}" -p "${PGPORT}" -U "${PGUSER}" -d "${DB}"