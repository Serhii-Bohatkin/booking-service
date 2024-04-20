package bookingservice.service.impl;

import bookingservice.config.BotConfig;
import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.booking.BookingDto;
import bookingservice.exception.TelegramBotException;
import bookingservice.model.Booking;
import bookingservice.model.Payment;
import bookingservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl
        extends TelegramLongPollingBot implements NotificationService {
    public static final String START_COMMAND = "/start";
    private final BotConfig botConfig;

    @Override
    public void sendSuccessBookingMessage(BookingDto dto) {
        sendMessageToAllAdmins("Booking success:" + dto.toString());
    }

    @Override
    public void sendCanceledBookingMessage(Booking booking) {
        sendMessageToAllAdmins("Booking cancelled:" + booking.toString());
    }

    @Override
    public void sendCreatedAccommodationMessage(AccommodationDto dto) {
        sendMessageToAllAdmins("Accommodation created:" + dto.toString());
    }

    @Override
    public void sendDeletedAccommodationMessage(Long id) {
        sendMessageToAllAdmins("Accommodation with id " + id + " has been deleted");
    }

    @Override
    public void sendSuccessfulPaymentMessage(Payment payment) {
        sendMessageToAllAdmins("Successful payment:" + payment.toString());
    }

    @Override
    public void sendPaymentCancelledMessage(Long id) {
        sendMessageToAllAdmins("Payment with id " + id + " cancelled");
    }

    @Override
    public void sendNotExpiredBookingMessage() {
        sendMessageToAllAdmins("There are no expired bookings today");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            if (START_COMMAND.equalsIgnoreCase(messageText)) {
                String firstName = update.getMessage().getChat().getFirstName();
                String s = "Hi, " + firstName + ", your chat id: " + chatId;
                sendMessage(chatId.toString(), s);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    private void sendMessageToAllAdmins(String text) {
        for (String chatId : botConfig.getAllowedChatIds()) {
            sendMessage(chatId, text);
        }
    }

    private void sendMessage(String chatId, String messageText) {
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new TelegramBotException("Failed to send message", e);
        }
    }
}
