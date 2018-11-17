## 1.1.4 [unreleased]
* [fix] Fixed issue with direction ID as `String` instead if `Integer` (#2)
* [fix] Fixed issue with vehicle ID being `null` (#3)
* [fix] Fixed issue with spaces in search parametes (#4)

## 1.1.3 [2018-11-13]
* [security] Updates Jackson dependency 2.9.4 to 2.9.7 (CVE-2018-7489)
* [improvement] Client and model classes implement `Serializable`
* [dependeny] Dependency updates

## 1.1.2 [2018-03-24]
* [improvement] Added automatic module name for JPMS compatibility

## 1.1.1 [2018-02-20]
* [improvement] On connection or parsing errors, the `IOException` is no longer ignored, but encapsulated in `RuntimeException` (no StackTraces printed)
* [internal] Code cleanup and minor improvements
* [dependeny] Minor dependency updates

## 1.1.0 [2017-01-07]
* [feature] Filter stops by coordinates and radius
* [feature] Filter trips by destination and and towards fields
* [test] Test coverage 100% (line); tested against ASEAG and TFL APIs

## 1.0.0 [2017-01-02]
* Initial release
