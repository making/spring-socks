#!/bin/bash
set -ex
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
export SOCK_FRONTENDURL=http://localhost:8080
export SOCK_CATALOGURL=http://localhost:15001
export SOCK_CARTURL=http://localhost:15005
export SOCK_ORDERURL=http://localhost:15004
export SOCK_PAYMENTURL=http://localhost:15002
export SOCK_SHIPPINGURL=http://localhost:15003
export SOCK_USERURL=http://localhost:15006

./mvnw test -V -f ${SCRIPT_DIR}/pom.xml