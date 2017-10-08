
# ppmselector
FROM openshift/base-centos7
MAINTAINER Johan Kindgren <johan.kindgren@gmail.com>

ENV BUILDER_VERSION 1.0

# TODO: Set labels used in OpenShift to describe the builder image
LABEL io.k8s.description="Platform for building PpmSelector" \
      io.k8s.display-name="PpmSelector builder 1.0.0" \
      io.openshift.expose-services="8080:http" \
      io.openshift.tags="builder,gradle-${GRADLE_VERSION},springboot"

# Install build tools on top of base image
# Java jdk 8, Maven 3.3, Gradle 2.6
ENV GRADLE_VERSION 4.2

RUN yum install -y --enablerepo=centosplus \
    tar unzip bc which lsof java-1.8.0-openjdk java-1.8.0-openjdk-devel && \
    yum clean all -y && \
    curl -sL -0 https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip /tmp/gradle-${GRADLE_VERSION}-bin.zip -d /usr/local/ && \
    rm /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    mv /usr/local/gradle-${GRADLE_VERSION} /usr/local/gradle && \
    ln -sf /usr/local/gradle/bin/gradle /usr/local/bin/gradle && \
    mkdir -p /opt/openshift && \
    mkdir -p /opt/app-root/source && chmod -R a+rwX /opt/app-root/source && \
    mkdir -p /opt/s2i/destination && chmod -R a+rwX /opt/s2i/destination && \
    mkdir -p /opt/app-root/src && chmod -R a+rwX /opt/app-root/src

ENV PATH=/opt/gradle/bin/:$PATH

LABEL io.openshift.s2i.scripts-url=image:///usr/local/sti
COPY ./s2i/bin/ /usr/local/sti

RUN chown -R 1001:1001 /opt/openshift

USER 1001

EXPOSE 8080

CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/openshift/app.jar"]
