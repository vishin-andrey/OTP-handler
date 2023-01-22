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
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.*;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.*;

public class GmailHandler {
    private static final String GMAIL_AUTHENTICATED_USER = "me";
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Gmail handler";
    private static final String PATH_TOKEN_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/credentials";
    private static final String PATH_CREDENTIALS_FILE = PATH_TOKEN_DIRECTORY + "/gmail_credentials.json";
    private final Gmail gmailService;
    private final List<String> SCOPES = Arrays.asList(GmailScopes.MAIL_GOOGLE_COM);

    public GmailHandler() {
        gmailService = startService();
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

        InputStream in = Files.newInputStream(new File(PATH_CREDENTIALS_FILE).toPath());
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + PATH_CREDENTIALS_FILE);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(PATH_TOKEN_DIRECTORY)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Start a new Gmail service
     */
    public Gmail startService () {
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return ID of the last email with a provided title
     * @param title - the message title
     * @return - last email id
     */
    public String getEmailIDByTitle(String title) {
        List<Message> listOfMessages;
        try {
            ListMessagesResponse response =this.gmailService.users().messages()
                    .list(GMAIL_AUTHENTICATED_USER)
                    .setQ("subject:" + title)
                    .execute();
            listOfMessages = response.getMessages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return (listOfMessages == null || listOfMessages.isEmpty()) ? null :  listOfMessages.get(0).getId();
    }

    /**
     * Return the email snippet text
     * @param emailID - the email ID to get the snippet from
     * @return - email snippet text as a String
     */
    public String getEmailSnippet(String emailID) {
        Message message;
        try {
            message = this.gmailService.users().messages()
                    .get(GMAIL_AUTHENTICATED_USER, emailID)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return message.getSnippet(); // Use getPayload() instead if you want to get the full email body
    }
}