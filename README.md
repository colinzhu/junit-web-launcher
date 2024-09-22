# junit-web-launcher
To launch JUnit tests in a browser

## Feature
- [x] Launch JUnit tests in a browser
- [x] Report in a table
- [x] View the running log in real-time
- [x] Logging message with testClass, testMethod and testDisplayName
- [x] Each run has its own log file
- [x] Support parallel execution

## Usage
### Run as standalone jar
- Build the source with ```mvn package -Dmaven.test.skip```
- Copy the jar to the same folder of your test cases
- Run below command (replace 8080 with your port, replace example.package with your junit package)
```shell
java -cp * junitweblauncher.App 8080 example.package 2
```
### Run as a dependency of your project
- Add the junit-web-launcher to your pom.xml
- Invoke ```junitweblauncher.App.main(new String[]{"8080", "example.package", "2"});```

## Try
There is an example in the test folder, try ```Example.main()```