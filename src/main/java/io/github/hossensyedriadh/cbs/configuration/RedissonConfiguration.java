package io.github.hossensyedriadh.cbs.configuration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SslVerificationMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguration {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.username}")
    private String redisUser;

    @Value("${spring.data.redis.password}")
    private String redisPwd;

    @Value("${spring.data.redis.database}")
    private Integer database;

    @Value("${spring.data.redis.lettuce.pool.min-idle}")
    private Integer poolMinIdle;

    @Value("${spring.data.redis.lettuce.pool.max-active}")
    private Integer poolMaxActive;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setUsername(this.redisUser);
        config.setPassword(this.redisPwd);
        config.setSslVerificationMode(SslVerificationMode.CA_ONLY);

        config.useSingleServer()
                .setAddress(String.format("rediss://%s:%d", redisHost, redisPort))
                .setDatabase(this.database)
                .setConnectTimeout(30000)
                .setIdleConnectionTimeout(60000)
                .setTimeout(15000)
                .setConnectionMinimumIdleSize(this.poolMinIdle)
                .setConnectionPoolSize(this.poolMaxActive);

        return Redisson.create(config);
    }
}
