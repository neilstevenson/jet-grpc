package neil.demo;

import java.util.concurrent.atomic.AtomicLong;

import io.grpc.Status;

import neil.demo.OutputMessage.Builder;

/**
 * <p>No streaming example would be complete with Word Count!
 * </p>
 */
public class WordCountImpl extends WordCountGrpc.WordCountImplBase {
    private static final AtomicLong calls = new AtomicLong(0);

    /**
     * <p>A simple call, takes some lines of text, and counts the number
     * of words on each line. We don't distinguish between words, only
     * counting them, assuming spaces separate them.
     * </p>
     * <p>Every <i>n</i> batches of input will trigger {@code RESOURCE_EXHUASTED}
     * response. This can be retried, if the client application is able to.
     * </p>
     */
    @Override
    public void myCall(neil.demo.InputMessage request,
            io.grpc.stub.StreamObserver<neil.demo.OutputMessage> responseObserver) {
        long call = calls.incrementAndGet();
        
        System.out.println("---");
        System.out.println();
        System.out.println("myCall call==" + call + " Request "
                + ", batchSize==" + request.getInputValueCount()
                + ", " + request);
        
        // Deliberate fail every n calls
        if (call % MyConstants.FAIL_EVERY == 0) {
            System.out.println("myCall call==" + call + " Response " + "**FAIL**");
            
            responseObserver
            .onError(Status.RESOURCE_EXHAUSTED
                           .withDescription("Call " + call)
                           .asRuntimeException());
        } else {
            Builder outputMessageBuilder = OutputMessage.newBuilder();
            
            for (int i = 0; i < request.getInputValueCount(); i++) {
                int words = request.getInputValue(i).split(" ").length;
                outputMessageBuilder.addOutputValue(String.valueOf(words));
            }
            OutputMessage outputMessage = outputMessageBuilder.build();
            
            System.out.println("myCall call==" + call + " Response " + outputMessage);
                        
            responseObserver.onNext(outputMessage);
            responseObserver.onCompleted();
        }
    }

}
