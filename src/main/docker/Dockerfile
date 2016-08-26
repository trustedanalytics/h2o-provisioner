FROM java:8
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
ENV SPRING_PROFILES_ACTIVE=cloud
ENV PATH=$PATH:/hadoop-2.6.0/bin/
ENV HADOOP_CONF_DIR=/yarn-conf

VOLUME /tmp

ADD /src/main/docker/krb5.conf /etc/krb5.conf
COPY / /h2o-provisioner/

RUN apt-get update --fix-missing
RUN apt-get install -y npm nodejs python-pip r-base krb5-user maven
RUN ln -s `which nodejs` /usr/bin/node
RUN pip install tabulate

RUN git clone https://github.com/trustedanalytics/h2o-3 /h2o-3
RUN export HTTP_PROXY_JAVA=`env | grep 'HTTP_PROXY=' | cut -d":" -f 2 | cut -d "/" -f 3`; \
  export HTTP_PROXY_JAVA_PORT=`env | grep 'HTTP_PROXY=' | cut -d":" -f 3`; \
  export HTTPS_PROXY_JAVA=`env | grep 'HTTPS_PROXY=' | cut -d":" -f 2 | cut -d "/" -f 3`; \
  export HTTPS_PROXY_JAVA_PORT=`env | grep 'HTTPS_PROXY=' | cut -d":" -f 3`; \
  if [ $HTTP_PROXY_JAVA ] && [ $HTTP_PROXY_JAVA_PORT ] && [ $HTTPS_PROXY_JAVA ] && [ $HTTPS_PROXY_JAVA_PORT ]; then export JAVA_OPTS="-Dhttp.proxyHost=$HTTP_PROXY_JAVA -Dhttp.proxyPort=$HTTP_PROXY_JAVA_PORT -Dhttps.proxyHost=$HTTPS_PROXY_JAVA -Dhttps.proxyPort=$HTTPS_PROXY_JAVA_PORT"; fi; \
  cd /h2o-3;  \
  export BUILD_HADOOP=1; ./gradlew build -x test

RUN mkdir /yarn-conf \
  && cat /etc/krb5.conf \
  && wget http://archive.apache.org/dist/hadoop/core/hadoop-2.6.0/hadoop-2.6.0.tar.gz \
  && tar -xvf hadoop-2.6.0.tar.gz \
  && cp /h2o-3/h2o-hadoop/h2o-cdh5.4.2-assembly/build/libs/h2odriver.jar /h2odriver.jar

RUN cd /h2o-provisioner; \
  export HTTP_PROXY_JAVA=`env | grep 'HTTP_PROXY=' | cut -d":" -f 2 | cut -d "/" -f 3`; \
  export HTTP_PROXY_JAVA_PORT=`env | grep 'HTTP_PROXY=' | cut -d":" -f 3`; \
  export HTTPS_PROXY_JAVA=`env | grep 'HTTPS_PROXY=' | cut -d":" -f 2 | cut -d "/" -f 3`; \
  export HTTPS_PROXY_JAVA_PORT=`env | grep 'HTTPS_PROXY=' | cut -d":" -f 3`; \
  if [ $HTTP_PROXY_JAVA ] && [ $HTTP_PROXY_JAVA_PORT ] && [ $HTTPS_PROXY_JAVA ] && [ $HTTPS_PROXY_JAVA_PORT ]; \
    then mvn clean package -Dhttp.proxyHost=$HTTP_PROXY_JAVA -Dhttp.proxyPort=$HTTP_PROXY_JAVA_PORT -Dhttps.proxyHost=$HTTPS_PROXY_JAVA -Dhttps.proxyPort=$HTTPS_PROXY_JAVA_PORT; \
    else mvn clean package; fi
RUN cp /h2o-provisioner/target/h2o-provisioner-*[0-9].jar /app.jar

RUN cd / \
  && rm -rf /h2o-3 \
  && rm -rf /h2o-provisioner \
  && rm -rf /h2o-3.0.1.7-cdh5.4.2* \
  && rm -rf /var/lib/apt/lists/*

EXPOSE 8080 54310-54319

RUN ln -s -f /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java /etc/alternatives/java
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
