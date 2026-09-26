#!/usr/bin/env bash
#
# Destructive: drops and recreates the hunting_fishing_store database.
#
# Uses the host psql client against the published Postgres port, so it works
# without Docker CLI access. Credentials default to the .env.example values
# and can be overridden via POSTGRES_* environment variables or a .env file.
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

echo "Resetting database '${DB}' on ${PGHOST}:${PGPORT} (user: ${PGUSER})..."
echo "  -> dropping (WITH FORCE terminates active connections)"
psql -h "${PGHOST}" -p "${PGPORT}" -U "${PGUSER}" -v ON_ERROR_STOP=1 \
  -c "DROP DATABASE IF EXISTS \"${DB}\" WITH (FORCE);"
echo "  -> recreating"
psql -h "${PGHOST}" -p "${PGPORT}" -U "${PGUSER}" -v ON_ERROR_STOP=1 \
  -c "CREATE DATABASE \"${DB}\";"
echo "Done. Start the app (dev profile) to re-run Flyway migrations and seed data."