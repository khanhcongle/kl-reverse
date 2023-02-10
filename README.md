# Kl-reverse

Please use this tool instead: https://www.npmjs.com/package/json-caching-proxy

## Run / debug configuration on Eclipse

Java Application:

Project: `kl-reverse`

Main class: `io.vertx.core.Launcher`

Arguments: `run kl.proxy.kl_reverse.MainVerticle -Dbind=8080:8082,8089:8080`

## Building

To launch your tests:

```shell
mvn clean test
```

To package your application:

```shell
mvn clean package
```

To run your application:

```shell
mvn clean compile exec:java -Dmaven.test.skip
```

To custom ports:

```shell
mvn clean compile exec:java -Dmaven.test.skip -Dbind=8080:8082,8089:8080
```