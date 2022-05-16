# Visible Thread Reactive Api Demo


## Basic Setup

### DB

Download and install mongo db or update the application properties to connect to a running instance on the network.

### Unit Tests

To run unit tests.

``` mvn clean test```


### Api Server

To start api server on port 8080.

```mvn clean spring-boot:run   ```

### Example Basic curl api tests

From the command line run.

```
curl -vvv http://localhost:8080/api/v1/vtdocs/team123/user123/627e74fa09aad703ef2a3c73
```

```
curl -vvv -F "projectIdForm=5d089cfacec41e4acadda4f5" -F "profile=profile1" -F "doc=@/Users/jimmy/Documents/visible_thread/demo/src/main/resources/alice_in_wonderland.txt" -X POST http://localhost:8080/api/v1/vtdocs/team123/user123
```

```
curl -vvv -F "projectIdForm=5d089cfacec41e4acadda4f5" -F "category=site image" -F "fileParts=@/Users/jimmy/Documents/visible_thread/demo/src/main/resources/alice_in_wonderland.txt" -F "fileParts=@/Users/jimmy/Documents/visible_thread/demo/src/main/resources/alice_in_wonderland.txt"  http://localhost:8080/api/v1/vtdocs/team123/user123
```

```
curl -vvv http://localhost:8080/api/v1/team123/user123/vtdocs/627e4fd9397bd54a1fa10234
```

### Example Basic db commands

From the mongo shell run. 

```show dbs```


``` use blog```

```show collections```

```db[‘fs.files'].find();```

```db.fs.files.find({"metadata.teamId": "team123","metadata.userId" : "user123"});```

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.6.7/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.6.7/maven-plugin/reference/html/#build-image)
* [Spring Reactive Web](https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#web.reactive)
* [Spring Data Reactive MongoDB](https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#boot-features-mongodb)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a Reactive RESTful Web Service](https://spring.io/guides/gs/reactive-rest-service/)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)
* [File Reading in Reactor](https://simonbasle.github.io/2017/10/file-reading-in-reactor/)

