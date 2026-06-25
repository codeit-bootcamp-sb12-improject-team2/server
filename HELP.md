# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.15/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.15/gradle-plugin/packaging-oci-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.15/reference/web/servlet.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.15/reference/using/devtools.html)
* [Docker Compose Support](https://docs.spring.io/spring-boot/3.5.15/reference/features/dev-services.html#features.dev-services.docker-compose)
* [Spring Session for Spring Data MongoDB](https://docs.spring.io/spring-session/reference/)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

### Docker Compose support
This project contains a Docker Compose file named `compose.yaml`.
In this file, the following services have been defined:

* mongodb: [`mongo:latest`](https://hub.docker.com/_/mongo)
* postgres: [`postgres:latest`](https://hub.docker.com/_/postgres)

Please review the tags of the used images and set them to the same as you're running in production.

