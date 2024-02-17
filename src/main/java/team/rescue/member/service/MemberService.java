package team.rescue.member.service;

import static team.rescue.error.type.ServiceError.USER_NOT_FOUND;
import static team.rescue.error.type.ServiceError.USER_PASSWORD_MISMATCH;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.rescue.cook.dto.CookDto.CookInfoDto;
import team.rescue.cook.entity.Cook;
import team.rescue.cook.repository.CookRepository;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;
import team.rescue.member.dto.MemberDto.MemberDetailDto;
import team.rescue.member.dto.MemberDto.MemberNicknameUpdateDto;
import team.rescue.member.dto.MemberDto.MemberPasswordUpdateDto;
import team.rescue.member.entity.Member;
import team.rescue.member.repository.MemberRepository;
import team.rescue.recipe.dto.RecipeDto.RecipeInfoDto;
import team.rescue.recipe.entity.Bookmark;
import team.rescue.recipe.entity.Recipe;
import team.rescue.recipe.repository.BookmarkRepository;
import team.rescue.recipe.repository.RecipeRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final CookRepository cookRepository;
	private final RecipeRepository recipeRepository;
	private final BookmarkRepository bookmarkRepository;

	public MemberDetailDto getMemberInfo(String email) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		validateMember(member);
		
		return MemberDetailDto.of(member);
	}

	@Transactional
	public MemberDetailDto updateMemberNickname(String email,
			MemberNicknameUpdateDto memberNicknameUpdateDto) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		validateMember(member);

		member.updateNickname(memberNicknameUpdateDto.getNickname());

		Member updatedMember = memberRepository.save(member);

		return MemberDetailDto.of(updatedMember);
	}

	@Transactional
	public MemberDetailDto updateMemberPassword(String email,
			MemberPasswordUpdateDto memberPasswordUpdateDto) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		validateMember(member);

		boolean passwordMatch = passwordEncoder.matches(memberPasswordUpdateDto.getCurrentPassword(),
				member.getPassword());

		if (!passwordMatch) {
			throw new ServiceException(USER_PASSWORD_MISMATCH);
		}

		member.updatePassword(passwordEncoder.encode(memberPasswordUpdateDto.getNewPassword()));

		Member updatedMember = memberRepository.save(member);

		return MemberDetailDto.of(updatedMember);
	}

	public Page<CookInfoDto> getCompletedCooks(String email, Pageable pageable) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		validateMember(member);

		Page<Cook> cookPage = cookRepository.findByMember(member, pageable);

		return cookPage.map(CookInfoDto::of);
	}

	public Page<RecipeInfoDto> getMyRecipes(String email, Pageable pageable) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		validateMember(member);

		Page<Recipe> recipePage = recipeRepository.findByMember(member, pageable);

		return recipePage.map(RecipeInfoDto::of);
	}

	public Page<RecipeInfoDto> getMyBookmarks(String email, Pageable pageable) {
		Member member = memberRepository.findUserByEmail(email)
				.orElseThrow(() -> new ServiceException(USER_NOT_FOUND));

		validateMember(member);

		Page<Bookmark> bookmarkPage = bookmarkRepository.findByMember(member, pageable);

		return bookmarkPage.map(bookmark -> RecipeInfoDto.of(bookmark.getRecipe()));
	}

	/**
	 * 탈퇴 유저 검증
	 *
	 * @param member 검증할 유저
	 */
	private void validateMember(Member member) {

		// 탈퇴한 멤버 조회 시
		if (!member.getIsEnabled()) {
			throw new ServiceException(ServiceError.USER_ALREADY_LEAVE);
		}
	}
}
