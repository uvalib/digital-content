FROM jetty:9.4.9-jre8-alpine

# export port 
EXPOSE 8080

# Move in necessary assets
COPY target/digital-content.war /var/lib/jetty/webapps/ROOT.war

# Add the build tag
COPY buildtag.* /
