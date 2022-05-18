# Visible Thread Reactive Api Demo


## Basic Setup

### Nosql DB

Download and install mongo db or update the application properties to connect to a running instance on the network.

### Unit Tests

To run unit tests.

``` mvn clean test```

### Api Server

To start api server on port 8080.

```mvn clean spring-boot:run   ```

### Example api usage (using curl)

From the command line run.

## Users API.

### Create an Organisation

```
curl -v -d '{"name":"Visible Thread", "description":"SAAS Provider"}' -H "Content-Type: application/json" -X POST http://localhost:8080/api/v1/organisations
export ORGANISATION_ID=6284993b76172b0235aac138
```

### Create a team

```
curl -v -d '{"name":"Sales Team", "description":"Super Sales Team"}' -H "Content-Type: application/json" -X POST http://localhost:8080/api/v1/${ORGANISATION_ID}/teams
export TEAM_ID=62852b807879ba7df8ee57f9
```

###  Get all teams by organisation id
```
curl -v  http://localhost:8080/api/v1/${ORGANISATION_ID}/teams
```

###  Get team by id 
```
curl -v  http://localhost:8080/api/v1/${ORGANISATION_ID}/teams/${TEAM_ID}
```

### Create a user assigned to specified team in the specified organisation.

```
 curl -v -d '{"email":"alice@test.com", "firstName":"Alice", "lastName": "Pope" }' -H "Content-Type: application/json" -X POST http://localhost:8080/api/v1/${ORGANISATION_ID}/teams/${TEAM_ID}/users
 export USER_ID=62852ba07879ba7df8ee57fa
 
 curl -v -d '{"email":"bob@test.com", "firstName":"Bob", "lastName": "Pope" }' -H "Content-Type: application/json" -X POST http://localhost:8080/api/v1/${ORGANISATION_ID}/teams/${TEAM_ID}/users
 export USER_ID=6283a353ea82dc21a411162e
```

### Add user to a team

```
curl -v  -H "Content-Type: application/json" -X POST http://localhost:8080/api/v1/${ORGANISATION_ID}/teams/${TEAM_ID}/users/add/${USER_ID}

```

### Remove a user from a team

```
curl -v  -H "Content-Type: application/json" -X DELETE http://localhost:8080/api/v1/${ORGANISATION_ID}/teams/${TEAM_ID}/users/remove/${USER_ID}

```

### Get all users for an organisation

```curl -v -X GET http://localhost:8080/api/v1/${ORGANISATION_ID}/users```


### Get user by id

```
curl -v -X GET http://localhost:8080/api/v1/${ORGANISATION_ID}/teams/${TEAM_ID}/users/${USER_ID}
```

### Get user by email

```
export USER_EMAIL=alice%40test.com
curl -v -X GET http://localhost:8080/api/v1/${ORGANISATION_ID}/teams/${TEAM_ID}/users/email/${USER_EMAIL}
```

### Get users created in between the specified date range.

```
 curl -v -d '{"startDate":"2022-02-01", "endDate":"2022-06-01" }' -H "Content-Type: application/json" -X POST http://localhost:8080/api/v1/${ORGANISATION_ID}/users/query_by_date_range
 
 
```

### Update a user

```curl -v -d '{"email":"alice@test.com", "firstName":"Alice", "lastName": "Hope" }' -H "Content-Type: application/json" -X POST http://localhost:8080/api/v1/${ORGANISATION_ID}/users/${USER_ID}```

### Delete a user

```
curl -v -X DELETE  http://localhost:8080/api/v1/${ORGANISATION_ID}/teams/${TEAM_ID}/users/${USER_ID}
```

### Upload a document 

```
curl -vvv -F "projectIdForm=5d089cfacec41e4acadda4f5" -F "profile=profile1" -F "doc=@/home/jimmy/Documents/visible_thread/visible-thread-demo/src/main/resources/alice_in_wonderland.txt" -X POST http://localhost:8080/api/v1/vtdocs/${ORGANISATION_ID}/teams/${TEAM_ID}/users/${USER_ID}
export DOCUMENT_ID=
```

```
curl -vvv -F "projectIdForm=5d089cfacec41e4acadda4f5" -F "category=site image" -F "fileParts=@/Users/jimmy/Documents/visible_thread/demo/src/main/resources/alice_in_wonderland.txt" -F "fileParts=@/Users/jimmy/Documents/visible_thread/demo/src/main/resources/alice_in_wonderland.txt"  http://localhost:8080/api/v1/vtdocs/team123/user123
```

### Get a document by Id
```
curl -vvv http://localhost:8080/api/v1/team123/user123/vtdocs/${DOCUMENT_ID}
```

### Example db commands

From the mongo shell run. 

```show dbs```

``` use visible_thread_docs ```

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
* [File Reading in Reactor](https://simonbasle.github.io/2017/10/file-reading-in-reactor/)

