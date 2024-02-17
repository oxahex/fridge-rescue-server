package team.rescue.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailDto {

	private String receiver;
	private String title;
	private String contents;
}
