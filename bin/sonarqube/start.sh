#!/bin/bash

echo "Restarting sonarqube"
docker stop sonarqube > /dev/null
docker rm sonarqube > /dev/null
docker pull sonarqube:community
docker run -d --name sonarqube -p 19000:9000 -p 19092:9092 sonarqube:community > /dev/null

echo "Waiting for sonarqube, and changing admin password"
sleep 90
curl -s -X POST -u admin:admin \
     "http://localhost:19000/api/users/change_password?login=admin&previousPassword=admin&password=ZxX62!D!7bf!aF3eJXnra2suo6NyWT"

echo "Generating token"
SONARQUBE_TOKEN=$(curl -s -X POST -H \
                       "Content-Type: application/x-www-form-urlencoded" \
                       -d "name=gradle" \
                       -u admin:ZxX62!D!7bf!aF3eJXnra2suo6NyWT \
                       "http://localhost:19000/api/user_tokens/generate" | jq -r '.token')
export SONARQUBE_TOKEN
echo "The following token has been generated: ${SONARQUBE_TOKEN}"
