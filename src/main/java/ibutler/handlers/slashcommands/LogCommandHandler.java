package ibutler.handlers.slashcommands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import ibutler.handlers.Handler;
import ibutler.services.workouttracker.WorkoutTrackerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Scope("singleton")
@Component
@Log4j2
public class LogCommandHandler implements Handler<ChatInputInteractionEvent> {

    private final WorkoutTrackerService service;

    @Autowired
    public LogCommandHandler(WorkoutTrackerService service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        final Optional<Snowflake> gid = event.getInteraction().getGuildId();
        final Snowflake uid = event.getInteraction().getUser().getId();

        return gid.map(guild -> service.logWorkout(guild.asString(), uid.asString())
                .then(event.reply()
                        .withEphemeral(true)
                        .withContent("You have successfully logged your workout for today!")
                ).doOnError(e -> log.error("Failed handling log workout command", e))
        ).orElseGet(() -> event.reply().withContent("Can't run this command in a DM"));    }
}