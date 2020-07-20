#!/bin/bash
set -ex
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
export SOCK_FRONTENDURL=http://localhost:808

./mvnw test -f ${SCRIPT_DIR}/pom.xml