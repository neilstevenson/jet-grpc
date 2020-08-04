# jet-grpc
Jet GRPC tester

# Instructions

## gRPC Server
Start one gRPC server:
```
java -jar grpc-server/target/grpc-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

## Non-JET examples

### `java -jar grpc-client-simple/target/grpc-client-simple-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
Calls `myCall` for larger and larger batches of input. Takes optional true/false argument for retry.

