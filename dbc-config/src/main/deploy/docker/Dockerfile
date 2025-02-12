#  ARM
# FROM anolis-registry.cn-zhangjiakou.cr.aliyuncs.com/openanolis/openjdk@sha256:28a70636419473cd5e40fc103dfb11beb62bc9fff235d968ca5098f09a51a093
# X86
# FROM anolis-registry.cn-zhangjiakou.cr.aliyuncs.com/openanolis/openjdk@sha256:2d10a2db5629ee59137f3e5cef5bbb6ffc0fe80426fac1603d830144e1dddcff

MAINTAINER hbq969@gmail.com

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /opt/app/dbc-config

ENV LANG=C.UTF-8

COPY / .

ENTRYPOINT ["/bin/bash", "/opt/app/dbc-config/deploy/docker/bootstrap.sh"]
