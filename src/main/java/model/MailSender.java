package model;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.regex.Pattern;
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

    private String verifyCaptchaAsUserInput() {
        String captchaId = "";
        Scanner scanner = new Scanner(System.in);
        boolean authSuccess = false;

        while (!authSuccess) {
            try {
                // new captcha
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

                System.out.println("Captcha Request Response: " + response.body());

                System.out.println("Captcha Code:\n" + captchaCode);
                System.out.print("Solve Captcha (r for reload, e for exit): ");
                String solution = scanner.nextLine();

                if (solution.equalsIgnoreCase("r")) {
                    System.out.println("Reloading captcha...");
                    continue; // Restart the loop to request a new captcha
                }
                if (solution.equalsIgnoreCase("e")) {
                    System.out.println("Exiting captcha verification...");
                    return ""; // Exit without completing captcha
                }

                HttpRequest verifyCaptchaRequest = HttpRequest.newBuilder()
                        .uri(URI.create(verifyCaptchaUrl + "?locale=" + locale + "&authID=" + captchaId + "&captchaSolution=" + solution))
                        .header("accept", "application/json, text/javascript, */*; q=0.01")
                        .header("referrer", "https://xitroo.com/")
                        .GET()
                        .build();

                HttpResponse<String> verifyResponse = httpClient.send(verifyCaptchaRequest, HttpResponse.BodyHandlers.ofString());
                JSONObject verifyResponseJson = new JSONObject(verifyResponse.body());

                System.out.println("Verify Captcha Response: " + verifyResponse.body());

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
                System.err.println("Error verifying captcha: " + e.getMessage());
                e.printStackTrace();
                return ""; // Exit on failure
            }
        }
        return captchaId;
    }

    public boolean sendMail(String recipient, String subject, String text, int mode, String captchaId) {
        try {
            if (!isValidEmail(recipient)) {
                System.out.println("Invalid recipient email address.");
                return false;
            }

            if (mode == 1) {
                captchaId = verifyCaptchaAsUserInput(); // Prompt user to solve captcha
            }
            if (captchaId == null || captchaId.isEmpty()) {
                System.out.println("Captcha verification failed or cancelled. Email will not be sent.");
                return false;
            }

            JSONObject emailData = new JSONObject();
            emailData.put("authID", captchaId);
            emailData.put("replyMailID", "");
            emailData.put("subject", subject);
            emailData.put("from", mailAddress);
            emailData.put("recipient", recipient);
            emailData.put("bodyText", text);

            HttpRequest sendMailRequest = HttpRequest.newBuilder()
                    .uri(URI.create(sendMailUrl + "?locale=" + locale))
                    .header("accept", "*/*")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(emailData.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(sendMailRequest, HttpResponse.BodyHandlers.ofString());
            JSONObject sendResponse = new JSONObject(response.body());

            System.out.println("Send Mail Response: " + response.body());

            return sendResponse.getBoolean("sendSuccess");
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public static void main(String[] args) {
        MailSender mailSender = new MailSender("en", "checklistul_tau_preferat");

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
