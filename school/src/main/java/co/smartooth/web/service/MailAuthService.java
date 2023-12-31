package co.smartooth.web.service;

import org.apache.ibatis.annotations.Param;

/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 04. 28
 * 수정일 : 2023. 08. 04
 * 서버분리 : 2023. 08. 01
 */
public interface MailAuthService {
	
	
	// 메일 발송
	public void sendMail(@Param("userId") String userId, @Param("email") String email) throws Exception;
	
	
	// 메일 인증 여부 확인 (Y, N) 
	public String isEmailAuthEnabled(@Param("userId") String userId) throws Exception;
	
	
	// 메일 인증 번호 검증
	public int isAuthKeyMatch(@Param("userId") String userId, @Param("authKey") String authKey) throws Exception;
	

	// 인증 메일 클릭 시 인증 상태를 'Y' 로 업데이트
	public void updateAuthStatusY(@Param("userId") String userId) throws Exception;
	
	
	// 메일 인증 키 업데이트 - service적으로 필요하지 않은 메소드로 작성하지 않음
	// public void insertAuthkey(@Param("userId") String userId, @Param("authEmail") String authEmail, @Param("authKey") String authKey) throws Exception;
	
	
	// 비밀번호 초기화 메일 발송
	// public void sendResetPasswordMail(String userId, String emailAuthKey) throws Exception;
	
	
	// UserId로 메일 인증 번호 발행 후 Database 에 입력
	// void insertAuthKeyById(WebMailAuthVO webMailAuthVO) throws Exception;

	
	// 메일 인증 번호 업데이트
	// public void updateAuthKeyById(WebMailAuthVO webMailAuthVO) throws Exception;
	

	// 인증 메일 클릭 시 인증 상태를 'N' 로 업데이트
	// public void updateAuthStatusN(String userId) throws Exception;

		
}
