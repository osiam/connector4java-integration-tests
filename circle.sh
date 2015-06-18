#!/bin/bash -ex

case $CIRCLE_NODE_INDEX in
    0)
        mvn verify -P postgres -Dit.test=org.osiam.test.integration.*IT -Ddocker.removeIntermediateImages=false
        ;;
    1)
        mvn verify -P mysql -Dit.test=org.osiam.test.integration.*IT -Ddocker.removeIntermediateImages=false
        ;;
    2)
        mvn verify -P postgres -Dit.test=org.osiam.client.*IT -Ddocker.removeIntermediateImages=false
        ;;
    3)
        mvn verify -P mysql -Dit.test=org.osiam.client.*IT -Ddocker.removeIntermediateImages=false
        ;;
esac
