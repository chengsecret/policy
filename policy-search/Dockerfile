FROM moxm/java:1.8-full

RUN mkdir -p /policy-search

WORKDIR /policy-search

ARG JAR_FILE=target/policy-search.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8989

ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms1024m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT  java $JAVA_OPTS -jar app.jar
