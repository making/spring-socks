#!/bin/bash
set -ex
#export JAVA_HOME=${HOME}/Library/Java/JavaVirtualMachines/liberica-1.8.0_252
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
${SCRIPT_DIR}/../mvnw -V clean generate-sources -f ${SCRIPT_DIR}/../pom.xml -P client
${SCRIPT_DIR}/../mvnw -V package -f ${SCRIPT_DIR}/../target/generated-sources/openapi/pom.xml