package co.smartooth.school.service.impl;

import java.util.HashMap;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.smartooth.school.service.SchoolTeethService;
import co.smartooth.school.mapper.SchoolTeethMapper;
import co.smartooth.school.vo.SchoolTeethInfoVO;
import co.smartooth.school.vo.SchoolTeethMeasureVO;

/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 04. 28
 * 수정일 : 2023. 03. 28
 */
@Service
public class SchoolTeethServiceImpl implements SchoolTeethService{
	
	
	@Autowired(required = false)
	SchoolTeethMapper SchoolTeethMapper;
	
	
	// 피측정자 치아 상태 값 등록
	@Override
	public void insertUserTeethInfo(SchoolTeethInfoVO schoolTeethInfoVO) throws Exception {
		SchoolTeethMapper.insertUserTeethInfo(schoolTeethInfoVO);
	}
	
	
	
	// 피측정자 충치 개수 업데이트 (측정일별)
	@Override
	public void updateUserCavityCntByMeasureDt(SchoolTeethMeasureVO schoolTeethMeasureVO) throws Exception {
		SchoolTeethMapper.updateUserCavityCntByMeasureDt(schoolTeethMeasureVO);
	}
	
	
	
	// 피측정자 치아 측정 값 측정일 기준으로 조회
	@Override
    public SchoolTeethMeasureVO selectUserMeasureValue(@Param("userId") String userId, @Param("measureDt") String measureDt) throws Exception {
		return SchoolTeethMapper.selectUserMeasureValue(userId, measureDt);
	}
	
	
	
    // 충치 단계별 수치 조회
	@Override
	public HashMap<String, Integer> selectCavityLevel() throws Exception {
		return SchoolTeethMapper.selectCavityLevel();
	}
	
	
	
	// 진단 내용 업데이트
	@Override
	public void updateDiagDescript(@Param("userId") String userId, @Param("measureDt") String measureDt, @Param("diagDescript") String diagDescript) throws Exception {
		SchoolTeethMapper.updateDiagDescript(userId, measureDt ,diagDescript);
	}
	
	
	
	// 측정일 목록 조회
	@Override
	public List<String> selectUserMeasureDtList(SchoolTeethMeasureVO schoolTeethMeasureVO) throws Exception {
		return SchoolTeethMapper.selectUserMeasureDtList(schoolTeethMeasureVO);
	}
	

	
	// 부서 소속 피측정자 전부의 측정 값 통계 목록
	@Override
	public List<HashMap<String, Object>> selectUserMeasureStatisticsList(@Param("schoolCode") String schoolCode, @Param("measureDt") String measureDt) throws Exception{
		return SchoolTeethMapper.selectUserMeasureStatisticsList(schoolCode, measureDt); 
	}

	
	
	// 악화 지수 점수 업데이트
	@Override
	public void updateUserDeteriorateScore(SchoolTeethMeasureVO schoolTeethMeasureVO) throws Exception {
		SchoolTeethMapper.updateUserDeteriorateScore(schoolTeethMeasureVO);
	}
	
	
	
//	// 피측정자의 치아 측정 값 조회 (기간)
//	@Override
//	public List<TeethMeasureVO> selectUserTeethMeasureValue(SchoolTeethMeasureVO schoolTeethMeasureVO) throws Exception {
//		return teethMapper.selectUserTeethMeasureValue(schoolTeethMeasureVO);
//	}
	
	
	
//	// 진단 제목 조회
//	@Override
//	public String selectDiagTitle(@Param("diagCd") String diagCd) throws Exception{
//		return teethMapper.selectDiagTitle(diagCd);
//	}
	
	
}