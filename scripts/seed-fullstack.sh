#!/usr/bin/env bash
#
# Apply the full-stack E2E journey seed (the pre-seeded ACTIVE admin) to the
# running Infra-FullStack-Tests Postgres. Run AFTER the fullstack backend has
# migrated the production schema (the iam_user table must exist).
#
# Used identically by local runs and the nightly CI workflow: both start the
# DB via docker/infra-fullstack-tests.yml, so the container name is the same
# and this script needs no local psql client. Idempotent. Keeps the production
# jar free of test fixtures (the SQL lives in src/test/resources).
set -euo pipefail

CONTAINER="${FULLSTACK_DB_CONTAINER:-rpm-ddd-fullstack-tests}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SEED_FILE="${REPO_ROOT}/src/test/resources/db/fixtures/fullstack-seed.sql"

docker exec -i "${CONTAINER}" psql -v ON_ERROR_STOP=1 -U postgres -d rpm_ddd < "${SEED_FILE}"
echo "Full-stack seed applied to ${CONTAINER}"
