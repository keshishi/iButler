package ibutler.services.workouttracker.pojos;

import java.time.LocalDate;

public record StreakResult(
        long longestStreak,
        LocalDate longestStreakStartDate,
        long currentStreak,
        LocalDate currentStreakStartDate
) {}
