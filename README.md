# Java class to handle OTP sent via email in E2E UI tests.

## Background
When implementing end-to-end automated testing of an application with one-time password  (OTP) authentication, we need to establish a way to test the entire flow of OTP delivery to the user.
Here I provide two classes that are ready to be used in this kind of test to receive OTP via email sent by the backend and a step-by-step guide on integrating into your framework.

## Constraints
To implement this approach, the following conditions must be met:
* The QA team manages a dedicated email account.
* Emails delivering OTP have a known constant Subject text
* A known constant phrase precedes OTP in the body of the email
* The OTP length is fixed

## Algorithm
Along execution, an autotest:
1. Creates and keeps an instance of the Email Provider service based on the configuration settings (Email Provider API credentials)
2. Gets and keeps a pointer to the last received email with a subject that is set for OTP emails (or retains null if no one was received before)
3. Triggers the OTP generation and delivery
4. Waits for email delivery to the Email Provider
5. Gets the email message
6. Parses OTP from the message
7. Uses OTP to verify login to the application under test

Steps 1-2 and 4-6 are implemented in the code provided.

## EmailProviderHandler interface
To make the approach flexible, the `EmailProviderHandler` interface has been declared. The implementation must provide the following:
- a service initiation to expect emails with specified subject
- checking if a new email with the subject has already been received
- getting the message from the received email

I have implemented the interface for Gmail (below), but you can implement it for another provider you use.

## EmailedOTPHandler class
To process emails with OTP, use the `EmailedOTPHandler` class.
An EmailedOTPHandler instance should be created for a specific combination of an 
email Subject, a passphrase followed by an OTP in the email body, and an OTP length. 
To give the handler a tool to access emails on a particular Email Provider, 
we should inject an instance of the implemented `EmailProviderHandler`.

Before using an `EmailedOTPHandler` instance for getting OTP, you need to initiate it using the `init()` method.
Then you can trigger OTP generation and delivery by either mimic user login behavior through your application's UI or by querying the BE endpoint.
To get the OTP from an email, use the `getOTPEmailSent()` method. The method waits for a new email with Subject set and then tries to parse the OTP from it.
If there is no new message within the time period, NULL is returned.

The complete class code is shown [below](#emailedotphandler).

## GmailHandler class
`GmailHandler` implements `EmailProviderHandler` to handle a Gmail service through API.
To start the Gmail service and get credentials are used using methods described in the [Google Gmail Java quick start guide](https://developers.google.com/gmail/api/quickstart).

On the first call to the Gmail API, `GmailHandler` creates a credential file in the project to authenticate all future access to the Gmail service (see detailed description [below](#add-json-client-id-file)).

The complete class code is shown [below](#gmailhandler).

## Notice

* To enable Java `assert` validation,  use the JVM parameter `-ea`
<details>
  <summary>IntelliJ IDEA (Click to expand)</summary>
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/zh7gvcftxk9z95k33neh.png)
</details>

* Before using the class, you must enable and configure the API for your Gmail account, as shown [below](#how-to-set-up-the-gmail-account-api).

* `GmailHandler` extracts OTP from the email snippet. If your OTP in the email body is too far from the beginning and therefore not included in the snippet, use `getPayload()` instead of `getSnippet()`.

## How to set up the Gmail account API
Before moving on, you must activate and configure the API for the Gmail account you will be using to receive OTP emails. Using the [Google Cloud Console](https://console.cloud.google.com/) follow the steps below.
<details>
  <summary>How to set up the Gmail account API - details (Click to expand)</summary>

#### Register a new project


* Click on CREATE PROJECT.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/cuic3ksh7bfqcxyoucbf.png)

* Then give your project a name.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/7rjx5d9e8ky7sny9feed.png)


#### Enable API


* Click on ENABLE APIS AND SERVICES.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/rceb36estm0gjegi78g1.png)

* Search for Gmail in the API Library.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/5g84okq1lrivovwb9q1y.png)

* Enable Gmail API

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/xlt9lfph4dx8efjkzpqr.png)


#### Create credentials for autotests to access your Gmail account

* Click on CREATE CREDENTIALS.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/0to4156boduxjdmm46oc.png)

* Choose for Gmail API a `User data` type.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/nqml60ezt25ie819k0jc.png)

* Customize the OAuth Consent Screen - enter any name for the app and add your contact email address

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/6237zf0vd8rwjppz6j4d.png)

* Set the scope
  It make sense to choose the `read only` scope

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/wdy1pbviwdlir0kj3d67.png)

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/7izys9bhdu750rsdr63o.png)

* Choose the `Desktop app` application type and give it a name

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/dv1xpfn3xi5vqw0uwnu0.png)

* Your credentials have been created; you need to download the Client ID file in JSON format.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/ikwzjgktw2j0orso8bvn.png)

* You can also customize your credentials at any time in the Credentials tab and then download the updated JSON file.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/ngy8ibr01ls83e3dton7.png)

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/s6na8ivc3rarcr0x9vex.png)

#### Register a trusted test user

* Navigate to the OAuth consent screen tab and click on `ADD USERS`

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/lccybxoxman269bidfmo.png)

* Add your any real Gmail account email address. You will need to act under this account later to verify access for the Client ID

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/4onacy77d16gea850vns.png)
</details>

## How to add the Gmail account credentials to the project
After receiving the Client ID file in JSON format (as shown above), you must exchange it for the `StoredCredential` file the first time you call the Gmail API.

<details>
  <summary>How to add the Gmail account credentials to the project - details (Click to expand)</summary>
#### Add JSON Client ID file

* Put the file into `src/main/resources/credentials`.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/t47rol8ailyqaqbpozg9.png)

#### Verify the autotest access to the account on Gmail

Run your project first time. At the first call to the Gmail API, a browser will be opened by Google. Your should follow the Google dialog.

* Choose the account you add on the [Register a trusted test user](#register-a-trusted-test-user) step.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/uj8qaoq1etfkmopooi09.png)

* Click on `Continue` to verify the app

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/q17846jqktuxzjpwbxrp.png)

* Click on `Continue` to grant the access

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/deayf0bgpytwxb6sve98.png)

* Check for the confirmation

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/6lq5g14lfiyjwkb97b26.png)

* Stop the first test project execution.

#### Check for the StoredCredential file


* The `StoredCredential` file should already be created automatically in `src/main/resources/credentials` during your first Gmail API call; if it is not, repeat [this section](#how-to-add-the-gmail-account-credentials-to-the-project) again.

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/ae2bbrwrzbvvqcf80mdk.png)

If you change the Gmail API configuration in the console in the future, you should delete the `StoredCredential` file and repeat these steps to add a new one.

</details>