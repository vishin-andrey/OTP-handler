import gmail.EmailedOTPHandler;

public class EmailedOTPTest {

    static String title = "OTP test";
    public static void main(String[] args) {

        EmailedOTPHandler otpHandler = new EmailedOTPHandler(title, "Your OTP is: ", 6);
        // -> Here trigger the OTP email sending
        String otp = otpHandler.getOTPEmailSent();

        assert otp != null : "No new email with title = '" + title + "' was received during the time period";
        System.out.println("OTP: " + otp);
    }

}
