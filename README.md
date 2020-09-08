# jURAclient 
[![Build Status](https://travis-ci.com/stklcode/juraclient.svg?branch=master)](https://travis-ci.com/stklcode/juraclient)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=de.stklcode.pubtrans%3Ajuraclient&metric=alert_status)](https://sonarcloud.io/dashboard?id=de.stklcode.pubtrans%3Ajuraclient) 
[![Javadocs](https://www.javadoc.io/badge/de.stklcode.pubtrans/juraclient.svg)](https://www.javadoc.io/doc/de.stklcode.pubtrans/juraclient)
[![Maven Central](https://img.shields.io/maven-central/v/de.stklcode.pubtrans/juraclient.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.stklcode.pubtrans%22%20AND%20a%3A%22juraclient%22)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/stklcode/juraclient/blob/master/LICENSE.txt)

Java client for URA based public transport APIs.

This client allows to simply connect any Java application to the public transport API to implement a monitor for the 
local bus station or any other custom queries. API versions 1.x and 2.x are supported.

## Usage Examples

### Initialization
```java
// Instantiate the client (e.g. using the TFL API)
UraClient ura = new UraClient("http://countdown.api.tfl.gov.uk");

// Initialize the API with non-standard endpoints (e.g. ASEAG with API V2)
UraClient ura = new UraClient("http://ivu.aseag.de", 
                              "interfaces/ura/instant_V2", 
                              "interfaces/ura/stream_V2");
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
List<Trip> trips = ura.forStop("100000")
                      .forLines("25", "35")
                      .forDirection(1)
                      .getTrips(10);

// Get trips from given stop towards your destination
List<Trip> trips = ura.forStopByName("Piccadilly Circus")
                      .towards("Marble Arch")
                      .getTrips();
```

### Get Messages

```java
// Get next 10 trips for given stops and lines in a single direction (all filters optional)
List<Message> msgs = ura.forStop("100000")
                        .getMessages();
```

## Maven Artifact
```xml
<dependency>
    <groupId>de.stklcode.pubtrans</groupId>
    <artifactId>juraclient</artifactId>
    <version>1.3.0</version>
</dependency>
```

## License

The project is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
