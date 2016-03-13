FROM java:8-jre

COPY osiam.war /osiam.war
RUN java -jar /osiam.war initHome --osiam.home=/var/lib/osiam
COPY osiam_${database.container}.yaml /var/lib/osiam/config/osiam.yaml

RUN cp -p /usr/share/zoneinfo/${user.timezone} /etc/localtime
RUN echo "${user.timezone}" > /etc/timezone

EXPOSE 8180 8000
ENV SERVER_PORT=8180

CMD java ${osiam.debug.opts} -jar /osiam.war --osiam.home=/var/lib/osiam
