#!/bin/bash
set -ex
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
GATEWAY_URL=http://localhost:8080
export SOCK_CATALOGURL=${GATEWAY_URL}
export SOCK_PAYMENTURL=${GATEWAY_URL}
export SOCK_SHIPPINGURL=${GATEWAY_URL}
export SOCK_ORDERURL=${GATEWAY_URL}
export SOCK_CARTURL=${GATEWAY_URL}

./mvnw test -f ${SCRIPT_DIR}/pom.xml