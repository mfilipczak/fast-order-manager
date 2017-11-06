FROM java:8
VOLUME /tmp
ADD target/fast-order-manager-0.1.0.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar