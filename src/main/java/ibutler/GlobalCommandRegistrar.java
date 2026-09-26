package ibutler;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import ibutler.controllers.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GlobalCommandRegistrar implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    private final RestClient client;

    @Autowired
    public GlobalCommandRegistrar(RestClient restClient) {
        this.client = restClient;
    }

    //This method will run only once on each start up and is automatically called with Spring so blocking is okay.
    @Override
    public void run(ApplicationArguments args) throws IOException {
        //Create an ObjectMapper that supported Discord4J classes
        final JacksonResources d4jMapper = JacksonResources.create();

        // Convenience variables for the sake of easier to read code below.
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
        final ApplicationService applicationService = client.getApplicationService();
        final long applicationId = client.getApplicationId().block();

        //Get our commands json from resources as command data
        List<ApplicationCommandRequest> commands = new ArrayList<>();
        for (Resource resource : matcher.getResources("slashcommands/*.json")) {
            ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                    .readValue(resource.getInputStream(), ApplicationCommandRequest.class);
            commands.add(request);
        }

        /* Bulk overwrite commands. This is now idempotent, so it is safe to use this even when only 1 command
        is changed/added/removed
        */
        applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, commands)
                .doOnNext(ignore -> log.debug("Successfully registered Global Commands"))
                .doOnError(e -> log.error("Failed to register global commands", e))
                .subscribe();

        // delete unused commands
        if (!System.getenv().containsKey("TEST")) {
            Flux<ApplicationCommandData> allRegisteredCommands = applicationService.getGuildApplicationCommands(applicationId, 819136212769046589L);
            allRegisteredCommands.filter(applicationCommandData -> {
                        for (ApplicationCommandRequest command : commands) {
                            if (applicationCommandData.name().equals(command.name())) {
                                log.debug("Command not redundant {}", command.name());
                                return false;
                            }
                        }
                        log.debug("Redundant command: {}", applicationCommandData.name());
                        return true;
                    }).flatMap(command -> applicationService.deleteGuildApplicationCommand(command.applicationId().asLong(), 819136212769046589L, command.id().asLong()))
                    .doOnNext(ignore -> log.info("Removed a redundant command")).subscribe();
        }
    }
}