FROM java:8
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
ENV SPRING_PROFILES_ACTIVE=cloud
VOLUME /tmp

ADD hadoop-2.6.0.tar.gz /
ENV PATH=$PATH:/hadoop-2.6.0/bin/

RUN mkdir /yarn-conf
ENV HADOOP_CONF_DIR=/yarn-conf

ADD krb5.conf /etc/krb5.conf
RUN apt-get update
RUN apt-get install -y krb5-user
RUN cat /etc/krb5.conf

ADD h2odriver.jar h2odriver.jar
ADD h2o-provisioner-0.4.3.jar app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
