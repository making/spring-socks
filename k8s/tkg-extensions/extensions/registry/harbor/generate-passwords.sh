#!/bin/bash
set -o errexit

function random_string() {
  len=${1:-8}
  LC_CTYPE=C tr -cd a-zA-Z0-9 < /dev/urandom | fold -w "$len" | head -n 1
}

function print_passwords() {
cat <<EOF
# [Required] The initial password of Harbor admin.
harborAdminPassword: $harborAdminPassword

# [Required] The secret key used for encryption. Must be a string of 16 chars.
secretKey: $secretKey

database:
  # [Required] The initial password of the postgres database.
  password: $databasePassword

core:
  # [Required] Secret is used when core server communicates with other components.
  secret: $coreSecret
  # [Required] The XSRF key. Must be a string of 32 chars.
  xsrfKey: $coreXsrfKey
jobservice:
  # [Required] Secret is used when job service communicates with other components.
  secret: $jobserviceSecret
registry:
  # [Required] Secret is used to secure the upload state from client
  # and registry storage backend.
  # See: https://github.com/docker/distribution/blob/master/docs/configuration.md#http
  secret: $registrySecret

Please copy the above randomly generated passwords and secrets into the data values yaml file.
EOF
}

function inject_passwords_inline() {
  yq w -i "$1" 'harborAdminPassword' "$harborAdminPassword"
  yq w -i "$1" 'secretKey' "$secretKey"
  yq w -i "$1" 'database.password' "$databasePassword"
  yq w -i "$1" 'core.secret' "$coreSecret"
  yq w -i "$1" 'core.xsrfKey' "$coreXsrfKey"
  yq w -i "$1" 'jobservice.secret' "$jobserviceSecret"
  yq w -i "$1" 'registry.secret' "$registrySecret"
  # shellcheck disable=SC1004
  sed -i -e '3i\
---
' "$1"
  rm -f "$1-e"

  echo "Successfully generated random passwords and secrets in $1"
}

function install_yq() {
  if ! which yq >/dev/null; then
    echo 'Please install yq from https://github.com/mikefarah/yq'
    exit 1
  fi
}

# Generate random passwords and secrets
harborAdminPassword=$(random_string 16)
secretKey=$(random_string 16)
databasePassword=$(random_string 16)
coreSecret=$(random_string 16)
coreXsrfKey=$(random_string 32)
jobserviceSecret=$(random_string 16)
registrySecret=$(random_string 16)

if [ $# = 0 ]; then
  print_passwords
else
  install_yq
  inject_passwords_inline "$1"
fi
