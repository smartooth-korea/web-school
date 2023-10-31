package co.smartooth.web.service.impl;

import java.util.HashMap;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.smartooth.web.mapper.UserMapper;
import co.smartooth.web.service.UserService;
import co.smartooth.web.vo.UserVO;

/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 07. 15
 * 수정일 : 2023. 08. 04
 * 서버분리 : 2023. 08. 01
 */
@Service
public class UserServiceImpl implements UserService{
	
	
	@Autowired(required = false)
	UserMapper userMapper;
	
	
	
	// 계정 등록
	@Override
	public void insertUserInfo(UserVO webUserVO) throws Exception {
		userMapper.insertUserInfo(webUserVO);
	}

	
	
	// 피측정자 정보 및 상세 정보 조회
	@Override
	public UserVO selectUserInfo(@Param("userId") String userId) throws Exception {
		return userMapper.selectUserInfo(userId);
	}

	
	
	// 피측정자 회원 아이디 조회 - ST_PARENT_USER_DEATAIL
	@Override
	public String selectChUserId(@Param("userId") String userId) throws Exception {
		return userMapper.selectChUserId(userId);
	}
	
	
	
	// 피측정자 회원 이름 조회 - ST_USER
	@Override
	public String selectChUserName(@Param("userId") String userId) throws Exception {
		return userMapper.selectChUserName(userId);
	}
	
	
	
	// 회원 상세 정보 등록 
	@Override
	public void insertStUserDetail(String userId, String schoolType, String classCode) throws Exception {
		userMapper.insertStUserDetail(userId, schoolType, classCode);
	}

	

	// 피측정자 법정대리인 회원 상세 정보 등록 
	@Override
	public void insertPaUserDetail(@Param("userId") String userId, @Param("childId") String childId) throws Exception {
		userMapper.insertPaUserDetail(userId, childId);
	}
	
	
	
	// 진단 결과지 첫 로그인 시 이메일 인증 후 비밀번호 변경
	@Override
	public void updateUserPwd(@Param("userId") String userId, @Param("userPwd") String userPwd) throws Exception {
		userMapper.updateUserPwd(userId, userPwd);
	}


	
	// 기관 내 피측정자 목록 조회
	@Override
	public List<HashMap<String, Object>> selectDepartUserList(@Param("classCode") String classCode, @Param("measureDt") String measureDt) throws Exception {
		return userMapper.selectDepartUserList(classCode, measureDt);
	}

	
//	// 계정 삭제
//	@Override
//	public void deleteUserInfo(@Param("userId") String userId) throws Exception {
//		webUserMapper.deleteUserInfo(userId);
//	}

	
	
//	// 피측정자 회원 정보 및 측정 데이터 조회 (회원 한명)
//	@Override
//	public List<HashMap<String, Object>> selectStUserInfo(String userId) throws Exception {
//		return webUserMapper.selectStUserInfo(userId);
//	}

	
	
//	// 피측정자 회원 아이디 목록 조회 - ST_PARENT_USER_DEATAIL
//	@Override
//	public List<HashMap<String, Object>> selectChUserList(@Param("schoolCode") String schoolCode, @Param("measureDt") String measureDt) throws Exception {
//		return webUserMapper.selectChUserList(schoolCode, measureDt);
//	}
	

	


	
}