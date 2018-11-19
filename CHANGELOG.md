# Changelog
All notable changes to this project will be documented in this file.

## 1.1.4 - 2018-11-19
### Fixed
* Fixed issue with direction ID as `String` instead if `Integer` (#2)
* Fixed issue with vehicle ID being `null` (#3)
* Fixed issue with spaces in search parametes (#4)


## 1.1.3 - 2018-11-13
### Security
* Updates Jackson dependency 2.9.4 to 2.9.7 (CVE-2018-7489)

### Changed
* Client and model classes implement `Serializable`
* Dependency updates


## 1.1.2 - 2018-03-24
### Changed
* Added automatic module name for JPMS compatibility


## 1.1.1 - 2018-02-20
### Changed
* On connection or parsing errors, the `IOException` is no longer ignored, but encapsulated in `RuntimeException` (no StackTraces printed)
* Code cleanup and minor improvements
* Minor dependency updates


## 1.1.0 - 2017-01-07
### Added
* Filter stops by coordinates and radius
* Filter trips by destination and and towards fields

### Misc
* Test coverage 100% (line); tested against ASEAG and TFL APIs


## 1.0.0 - 2017-01-02
* Initial release
