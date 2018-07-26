package com.biz.primus.ms.base.lock;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson配置信息
 */
@Data
@Configuration
public class RedissonAutoConfiguration {

    @Value("${redisson.address}")
    private String address;

    @Value("${redisson.password}")
    private String password;

    private int timeout = 3000;
    private int connectionPoolSize = 64;
    private int connectionMinimumIdleSize=10;
    private int slaveConnectionPoolSize = 250;
    private int masterConnectionPoolSize = 250;

    //针对redis为集群
    private String[] sentinelAddresses;
    private String masterName;

    /**
     * 单机模式自动装配
     *
     */
    @Bean
    @ConditionalOnProperty(name="redisson.address")
    RedissonClient redissonSingle() {
        //TODO 需要注意,正式上线如果redis为集群,需要修改
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(this.address)
                .setTimeout(this.timeout)
                .setConnectionPoolSize(this.connectionPoolSize)
                .setConnectionMinimumIdleSize(this.connectionMinimumIdleSize);

        if(StringUtils.isNotBlank(this.password)) {
            serverConfig.setPassword(this.password);
        }
        return Redisson.create(config);
    }
}