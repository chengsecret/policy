FROM moxm/java:1.8-full

RUN mkdir -p /policy-auth

WORKDIR /policy-auth

ARG JAR_FILE=target/policy-auth.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8989

ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms256m -Xmx256m -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT java $JAVA_OPTS -jar app.jar
