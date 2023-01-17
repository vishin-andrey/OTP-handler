import gmail.GmailHandler;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EmailedOTPTest {

    static String pathTokenDirectory = System.getProperty("user.dir") + "/src/main/resources/credentials";
    static String pathCredentialsFile = pathTokenDirectory + "/gmail_credentials.json";

    static String title = "QA Automation Engineer Role";
    public static void main(String[] args) throws GeneralSecurityException, IOException {

        GmailHandler gmailHandler = new GmailHandler(pathTokenDirectory, pathCredentialsFile);

        String lastEmailId = gmailHandler.getIdOfLastMessageWithTitle(title);
        System.out.println("Last email id: " + lastEmailId);
        System.out.println("Message: " + gmailHandler.getMessageById(lastEmailId));
    }

}
