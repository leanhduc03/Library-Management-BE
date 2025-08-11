package com.spring.LibraryManagement.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendVerificationEmail(String toEmail, String verificationCode, boolean isPasswordReset) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        
        if (isPasswordReset) {
            message.setSubject("Đặt lại mật khẩu - Mã xác thực");
            message.setText("Mã xác thực để đặt lại mật khẩu của bạn là: " + verificationCode + 
                    "\nMã này sẽ hết hạn sau 15 phút.");
        } else {
            message.setSubject("Xác thực tài khoản");
            message.setText("Cảm ơn bạn đã đăng ký tài khoản. Mã xác thực của bạn là: " + verificationCode + 
                    "\nMã này sẽ hết hạn sau 15 phút.");
        }
        
        mailSender.send(message);
    }
}