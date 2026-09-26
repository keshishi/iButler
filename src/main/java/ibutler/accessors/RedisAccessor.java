package ibutler.accessors;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

@Log4j2
@Repository
public class RedisAccessor {

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisReactiveCommands<String, String> reactive;

    public RedisAccessor() {
        // Using local host for redis server now, change to a environment variable to avoid committing secrets in future
        this.client = RedisClient.create("redis://localhost:6379");
        this.connection = client.connect();
        this.reactive = connection.reactive();
    }

    public RedisReactiveCommands<String, String> commands() {
        return reactive;
    }

    public void close() {
        connection.close();
        client.shutdown();
    }
}
