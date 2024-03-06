package bookingservice.config;

import java.util.Set;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class BotConfig {
    @Value("${bot.username}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.allowed-chat-ids}")
    private Set<String> allowedChatIds;
}
