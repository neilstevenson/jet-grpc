package neil.demo;

import io.grpc.ManagedChannel;
import neil.demo.InputMessage.Builder;
import neil.demo.WordCountGrpc.WordCountBlockingStub;

public class Application {

    /**
     * <p>Call the "{@code myCall}" gRPC service, for the first line of
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
            = MyUtils.getManagedChannelBuilder(MyConstants.MY_CALL, retry).build();

        WordCountBlockingStub wordCountBlockingStub =
            WordCountGrpc.newBlockingStub(managedChannel);

        int batchSize = 1;
        for (int offSet = 0 ; offSet < MyConstants.HAMLET.length ; offSet += batchSize++) {
            
            Builder inputMessageBuilder = InputMessage.newBuilder();
            for (int i = offSet ; (i < MyConstants.HAMLET.length && i < offSet + batchSize) ; i++) {
                inputMessageBuilder.addInputValue(MyConstants.HAMLET[i]);
            }

            System.out.println("---");
            System.out.println();

            InputMessage inputMessage = inputMessageBuilder.build();
            System.out.println(inputMessage);
            
            try {
                OutputMessage outputMessage = wordCountBlockingStub.myCall(inputMessage);
                System.out.println(outputMessage);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
        
        managedChannel.shutdown();
    }
}
