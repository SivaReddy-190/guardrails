package com.project.guardrails.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class RedisGuardrailService {

    private final StringRedisTemplate redisTemplate;

   public void incrementViralityScore(Long posiId, int points) {

       String key = "Post:"+ posiId + "virality_score:";
         redisTemplate.opsForValue().increment(key, points);
   }

   public boolean allowBotCommentsOnPost(Long postId) {

       String key = "Post: "+ postId + ":bot_count";
       Long currentCount = redisTemplate.opsForValue().increment(key);

       if(currentCount!=null && currentCount > 100) {
           redisTemplate.opsForValue().decrement(key);
           return false;
       }

       return true;
   }

   public boolean checkAndSetBotCooldown(Long botId, long humanId) {

       String key = "cooldown:bot_"+botId+"human_"+humanId;

       Boolean isAllowed = redisTemplate.opsForValue()
               .setIfAbsent(key, "locked", Duration.ofMinutes(10));

       return Boolean.TRUE.equals(isAllowed);
   }
}
