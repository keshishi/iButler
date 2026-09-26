package ibutler.controllers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import ibutler.handlers.Handler;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.util.Collection;

public abstract class BaseController<D4JEvent extends Event> {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    @Getter
    @Autowired
    protected final Collection<Handler<D4JEvent>> handlers;

    @Autowired
    protected BaseController(Collection<Handler<D4JEvent>> handlers, Class<D4JEvent> eventClass, GatewayDiscordClient client) {
        //Debug Code?
        this.handlers = handlers;
        for (Handler<D4JEvent> h : this.handlers) {
            log.info("Added {} handler to the {} controller", h.getName(), this.getClass().getSimpleName());
        }

        //Handle all D4JEvents
        client.on(eventClass, this::handle).subscribe(
                m -> log.info(m.toString()), //onNext
                error -> log.error("Error handling Event:", error), //onError
                () -> log.info("Event handled successfully") //onComplete
        );
    }

    protected abstract Flux<Void> handle(D4JEvent event);
}
