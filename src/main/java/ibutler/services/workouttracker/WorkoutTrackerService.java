package ibutler.services.workouttracker;

import ibutler.accessors.RedisAccessor;
import ibutler.services.workouttracker.pojos.StreakResult;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class WorkoutTrackerService {

    private final RedisAccessor redis;
    private final String KEY_NAME = "streakers";

    @Autowired
    public WorkoutTrackerService(RedisAccessor redis) {
        this.redis = redis;
    }

    public Mono<Long> signup(@NonNull final String gid, @NonNull final String user) {
        log.info("Request to to sign up user: {} to workouts", user);
        return redis.commands().sadd(String.format("guild:%s:%s", gid, KEY_NAME), user)
                .doOnError(e -> log.error("Redis signup FAILED", e));
    }

    public Mono<Long> logWorkout(@NonNull final String gid, @NonNull final String user) {
        final LocalDate today = LocalDate.now();
        final String key = String.format("guild:%s:user:%s:workouts", gid, user);
        return redis.commands().zadd(key, today.toEpochDay(), today.toString());
    }

    public Mono<StreakResult> getStreak(@NonNull final String gid, @NonNull final String user) {
        final String key = String.format("guild:%s:user:%s:workouts", gid, user);
        return redis.commands()
                .zrange(key, 0, -1)
                .map(LocalDate::parse)
                .collectList()
                .map(this::calculateStreak);
    }

    private StreakResult calculateStreak(List<LocalDate> dates) {
        if (dates.isEmpty()) {
            return new StreakResult(0, null, 0, null);
        }

        // Dates should already be sorted by Redis, but this makes the method
        // safe if it is called independently.
        final List<LocalDate> sortedDates = dates.stream().distinct().sorted().toList();

        // ------------------------------------------------------------
        // Find longest streak
        // ------------------------------------------------------------

        int longestStreak = 1;
        int streakLength = 1;
        List<LocalDate> streakDates = new ArrayList<>();
        List<LocalDate> longestStreakDates = List.of(sortedDates.getFirst());

        streakDates.add(sortedDates.getFirst());

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate current = sortedDates.get(i);
            LocalDate previous = sortedDates.get(i - 1);

            if (current.equals(previous.plusDays(1))) {
                streakLength++;
                streakDates.add(current);
            } else {
                if (streakLength > longestStreak) {
                    longestStreak = streakLength;
                    longestStreakDates = List.copyOf(streakDates);
                }
                streakLength = 1;
                streakDates = new ArrayList<>();
                streakDates.add(current);
            }
        }

        // Check the final streak
        if (streakLength > longestStreak) {
            longestStreak = streakLength;
            longestStreakDates = List.copyOf(streakDates);
        }

        // ------------------------------------------------------------
        // Find current streak
        // ------------------------------------------------------------

        final LocalDate today = LocalDate.now();
        final LocalDate streakEnd;
        LocalDate streakStart;

        if (sortedDates.getLast().equals(today)) {
            // Worked out today, so the streak ends today.
            streakStart = streakEnd = today;
        } else if (sortedDates.getLast().equals(today.minusDays(1))) {
            // Didn't work out today, but did yesterday.
            streakStart = streakEnd = today.minusDays(1);
        } else {
            // Last workout was before yesterday, so there is no current streak.
            streakStart = streakEnd = null;
        }

        long currentStreak = 0;
        if (streakEnd != null) {
            LocalDate expected = streakEnd;

            for (int i = sortedDates.size() - 1; i >= 0; i--) {
                streakStart = sortedDates.get(i);

                if (streakStart.equals(expected)) {
                    expected = expected.minusDays(1);
                } else if (streakStart.isBefore(expected)) {
                    break;
                }
            }
            currentStreak = streakEnd.toEpochDay() - streakStart.toEpochDay() + 1L;
        }

        return new StreakResult(
                longestStreak,
                longestStreakDates.getFirst(),
                currentStreak,
                streakStart
        );
    }
}
