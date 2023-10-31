package co.smartooth.school.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import co.smartooth.school.vo.SchoolAuthVO;

/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 7. 18 ~
 */
@Mapper
public interface SchoolAuthMapper {

	
	// 회원 아이디 존재 여부 :: true = 1, false = 0
	public int loginChkByIdPwd(SchoolAuthVO schoolAuthVO) throws Exception;

	
	
	// 회원 아이디가 존재하는지 여부 확인 :: true = 1, false = 0
	public int isIdExist(@Param("userId") String userId) throws Exception;
	
	
	
	// 회원 아이디와 기관 코드 대조 
	public int loginChkByIdOrganCd(@Param("userId") String userId, @Param("organCd") String organCd) throws Exception;

}
