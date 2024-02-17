package team.rescue.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.rescue.auth.user.PrincipalDetails;
import team.rescue.common.dto.ResponseDto;
import team.rescue.cook.dto.CookDto.CookInfoDto;
import team.rescue.member.dto.MemberDto;
import team.rescue.member.dto.MemberDto.MemberDetailDto;
import team.rescue.member.service.MemberService;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/info")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<MemberDetailDto>> getMemberInfo(
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {
		String email = principalDetails.getUsername();

		MemberDetailDto memberDetailDto = memberService.getMemberInfo(email);

		return ResponseEntity.ok(new ResponseDto<>("회원 정보 조회에 성공하였습니다.", memberDetailDto));
	}

	@PatchMapping("/info/nickname")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<MemberDetailDto>> updateMemberNickname(
			@RequestBody @Valid MemberDto.MemberNicknameUpdateDto memberNicknameUpdateDto,
			BindingResult bindingResult,
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {
		String email = principalDetails.getUsername();

		MemberDetailDto memberDetailDto = memberService.updateMemberNickname(email,
				memberNicknameUpdateDto);

		return ResponseEntity.ok(new ResponseDto<>("회원 닉네임 변경에 성공하였습니다.", memberDetailDto));
	}

	@PatchMapping("/info/password")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<MemberDetailDto>> updateMemberPassword(
			@RequestBody @Valid MemberDto.MemberPasswordUpdateDto memberPasswordUpdateDto,
			BindingResult bindingResult,
			@AuthenticationPrincipal PrincipalDetails principalDetails
	) {
		String email = principalDetails.getUsername();

		MemberDetailDto memberDetailDto = memberService.updateMemberPassword(email,
				memberPasswordUpdateDto);

		return ResponseEntity.ok(new ResponseDto<>("회원 비밀번호 변경에 성공하였습니다.", memberDetailDto));
	}

	@GetMapping("/cooks")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<Page<CookInfoDto>>> getCompletedCooks(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@PageableDefault Pageable pageable
	) {

		Page<CookInfoDto> cookInfoDtoPage = memberService.getCompletedCooks(
				principalDetails.getUsername(), pageable);

		return ResponseEntity.ok(new ResponseDto<>("완료된 요리를 조회하였습니다.", cookInfoDtoPage));
	}

	@GetMapping("/recipes")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<Page<RecipeInfoDto>>> getMyRecipes(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@PageableDefault Pageable pageable
	) {
		Page<RecipeInfoDto> recipeInfoDtos = memberService.getMyRecipes(
				principalDetails.getUsername(), pageable);

		return ResponseEntity.ok(new ResponseDto<>("등록한 레시피 내역을 조회하였습니다.", recipeInfoDtos));
	}

	@GetMapping("/bookmarks")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<ResponseDto<Page<RecipeInfoDto>>> getMyBookmarks(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@PageableDefault Pageable pageable
	) {
		Page<RecipeInfoDto> recipeInfoDtos = memberService.getMyBookmarks(
				principalDetails.getUsername(), pageable
		);

		return ResponseEntity.ok(new ResponseDto<>("북마크한 레시피 내역을 조회하였습니다.", recipeInfoDtos));
	}
}
