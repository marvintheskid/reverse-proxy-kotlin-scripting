# reverse-proxy-kotlin-scripting
Kotlin scripting for https://github.com/marvintheskid/mc-reverse-proxy
## Usage
If you want to use this, you need to build it first using ```gradle build```.

Building requires you to include the proxy as a flat dependency (for now; folder is named ```deps```) named ```standalone-all.jar``` (you can get it by building the proxy itself).

The whole jar is sized around 70 megabytes, located in ```build/libs``` under the name ```kotlin-scripting-1.0-SNAPSHOT-all.jar```.
