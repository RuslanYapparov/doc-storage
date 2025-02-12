FROM eclipse-temurin:21-alpine
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081"
ENV JAVA_OPTS="-Xmx2G"
ENV TZ="Europe/Samara"
ENV LANG="ru_RU.UTF-8"
ENV LANGUAGE="ru_RU.UTF-8"
ENV LC_ALL="ru_RU.UTF-8"
RUN mkdir /opt/app
COPY target/doc-storage-1.0-SNAPSHOT.jar /opt/app
ENTRYPOINT ["java","-jar","/opt/app/doc-storage-1.0-SNAPSHOT.jar"]