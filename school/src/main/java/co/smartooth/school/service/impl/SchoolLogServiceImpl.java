package co.smartooth.school.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.smartooth.school.mapper.SchoolLogMapper;
import co.smartooth.school.service.SchoolLogService;
import co.smartooth.school.vo.SchoolAuthVO;

/**
 * 작성자 : 정주현 
 * 작성일 : 2023. 08. 25
 * 수정일 : 2023. 08. 25
 */
@Service
public class SchoolLogServiceImpl implements SchoolLogService{

	
	@Autowired(required = false)
	SchoolLogMapper schoolLogMapper;
	
	
	
	// 회원 로그인 기록 INSERT
	@Override
	public void insertUserLoginHistory(SchoolAuthVO schooAuthVO) throws Exception {
		schoolLogMapper.insertUserLoginHistory(schooAuthVO);
	}
	
	
	
	// 회원 접속일 UPDATE
	@Override
	public void updateLoginDt(SchoolAuthVO schooAuthVO) throws Exception {
		schoolLogMapper.updateLoginDt(schooAuthVO);
	}
	
	
}
