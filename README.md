# money-transfer

Required
- Java 8
- Maven

To build
`
  $> mvn clean install
`

To run application
`
  java -jar ./target/money-transfer-1.0-SNAPSHOT.jar
`

To run jcstress tests after build. For filtering use "t" param and regexp: (ex. -t ".*CAS.*")
`
  java -jar ./target/jcstress.jar
`

To run benchmark
`
  java -jar ./target/jmh-money-transfer.jar
`

**Swagger**
Swagger is accessible on
`
  http://localhost:8080
`

You can configure server port via *application.properties* file.
