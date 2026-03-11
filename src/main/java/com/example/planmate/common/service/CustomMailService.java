package com.example.planmate.common.service;

import java.nio.charset.StandardCharsets;

import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomMailService {

    private static final String PASSWORD_RESET_SUBJECT = "planMate 임시 비밀번호입니다.";
    private static final String PASSWORD_RESET_HEADLINE = "임시 비밀번호를 발급해드렸어요";
    private static final String PASSWORD_RESET_DESCRIPTION = "아래 임시 비밀번호로 로그인한 뒤, 마이페이지에서 원하는 비밀번호로 바로 변경해 주세요.";

    private final JavaMailSender mailSender;
    private final String defaultFrom = "planMate <no_reply@planmate.site>";

    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(defaultFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendVerificationCodeMail(String to, String subject, String headline, String description, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(defaultFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildHighlightedValueText(headline, description, "인증번호", code, "유효시간", "5분", "인증번호는 타인과 공유하지 마세요.", "EMAIL VERIFICATION"),
                    buildHighlightedValueHtml(headline, description, "인증코드", code, "유효시간", "5분", "인증번호는 타인과 공유하지 마세요.", "EMAIL VERIFICATION"));

            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new MailSendException("Failed to compose verification email", exception);
        }
    }

    public void sendTemporaryPasswordMail(String to, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(defaultFrom);
            helper.setTo(to);
            helper.setSubject(PASSWORD_RESET_SUBJECT);
            helper.setText(
                    buildHighlightedValueText(PASSWORD_RESET_HEADLINE, PASSWORD_RESET_DESCRIPTION, "임시 비밀번호", tempPassword, "보안 안내", "로그인 후 즉시 변경 권장", "임시 비밀번호는 타인과 공유하지 마시고, 로그인 후 바로 변경해 주세요.", "PASSWORD RESET"),
                    buildHighlightedValueHtml(PASSWORD_RESET_HEADLINE, PASSWORD_RESET_DESCRIPTION, "임시 비밀번호", tempPassword, "보안 안내", "로그인 후 즉시 변경 권장", "임시 비밀번호는 타인과 공유하지 마시고, 로그인 후 바로 변경해 주세요.", "PASSWORD RESET"));

            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new MailSendException("Failed to compose temporary password email", exception);
        }
    }

    private String buildHighlightedValueText(
            String headline,
            String description,
            String valueLabel,
            String value,
            String metaLabel,
            String metaValue,
            String notice,
            String badgeText) {
        return String.join("\n\n",
                headline,
                description,
                valueLabel + ": " + value,
                metaLabel + ": " + metaValue,
                notice,
                "PlanMate");
    }

    private String buildHighlightedValueHtml(
            String headline,
            String description,
            String valueLabel,
            String value,
            String metaLabel,
            String metaValue,
            String notice,
            String badgeText) {
        String escapedHeadline = HtmlUtils.htmlEscape(headline);
        String escapedDescription = HtmlUtils.htmlEscape(description);
        String escapedValueLabel = HtmlUtils.htmlEscape(valueLabel);
        String escapedValue = HtmlUtils.htmlEscape(value);
        String escapedMetaLabel = HtmlUtils.htmlEscape(metaLabel);
        String escapedMetaValue = HtmlUtils.htmlEscape(metaValue);
        String escapedNotice = HtmlUtils.htmlEscape(notice);
        String escapedBadgeText = HtmlUtils.htmlEscape(badgeText);

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>PlanMate Verification</title>
                </head>
                <body style="margin:0; padding:0; background-color:#edf3ff; font-family:'Apple SD Gothic Neo','Malgun Gothic','Noto Sans KR',sans-serif; color:#111827;">
                    <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" bgcolor="#edf3ff" style="background-color:#edf3ff;">
                        <tr>
                            <td style="padding:28px 12px;">
                                <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                                    <tr>
                                    </tr>
                                </table>
                                <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="max-width:640px; margin:0 auto;">
                                    <tr>
                                        <td bgcolor="#ffffff" style="background-color:#ffffff; border:1px solid #d7e3ff; border-radius:28px; overflow:hidden; box-shadow:0 10px 30px rgba(48,86,211,0.10);">
                                            <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                                                <tr>
                                                    <td bgcolor="#2453ff" style="background-color:#2453ff; padding:18px 24px;">
                                                        <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                                                            <tr>
                                                                <td align="left" style="color:#cfd9ff; font-size:11px; font-weight:700; letter-spacing:0.18em;">__BADGE_TEXT__</td>
                                                                <td align="right" style="color:#ffffff; font-size:20px; font-weight:800; letter-spacing:0.12em;">PLANMATE</td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="padding:32px 32px 18px;">
                                                        <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                                                            <tr>
                                                                <td style="padding:0 0 14px; color:#111827; font-size:28px; line-height:1.35; font-weight:800;">__HEADLINE__</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="padding:0 0 24px; color:#5b6478; font-size:16px; line-height:1.8;">__DESCRIPTION__</td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="padding:0 32px 24px;">
                                                        <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" bgcolor="#f4f7ff" style="background-color:#f4f7ff; border:1px solid #d8e3ff; border-radius:24px;">
                                                            <tr>
                                                                <td style="padding:22px 24px 10px; color:#6676a8; font-size:13px; font-weight:700; letter-spacing:0.12em; text-align:center;">__VALUE_LABEL__</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="padding:0 24px 24px; color:#2453ff; font-size:44px; line-height:1.15; font-weight:900; letter-spacing:0.16em; text-align:center; word-break:break-word;">__VALUE__</td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="padding:0 32px 28px;">
                                                        <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                                                            <tr>
                                                                <td width="50%%" valign="top" style="padding:0 8px 0 0;">
                                                                    <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" bgcolor="#f8faff" style="background-color:#f8faff; border:1px solid #e3e9f6; border-radius:18px;">
                                                                        <tr>
                                                                            <td style="padding:18px 18px 8px; color:#7c869f; font-size:12px; font-weight:700; letter-spacing:0.08em;">__META_LABEL__</td>
                                                                        </tr>
                                                                        <tr>
                                                                            <td style="padding:0 18px 18px; color:#111827; font-size:18px; line-height:1.5; font-weight:800;">__META_VALUE__</td>
                                                                        </tr>
                                                                    </table>
                                                                </td>
                                                                <td width="50%%" valign="top" style="padding:0 0 0 8px;">
                                                                    <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" bgcolor="#f8faff" style="background-color:#f8faff; border:1px solid #e3e9f6; border-radius:18px;">
                                                                        <tr>
                                                                            <td style="padding:18px 18px 8px; color:#7c869f; font-size:12px; font-weight:700; letter-spacing:0.08em;">NOTICE</td>
                                                                        </tr>
                                                                        <tr>
                                                                            <td style="padding:0 18px 18px; color:#374151; font-size:14px; line-height:1.7; font-weight:600;">__NOTICE__</td>
                                                                        </tr>
                                                                    </table>
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="padding:0 32px 32px;">
                                                        <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" bgcolor="#1d2433" style="background-color:#1d2433; border-radius:20px;">
                                                            <tr>
                                                                <td style="padding:18px 22px; color:#d4dbeb; font-size:13px; line-height:1.8; text-align:center;">
                                                                    본 메일은 발신 전용입니다. 요청하지 않은 메일이라면 이 메일을 무시해 주세요.
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .replace("__BADGE_TEXT__", escapedBadgeText)
                .replace("__HEADLINE__", escapedHeadline)
                .replace("__DESCRIPTION__", escapedDescription)
                .replace("__VALUE_LABEL__", escapedValueLabel)
                .replace("__VALUE__", escapedValue)
                .replace("__META_LABEL__", escapedMetaLabel)
                .replace("__META_VALUE__", escapedMetaValue)
                .replace("__NOTICE__", escapedNotice);
    }
}
