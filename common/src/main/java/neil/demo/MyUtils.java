package neil.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.grpc.ManagedChannelBuilder;

public class MyUtils {

    /**
     * <p>Returns a builder to create a channel to the gRPC server.
     * </p>
     * <p>If arg 2 is {@code true} we use arg 1, and specify the given
     * gRPC method call can be retried some number of times for specific
     * error responses.
     * </p>
     * 
     * @param method The method name in gRPC proto definition
     * @param retry Boolean, whether to retry the method
     * @return
     */
    public static ManagedChannelBuilder<?> getManagedChannelBuilder(String method, boolean retry) {
         ManagedChannelBuilder<?> managedChannelBuilder =
                ManagedChannelBuilder
                .forAddress(MyConstants.GRPC_HOST, MyConstants.GRPC_PORT)
                .usePlaintext();

         if (retry) {
             Map<String, Object> retryPolicy = new HashMap<>();
             retryPolicy.put("maxAttempts", Double.valueOf(5));
             retryPolicy.put("initialBackoff", "0.2s");
             retryPolicy.put("maxBackoff", "10s");
             retryPolicy.put("backoffMultiplier", Double.valueOf(2));
             retryPolicy.put("retryableStatusCodes", List.of("RESOURCE_EXHAUSTED"));

             Map<String, Object> methodConfig = new HashMap<>();
             Map<String, Object> name = new HashMap<>();
             name.put("service", "neil.demo.WordCount");
             name.put("method", method);

             methodConfig.put("name", List.of(name));
             methodConfig.put("retryPolicy", retryPolicy);

             Map<String, Object> serviceConfig = new HashMap<>();
             serviceConfig.put("methodConfig", List.of(methodConfig));

             managedChannelBuilder.defaultServiceConfig(serviceConfig);

             managedChannelBuilder.enableRetry();
         }

        return managedChannelBuilder;
    }
    
}
