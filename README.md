## GitHub Repository API

This Spring Boot application provides an efficient way to fetch all non-forked repositories for a given GitHub username, retrieving detailed information about each repository, including branch names and their last commit SHA.
Technologies used: Java 21, Spring Boot 3, Maven, Mockito, WireMock.


## Features
- Reactive Repository Fetching: Leverages Spring WebFlux for asynchronous communication with the GitHub
- JSON Format Enforcement: Ensures API responses in JSON format, handling incorrect Accept headers
- Logging: Employs Log4j2 for recording significant events and errors
- Custom Exception Handling: Defines and manages custom exceptions
- Reactive Testing Support: Enables effective testing of reactive streams using tools like StepVerifier


## Run
Clone the project:
```bash
git clone https://github.com/rafal-paton/atipera-github.git
```

Navigate to the project directory and run the Application:
```bash
cd /atipera-github
./mvnw spring-boot:run
```


## Usage

#### Base URL:

```bash
http://localhost:8080
```

#### Endpoint that lists GitHub repositories for desired user:

```bash
GET /api/github/{username}
Accept: application/json
```
Where `{username}` is username of GitHub user, which repositories you want to retrieve.

You can explore the API using the Swagger UI at `http://localhost:8080/swagger-ui/index.html#/` or execute direct request e.g. `http://localhost:8080/api/github/{username}` where `{username}` is the GitHub username.

#### Example response:
```bash
GET /api/github/rafal-paton
Accept: application/json
```
```json
[
    {
        "repositoryName": "songify",
        "ownerLogin": "rafal-paton",
        "branches": [
            {
                "name": "main",
                "sha": "bb74ac9e794bddf86679714ba80c4d3c2dcd2881"
            }
        ]
    },
    {
        "repositoryName": "todos",
        "ownerLogin": "rafal-paton",
        "branches": [
            {
                "name": "main",
                "sha": "98b4558cfdd5b90e10657e185ec5b0696bc87a2f"
            }
        ]
    }
]
```

#### Errors:
When an error occurs, the application will return a response with appropriate HTTP status code and body with the following structure:
```json
{
"status": "${httpStatusCode}",
"message": "${errorMessage}"
}
```