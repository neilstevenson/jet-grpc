package neil.demo;

import java.util.concurrent.ForkJoinPool;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Application {

    /**
     * <p>Create a gRPC server, single-threaded, that
     * has a service {@link WordCountImpl} implementing
     * {@code common/src/main/proto/Neil.proto}
     * </p>
     *
     * @param args Ignored
     * @throws Exception Probably not thrown
     */
    public static void main(String[] args) throws Exception {
        ForkJoinPool forkJoinPool = new ForkJoinPool(1);
        
        Server server = ServerBuilder
                .forPort(MyConstants.GRPC_PORT)
                .addService(new WordCountImpl())
                .executor(forkJoinPool)
                .build();
                
        System.out.println(server);
        server.start().awaitTermination();
        forkJoinPool.shutdownNow();
    }
}
