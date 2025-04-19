# jURAclient 
[![CI](https://github.com/stklcode/juraclient/actions/workflows/ci.yml/badge.svg)](https://github.com/stklcode/juraclient/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=de.stklcode.pubtrans%3Ajuraclient&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=de.stklcode.pubtrans%3Ajuraclient)
[![Javadocs](https://www.javadoc.io/badge/de.stklcode.pubtrans/juraclient.svg)](https://www.javadoc.io/doc/de.stklcode.pubtrans/juraclient)
[![Maven Central Version](https://img.shields.io/maven-central/v/de.stklcode.pubtrans/juraclient.svg)](https://central.sonatype.com/artifact/de.stklcode.pubtrans/juraclient)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/stklcode/juraclient/blob/master/LICENSE.txt)

Java client for URA based public transport APIs.

This client allows to simply connect any Java application to the public transport API to implement a monitor for the 
local bus station or any other custom queries. API versions 1.x and 2.x are supported.

## Supported versions
Version 2.x requires Java 11 or later.
It also contains some new features and allows configuration using a dedicated configuration object.

Version 1.x requires Java 8 or later.
This version is no longer supported and will not receive any future updates.

## Usage Examples

### Initialization
```java
// Instantiate the client.
UraClient ura = new UraClient("https://ura.example.com");

// Initialize the API with non-standard endpoints.
UraClient ura = new UraClient("https://ura.example.com", 
                              "interfaces/ura/instant_V2", 
                              "interfaces/ura/stream_V2");

// Initialization with configuration builder (Client v2.x)
UraClient ura = new UraClient(
    UraClientConfiguration.forBaseURL("https://ura.example.com")
                          .withInstantPath("interfaces/ura/instant_V2")
                          .withStreamPath("interfaces/ura/stream_V2")
                          .withConnectTimeout(Duration.ofSeconds(2))
                          .withTimeout(Duration.ofSeconds(10))
                          .build()
);
```

### List Stops

```java
// List ALL available stops
List<Stop> stops = ura.getStops();

// List available stops in a 200m radius around given coordinates
List<Stop> stops = ura.forPosition(51.51009, -0.1345734, 200)
                      .getStops();

```

### Get Trips

```java
// Get next 10 trips for given stops and lines in a single direction (all filters optional)
List<Trip> trips = ura.forStops("100000")
                      .forLines("25", "35")
                      .forDirection(1)
                      .getTrips(10);

// Get trips from given stop towards your destination
List<Trip> trips = ura.forStopsByName("Piccadilly Circus")
                      .towards("Marble Arch")
                      .getTrips();
```

### Get Messages

```java
// Get next 10 trips for given stops and lines in a single direction (all filters optional)
List<Message> msgs = ura.forStops("100000")
                        .getMessages();
```

## Maven Artifact
```xml
<dependency>
    <groupId>de.stklcode.pubtrans</groupId>
    <artifactId>juraclient</artifactId>
    <version>2.0.9</version>
</dependency>
```

## License

The project is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
