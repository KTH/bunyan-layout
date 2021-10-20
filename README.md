# bunyan-layout

Tools for logging in Bunyan JSON format (see See: https://github.com/trentm/node-bunyan)
in various logging frameworks for Java.

* [log4j2-bunyan](log4j2-bunyan/) - Log4j 2.x log layout.
* [java-logging-bunyan](java-logging-bunyan/) - Java Util Logging log formatter.
* [logback-bunyan](logback-bunyan/) - Logback classic log layout.

Please note that the msg field is currently being truncated with a max length of 20000 characters.

## Development

Please feel free to create pull requests.

### Java version

Releases up to and including release 2.6.0 was build using Java 8. Release 2.7.0 is built using Java 11. 
### Building

Complete build and testing is run with maven: `mvn clean install`

Pre-built binaries are published into 
[private artifact repo](https://dev.azure.com/kth-integration/team%20integration/_packaging?_a=feed&feed=integration). 
Publishing is done with `mvn clean deploy` 
