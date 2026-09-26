package ibutler.common;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public class Utils {

    public static String getGuildKey(@NonNull final Snowflake guild) {
        return String.format("guild:%s", guild.asString());
    }

    public static String getUserKey(@NonNull final Snowflake guild, @NonNull final Snowflake user) {
        return String.format("guild:%s:user:%s", guild.asString(), user.asString());
    }

    public static String getUserKey(@NonNull final String guild, @NonNull final String user) {
        return String.format("guild:%s:user:%s", guild, user);
    }

    public static Mono<Void> addRoleToUser(@NonNull final Snowflake gid, @NonNull final Snowflake uid,
                                     @NonNull final ChatInputInteractionEvent event,
                                     @NonNull final String roleName) {
        return event.getClient().getGuildById(gid)
                .flatMapMany(Guild::getRoles)
                .filter(role -> role.getName().equalsIgnoreCase(roleName))
                .collectList()
                .flatMap(list -> {
                    if (list.size() > 1) {
                        return Mono.error(new InternalError("We have defined two roles with the same name! " + list));
                    }
                    if (list.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Role not found: " + roleName));
                    }
                    return Mono.just(list.getFirst());
                })
                // Flatten the Member fetch
                .flatMap(role -> event.getClient().getMemberById(gid, uid)
                        // Flatten the actual role assignment
                        .flatMap(member -> member.addRole(role.getId()))
                        .doOnError(e -> log.error(String.format("Failed to add %s role", roleName), e))
                );
    }

}
