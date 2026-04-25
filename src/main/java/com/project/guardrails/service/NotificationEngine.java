package com.project.guardrails.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEngine {

    private final StringRedisTemplate redisTemplate;

    public void processBotInteraction(Long humanUserId, Long botId) {
        String cooldownKey = "user:"+humanUserId+":notif_cooldown";
        String pendingQueueKey = "user:"+humanUserId+":pending_notifs";
        String notifMessage = "bot: "+botId+" :replied to your post";

        Boolean hasCooldown = redisTemplate.hasKey(cooldownKey);

        if (!hasCooldown) {

            log.info("Sending notification to user {}:  {}", humanUserId,notifMessage);
            redisTemplate.opsForValue().set(cooldownKey, "active", java.time.Duration.ofMinutes(15));
        }
        else {
            redisTemplate.opsForList().rightPush(pendingQueueKey,notifMessage);
        }


    }


    @Scheduled(fixedDelay = 300000)
    public void sweepPendingNotifications() {
        log.info("Running CRON sweeper for pending notifications");

        Set<String> keys = redisTemplate.keys("user:*:pending_notifs");

        if(keys == null || keys.isEmpty()) {
            return ;
        }

        for (String key : keys) {
            String userId = key.split(":")[1];
            List<String> pendingNotifs = redisTemplate.opsForList().range(key, 0, -1);

            if (pendingNotifs != null && !pendingNotifs.isEmpty()) {
                int totalPending = pendingNotifs.size();

                String firstMessage = pendingNotifs.getFirst();

                if(totalPending == 1) {
                    log.info("Summarized push notifications for user {}: {}", userId, firstMessage);
                }

                else {
                    log.info("Summarized push notifications for user {}: {} and {} others", userId, firstMessage, totalPending-1);
                }

                redisTemplate.delete(key);
            }
        }

    }
}
