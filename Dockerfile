FROM anapsix/alpine-java:8_server-jre_unlimited

ENV MULE_VERSION=3.9.0 HTTP_CONN_PORT_1=8088 INTEROP_DOMAIN_VERSION=0.1.38

# SSL Cert for downloading mule zip
RUN apk --no-cache update && \
    apk --no-cache upgrade && \
    apk --no-cache add ca-certificates && \
    update-ca-certificates && \
    apk --no-cache add openssl
    
RUN if [ "${MULE_VERSION}" != "3.9.0" ]; then echo "-----   Unsupported version: ${MULE_VERSION}   -----" && return 1; fi

# For checksum, alpine linux needs two spaces between checksum and file name
RUN cd ~ && wget https://repository-master.mulesoft.org/nexus/content/repositories/releases/org/mule/distributions/mule-standalone/${MULE_VERSION}/mule-standalone-${MULE_VERSION}.tar.gz && echo "39b773bf20702f614faf30b2ffca4716  mule-standalone-${MULE_VERSION}.tar.gz" | md5sum -c

RUN cd /opt && tar xvzf ~/mule-standalone-${MULE_VERSION}.tar.gz && rm ~/mule-standalone-${MULE_VERSION}.tar.gz && ln -s /opt/mule-standalone-${MULE_VERSION} /opt/mule

RUN cd /opt/mule && wget https://docker-user:BMGFGates2016@modusbox.jfrog.io/modusbox/libs-release/com/l1p/interop/interop-domain/${INTEROP_DOMAIN_VERSION}/interop-domain-${INTEROP_DOMAIN_VERSION}.zip -O domains/interop-domain.zip

ENV MULE_HOME /opt/mule

COPY target/dfsp-scheme-adapter*.zip /opt/mule/apps

# Define mount points.
VOLUME ["/opt/mule/logs", "/opt/mule/conf", "/opt/mule/apps", "/opt/mule/domains"]

# Define working directory.
WORKDIR /opt/mule

CMD [ "/opt/mule/bin/mule"]

# Default http ports
EXPOSE ${HTTP_CONN_PORT_1}
