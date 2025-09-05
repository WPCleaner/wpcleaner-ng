# Sonarqube

This document explains how to locally run a SonarQube analysis of the project.

## Run and configure SonarQube

Run the following command, which will start and configure SonarQube:
```shell script
. bin/sonarqube/start.sh
```

This command will run a SonarQube instance in docker and generate a token necessary for the next steps.
The command stores the generated token in the `SONARQUBE_TOKEN` environment variable.

## Run the SonarQube analysis on the project

```shell script
. bin/sonarqube/run.sh
```

## Display the SonarQube analysis

Open [SonarQube analysis](http://localhost:19000/dashboard?id=org.wpcleaner%3Awpcleaner-ng&codeScope=overall) and login with `admin`/`ZxX62!D!7bf!aF3eJXnra2suo6NyWT`

## Manually configuring SonarQube

The command `. bin/sonarqube/start.sh` is equivalent to manually doing the following actions:
```shell script
docker run -d --name sonarqube -p 19000:9000 -p 19092:9092 sonarqube
```
* Open [SonarQube](http://localhost:19000)
* Connect with `admin`/`admin`
* Change the password to `ZxX62!D!7bf!aF3eJXnra2suo6NyWT`
* Go to My Account (click on the user icon `A`)
* Click on Security tab
* Enter a Token name and click on Generate
* Export the generated token as an environment variable `export SONARQUBE_TOKEN=TOKEN` as it will be necessary for the other steps
