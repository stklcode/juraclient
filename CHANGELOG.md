# Changelog
All notable changes to this project will be documented in this file.

## unreleased

### Fixed
* Remove `Automatic-Module-Name` from JAR manifest

### Dependencies
* Updated Jackson dependency to 2.17.2


## 2.0.7 - 2024-06-29

### Fixed
* renamed `UraClientConfiguration#getStreeamPath()` to `getStreamPath()`

### Dependencies
* Updated Jackson dependency to 2.17.1

### Improvement
* Generate and attach CycloneDX SBOM


## 2.0.6 - 2024-03-23
### Dependencies
* Updated Jackson dependency to 2.17.0

### Misc
* Tested with JDK 21


## 2.0.5 - 2023-10-03
### Dependencies
* Updated Jackson dependency to 2.15.2

### Misc
* Tested with JDK 20


## 2.0.4 - 2022-11-21
### Security
* Updated Jackson dependency to 2.14.0

### Fixed
* Querying trips and messages with limit directly from `Query` instance (#18)

### Misc
* Tested with JDK 19


## 2.0.3 - 2022-08-30
### Security
* Updated dependencies


## 2.0.2 - 2022-04-13
### Security
* Updated dependencies


## 2.0.1 - 2021-10-02
### Security
* Updated dependencies

### Improvement
* Built and tested with JDK 17

## 2.0.0 - 2021-01-30
### Breaking
* Java 11 or later required

### Changes
* Using native Java 11 HTTP client
* Client configuration with separate `UraClientConfiguration` class and builder
* Client throws custom checked exception `UraClientException` instead of runtime exceptions on errors (#10)

### Features
* Configuration builder for client initialization (#9)
* Configurable connect and read timeouts (#14)

### Fixed
* Allow reopening an `AsyncUraTripReader` without raising an exception (#12)

----

## 1.3.3 - 2022-11-21
### Security
* Updated Jackson dependency to 2.14.0

### Fixed
* Querying trips and messages with limit directly from `Query` instance (#19)


## 1.3.2 - 2022-08-30

### Improvements
* Dependency updates


## 1.3.1 - 2020-12-12
### Fixed
* Allow reopening an `AsyncUraTripReader` without raising an exception (#13)

### Improvements
* Dependency updates


## 1.3.0 - 2019-12-04
### Security
* Updated dependencies

### Features
* Added support for reading messages, using `getMessages()` method (#5)


## 1.2.0 - 2019-06-20
### Security
* Updated dependencies

### Features
* Added support for stream API with asynchronous reader, using `getTripsStream()` method (#1)


## 1.1.4 - 2018-11-19
### Fixed
* Fixed issue with direction ID as `String` instead if `Integer` (#2)
* Fixed issue with vehicle ID being `null` (#3)
* Fixed issue with spaces in search parameters (#4)


## 1.1.3 - 2018-11-13
### Security
* Updates Jackson dependency 2.9.4 to 2.9.7 (CVE-2018-7489)

### Improvements
* Client and model classes implement `Serializable`
* Dependency updates


## 1.1.2 - 2018-03-24
### Improvements
* Added automatic module name for JPMS compatibility


## 1.1.1 - 2018-02-20
### Improvements
* On connection or parsing errors, the `IOException` is no longer ignored, but encapsulated in `RuntimeException` (no StackTraces printed)
* Code cleanup and minor improvements
* Minor dependency updates


## 1.1.0 - 2017-01-07
### Features
* Filter stops by coordinates and radius
* Filter trips by destination and and towards fields

### Misc
* Test coverage 100% (line); tested against ASEAG and TFL APIs


## 1.0.0 - 2017-01-02
* Initial release
