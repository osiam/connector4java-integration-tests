FROM tomcat:8-jre8

COPY osiam.xml /usr/local/tomcat/conf/Catalina/localhost/osiam.xml
COPY osiam.war $CATALINA_HOME/webapps/osiam.war
COPY osiam-home /var/lib/osiam
COPY osiam-home/config/osiam_${database.container}.yaml /var/lib/osiam/config/osiam.yaml

COPY manager $CATALINA_HOME/webapps/manager

RUN sed -i 's/8080/8180/g' $CATALINA_HOME/conf/server.xml
RUN echo "JPDA_ADDRESS=8000" > $CATALINA_HOME/bin/setenv.sh

RUN cp -p /usr/share/zoneinfo/${user.timezone} /etc/localtime
RUN echo "${user.timezone}" > /etc/timezone

EXPOSE 8180 8000

CMD [ "catalina.sh", ${debug.pre.command.tomcat} "run"]
