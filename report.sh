#!/bin/bash -ex

mkdir -p $CIRCLE_TEST_REPORTS/junit/
find . -type f -regex ".*/target/.*-reports/.*xml" -exec cp {} $CIRCLE_TEST_REPORTS/junit/ \;
