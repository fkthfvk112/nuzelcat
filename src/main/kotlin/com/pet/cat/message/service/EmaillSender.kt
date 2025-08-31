package com.pet.cat.message.service

import com.pet.cat.message.dto.EmailRequest
import jakarta.mail.MessagingException
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class EmailSender(val emailSender: JavaMailSender,
                  @Value("\${SMTP_USERNAME}") val emailFrom:String) {

    @Async
    @Throws(MessagingException::class)
    fun sendHtmlMessageAsync(mailDto: EmailRequest) {
        val message = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true) // true: HTML 메일 사용

        helper.setFrom(emailFrom)
        helper.setTo(mailDto.emailAddress)
        helper.setSubject("[누젤캣] " + mailDto.emailTitle)
        helper.setText(emailBuilder(mailDto.emailContent), true)

        emailSender.send(message)
    }

    fun emailBuilder(HTMLContent: String?): String {
        val sb = StringBuffer()
        sb.append("<div style=\"width: 100%; max-width: 650px;\">\n")
        sb.append("    <div style=\"padding: 0.5rem; text-align: center; background-color: #6200EE;\">\n")
        sb.append("        <h1 style=\"color: white;\">Planet plan</h1>\n")
        sb.append("    </div>\n")
        sb.append("    <section style=\"padding: 10px; margin-top: 1rem; white-space:normal;\">\n")
        sb.append(HTMLContent)
        sb.append("    </section>\n")
        sb.append("</div>")

        val htmlContent = sb.toString()

        return htmlContent
    }

    fun signUpCertifNumBuilder(certifNumber: Int): String {
        val sb = StringBuffer()
        sb.append("        <h2><span style=\"color: #fb8500;\">🎉 플래닛 플랜 회원가입</span>을 환영합니다.</h2>\n")
        sb.append("        <div style=\"margin-top: 2rem;\">\n")
        sb.append("            <br>\n")
        sb.append("            본 메일은 회원가입 시 입력하신 이메일 주소로 발송되었습니다.\n")
        sb.append("            <br><br>\n")
        sb.append("            아래 인증번호를 회원가입 페이지의 인증번호 칸에 입력해 주세요.\n")
        sb.append("            <br><br><br>\n")
        sb.append("            만약 플래닛 플랜에 가입하신 게 아니라면 이 메일을 무시해주시기 바랍니다.\n")
        sb.append("        </div>\n")
        sb.append("        <div style=\"margin-top: 3rem; background-color: #e1e1e1; padding: 1rem;\">\n")
        sb.append("             인증번호 : $certifNumber")
        sb.append("        </div>\n")
        val content = sb.toString()

        return content
    }
}