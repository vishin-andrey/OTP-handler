package gmail;

import static java.lang.Thread.sleep;

interface EmailProviderHandler {
    void init(String emailSubject);
    boolean isEmailReceived();
    String getMessage();
}

public class EmailedOTPHandler {
    private final String emailSubject; // Subject pattern of the emails containing the OTP
    private final String otpKeyPhrase; // Phrase that precedes the OTP in the email body
    private final int otpLength; // Length of the OTP
    private final EmailProviderHandler emailProvider; // Gmail service

    public EmailedOTPHandler(String emailSubject, String otpKeyPhrase, int otpLength, EmailProviderHandler emailProvider) {
        this.emailSubject = emailSubject;
        this.otpKeyPhrase = otpKeyPhrase;
        this.otpLength = otpLength;
        this.emailProvider = emailProvider;
    }

    public void init() {
        emailProvider.init(emailSubject);
    }

    /**
     * Parse and return OTP from the email with id = this.emailID
     */
    private String parseOTP() {
        String mailText = emailProvider.getMessage();
        // Parse OTP
        int pos = mailText.indexOf(otpKeyPhrase);   // Find the OTP key phrase
        assert pos != -1 : "OTP key phrase not found in the email";
        pos = pos + otpKeyPhrase.length();         // Move to the OTP start position
        return mailText.substring(pos, pos + otpLength);
    }

    /**
     * Trying to get a new email comparing its ID with this.emailID.
     * Checking for a new message every 5 sec for 6 times.
     * If gotten a new message, return the OTP from it.
     * If there is no new message during the time period, return NULL
     */
    public String getOTP() {
        String otp = null;
        for (int attempt = 0; attempt < 6; attempt++) {
            if (emailProvider.isEmailReceived()) {
                otp = parseOTP();
                break;
            }
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return otp;
    }
}
