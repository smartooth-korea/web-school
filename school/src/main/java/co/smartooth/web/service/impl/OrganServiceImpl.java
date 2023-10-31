package co.smartooth.web.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.smartooth.web.mapper.OrganMapper;
import co.smartooth.web.service.OrganService;
import co.smartooth.web.vo.OrganVO;


/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 07. 15
 * 수정일 : 2023. 08. 04
 * 서버분리 : 2023. 08. 01
 */
@Service
public class OrganServiceImpl implements OrganService{
	
	
	@Autowired(required = false)
	OrganMapper organMapper; 

	
	// 기관 실시간 검색
	@Override
	public List<Map<String, Object>> ajaxSelectOrganList(@Param("searchType") String searchType, @Param("searchData") String searchData) throws Exception {
		return organMapper.ajaxSelectOrganList(searchType, searchData);
	}
	
	
	
	// 기관 정보 조회
	@Override
	public OrganVO selectOrganInfo(@Param("schoolCode") String schoolCode) throws Exception {
		return organMapper.selectOrganInfo(schoolCode);
	}

	
	
	// 부서 목록 조회
	@Override
	public List<HashMap<String, Object>> selectDepartmentList(@Param("searchType") String searchType, @Param("searchData") String searchData) throws Exception {
		return organMapper.selectDepartmentList(searchType, searchData);
	}
	
	
	
	// 그래프 계산 일괄 작업 여부 업데이트 (Y)
	@Override
	public void updateGraphBatchedStatus(@Param("schoolCode") String schoolCode) throws Exception {
		organMapper.updateGraphBatchedStatus(schoolCode);
	}
	

	
	// 기관에 속한 피측정자 시퀀스 증가
	@Override
	public void updateOrganUserSeqNo(OrganVO webOrganVO) throws Exception {
		organMapper.updateOrganUserSeqNo(webOrganVO);
	}

	
	
	// 기관 치아 측정일 목록 조회 (기관코드)
	@Override
	public List<HashMap<String, Object>> selectOrganMeasureDtList(@Param("schoolCode") String schoolCode) throws Exception {
		return organMapper.selectOrganMeasureDtList(schoolCode);
	}

	
	
	// 부서 이름 조회
	@Override
	public String selectDepartmentNm(@Param("userId") String userId) throws Exception {
		return organMapper.selectDepartmentNm(userId);
	}
	
	
	

	
	
	
//	// 치과 병원 실시간 검색
//	@Override
//	public List<Map<String, Object>> ajaxSelectDentistList(@Param("searchType") String searchType, @Param("searchData") String searchData) throws Exception {
//		return organMapper.ajaxSelectDentistList(searchType, searchData);
//	}
	
	
	
//	// 치과 실시간 검색 (ajax)
//	@Override
//	public List<Map<String, Object>> ajaxSelectDentalHospitalList(@Param("searchType") String searchType, @Param("searchData")  String searchData) throws Exception {
//		return organMapper.ajaxSelectDentalHospitalList(searchType, searchData);
//	}

	
	
//	// 기관 정보 등록
//	@Override
//	public void insertOrganInfo(AdminOrganVO adminOrganVO) throws Exception {
//		adminOrganMapper.insertOrganInfo(adminOrganVO);
//	}

	
	
//	// 기관 정보 삭제
//	@Override
//	public void deleteOrganInfo(String schoolCode) throws Exception {
//		adminOrganMapper.deleteOrganInfo(schoolCode);
//	}

	
	
//	// 자문치과 등록
//	@Override
//	public void insertAdviceInfo(@Param("schoolCode") String schoolCode, @Param("dentistName") String dentistName) throws Exception{
//		adminOrganMapper.insertAdviceInfo(schoolCode, dentistName);
//	}
	
	
	
//	// 자문치과 조회
//	@Override
//	public AdviceVO selectAdviceDentistInfo(@Param("schoolCode") String schoolCode) throws Exception {
//		return organMapper.selectAdviceDentistInfo(schoolCode);
//	}
	

	

//	// 진단지 계산 일괄 작업 여부 확인
//	@Override
//	public String selectIsDiagnosisBatched(@Param("schoolCode") String schoolCode) throws Exception {
//		return webOrganMapper.selectIsDiagnosisBatched(schoolCode);
//	}


//	// 진단지 계산 일괄 작업 여부 업데이트 (Y)
//	@Override
//	public void updateDignosisBatchedStatus(@Param("schoolCode") String schoolCode) throws Exception {
//		webOrganMapper.updateDignosisBatchedStatus(schoolCode);
//	}
	
	
//	// 그래프 계산 일괄 작업 여부 확인
//	@Override
//	public String selectIsGraphBatched(@Param("schoolCode") String schoolCode) throws Exception {
//		return webOrganMapper.selectIsGraphBatched(schoolCode);
//	}
	

	
}