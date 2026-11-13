package com.v1.pension.service.other;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import jakarta.xml.bind.DatatypeConverter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.v1.pension.exception.BadRequestException;

@Service
public class SmsService {
  private final WebClient webClient;

  private static final String NTC_PATTERN = "9[87][4-6]\\d{7}";
  private static final String NCELL_PATTERN = "9[87][0-2]\\d{7}";
  private static final String SMS_API_URL = "http://10.100.210.38:8080/sms/send";
  private final Logger logger = LoggerFactory.getLogger(SmsService.class);

  public SmsService() {
    this.webClient = WebClient.create();
  }

  // public void sendSmsViaSparrow(String to, String text) {
  //   var token = "v2_MSMmSM4gNQPH2MumjphyjuQ1Xfy.cITY";
  //   var url = "https://api.sparrowsms.com/v2/sms";
  //   var from = "infosms";
  //   String url1 = url + "?from=" + from + "&to=" + to + "&text=" + text + "&token=" + token;
  //   try {
  //     webClient.get()
  //         .uri(url1)
  //         .retrieve()
  //         .bodyToMono(String.class)
  //         .block();
  //   } catch (Exception e) {
  //     System.out.println(e.getMessage());
  //   }
  // }

  public boolean sendOtpMessage(String phone, String message) {
    return sendSmsViaFcgo(phone, message, "otp");
  }

  public boolean sendAlertMessage(String phone, String message) {
    return sendSmsViaFcgo(phone, message, "alert");
  }

  private boolean sendSmsViaFcgo(String phone, String message, String type) {
    try {
      if (!isNumberValid(phone))
        throw new BadRequestException("Invalid Mobile Number");

      String apiKey = "";
      String apiSecret = "";
      String nonce = "";

      // Credentials
      if (Pattern.matches(NTC_PATTERN, phone)) {
        apiKey = "pension-ntc";
        apiSecret = "v3Gr1J4THZbt0owWOuwlJcnfQrsnBhfn0JjFbtLr69DHgKGtfp";
        nonce = "v3Gr1J4THZbt0owWOuwlJcnfQrsnBhfn0JjFbtLr69DHgKGtfp";
      } else {
        apiKey = "pension-ncell";
        apiSecret = "AQfJAtpxP634vIhzXLleUlXhTFGTU8xwEHHA2KQUv6oD2RJgtn";
        nonce = "AQfJAtpxP634vIhzXLleUlXhTFGTU8xwEHHA2KQUv6oD2RJgtn";
      }

      var uniqueId = "ipmsv2";
      String delimiter = " ";
      String requestPayload = "{\"mobileNumber\":\"" + phone + "\",\"message\":\"" + message + "\",\"type\":\"" + type
          + "\",\"uniqueId\":\"" + uniqueId + "\"}";
      String signatureBase = delimiter + apiKey + delimiter + nonce + delimiter + requestPayload + delimiter;
      Mac hasher = Mac.getInstance("HmacSHA512");
      hasher.init(new SecretKeySpec(apiSecret.getBytes(), "HmacSHA512"));
      byte[] hash = hasher.doFinal(signatureBase.getBytes());
      String signature = DatatypeConverter.printBase64Binary(hash);
      String header = "HmacSHA512" + " " + apiKey + ":" + nonce + ":" + signature;

      // Set up the URL and HttpURLConnection object
      URL url = new URL(SMS_API_URL);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      // Set the request method and properties
      con.setRequestMethod("POST");
      con.setRequestProperty("User-Agent", "Mozilla/5.0");
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Accept", "application/json");
      con.setRequestProperty("Authorization", header);
      con.setDoOutput(true);

      // JSON data you want to send as a payload
      String jsonPayload = requestPayload;

      // Write the payload to the output stream
      try (OutputStream os = con.getOutputStream()) {
        byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      // Get the HTTP response code (e.g., 200 for success)
      int responseCode = con.getResponseCode();

      if (responseCode != 200)
        throw new BadRequestException("Status Code: " + responseCode);

      return true;
    } catch (Exception e) {
      logger.error("SMS Send Failed: {}", e.getMessage());
      return false;
    }
  }

  private boolean isNumberValid(String number) {
    return (Pattern.matches(NTC_PATTERN, number) || Pattern.matches(NCELL_PATTERN, number));
  }
}
