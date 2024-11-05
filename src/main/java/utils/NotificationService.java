package utils;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class NotificationService {
    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String TWILIO_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");

    public NotificationService() {
        if (ACCOUNT_SID == null || AUTH_TOKEN == null || TWILIO_PHONE_NUMBER == null) {
            throw new IllegalStateException("Twilio environment variables are not set.");
        }
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendSms(String to, String messageBody) {
        Message message = Message.creator(new PhoneNumber(to), new PhoneNumber(TWILIO_PHONE_NUMBER), messageBody).create();
        System.out.println("SMS sent with SID: " + message.getSid());
    }
}
