package neil.demo;

import java.util.concurrent.atomic.AtomicLong;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import neil.demo.OutputMessage.Builder;

/**
 * <p>No streaming example would be complete with Word Count!
 * </p>
 */
public class WordCountImpl extends WordCountGrpc.WordCountImplBase {
    private static final AtomicLong calls = new AtomicLong(0);
    private static final String NEWLINE = System.getProperty("line.separator");

    /**
     * <p>A simple call, unary style. Takes input with multiple lines and gives output
     * with the same number of lines when successful.
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
        System.out.println("myCall server seqno==" + call + " Request "
                + ", batchSize==" + request.getInputValueCount()
                + ", " + request);

        // Deliberate fail every n calls
        if (call % MyConstants.FAIL_EVERY == 0) {
            System.out.println("myCall server seqno==" + call + " Response " + "**FAIL**");

            responseObserver
            .onError(Status.RESOURCE_EXHAUSTED
                           .withDescription(makeExceptionMessage(call, request))
                           .asRuntimeException());
        } else {
            OutputMessage outputMessage = makeOutputMessage(request);
            
            System.out.println("myCall server seqno==" + call + " Response " + outputMessage);
                        
            responseObserver.onNext(outputMessage);
            responseObserver.onCompleted();
        }
    }

    /**
     * <p>A bidirectional streaming call.
     * </p>
     * <p>Every <i>n</i> batches of input will trigger {@code RESOURCE_EXHUASTED}
     * response. This can be retried, if the client application is able to.
     * </p>
     */
    @Override
    public io.grpc.stub.StreamObserver<neil.demo.InputMessage> myStreamingCall(
            io.grpc.stub.StreamObserver<neil.demo.OutputMessage> responseObserver) {
        return new StreamObserver<neil.demo.InputMessage>() {

            @Override
            public void onNext(InputMessage request) {
                long call = calls.incrementAndGet();

                System.out.println("---");
                System.out.println();
                System.out.println("myStreamingCall server seqno==" + call + " Request "
                        + ", batchSize==" + request.getInputValueCount()
                        + ", " + request);

                // Deliberate fail every n calls
                if (call % MyConstants.FAIL_EVERY == 0) {
                    System.out.println("myStreamingCall server seqno==" + call + " Response " + "**FAIL**");

                    responseObserver
                    .onError(Status.RESOURCE_EXHAUSTED
                                   .withDescription(makeExceptionMessage(call, request))
                                   .asRuntimeException());
                } else {
                    OutputMessage outputMessage = makeOutputMessage(request);

                    System.out.println("myStreamingCall server seqno==" + call + " Response " + outputMessage);

                    responseObserver.onNext(outputMessage);
                }
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                try {
                    responseObserver.onCompleted();
                } catch (IllegalStateException ignored) {
                    // If closed by responseObserver.onError
                }
            }
          };
    }

    /**
     * <p>Flatten multi-line input into an exception message.
     * </p>
     *
     * @param call Counter
     * @param request At least one line of input
     * @return Single line
     */
    private String makeExceptionMessage(long call, InputMessage request) {
        String inputOneLine = request.toString().replaceAll(NEWLINE, ",");

        return "server seqno " + call + " [" + inputOneLine.substring(0, inputOneLine.length() - 1) + "]";
    }

    /**
     * <p>The actual word count!</p>
     * <p>For each line of input, count how many words are in it, return one
     * line of output with that count.
     * </p>
     *
     * @param inputMessage At least one line of text
     * @return Same number of lines as input, each line holds a number as a string
     */
    private static OutputMessage makeOutputMessage(InputMessage inputMessage) {
        Builder outputMessageBuilder = OutputMessage.newBuilder();

        for (int i = 0; i < inputMessage.getInputValueCount(); i++) {
            int words = inputMessage.getInputValue(i).split(" ").length;
            outputMessageBuilder.addOutputValue(String.valueOf(words));
        }

        return outputMessageBuilder.build();
    }

}
