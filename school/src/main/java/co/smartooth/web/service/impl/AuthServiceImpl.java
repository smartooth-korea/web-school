package co.smartooth.web.service.impl;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import co.smartooth.web.mapper.AuthMapper;
import co.smartooth.web.service.AuthService;
import co.smartooth.web.vo.AuthVO;

/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 07. 18
 * 수정일 : 2023. 08. 04
 * 서버분리 : 2023. 08. 01
 */
@Component
@Service
public class AuthServiceImpl implements AuthService{

	
	@Autowired(required = false)
	AuthMapper authMapper;
	
	
	
	// 회원 아이디와 비밀번호로 존재 여부 확인 :: true = 1, false = 0
	@Override
	public int loginChkByIdPwd(@Param("userId") String userId, @Param("userPwd") String userPwd) throws Exception{
		return authMapper.loginChkByIdPwd(userId, userPwd);
	}

	
	
	// 회원 아이디가 존재하는지 여부 확인 :: true = 1, false = 0
	@Override
	public int isIdExist(@Param("userId") String userId) throws Exception {
		return authMapper.isIdExist(userId);
	}

	
	// 회원 아이디와 기관 코드 대조 
	@Override
	public int loginChkByIdOrganCd(@Param("userId") String userId, @Param("organCd") String organCd) throws Exception {
		return authMapper.loginChkByIdOrganCd(userId, organCd);
	}
}
