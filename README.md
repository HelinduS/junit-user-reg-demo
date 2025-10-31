# junit-user-reg-demo

This small project demonstrates unit testing in Java using JUnit 5 and Mockito.

What's in the repo

- src/main/java/com/example/user - service and DTOs for user registration
  - `UserService` - validation, hashing, repository save
  - `UserRepository` - interface (mocked in tests)
  - `RegisterRequest`, `User`, `RegistrationException`
- src/test/java/com/example/user - unit tests using JUnit 5 and Mockito
  - `UserServiceTest` - examples of normal, duplicate, parameterized validation tests

Why this project

It is intentionally small so you can focus on writing and running unit tests with JUnit 5 and Mockito. Tests are written to show:

- Arrange/Act/Assert style with Mockito mocks and `@InjectMocks`
- Parameterized tests (`@ParameterizedTest` + `@ValueSource`)
- `@DisplayName` for human-friendly test names
- ArgumentCaptor usage

Running the tests

From the project root run:

```bash
mvn clean test
```

The `pom.xml` configures the Surefire plugin to print test output to the console (so you can see results quickly during demos). Detailed per-test reports are also available in `target/surefire-reports/`.

Notes about display names

Maven's console output shows test identifiers by default; IDEs (IntelliJ/Eclipse) and Surefire reports will show the `@DisplayName` values. We configured the Surefire plugin to print plain summaries to the console for clarity during demos.

