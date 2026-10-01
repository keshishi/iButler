package ibutler.services.wordleleaderboard;

import ibutler.accessors.RedisAccessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class WordleLeaderboardService {

    @Autowired
    private final RedisAccessor redis;
    private final String KEY_NAME = "streakers";

    public WordleLeaderboardService(RedisAccessor redis) {
        this.redis = redis;
    }



}
