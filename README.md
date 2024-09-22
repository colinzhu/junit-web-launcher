# junit-web-launcher
Welcome to the JUnit Web Launcher! This project provides a simple way to run JUnit tests from a web browser.

![image](https://github.com/colinzhu/resources/blob/master/junit-web-launcher/screenshot-1.png?raw=true)

## Feature
- [x] Run JUnit tests directly from a web browser
- [x] View the running log in real-time
- [x] Lightweight and easy to set up
- [x] Support parallel execution
- [x] Easy integration with existing Java projects

## Usage
### Prerequisites
- Java 21 or higher
- Maven

### Installation
- Clone the repository
- Build the source with ```mvn package -Dmaven.test.skip```

## Try with the embedded example
- Load the source into your IDE, then run ```Example.main()``` in the test folder
- Open http://localhost:8080 in your browser

### Run as standalone jar
- Copy the jar to the same folder of your test cases
- Run below command (replace 8080 with your port, replace example.package with your junit package, replace 2 with the number of parallel size)
```shell
java -cp * junitweblauncher.App 8080 example.package 2
```
- Open http://localhost:8080 in your browser


### Run as a dependency of your project
- Add the junit-web-launcher to your pom.xml
- Invoke ```junitweblauncher.App.main(new String[]{"8080", "example.package", "2"});```
- Open http://localhost:8080 in your browser
