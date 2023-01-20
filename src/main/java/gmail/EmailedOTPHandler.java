package gmail;

import static java.lang.Thread.sleep;

public class EmailedOTPHandler {
    private final String emailTitle;
    private final String otpKeyPhrase;
    private final int otpLength;
    private final GmailHandler gmail;
    private String emailID;

    public EmailedOTPHandler(String emailTitle, String otpKeyPhrase, int otpLength) {
        this.emailTitle = emailTitle;
        this.otpKeyPhrase = otpKeyPhrase;
        this.otpLength = otpLength;
        gmail = new GmailHandler();
        refreshID();
    }

    /**
     * Parse and return OTP from the email with id = this.emailID
     */
    private String getOTP() {
        String mailText = gmail.getEmailSnippet(emailID);
        // Parse OTP
        int pos = mailText.indexOf(otpKeyPhrase);   // Find the OTP key phrase
        assert pos != -1 : "OTP key phrase not found in the email";
        pos = pos + otpKeyPhrase.length();         // Move to the OTP start position
        return mailText.substring(pos, pos + otpLength);
    }

    /**
     * Update this.emailID to the last email with a title = this.emailTitle
     */
    private void refreshID() {
        emailID = getID();
    }

    /**
     * Return ID of the last email with a title = this.emailTitle
     */
    private String getID() {
        return gmail.getEmailIDByTitle(this.emailTitle);
    }

    /**
     * Trying to get a new email comparing its ID with this.emailID.
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
            if (id != null && !id.equals(emailID)) {
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
