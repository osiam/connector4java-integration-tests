#!/bin/bash -ex

case $CIRCLE_NODE_INDEX in
    0)
        mvn verify -P postgres -Dit.test=org.osiam.test.integration.*IT
        ;;
    1)
        mvn verify -P mysql -Dit.test=org.osiam.test.integration.*IT
        ;;
    2)
        mvn verify -P postgres -Dit.test=org.osiam.client.*IT
        ;;
    3)
        mvn verify -P mysql -Dit.test=org.osiam.client.*IT
        ;;
esac
