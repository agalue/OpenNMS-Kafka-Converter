#!/bin/bash -e
# =====================================================================
# Build script running OpenNMS gRPC Server in Docker environment
# =====================================================================

function join { local IFS="$1"; shift; echo "$*"; }

IFS=$'\n'
OPTIONS=("acks=1")
for VAR in $(env)
do
  env_var=$(echo "$VAR" | cut -d= -f1)
  if [[ $env_var =~ ^KAFKA_ ]]; then
    key=$(echo "$env_var" | cut -d_ -f2- | tr '[:upper:]' '[:lower:]' | tr _ .)
    val=${!env_var}
    if [[ $key == "manager."* ]]; then
      echo "[Skipping] '$key'"
    else
      echo "[Configuring] '$key'='$val'"
      OPTIONS+=("$key=$val")
    fi
  fi
done

exec java ${JAVA_OPTS} -jar kafka-message-converter.jar -b ${BOOTSTRAP_SERVERS} -a ${APP_ID-gpb2json} -k ${SOURCE_KIND-events} -s ${SOURCE_TOPIC} -t ${DEST_TOPIC} -e $(join , ${OPTIONS[@]})
