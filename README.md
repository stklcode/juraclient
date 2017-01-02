jURAclient [![Build status](https://travis-ci.org/stklcode/juraclient.svg?branch=master)](https://travis-ci.org/stklcode/juraclient)
==========


Java client for URA based public transport APIs.

This client allows to simply connect any Java application to the public transport API to implement a monitor for the 
local bus station or any other custom queries.

**Usage Example**

```java
// Instantiate the client (e.g. using the ASEAG API)
UraClient ura = new UraClient("http://ivu.aseag.de");

// Initiailize with non-standard endpoints
UraClient ura = new UraClient("http://ivu.aseag.de", 
                              "interfaces/ura/instant_V2", 
                              "interfaces/ura/stream_V2");

// List available stops
List<Stop> stops = ura.listStops();

// Get next 10 trips for given stops and lines (all filters optional)
List<Trip> trips = ura.forStop("100000")
                      .forLines("25", "35")
                      .getTrips(10);
```

**Maven Artifact**
```
<dependency>
    <groupId>de.stklcode.pubtrans</groupId>
    <artifactId>juraclient</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Planned features:**

* More refined query parameters
* Stream API with asynchronous consumer

**License**

The project is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).