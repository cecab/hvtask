## hvtask - A Simple MQTT web bridge
This is a demo sample webservice to show an example of an bridge between http and MQTT broker.

## For Developers
To run in a local dev environment, start the docker containers needed:

```shell
% docker compose up db mqttbroker -d
[+] Running 2/0
 ⠿ Container hvtask-mqttbroker-1  Running                                                                                                                                                                        0.0s
 ⠿ Container hvtask-db-1          Running        
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

## Manual testing of the Web API
For a full running environment deployed in docker, just start everything with `docker compose`:
```shell
% docker compose up -d 
[+] Running 3/0
 ⠿ Container hvtask-db-1          Running                                                                                                                                                                        0.0s
 ⠿ Container hvtask-mqttbroker-1  Running                                                                                                                                                                        0.0s
 ⠿ Container hvtask-hvtask-1      Running  
```
To register a new MQTT broker, create a JSON file `broker.json` with the payload content in your home directory:
```json
{
  "hostname": "localhost",
  "port": 56321
}
```
With curl, make a PUT request:
```shell
 % curl -v -X PUT -H 'Content-Type: application/json'  --data-binary @$HOME/broker.json 'http://localhost:38080/mqtt/dockerBroker'
*   Trying 127.0.0.1:38080...
* Connected to localhost (127.0.0.1) port 38080 (#0)
> PUT /mqtt/dockerBroker HTTP/1.1
> Host: localhost:38080
> User-Agent: curl/7.79.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 46
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< date: Fri, 17 Feb 2023 10:05:03 GMT
< Content-Type: application/json
< content-length: 57
< connection: keep-alive
< 
* Connection #0 to host localhost left intact
{"name":"dockerBroker","hostname":"localhost","port":56321}
```
Read the JSON value back by a GET request:
```shell
% curl -v 'http://localhost:38080/mqtt/dockerBroker' 
*   Trying 127.0.0.1:38080...
* Connected to localhost (127.0.0.1) port 38080 (#0)
> GET /mqtt/dockerBroker HTTP/1.1
> Host: localhost:38080
> User-Agent: curl/7.79.1
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< date: Fri, 17 Feb 2023 10:08:18 GMT
< Content-Type: application/json
< content-length: 58
< connection: keep-alive
< 
* Connection #0 to host localhost left intact
{"name":"dockerBroker","hostname":"localhost","port":56321}
```
And to remove the broker `dockerBroker`, send a DELETE request:
```shell
 % curl -v -X DELETE  'http://localhost:38080/mqtt/dockerBroker'
*   Trying 127.0.0.1:38080...
* Connected to localhost (127.0.0.1) port 38080 (#0)
> DELETE /mqtt/dockerBroker HTTP/1.1
> Host: localhost:38080
> User-Agent: curl/7.79.1
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 204 No Content
< date: Fri, 17 Feb 2023 10:09:17 GMT
< connection: keep-alive
< 
* Connection #0 to host localhost left intact
```

### Sending MQTT messages
To send a message use the endpoint like `/mqtt/dockerBroker/send/curl-topic` to identity the broker and the topic:
```shell
% curl -v -X POST -H 'Content-Type: application/json'  --data-binary "Test message"  'http://localhost:38080/mqtt/dockerBroker/send/curl-topic'
Note: Unnecessary use of -X or --request, POST is already inferred.
*   Trying 127.0.0.1:38080...
* Connected to localhost (127.0.0.1) port 38080 (#0)
> POST /mqtt/dockerBroker/send/curl-topic HTTP/1.1
> Host: localhost:38080
> User-Agent: curl/7.79.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 12
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< date: Fri, 17 Feb 2023 10:18:51 GMT
< connection: keep-alive
< transfer-encoding: chunked
< 
* Connection #0 to host localhost left intact
```

To receive messages by subscription to a topic, use CURL with long polling option, the 
result will be a JSON list of strings, where the elements will arrive as soon as
the backend broker broadcast messages.

```shell
% curl -v -N -X GET 'http://localhost:38080/mqtt/dockerBroker/receive/curl-topic'
Note: Unnecessary use of -X or --request, GET is already inferred.
*   Trying 127.0.0.1:38080...
* Connected to localhost (127.0.0.1) port 38080 (#0)
> GET /mqtt/dockerBroker/receive/curl-topic HTTP/1.1
> Host: localhost:38080
> User-Agent: curl/7.79.1
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Transfer-Encoding: chunked
< Content-Type: application/json
< date: Fri, 17 Feb 2023 10:23:39 GMT
< 
[Item 1,Item 2
```
You need to issue the values 'Item 1', 'Item 2' in another term/tab using the `/send` URI path.

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
