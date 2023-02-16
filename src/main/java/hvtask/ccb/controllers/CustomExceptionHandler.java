package hvtask.ccb.controllers;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Produces
@Singleton
@Requires(classes = {MqttController.class, ExceptionHandler.class})
public class CustomExceptionHandler implements ExceptionHandler<Exception, HttpResponse> {
    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @Override
    public HttpResponse handle(HttpRequest request, Exception exception) {
        logger.error("Top level catch: ", exception);
        return HttpResponse.serverError(Map.of("error","Something went wrong. We will look at it shortly."));
    }
}
