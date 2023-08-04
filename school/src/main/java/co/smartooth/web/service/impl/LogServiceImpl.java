package co.smartooth.web.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.smartooth.web.mapper.LogMapper;
import co.smartooth.web.service.LogService;
import co.smartooth.web.vo.AuthVO;


/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 10. 14
 * 수정일 : 2023. 08. 04
 * 서버분리 : 2023. 08. 01
 */
@Service
public class LogServiceImpl implements LogService{

	
	@Autowired(required = false)
	LogMapper logMapper;
	

	
	// 회원 로그인 기록 INSERT
	@Override
	public void insertUserLoginHistory(AuthVO webAuthVO) throws Exception {
		logMapper.insertUserLoginHistory(webAuthVO);
	}
	
	
	
	// 회원 접속일 UPDATE (관리자테이블)
	@Override
	public void updateLoginDt(AuthVO webAuthVO) throws Exception {
		logMapper.updateLoginDt(webAuthVO);
	}
}
