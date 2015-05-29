# connector4java-integration-tests [![Circle CI](https://circleci.com/gh/osiam/connector4java-integration-tests.svg?style=svg)](https://circleci.com/gh/osiam/connector4java-integration-tests)

The integration-tests for OSIAM.

## Install

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

## Run

To run the integration-tests against 

postgres (default)

    $ mvn clean verify

mysql

    $ mvn clean verify -P mysql

## Run in your IDE

To run the integration-tests in your IDE against the started containers

postgres (default)

    $ mvn clean pre-integration-test

mysql

    $ mvn clean pre-integration-test -P mysql

If you are on mac or want to run them in a VM, just checkout the
[OSIAM vagrant VM](https://github.com/osiam/vagrant). It's pretty easy to setup.
Just run the above mentioned command in the OSIAM vagrant VM and run the
integration-tests against the VM.
