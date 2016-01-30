FROM tomcat:8-jre8

COPY osiam $CATALINA_HOME/webapps/osiam
COPY addon-self-administration $CATALINA_HOME/webapps/addon-self-administration
COPY addon-self-administration-plugin-example.jar /install/plugin-example.jar
COPY selfadminexample.properties /install/
COPY manager $CATALINA_HOME/webapps/manager
COPY greenmail-webapp-1.4.1 $CATALINA_HOME/webapps/greenmail-webapp

RUN cat /install/selfadminexample.properties >> $CATALINA_HOME/conf/catalina.properties
RUN sed -i 's/8080/8180/g' $CATALINA_HOME/conf/server.xml
RUN echo "JPDA_ADDRESS=8000" > $CATALINA_HOME/bin/setenv.sh
RUN cp -p /usr/share/zoneinfo/${user.timezone} /etc/localtime
RUN echo "${user.timezone}" > /etc/timezone

EXPOSE 8180 11110 8000

CMD [ "catalina.sh", ${debug.pre.command.tomcat} "run"]
