package ibutler.handlers.slashcommands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import ibutler.handlers.Handler;
import ibutler.services.workouttracker.WorkoutTrackerService;
import ibutler.services.workouttracker.pojos.StreakResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Scope("singleton")
@Component
@Log4j2
public class StatsCommandHandler implements Handler<ChatInputInteractionEvent> {

    private final WorkoutTrackerService service;

    @Autowired
    public StatsCommandHandler(WorkoutTrackerService service) {
        this.service = service;
    }

    @Override
    public boolean apply(ChatInputInteractionEvent event) {
        return event.getCommandName().equals(getName());
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        final Optional<Snowflake> gid = event.getInteraction().getGuildId();
        final Snowflake uid = event.getInteraction().getUser().getId();

        return gid.map(guild -> service.getStreak(guild.asString(), uid.asString())
                        .flatMap(streak -> event.reply().withContent(formatStreakMessage(streak)))
                        .doOnError(e -> log.error("Failed handling stats command", e))
        ).orElseGet(() -> event.reply().withContent("Can't run this command in a DM"));
    }

    private String formatStreakMessage(StreakResult streak) {
        return String.format(
                """
                🔥 Current streak: %d day%s
                📅 Current streak started: %s
    
                🏆 Longest streak: %d day%s
                📅 Longest streak started: %s
                """,
                streak.currentStreak(),
                streak.currentStreak() == 1 ? "" : "s",
                formatDate(streak.currentStreakStartDate()),
                streak.longestStreak(),
                streak.longestStreak() == 1 ? "" : "s",
                formatDate(streak.longestStreakStartDate())
        );
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "—";
        }

        return date.format(
                DateTimeFormatter.ofPattern("MMM d, yyyy")
        );
    }
}