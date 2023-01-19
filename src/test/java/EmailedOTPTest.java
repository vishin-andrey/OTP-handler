import gmail.EmailedOTPHandler;


public class EmailedOTPTest {

    static String title = "OTP test";
    public static void main(String[] args) {

        EmailedOTPHandler otpHandler = new EmailedOTPHandler(title, "Your OTP is: ", 6);
        String result = otpHandler.getOTPEmailSent();
        if ((result == null)) System.out.println("OTP email wasn't received");
        else System.out.println("OTP: " + result);
    }

}
