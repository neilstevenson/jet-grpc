# jet-grpc
Jet GRPC tester

Experiments with handling errors such as "`RESOURCE_EXHAUSTED`" from gRPC server.

# Instructions

# gRPC Server
Start one gRPC server:
```
java -jar grpc-server/target/grpc-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

# gRPC Clients

All clients take an option command line argument "`true`" for whether or not the channel created by
[getManagedChannelBuilder](https://github.com/neilstevenson/jet-grpc/blob/master/common/src/main/java/neil/demo/MyUtils.java#L23)
has retry functionality enabled.

## Non-Jet examples

### `java -jar grpc-client-unary/target/grpc-client-unary-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
Unary call. Calls `myCall` for larger and larger batches of input.

### `java -jar grpc-client-bidirectional/target/grpc-client-bidirectional-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
Bidirectional call. Calls `myStreamingCall` for larger and larger batches of input.

## Jet examples

### `java -jar jet-server-grpc-client-unary/target/jet-server-grpc-client-unary-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
Unary call. Calls `myCall` for input of a fixed batch size.

### `java -jar jet-server-grpc-client-bidirectional/target/jet-server-grpc-client-bidirectional-0.0.1-SNAPSHOT-jar-with-dependencies.jar`
Bidirectional call. Calls `myStreamingCall` for input of a fixed batch size.

