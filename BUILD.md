# 2026-02-13-F SJW

To build native image that will run on UNH AI compute cluster without java installed:

1. Install SDKMAN from sdkman.io
2. Install dependencies:
```
sdk install java 25.0.2-graal # or whatever latest version is listed on graalvm..org/downloads
sdk install maven
```
3. Build:
```
mvn -Pnative package
```
