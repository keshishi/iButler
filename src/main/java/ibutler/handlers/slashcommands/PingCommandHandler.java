package ibutler.handlers.slashcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import ibutler.handlers.Handler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Scope("singleton")
public class PingCommandHandler implements Handler<ChatInputInteractionEvent> {

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        long start = event.getInteraction().getId().getTimestamp().getEpochSecond() * 1000 + event.getInteraction().getId().getTimestamp().getNano() / 1000_000;
        event.deferReply().withEphemeral(true).subscribe();

        //Reply to the slash command, with the name the user supplied
        return event.editReply("Pong! Response took " + (System.currentTimeMillis() - start) + "ms").then();
    }
}