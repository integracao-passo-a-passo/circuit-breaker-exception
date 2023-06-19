package org.apache.camel.example.cb;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectingOriginalExceptionRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(CollectingOriginalExceptionRoute.class);

    private void processFallBack(Exchange exchange) {
        LOG.info("Exception: {}", exchange.getException()); // This will be null here
        LOG.info("Exception handled? {}", exchange.getProperty(Exchange.EXCEPTION_HANDLED)); // True if it was handled, or false otherwise
        LOG.info("Exception caught: {}", exchange.getProperty(Exchange.EXCEPTION_CAUGHT)); // The original exception
        exchange.getMessage().setBody("This is the new body!"); // do this if you want to overwrite the body when handling the fallback
    }

    @Override
    public void configure() throws Exception {

        from("timer:bootstrapTimer?period=1000&fixedRate=true&repeatCount=5")
                .to("direct:start");

        from("direct:start")
                .routeId("start-route")
                .setBody(constant("This is the original body"))
                .circuitBreaker()
                .to("http://localhost:1999")
                .onFallback()
                .process(this::processFallBack)
                .end()
                .log("Completed ${body}");

    }
}
