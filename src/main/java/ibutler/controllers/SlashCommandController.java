package ibutler.controllers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import ibutler.handlers.Handler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Log4j2
@Controller
public class SlashCommandController extends BaseController<ChatInputInteractionEvent> {

    @Autowired
    public SlashCommandController(List<Handler<ChatInputInteractionEvent>> chatHandlers, GatewayDiscordClient client) {
        super(chatHandlers, ChatInputInteractionEvent.class, client);
    }

    @Override
    protected Flux<Void> handle(ChatInputInteractionEvent event) {
        // Convert our list to a flux that we can iterate through
        return Flux.fromIterable(handlers)
                // Filter out all commands that don't match the name this event is for
                .filter(handler -> handler.apply(event))
                .collectList()
                .flatMap(c -> c.size() > 1 ? Mono.error(new InternalError("We have defined two slash commands with the same name! " + c)) : Mono.just(c))
                .flatMapMany(Flux::fromIterable)
                // Have our command class handle all logic related to its specific command.
                .flatMap(handler -> handler.handle(event));
    }
}
