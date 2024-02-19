import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HandlerTask1 implements Handler {

    private final Client client;

    public HandlerTask1(Client client) {
        this.client = client;
    }

    @Override
    public ApplicationStatusResponse performOperation(String id) {
        CompletableFuture<Response> future1 = CompletableFuture.supplyAsync(() -> client.getApplicationStatus1(id));
        CompletableFuture<Response> future2 = CompletableFuture.supplyAsync(() -> client.getApplicationStatus2(id));

        CompletableFuture<Response> fastest = CompletableFuture.anyOf(future1, future2);

        try {
            Response response = fastest.get(15, TimeUnit.SECONDS);

            if (response instanceof Response.Success) {
                Response.Success success = (Response.Success) response;
                return new ApplicationStatusResponse.Success(success.applicationId(), success.applicationStatus());
            } else if (response instanceof Response.RetryAfter) {
                return new ApplicationStatusResponse.Failure(null, 1);
            } else if (response instanceof Response.Failure) {
                return new ApplicationStatusResponse.Failure(null, 1);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {

            return new ApplicationStatusResponse.Failure(null, 1);
        }
        return new ApplicationStatusResponse.Failure(null, 1);
    }

}