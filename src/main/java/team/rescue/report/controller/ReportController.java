package team.rescue.report.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.dto.ResponseDto;
import team.rescue.report.dto.ReportDto.ReportCreateDto;
import team.rescue.report.dto.ReportDto.ReportInfoDto;
import team.rescue.report.service.ReportService;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	/**
	 * 특정 레시피 신고
	 *
	 * @param reportCreateDto 신고 레시피 및 신고 이유
	 * @param details         로그인 유저
	 * @return 신고 처리 결과
	 */
	@PostMapping
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<ReportInfoDto>> reportRecipe(
			@RequestBody ReportCreateDto reportCreateDto,
			BindingResult bindingResult,
			@AuthenticationPrincipal PrincipalDetails details
	) {

		log.info("[레시피 신고] recipeId={}, reporter={}",
				reportCreateDto.getRecipeId(),
				details.getMember().getId()
		);

		ReportInfoDto reportInfoDto =
				reportService.createReport(
						reportCreateDto.getRecipeId(),
						details.getMember().getId(),
						reportCreateDto.getReason()
				);

		return new ResponseEntity<>(
				new ResponseDto<>("신고 처리되었습니다.", reportInfoDto),
				HttpStatus.CREATED
		);
	}
}
