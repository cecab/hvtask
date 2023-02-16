## hvtask - A Simple MQTT web bridge
This is a demo sample webservice to show an example of an bridge between http and MQTT broker.

## For Developers
To run in a local dev environment, first start the docker container to spin up the Postgresql DB

```shell
% docker compose up db -d
[+] Running 2/2
 ⠿ Network hvtask_default  Created                                                                                                                                                                               0.0s
 ⠿ Container hvtask-db-1   Started      
```
Open your project in your favorite IDE, or to try some request, start the project from command line with `gradlew` script:
```shell
% ./gradlew run

> Task :compileJava
Note: Creating bean classes for 5 type elements

> Task :run
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
  Micronaut (v3.8.4)

...
```

## For Deployment
Build the JAR file to deploy the project:
```shell
 % ./gradlew shadowJar

BUILD SUCCESSFUL in 3s
3 actionable tasks: 1 executed, 2 up-to-date

```
A new JAR file exist in `./build/libs/hvtask-0.1-all.jar`


## About Micronaut
Here is a list of some of Micronaut modules used by this project.
- [User Guide](https://docs.micronaut.io/3.8.4/guide/index.html)
- [API Reference](https://docs.micronaut.io/3.8.4/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/3.8.4/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
- [Shadow Gradle Plugin](https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow)
- [Micronaut Hikari JDBC Connection Pool documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#jdbc)
- [Micronaut Liquibase Database Migration documentation](https://micronaut-projects.github.io/micronaut-liquibase/latest/guide/index.html)
- [https://www.liquibase.org/](https://www.liquibase.org/)
