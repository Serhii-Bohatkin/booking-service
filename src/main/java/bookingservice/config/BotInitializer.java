package bookingservice.config;

import bookingservice.exception.TelegramBotException;
import bookingservice.service.impl.TelegramNotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@RequiredArgsConstructor
public class BotInitializer {
    private final TelegramNotificationServiceImpl bot;
    private final BotConfig botConfig;

    @EventListener(ContextRefreshedEvent.class)
    public void init() throws TelegramApiException {
        //This check is required to build a project on GitHub
        if (!(botConfig.getBotName().isEmpty() || botConfig.getBotToken().isEmpty())) {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            try {
                telegramBotsApi.registerBot(bot);
            } catch (TelegramApiException e) {
                throw new TelegramBotException("Failed to start bot", e);
            }
        }
    }
}
