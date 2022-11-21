# Changelog
All notable changes to this project will be documented in this file.

## unreleased

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
