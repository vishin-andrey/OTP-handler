package gmail;

import static java.lang.Thread.sleep;

public class EmailedOTPHandler {
    private String emailTitle;
    private String otpKeyPhrase;
    private int otpLength;
    private String emailID;
    private GmailHandler gmail;

    public EmailedOTPHandler(String emailTitle, String otpKeyPhrase, int otpLength) {
        this.emailTitle = emailTitle;
        this.otpKeyPhrase = otpKeyPhrase;
        this.otpLength = otpLength;
        this.gmail = new GmailHandler();
        this.refreshID();
    }

    /**
     * Parse and return OTP from the email with emailLastID
     */
    private String getOTP() {
        String mailText = this.gmail.getEmailText(this.emailID);
        // Base64 content decoding from MIME
        String decodedText = mailText;
        // Parse the bank code from the message
        int pos = decodedText.indexOf(this.otpKeyPhrase) + this.otpKeyPhrase.length();
        String code = decodedText.substring(pos, pos + otpLength);
        return code;
    }

    private void refreshID() {
        this.emailID = getID();
    }

    private String getID() {
        return this.gmail.getIdLastEmailByTitle(this.emailTitle);
    }

    /**
     * Trying to get a new email comparing its ID with the provided one.
     * Checking for a new message every 5 sec for 6 times.
     * If gotten a new message, return the OTP from it.
     * If there is no new message during the time period, return NULL
     */
    public String getOTPEmailSent() {
        String id;
        int i = 0, limit = 6;
        // Get the last email ID and compare if it's changed
        while (i < limit) {
            id = getID();
            if (id != null && !id.equals(this.emailID)) {
                this.refreshID();
                return getOTP();
            }
            i++;
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
