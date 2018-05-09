FROM jetty:9.4.9-jre8-alpine

# export port 
EXPOSE 8080

# Move in necessary assets
COPY target/content-1.0-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war

# Add the build tag
COPY buildtag.* /
