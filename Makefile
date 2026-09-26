.PHONY: db-reset db-seed db-setup db-shell

# Destructive: drop and recreate the database (data loss).
db-reset:
	bash scripts/db-reset.sh

# Reset the database and start the app so the dev seeders run.
db-seed:
	bash scripts/db-seed.sh

# Alias for db-seed: reset + start the app to seed.
db-setup: db-seed

# Open an interactive psql session against the store database.
db-shell:
	bash scripts/db-shell.sh