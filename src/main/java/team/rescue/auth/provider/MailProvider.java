package team.rescue.auth.provider;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import team.rescue.member.entity.Member;
import team.rescue.util.RandomCodeUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailProvider {

	private final JavaMailSender javaMailSender;
	private final static String ENCODING = "UTF-8";

	/**
	 * Email 회원 가입시 인증 메일 전송
	 *
	 * @param member 가입 유저
	 * @return Email 인증 코드
	 * <p>
	 * TODO: Email 인증 코드 저장 Redis 고려
	 * TODO: Email 포맷(HTML)
	 */

	public String sendEmail(Member member) {

		String emailCode = RandomCodeUtil.generateCode();

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();

		try {
			MimeMessageHelper messageHelper =
					new MimeMessageHelper(mimeMessage, false, ENCODING);
			messageHelper.setTo(member.getEmail());
			messageHelper.setSubject(member.getNickname() + "님, 회원가입 인증 메일입니다.");
			messageHelper.setText(emailCode);
			javaMailSender.send(mimeMessage);

			log.info("[회원 가입 인증 메일 전송 완료]");

		} catch (MessagingException e) {

			log.error("[Email 전송 오류]");
			throw new RuntimeException(e);
		}

		return emailCode;
	}


}
