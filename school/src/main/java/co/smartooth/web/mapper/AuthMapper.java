package co.smartooth.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import co.smartooth.web.vo.AuthVO;

/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 7. 18 ~
 */
@Mapper
public interface AuthMapper {

	
	// 회원 아이디 존재 여부 :: true = 1, false = 0
	public int loginChkByIdPwd(AuthVO webAuthVO) throws Exception;

	
	
	// 회원 아이디가 존재하는지 여부 확인 :: true = 1, false = 0
	public int isIdExist(AuthVO webAuthVO) throws Exception;
	
	
	
	// 회원 아이디와 기관 코드 대조 
	public int loginChkByIdOrganCd(@Param("userId") String userId, @Param("organCd") String organCd) throws Exception;

}
