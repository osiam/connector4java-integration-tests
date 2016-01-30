FROM mysql:5.7

RUN cp -p /usr/share/zoneinfo/${user.timezone} /etc/localtime
RUN echo "${user.timezone}" > /etc/timezone

RUN echo '[mysqld]\nskip-name-resolve' > /etc/mysql/conf.d/no-dns.cnf

ENV MYSQL_ROOT_PASSWORD mysql_root_password
ENV MYSQL_DATABASE ong
ENV MYSQL_USER ong
ENV MYSQL_PASSWORD b4s3dg0d
