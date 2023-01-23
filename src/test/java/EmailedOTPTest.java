import gmail.EmailedOTPHandler;
import gmail.GmailHandler;

public class EmailedOTPTest {

    static String title = "OTP test";
    static String keyPhrase = "Your OTP is ";
    static int otpLength = 6;

    public static void main(String[] args) {

        GmailHandler gmail = new GmailHandler();
        EmailedOTPHandler otpHandler = new EmailedOTPHandler(title, keyPhrase, otpLength, gmail);

        otpHandler.init();
        // -> Here trigger the OTP email sending
        String otp = otpHandler.getOTP();

        assert otp != null : "No new email with title = '" + title + "' was received during the time period";
        System.out.println("OTP: " + otp);
    }

}
