FROM jetty:9.4.9-jre8-alpine
EXPOSE 8080
COPY target/content-1.0-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war
