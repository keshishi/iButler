package ibutler.handlers.messageHandlers;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ibutler.handlers.Handler;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Scope("singleton")
@Log4j2
public class PingMessageHandler implements Handler<MessageCreateEvent> {

    @Override
    public boolean apply(MessageCreateEvent event) {
        return event.getMessage().getContent().equals("!ping");
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        final long start = event.getMessage().getId().getTimestamp().getEpochSecond() * 1000 + event.getMessage().getId().getTimestamp().getNano() / 1_000_000;
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("Pong! Response took " + (System.currentTimeMillis() - start) + "ms"))
                .then();
    }
}