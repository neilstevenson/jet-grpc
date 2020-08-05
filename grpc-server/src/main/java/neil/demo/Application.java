package neil.demo;

import java.util.concurrent.ForkJoinPool;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Application {

    /**
     * <p>We are trying to solve the problem when
     * the gRPC server is overloaded by calls,
     * which occurs when multi-threading isn't viable.
     * </p>
     */
    private static final int MUST_BE_ONE = 1;


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
        ForkJoinPool forkJoinPool = new ForkJoinPool(MUST_BE_ONE);
        
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
