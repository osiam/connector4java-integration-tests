FROM java:8-jre

COPY osiam.war /osiam.war
RUN chmod +x /osiam.war
RUN /osiam.war initHome --osiam.home=/var/lib/osiam
COPY osiam_${database.container}.yaml /var/lib/osiam/config/osiam.yaml

RUN cp -p /usr/share/zoneinfo/${user.timezone} /etc/localtime
RUN echo "${user.timezone}" > /etc/timezone

EXPOSE 8180 8000
ENV SERVER_PORT=8180 JAVA_OPTS="${osiam.debug.opts}"

CMD /osiam.war --osiam.home=/var/lib/osiam
