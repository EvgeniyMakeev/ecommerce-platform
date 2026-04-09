package dev.makeev.cart.config;

import dev.makeev.cart.model.Cart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Cart> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        @SuppressWarnings("unchecked")
        RedisSerializer<Cart> valueSerializer = (RedisSerializer<Cart>)(RedisSerializer<?>) RedisSerializer.json();
        
        RedisSerializationContext<String, Cart> context = RedisSerializationContext
                .<String, Cart>newSerializationContext()
                .key(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .value(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .hashKey(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .hashValue(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .build();
        
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
