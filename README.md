# abnamro-recipes
ABN AMRO Recipes Application

### Prerequisites
1. Java Development Kit (JDK): Make sure you have JDK 17 installed
2. Docker

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
    ```
3. Once the build has completed, you can start the docker environment with the following command:
    ```shell
    docker-compose up
    ```
4. Once the application has started, you can access the REST API documentation at the following URL:
    ```
    http://localhost:8080/swagger-ui.html
    ```
5. You can use the API documentation to try out the different endpoints and see how the application works.


