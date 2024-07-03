# builder
FROM public.ecr.aws/docker/library/amazoncorretto:8-al2023-jdk as builder

# update and install dependancies
RUN yum -y update && yum -y install maven

# copy assets
WORKDIR /build
COPY src ./src
COPY pom.xml ./

# build the tartet WAR file
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-amazon-corretto.x86_64
RUN mvn clean package

# base image
FROM jetty:9.4.9-jre8-alpine

# export port 
EXPOSE 8080

# copy the WAR file
COPY --from=builder /build/target/digital-content.war /var/lib/jetty/webapps/ROOT.war

# Add the build tag
COPY buildtag.* /

#
# end of file
#
