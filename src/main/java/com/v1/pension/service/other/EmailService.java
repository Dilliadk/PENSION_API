package com.v1.pension.service.other;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  @Autowired
  private JavaMailSender javaMailSender;
  
  @Value("${spring.mail.username}")
  private String sender;

  public String sendEmail(String to, String subject, String body) {
    try {
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setFrom(sender);
      mailMessage.setTo(to);
      mailMessage.setSubject(subject);
      mailMessage.setText(body);

      javaMailSender.send(mailMessage);
      return "Email Sent Successfully, Please check Email";
    } catch (MailException e) {
      System.out.println(e.toString());
      return "Error Sending Email, Please Try Again.";
    }
  }
}
