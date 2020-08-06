package neil.demo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import neil.demo.InputMessage.Builder;

public class Application {

    /**
     * <p>Call the "{@code myStreamingCall}" gRPC service, for the first line of
     * input in a batch, then the next two lines, then the next three lines.
     * and so on until the test data is exhausted.
     * </p>
     *
     * @param args If "{@code true}" then retry on failure.
     */
    public static void main(String[] args) {
        boolean retry = args.length == 1 ? retry = Boolean.parseBoolean(args[0]) : false;
        System.out.println("retry==" + retry);
        
        ManagedChannel managedChannel
            = MyUtils.getManagedChannelBuilder(MyConstants.MY_STREAMING_CALL, retry).build();

        // Async
        WordCountGrpc.WordCountStub wordCountStub =
                WordCountGrpc.newStub(managedChannel);
        
        AtomicLong total = new AtomicLong(0);
        AtomicLong pendingResponses = new AtomicLong(0);
        AtomicBoolean exceptions = new AtomicBoolean(false);

        // Callback handler
        StreamObserver<neil.demo.InputMessage> requestObserver =
                wordCountStub.myStreamingCall(new StreamObserver<neil.demo.OutputMessage>() {
                    @Override
                    public void onNext(OutputMessage outputMessage) {
                        System.out.println("requestObserver.onNext(" + outputMessage + ")");
                        
                        for (int j = 0; j < outputMessage.getOutputValueCount(); j++) {
                            total.addAndGet(Integer.parseInt(outputMessage.getOutputValue(j)));
                        }
                        pendingResponses.decrementAndGet();
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println("requestObserver.onError(" + t.getMessage() + ")");
                        exceptions.set(true);
                    }

                    @Override
                    public void onCompleted() {
                    }
        });

        int batchSize = 1;
        /*FIXME
         * offSet=0 is the required value.
         *
         * If "offSet=33", sends two batches. On every second run it will get RESOURCE_EXHAUSTED
         * from the server, but does retry.
         * 
         * If "offSet=31", sends three batches. It will get RESOURCE_EXHAUSTED but retry doesn't
         * apear to work.
         */
        for (int offSet = 31; offSet < MyConstants.HAMLET.length ; offSet += batchSize++) {
            
            Builder inputMessageBuilder = InputMessage.newBuilder();
            for (int i = offSet ; (i < MyConstants.HAMLET.length && i < offSet + batchSize) ; i++) {
                inputMessageBuilder.addInputValue(MyConstants.HAMLET[i]);
            }

            System.out.println("---");
            System.out.println();

            InputMessage inputMessage = inputMessageBuilder.build();
            System.out.println(inputMessage);

            // Sending async
            pendingResponses.incrementAndGet();
            requestObserver.onNext(inputMessage);
        }

        // Wait for responses
        try {
            while (pendingResponses.get() > 0 && !exceptions.get()) {
                TimeUnit.MILLISECONDS.sleep(50);
            }
        } catch (InterruptedException e) {
            exceptions.set(true);
            e.printStackTrace();
        }

        requestObserver.onCompleted();
        managedChannel.shutdown();

        System.out.println("===");
        System.out.println();
        if (exceptions.get()) {
            System.out.println("Total: N/a due to call failures");
        } else {
            System.out.println("Total: " + total);
        }
    }
}
