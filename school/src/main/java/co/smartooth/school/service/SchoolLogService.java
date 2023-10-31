package co.smartooth.school.service;

import org.springframework.stereotype.Service;

import co.smartooth.school.vo.SchoolAuthVO;

/**
 * 작성자 : 정주현 
 * 작성일 : 2023. 08. 25
 * 수정일 : 2023. 08. 25
 */
@Service
public interface SchoolLogService {
	
	
	// 회원 로그인 기록 INSERT
	public void insertUserLoginHistory(SchoolAuthVO schoolAuthVO) throws Exception;
	
	
	// 회원 접속일 UPDATE
	public void updateLoginDt(SchoolAuthVO schoolAuthVO) throws Exception;

	
	
}