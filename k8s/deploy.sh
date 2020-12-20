#!/bin/bash
set -eo pipefail

APP=$1

if [ "${APP}" == "" ];then
  echo "Usage: $0 <APP>"
  exit 1
fi

shift

set -x
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

kapp deploy -a ${APP} \
  -f <(ytt -f ${SCRIPT_DIR}/../${APP}/k8s --data-values-env YTT | kbld -f -) \
  -c $@