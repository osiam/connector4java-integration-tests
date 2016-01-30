FROM osixia/openldap:1.0.9

ENV LDAP_ORGANISATION="OSIAM ITs"
ENV LDAP_DOMAIN=osiam.org
ENV LDAP_TLS=false

COPY security.ldif /container/service/slapd/assets/config/bootstrap/ldif/90-osiam-security.ldif
COPY test-data.ldif /container/service/slapd/assets/config/bootstrap/ldif/99-test-data.ldif
