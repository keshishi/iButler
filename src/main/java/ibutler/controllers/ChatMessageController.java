package ibutler.controllers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import ibutler.handlers.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;

@Controller
public class ChatMessageController extends BaseController<MessageCreateEvent> {

    @Autowired
    public ChatMessageController(List<Handler<MessageCreateEvent>> chatHandlers, GatewayDiscordClient client) {
        super(chatHandlers, MessageCreateEvent.class, client);
    }

    @Override
    protected Flux<Void> handle(MessageCreateEvent event) {
        // Convert our list to a flux that we can iterate through
        return Flux.fromIterable(handlers)
                .filter(handler -> handler.apply(event))
                .collectList()
                .flatMapMany(Flux::fromIterable)
                // Have our command class handle all logic related to its specific command.
                .flatMap(command -> command.handle(event));
    }
}
