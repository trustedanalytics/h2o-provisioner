FROM java:8
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
ENV SPRING_PROFILES_ACTIVE=cloud
ENV PATH=$PATH:/hadoop-2.6.0/bin/
ENV HADOOP_CONF_DIR=/yarn-conf

VOLUME /tmp

RUN apt-get update \
  && apt-get install -y npm \
  && apt-get install -y nodejs \
  && ln -s `which nodejs` /usr/bin/node \
  && apt-get install -y python-pip \
  && pip install tabulate \
  && apt-get install -y r-base

RUN git clone https://github.com/trustedanalytics/h2o-3 /h2o-3 \
  && cd /h2o-3 \
  && export BUILD_HADOOP=1; ./gradlew build -x test

ADD /src/main/docker/krb5.conf /etc/krb5.conf
COPY / /h2o-provisioner/

RUN mkdir /yarn-conf \
  && apt-get update \
  && apt-get install -y krb5-user maven \
  && cat /etc/krb5.conf \
  && wget http://archive.apache.org/dist/hadoop/core/hadoop-2.6.0/hadoop-2.6.0.tar.gz \
  && cp /h2o-3/h2o-hadoop/h2o-cdh5.4.2-assembly/build/libs/h2odriver.jar /h2odriver.jar

RUN cd /h2o-provisioner \
  && mvn clean package \
  && cp /h2o-provisioner/target/h2o-provisioner-*[0-9].jar /app.jar

RUN cd / \
  && rm -rf /h2o-provisioner \
  && rm -rf /h2o-3.0.1.7-cdh5.4.2* \
  && rm -rf /var/lib/apt/lists/*

EXPOSE 8080 54310-54319

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
