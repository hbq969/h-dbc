#!/bin/bash

export op="k8s-apply"

if [[ -f "../setenv.sh" ]];then
. ../setenv.sh
fi

if [[ -z "$k8s_api_version" ]]; then
    export k8s_api_version=extensions/v1beta1
fi
echo "k8s_api_version:$k8s_api_version"

APP_NAME=dbc-config-deployment

kubectl set image deployment ${APP_NAME} ${APP_NAME}=${docker_prefix}/dbc-config:${tag} -n ${k8s_ns}

