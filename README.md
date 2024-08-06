# abnamro-recipes
ABN AMRO Recipes Application

### Prerequisites
1. Java Development Kit (JDK): Make sure you have JDK 17 installed
2. Docker
3. Docker-compose

### Installation

#### Building the project
```sh
mvn clean install
```

#### Starting the application
```sh
./mvnw spring-boot:run
```
## Once you have the Prerequisites dependencies installed, follow these steps:

1. Clone the repository from GitHub:
    ```
    git clone https://github.com/my-username/recipe-manager.git
    ```
2. From the root directory of the project, run the following command to build and run the application:
    ```shell
    mvn clean package
   
    mvn flyway:migrate

    ```
3. Once the build has completed, you can start the docker environment with the following command:
    ```shell
    docker-compose up --build
    ```
4. Once the application has started, you can access the REST API documentation at the following URL:
    ```
    http://localhost:8080/swagger-ui.html
    ```
5. You can use the API documentation to try out the different endpoints and see how the application works.


### Integration Tests
Run the following command:
```shell
mvn clean test -P integration-tests

```


In a real production scenario the credential should not be exposed like it is now

Create docker image with pgAdmin (Postgres UI):
docker run --name pgadmin -p 80:80 -e PGADMIN_DEFAULT_EMAIL=admin@admin.com -e PGADMIN_DEFAULT_PASSWORD=admin -d dpage/pgadmin4


To do:
-- Add a config server
- Send a list of Ingredient ids instead of the whole ingredients
- Removes os auto wired
- Add indexes to the database
- Add a messages class
- Add unique contraint to Ingredients name
- Recipes controller not returning uuid

