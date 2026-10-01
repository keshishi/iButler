package ibutler.handlers;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;


public interface Handler<D4JEvent extends Event> {

    boolean apply(D4JEvent event);
    String getName();
    Mono<Void> handle(D4JEvent event);
}