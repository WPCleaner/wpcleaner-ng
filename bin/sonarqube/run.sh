#!/bin/bash

echo "Checking if sonarqube is configured"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
if [ -z ${SONARQUBE_TOKEN+x} ]; then . "$SCRIPT_DIR"/start.sh; fi

echo "Running sonarqube analysis"
./gradlew build
./gradlew sonar -Dsonar.token="$SONARQUBE_TOKEN"
