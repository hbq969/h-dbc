#!/bin/bash

export op="docker-build"

if [[ -f "../setenv.sh" ]];then
. ../setenv.sh
fi

name=dbc-config
ver=${tag}
tag=${name}:${ver}

echo "Stop Docker Containers ..."
cid=`docker ps -a|grep ${name}|grep ${ver}|awk '{print $1}'`
if [[ -n "${cid}" ]]; then
  docker rm -f ${cid}
  echo "${tag},${cid} was stop."
fi

echo "Uninstall Docker Images ..."
mid=`docker images|grep "${name}"|grep "${ver}"|awk '{print $3}'`
if [[ -n "${mid}" ]]; then
  docker rmi -f ${mid}
  echo "${tag},${mid} was uninstalled."
fi

if [[ "$platform" == "linux/arm64"* ]]; then
    cat Dockerfile |  sed "s/# FROM anolis-registry.cn-zhangjiakou.cr.aliyuncs.com\/openanolis\/openjdk@sha256:28a70636419473cd5e40fc103dfb11beb62bc9fff235d968ca5098f09a51a093/FROM anolis-registry.cn-zhangjiakou.cr.aliyuncs.com\/openanolis\/openjdk@sha256:28a70636419473cd5e40fc103dfb11beb62bc9fff235d968ca5098f09a51a093/g" > ../../Dockerfile
else
    cat Dockerfile | sed "s/# FROM anolis-registry.cn-zhangjiakou.cr.aliyuncs.com\/openanolis\/openjdk@sha256:2d10a2db5629ee59137f3e5cef5bbb6ffc0fe80426fac1603d830144e1dddcff/FROM anolis-registry.cn-zhangjiakou.cr.aliyuncs.com\/openanolis\/openjdk@sha256:2d10a2db5629ee59137f3e5cef5bbb6ffc0fe80426fac1603d830144e1dddcff/g" > ../../Dockerfile
fi

#cp Dockerfile ../../
echo "Start Docker Images Building ..."
docker build --platform ${platform} -t ${docker_prefix}/${tag} ../../
