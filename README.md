# jURAclient [![Build status](https://travis-ci.org/stklcode/juraclient.svg?branch=master)](https://travis-ci.org/stklcode/juraclient)

Java client for URA based public transport APIs.

This client allows to simply connect any Java application to the public transport API to implement a monitor for the 
local bus station or any other custom queries. API versions 1.x and 2.x are supported.

## Usage Examples

### Initialization
```java
// Instantiate the client (e.g. using the TFL API)
UraClient ura = new UraClient("http://countdown.api.tfl.gov.uk");

// Initiailize the API with non-standard endpoints (e.g. ASEAG with API V2)
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

## Maven Artifact
```
<dependency>
    <groupId>de.stklcode.pubtrans</groupId>
    <artifactId>juraclient</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Planned Features

* More refined query parameters
* Stream API with asynchronous consumer

## License

The project is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).