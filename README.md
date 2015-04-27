connector4java-integration-tests [![Build Status](https://travis-ci.org/osiam/connector4java-integration-tests.png?branch=master)](https://travis-ci.org/osiam/connector4java-integration-tests)
================================

The integration-tests for OSIAM.

# Install

You can run the integration-tests on your machine, you only need to install
java, maven and docker.

The tests will fetch the snapshot dependencies from evolvis or you clone the
following repos and install them with ```mvn clean install```

```
https://github.com/osiam/scim-schema
https://github.com/osiam/connector4java
https://github.com/osiam/server
https://github.com/osiam/addon-self-administration
https://github.com/osiam/addon-self-administration-plugin-api
https://github.com/osiam/examples/tree/master/addon-self-administration-plugin
https://github.com/osiam/addon-administration
```

# Run

To run the integration-tests against 

postgres (default)

```
mvn clean verify
```

mysql

```
mvn clean verify -P mysql
```
