# connector4java-integration-tests [![Circle CI](https://circleci.com/gh/osiam/connector4java-integration-tests.svg?style=svg)](https://circleci.com/gh/osiam/connector4java-integration-tests)

The integration-tests for OSIAM.

## Install

You can run the integration-tests on your machine, you only need to install
java, maven and docker, and configure docker.

The tests will fetch the snapshot dependencies from evolvis or you clone the
following repos and install them with ```mvn clean install```

```
https://github.com/osiam/scim-schema
https://github.com/osiam/connector4java
https://github.com/osiam/auth-server
https://github.com/osiam/resource-server
https://github.com/osiam/addon-self-administration
https://github.com/osiam/addon-self-administration-plugin-api
https://github.com/osiam/examples/tree/master/addon-self-administration-plugin
https://github.com/osiam/addon-administration
```

### Configure Docker

The integration-tests use the [docker-maven-plugin](https://github.com/alexec/docker-maven-plugin),
which utilizes [docker-java](https://github.com/docker-java/docker-java).
In order to run the integration-tests, you need to ensure that your docker daemon
listens on the TCP port `2375`.

How exactly this works depends on your operating system, but

    echo 'DOCKER_OPTS="-H tcp://127.0.0.1:2375 -H unix:///var/run/docker.sock' >> /etc/default/docker

is a good starting point. For further information, please refer to  the
[docker-java README](https://github.com/docker-java/docker-java#build-with-maven)
and the official Docker documentation.

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

## Cross Project Debugging

If you want to use the integration tests to debug code in other OSIAM projects,
you need to enable the `debug` profile. Please remember that this overrides
the active-by-default setting for the `postgres` profile, so if you want to use
it, run it like

    $ mvn clean pre-integration-test -P postgres,debug

This changes nothing for the `mysql` profile, so running

    $ mvn clean pre-integration-test -P mysql,debug

is equivalent.

In your IDE containing the project you want to debug, you can now attach the debugger.
Just use the normal remote debugging setup for your IDE and connect to `localhost:8000`.
Set your breakpoints as usual and run the test in the ITs project.
Your IDE should pop up as soon as the service reaches the breakpoint.

