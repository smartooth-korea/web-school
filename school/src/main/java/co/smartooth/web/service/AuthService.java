package co.smartooth.web.service;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import co.smartooth.web.vo.AuthVO;


/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 07. 18
 */
@Service
public interface AuthService {

	// 회원 아이디와 비밀번호로 존재 여부 확인 :: true = 1, false = 0
	public int loginChkByIdPwd(@Param("userId") String userId, @Param("userPwd") String userPwd) throws Exception;
	
	
	
	// 회원 아이디가 존재하는지 여부 확인 :: true = 1, false = 0
	public int isIdExist(@Param("userId") String userId) throws Exception;
	
	
	
	// 회원 아이디와 기관 코드 대조 
	public int loginChkByIdOrganCd(@Param("userId") String userId, @Param("organCd") String organCd) throws Exception;
	

}