package co.smartooth.school.mapper;

import org.apache.ibatis.annotations.Mapper;
import co.smartooth.school.vo.SchoolAuthVO;

/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 04. 28
 * 수정일 : 2022. 08. 18
 */
@Mapper
public interface SchoolLogMapper {

	// 회원 로그인 기록 INSERT
	public void insertUserLoginHistory(SchoolAuthVO schoolAuthVO) throws Exception;
	
	
	// 회원 접속일 UPDATE
	public void updateLoginDt(SchoolAuthVO schoolAuthVO) throws Exception;
	
}
