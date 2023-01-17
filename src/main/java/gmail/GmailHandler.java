package gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import org.testng.Assert;

import java.io.*;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.*;

import static java.lang.Thread.sleep;

public class GmailHandler {
    private static final String user = "me";
    private final String BANK_EMAIL_TITLE = "Your Requested Online Banking Identification Code";
    private final String CODE_KEY = "Code is: ";
    private final int CODE_LENGTH = 8;
    private final String APPLICATION_NAME = "TellusAutoTests";
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private String TOKENS_DIRECTORY_PATH;
    /**
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private List<String> SCOPES = Arrays.asList(GmailScopes.MAIL_GOOGLE_COM);
    private String CREDENTIALS_FILE_PATH;

    public GmailHandler() {

        TOKENS_DIRECTORY_PATH = System.getProperty("user.dir") +
                File.separator + "src" +
                File.separator + "test" +
                File.separator + "resources" +
                File.separator + "gmail_credential";
        CREDENTIALS_FILE_PATH = System.getProperty("user.dir") +
                File.separator + "src" +
                File.separator + "test" +
                File.separator + "resources" +
                File.separator + "gmail_credential" +
                File.separator + "gmail_credentials.json";
    }

    /**
     * Creates an authorized Credential object.
     * This is the code from the Google Developer Console documentation.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        InputStream in = new FileInputStream(new File(CREDENTIALS_FILE_PATH));
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Start a new Gmail service
     */
    public Gmail startService () throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail newService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return newService;
    }

    /**
     * Return ID of the last email with a provided title
     * @param title - the message title
     * @return - last email id
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public String getIdOfLastMessageWithTitle(String title) throws IOException, GeneralSecurityException {
        Gmail service = startService();
        Gmail.Users.Messages.List request = service.users().messages().list(user)
                .setQ("subject:" + title);
        List<Message> listOfMessages = request.execute().getMessages();
        return listOfMessages.get(0).getId();
    }

    /**
     * Return ID of the last email with the title of the bank template
     * @return - last bank email id
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public String getLastBankMessageId() throws IOException, GeneralSecurityException {
        return getIdOfLastMessageWithTitle(BANK_EMAIL_TITLE);
    }

    /**
     * Return the email with the provided ID
     * @param id - email id
     * @return - an object of the Gmail class Message
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public Message getMessageById(String id) throws GeneralSecurityException, IOException {
        Gmail service = startService();
        // Get the Gmail class Message object in FULL format
        return service.users().messages().get(user, id).setFormat("FULL").execute();
    }

    /**
     * Parse and return the CODE from the email with provided ID
     * @param id - email id
     * @return - the code from email
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws IllegalAccessException
     */
    public String getBankCodeFromMessage(String id) throws GeneralSecurityException, IOException, IllegalAccessException {

        String mailText = null;
        // Get the Gmail class MessagePart object and extract the MessagePartBody from it
        Object messageBody = getMessageById(id).getPayload().get("body");
        // Search for the "data" field in the MessagePartBody
        for (Field field : messageBody.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            // If the "data" field found, get the content of mail
            if (field.getName() == "data") {
                mailText = String.valueOf(field.get(messageBody));
            }
        }
        Assert.assertNotEquals(mailText, "null","The Gmail response doesn't have the 'data' field.");
        // Base64 content decoding from MIME
        String decodedText = new String(Base64.decodeBase64(mailText));
        // Parse the bank code from the message
        int pos = decodedText.indexOf(CODE_KEY) + CODE_KEY.length();
        String code = decodedText.substring(pos, pos + CODE_LENGTH);
        return code;
    }

    /**
     * Trying to get a new email comparing its ID with the provided one.
     * Checking for a new message every 10 sec for 30 times.
     * If has a new message, return the bank code from it.
     * If there is no new message during this time period, return NULL
     * @param previousId id of the last email before start of the transaction
     * @return bank code or NULL if a new message not received
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws IllegalAccessException
     */
    public String getNewCode(String previousId) throws GeneralSecurityException, IOException, IllegalAccessException, InterruptedException {
        String code = null;
        String currentId = getLastBankMessageId();
        int i = 0, limit = 30;
        // Get the last bank email ID and compare if it's changed
        while (currentId.equals(previousId) && i < limit) {
            sleep(4000);
            currentId = getLastBankMessageId();
            i++;
        }
        // Check if the loop finished because the time exceeded the limit
        if (i != limit) {
            code = getBankCodeFromMessage(currentId);
        }
        return code;
    }
}