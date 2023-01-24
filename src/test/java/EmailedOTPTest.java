import gmail.EmailedOTPHandler;
import gmail.GmailHandler;

public class EmailedOTPTest {

    static String subject = "OTP test";
    static String keyPhrase = "Your OTP is: ";
    static int otpLength = 6;

    public static void main(String[] args) {

        GmailHandler gmail = new GmailHandler();
        EmailedOTPHandler otpHandler = new EmailedOTPHandler(subject, keyPhrase, otpLength, gmail);

        otpHandler.init();
        // -> Here trigger the OTP email sending
        String otp = otpHandler.getOTP();

        assert otp != null : "No new email with subject = '" + subject + "' was received during the time period";
        System.out.println("OTP: " + otp);
    }

}
