# Mono Edge Repo

## Generate Profofiles
`protoc --java_out=src/main/java .\src\main\java\com\seabrief\sysmon\Models\Proto\MonitorData.proto`

## Build

`mvn assembly:assembly -DdescriptorId=jar-with-dependencies`