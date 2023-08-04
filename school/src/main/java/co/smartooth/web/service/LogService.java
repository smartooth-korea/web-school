package co.smartooth.web.service;

import org.springframework.stereotype.Service;

import co.smartooth.web.vo.AuthVO;

/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 10. 14
 * 수정일 : 2023. 08. 04
 * 서버분리 : 2023. 08. 01
 */
@Service
public interface LogService {
	
	
	// 회원 로그인 기록 INSERT
	public void insertUserLoginHistory(AuthVO webAuthVO) throws Exception;
	
	
	// 회원 접속일 UPDATE (관리자테이블)
	public void updateLoginDt(AuthVO webAuthVO) throws Exception;
	
	
}