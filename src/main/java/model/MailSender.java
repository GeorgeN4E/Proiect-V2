import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import org.json.JSONObject;

public class MailSender {

    private final String locale;
    private final String mailAddress;
    private final HttpClient httpClient;
    private final String getCaptchaUrl = "https://api.xitroo.com/v1/mail/send/requestCaptcha";
    private final String verifyCaptchaUrl = "https://api.xitroo.com/v1/mail/send/verifyCaptcha";
    private final String sendMailUrl = "https://api.xitroo.com/v1/mail/send/sendMail";

    public MailSender(String locale, String mailAddress) {
        this.locale = locale;
        this.mailAddress = mailAddress;
        this.httpClient = HttpClient.newHttpClient();
    }

    // Method to verify captcha with user input
    private String verifyCaptchaAsUserInput() {
        String captchaId = "";
        Scanner scanner = new Scanner(System.in);
        boolean authSuccess = false;

        while (!authSuccess) {
            try {
                // Request a new captcha
                HttpRequest getCaptchaRequest = HttpRequest.newBuilder()
                        .uri(URI.create(getCaptchaUrl + "?locale=" + locale))
                        .header("accept", "application/json, text/javascript, */*; q=0.01")
                        .header("referrer", "https://xitroo.com/")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(getCaptchaRequest, HttpResponse.BodyHandlers.ofString());
                JSONObject captchaResponse = new JSONObject(response.body());
                captchaId = captchaResponse.getString("authID");
                String captchaCode = captchaResponse.getString("captchaCode");

                // Log raw response for debugging
                System.out.println("Captcha Request Response: " + response.body());

                // Print captcha and ask for user input
                System.out.println("Captcha Code:\n" + captchaCode);
                System.out.print("Solve Captcha (r for reload, e for exit): ");
                String solution = scanner.nextLine();

                if (solution.equalsIgnoreCase("r")) {
                    System.out.println("Reloading captcha...");
                    continue;
                }
                if (solution.equalsIgnoreCase("e")) {
                    System.out.println("Exiting captcha verification...");
                    return "";
                }

                // Verify captcha solution
                HttpRequest verifyCaptchaRequest = HttpRequest.newBuilder()
                        .uri(URI.create(verifyCaptchaUrl + "?locale=" + locale + "&authID=" + captchaId + "&captchaSolution=" + solution))
                        .header("accept", "application/json, text/javascript, */*; q=0.01")
                        .header("referrer", "https://xitroo.com/")
                        .GET()
                        .build();

                HttpResponse<String> verifyResponse = httpClient.send(verifyCaptchaRequest, HttpResponse.BodyHandlers.ofString());
                JSONObject verifyResponseJson = new JSONObject(verifyResponse.body());

                // Log raw response for debugging
                System.out.println("Verify Captcha Response: " + verifyResponse.body());

                // Check if authSuccess is present in the response
                if (verifyResponseJson.has("authSuccess")) {
                    authSuccess = verifyResponseJson.getBoolean("authSuccess");
                    if (!authSuccess) {
                        System.out.println("Captcha failed, try again.");
                    } else {
                        System.out.println("Captcha solved successfully!");
                    }
                } else {
                    System.out.println("authSuccess field not found in response.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        return captchaId;
    }

    // Method to send email
    public boolean sendMail(String recipient, String subject, String text, int mode, String captchaId) {
        try {
            if (mode == 1) {
                captchaId = verifyCaptchaAsUserInput();
            }
            if (captchaId == null || captchaId.isEmpty()) {
                System.out.println("Captcha verification failed or cancelled. Email will not be sent.");
                return false;
            }

            // Prepare the email data
            JSONObject emailData = new JSONObject();
            emailData.put("authID", captchaId);
            emailData.put("replyMailID", "");
            emailData.put("subject", subject);
            emailData.put("from", mailAddress);
            emailData.put("recipient", recipient);
            emailData.put("bodyText", text);

            // Send email request
            HttpRequest sendMailRequest = HttpRequest.newBuilder()
                    .uri(URI.create(sendMailUrl + "?locale=" + locale))
                    .header("accept", "*/*")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(emailData.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(sendMailRequest, HttpResponse.BodyHandlers.ofString());
            JSONObject sendResponse = new JSONObject(response.body());

            // Log raw response for debugging
            System.out.println("Send Mail Response: " + response.body());

            return sendResponse.getBoolean("sendSuccess");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        MailSender mailSender = new MailSender("en", "numelemeunumelemeu@example.com");

        // Example usage
        boolean success = mailSender.sendMail(
                "georgeradu190@yahoo.com",
                "Test Subject",
                "Test Body",
                1,
                ""
        );

        if (success) {
            System.out.println("Email sent successfully!");
        } else {
            System.out.println("Failed to send email.");
        }
    }
}
