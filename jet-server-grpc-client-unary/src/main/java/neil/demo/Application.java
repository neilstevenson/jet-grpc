package neil.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.grpc.GrpcService;
import com.hazelcast.jet.grpc.GrpcServices;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.ServiceFactory;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.test.TestSources;

public class Application {

    public static void main(String[] args) throws Exception {
        boolean retry = args.length == 1 ? retry = Boolean.parseBoolean(args[0]) : false;
        System.out.println("retry==" + retry);

        JetInstance jetInstance = Jet.newJetInstance(new JetConfig());
        
        Pipeline pipeline = Application.build(retry);
        
        try {
            jetInstance.newJob(pipeline).join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        jetInstance.shutdown();
    }

    /**
     * <p>Send batches of input to gRPC server. gRPC server will count
     * the words per line, and the Jet job will totalize.</p>
     *
     * @param retry Whether gRPC retry should be enabled (and may work)
     * @return
     * @throws Exception
     */
    private static Pipeline build(boolean retry) throws Exception {
        Pipeline pipeline = Pipeline.create();
        
        pipeline
        .readFrom(TestSources.items(MyConstants.HAMLET)).setLocalParallelism(1)
        .mapUsingServiceAsyncBatched(getUnaryService(retry),
                MyConstants.JET_BATCH_SIZE,
                (service, list) -> {
                    InputMessage inputMessage =
                            InputMessage.newBuilder().addAllInputValue(list).build();
 
                    return retry(() -> service.call(inputMessage), 1).thenApply(outputMessage -> {
                        if (outputMessage == null) {
                            // Failed N times, record a failed batch
                            System.out.println("Failed batch: " + list);

                            // Need to return a list with null values to filter out the failed items
                            ArrayList<String> failedBatch = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                failedBatch.add(null);
                            }
                            return failedBatch;
                        }
                        System.out.println("---");
                        System.out.println();
                        System.out.println(inputMessage);
                        System.out.println(outputMessage);
                        
                        List<String> batch = new ArrayList<>();
                        for (int i = 0 ; i < outputMessage.getOutputValueCount(); i++) {
                            String item = outputMessage.getOutputValue(i);
                            batch.add(item);
                        }
                        
                        return batch;
                       });
        }).setLocalParallelism(1)
        .aggregate(AggregateOperations.summingLong(Long::parseLong))
        .writeTo(Sinks.logger(o -> new String("Total: " + o)));
                
        return pipeline;
    }


    private static <T> CompletableFuture<T> retry(Supplier<CompletableFuture<T>> supplier, int retry) {
        if (retry < 0) {
            // Exceeded number of retries, return null future, instead of throwing exception
            CompletableFuture<T> f = new CompletableFuture<>();
            f.complete(null);
            return f;
        }
        System.out.println("Retry " + retry);
        return supplier.get()
                       .thenApply(CompletableFuture::completedFuture)
                       .exceptionally(t -> {
                           // Log t if needed

                           return retry(supplier, retry - 1);
                       })
                       .thenCompose(Function.identity());
    }
    
    /*FIXME Generics. Java accepts this, but at least Eclipse doesn't.
     */
    private static ServiceFactory<?, ? extends GrpcService<InputMessage, OutputMessage>>
        getUnaryService(boolean retry) {
        return GrpcServices.unaryService(
                        () -> MyUtils.getManagedChannelBuilder(MyConstants.MY_CALL, retry),
                        channel -> WordCountGrpc.newStub(channel)::myCall
                );
    }


}
