package team.rescue.cook.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.dto.ResponseDto;
import team.rescue.cook.dto.CookDto.CookCreateDto;
import team.rescue.cook.dto.CookDto.CookInfoDto;
import team.rescue.cook.service.CookService;

@Slf4j
@RestController
@RequestMapping("/api/cooks")
@RequiredArgsConstructor
public class CookController {

	private final CookService cookService;

	@PostMapping
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<CookInfoDto>> completeCook(
			@RequestBody CookCreateDto cookCreateDto,
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {
		CookInfoDto cookInfoDto = cookService.completeCook(cookCreateDto,
				principalDetails.getUsername());

		return ResponseEntity.ok(new ResponseDto<>("요리 완료 처리되었습니다.", cookInfoDto));
	}
}
