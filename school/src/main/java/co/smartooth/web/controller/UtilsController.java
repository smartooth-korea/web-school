package co.smartooth.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import co.smartooth.web.service.OrganService;
import co.smartooth.web.service.TeethService;
import co.smartooth.web.vo.TeethMeasureVO;


/**
 * 작성자 : 정주현
 * 작성일 : 2022. 11. 28
 * 수정일 : 2023. 07. 28
 * 서버 분리 : 2023. 07. 28
 * @RestController를 쓰지 않는 이유는 몇 안되는 mapping에 jsp를 반환해줘야하는게 있으므로 @Controller를 사용한다.
 * @RestAPI로 반환해야할 경우 @ResponseBody를 사용하여 HashMap으로 return 해준다.
 */
@Controller
public class UtilsController {

	@Value("${loginUrl}")
	private String loginUrl;

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired(required = false)
	private TeethService teethService;

	@Autowired(required = false)
	private OrganService organService;


	
	
	/**
	 * 그래프 통계 일괄 계산
	 */
	@PostMapping(value = { "/web/statistics/graphBatch.do" })
	@ResponseBody
	public HashMap<String,Object> graphBatch(@RequestBody HashMap<String, Object> paramMap) throws Exception {

		String schoolCode = (String) paramMap.get("schoolCode");
		String measureDt = (String) paramMap.get("measureDt");

		// 진단 내용에 따른 기준 점수 조회 (하드코딩 되어 있으므로 조회하도록 변경해야함)
		double cavityCautionScore = 1;
		double cavityDangerScore = 4;
		double permCavityCautionScore = 1.5;
		double permCavityDangerScore = 6;

		HashMap<String, Object> hm = new HashMap<String, Object>();
		TeethMeasureVO teethMeasureVO = new TeethMeasureVO();

		// 회원들의 측정 값 통계 목록
		List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
		
		dataList = teethService.selectUserMeasureStatisticsList(schoolCode, measureDt);
		
		for (int i = 0; i < dataList.size(); i++) {
			// 측정 회원의 아이디
			String stUserId = (String) dataList.get(i).get("USER_ID");
			// 측정 회원 진단 태그 항목
			String diagCd = (String) dataList.get(i).get("DIAG_CD");
	
			// 유치 및 영구치 >> 주의(caution) 치아 및 충치(danger) 치아 개수
			int cavityCautionCnt = Integer.parseInt(dataList.get(i).get("CAVITY_CAUTION").toString());
			int cavityDangerCnt = Integer.parseInt(dataList.get(i).get("CAVITY_DANGER").toString());
			int permCavityCautionCnt = Integer.parseInt(dataList.get(i).get("PERM_CAVITY_CAUTION").toString());
			int permCavityDangerCnt = Integer.parseInt(dataList.get(i).get("PERM_CAVITY_DANGER").toString());
			// 악화지수 계산ㄴ
			double deteriorateScore = (cavityCautionCnt * cavityCautionScore) + (cavityDangerCnt * cavityDangerScore) + (permCavityCautionCnt * permCavityCautionScore) + (permCavityDangerCnt * permCavityDangerScore);
	
			// 충치 및 주의가 있을 경우 소견 점수는 포함되지 않는다.
			if (deteriorateScore == 0) {
				// 치태 및 치석이 존재 할 경우 (하드코딩)
				if (diagCd.contains("A:006:1")) {
					deteriorateScore = 0.3;
				}
			}
	
			teethMeasureVO.setUserId(stUserId);
			teethMeasureVO.setMeasureDt(measureDt);
			teethMeasureVO.setDeteriorateScore(deteriorateScore);
	
			// 악화 지수 업데이트
			teethService.updateUserDeteriorateScore(teethMeasureVO);
		}
		// 그래프 계산 일괄 작업 여부 업데이트 (Y)
		organService.updateGraphBatchedStatus(schoolCode);
		
		return hm;
	}
}
