#!/bin/bash -ex

case $CIRCLE_NODE_INDEX in
    0)
        ./mvnw verify -P postgres -Dit.test=org.osiam.test.integration.*IT -Ddocker.removeIntermediateImages=false
        ;;
    1)
        ./mvnw verify -P mysql -Dit.test=org.osiam.test.integration.*IT -Ddocker.removeIntermediateImages=false
        ;;
    2)
        ./mvnw verify -P postgres -Dit.test=org.osiam.client.*IT -Ddocker.removeIntermediateImages=false
        ;;
    3)
        ./mvnw verify -P mysql -Dit.test=org.osiam.client.*IT -Ddocker.removeIntermediateImages=false
        ;;
esac
