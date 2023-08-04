package co.smartooth.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import co.smartooth.web.service.DiagnosisService;
import co.smartooth.web.service.OrganService;
import co.smartooth.web.service.TeethService;
import co.smartooth.web.service.UserService;
// import co.smartooth.web.vo.AdviceVO;
import co.smartooth.web.vo.TeethMeasureVO;
import co.smartooth.web.vo.UserVO;

/**
 * 작성자 : 정주현
 * 작성일 : 2022. 11. 28
 * 수정일 : 2023. 08. 03
 * 서버분리 : 2023. 08. 01
 * @RestController를 쓰지 않는 이유는 몇 안되는 mapping에 jsp를 반환해줘야하는게 있으므로 @Controller를 사용한다.
 * @RestAPI로 반환해야할 경우 @ResponseBody를 사용하여 HashMap으로 return 해준다.
 */
@Controller
public class StatisticsController {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${loginUrl}")
	private String loginUrl;

	@Autowired(required = false)
	private UserService userService;

	@Autowired(required = false)
	private TeethService teethService;

	@Autowired(required = false)
	private DiagnosisService diagnosisService;

	@Autowired(required = false)
	private OrganService organService;

	
	
	/**
	 * 기능 : 결과 진단지 화면
	 * 작성자 : 정주현
	 * 작성일 : 2022. 11. 28
	 * 수정일 : 2023. 08. 03
	 */
	@GetMapping(value = { "/web/statistics/general/diagnosis" })
	public String webDiagnosis(HttpServletRequest request, HttpSession session, Model model) throws Exception {

		// 세션 값
		UserVO sessionVO = (UserVO) session.getAttribute("userInfo");
		if (sessionVO == null) {
			return "/web/login/schoolLoginForm";
		}
		return "redirect:/web/statistics/general/diagnosis.do";
		
	}
	
	
	
	/**
	 * 기능 : 그래프 화면
	 * 작성자 : 정주현
	 * 작성일 : 2022. 11. 28
	 * 수정일 : 2023. 08. 03
	 */
	@GetMapping(value = { "/web/statistics/graph" })
	public String webGraph(HttpServletRequest request, HttpSession session, Model model) throws Exception {

		// 세션 값
		UserVO sessionVO = (UserVO) session.getAttribute("userInfo");
		if (sessionVO == null) {
			return "/web/login/schoolLoginForm";
		}
		return "redirect:/web/statistics/graph.do";
		
	}
	
	
	
	/**
	 * 기능 : 결과 진단지 로직 - 학교,유치원,어린이집
	 * 작성자 : 정주현
	 * 작성일 : 2022. 11. 28
	 * 수정일 : 2023. 07. 24
	 *         
	 */
	@GetMapping(value = { "/web/statistics/general/diagnosis.do"})
	public String diagnosis(HttpServletRequest request, HttpSession session, Model model) throws Exception {

		// 세션 값
		UserVO sessionVO = (UserVO) session.getAttribute("userInfo");
//		// 언어팩
//		String lang = (String)model.getAttribute("lang");
//		// Url 타입
//		String urlType = (String) model.getAttribute("urlType");
//		if(lang == null || lang.equals("")) {
//			lang = "ko";
//		}
		
		String returnUrl = null;
		
		if (sessionVO == null) {
//			// 언어팩만 사용을 해야할 것
//			if(lang.equals("ko") && urlType.equals("statistics")) {
//				return "/web/statistics/login/statisticsLoginForm";
//			}else if(lang.equals("en")) {
//				return "web/statistics/login/statisticsLoginForm";
//			}
//			if(urlType.equals("dentist")) {
//				return "redirect:/dentist";
//			}
			return "/web/login/schoolLoginForm";
		}
		
		String paUserId = null;
		String userId = null;
		String userName = null;
		String teethType = null;
		// String userBirthday = null;
		String userType = sessionVO.getUserType();
		String schoolCode = sessionVO.getSchoolCode();
		
		UserVO webUserVO = new UserVO();
		

		// 법정대리인이 로그인한 경우
		if(userType.equals("PR")) {
			paUserId = sessionVO.getUserId();
			// 법정대리인 아이디로 피측정자 아이디 조회
			userId = userService.selectChUserId(paUserId);
			// 피측정자 정보 및 상세정보 조회
			webUserVO = userService.selectUserInfo(userId);
			// 치아 형태 : 유치원 : M
			teethType = webUserVO.getTeethType();
			userName = webUserVO.getUserName();
			// userBirthday = webUserVO.getUserBirthday();
		}else {
			// 본인이 직접 로그인한 경우
			userId = sessionVO.getUserId();
			webUserVO = userService.selectUserInfo(userId);
			teethType = webUserVO.getTeethType();
			userName = sessionVO.getUserName();
			// userBirthday = sessionVO.getUserBirthday();
		}

		// 최근 측정일
		String measureDt = null;
		// 유치 개수 20개
		int[] babyTeethValueArray = null;
		// 영구치 개수 8개
		int[] permTeethValueArray = null;
		// 영구치 어금니 개수 4개
		int[] permanentMolarsValueArray = null;
		// 임시 유치 배열 20개
		int[] tmpBabyTeethValueArray = null;
		
		// 충치단계 값(주의)
		Integer cautionLevel = 0;
		// 충치단계 값(위험)
		Integer dangerLevel = 0;
	
		// 정상 유치 개수
		int babyCvNormalCnt = 0;
		// 주의 유치 개수
		int babyCvCautionCnt = 0;
		// 충치 유치 개수
		int babyCvDangerCnt = 0;
	
		// 정상 영구치 개수
		int pmCvNomalCnt = 0;
		// 주의 영구치 개수
		int pmCvCautionCnt = 0;
		// 충치 영구치 개수
		int pmCvDangerCnt = 0;
	
		// 유치+영구치 개수
		int sheetNormalCnt = 0;
		int sheetCautionCnt = 0;
		int sheetdangerCnt = 0;
		
		// 진단 코드
		String userDiagCd = null;
		// 진단 코드 분리한 배열
		String[] diagCdArray;
		// 진단 코드 문자열
		String diagCdStr = null;
		// 진단 코드 별 제목 배열
		String[] diagCdTitleArray;
		// 진단 코드 설명
		String diagDescript = "";
		// 진단 코드 설명 업데이트 여부
		String diagDescriptFl = "";
		// 진단 코드 카운트
		int isDiagCnt = 0;
	
		// 결제 정보에 따라서 보여지는 양을 다르게 조정 :: 기본 값은 3
		int limit = 100;
		
		TeethMeasureVO webTeethMeasureVO = new TeethMeasureVO();
		// AdviceVO webAdviceVO = new AdviceVO();
		List<String> measureDtList = new ArrayList<String>();
		
		try {
			// 진단 결과
			// 치아형태 - M(유치원)
			if(teethType.equals("M")) {
			
				// 유치 개수 20개
				babyTeethValueArray = new int[20];
				// 영구치 개수 12개
				permTeethValueArray = new int[8];
				// 영구치 어금니 4개
				permanentMolarsValueArray = new int[4];
				// 영구치 위치에 해당하는 임시 유치 배열
				tmpBabyTeethValueArray = new int[8];

				// CAVITY_LEVEL 분류 부분 - 충치 단계별 수치 조회
				HashMap<String, Integer> cavityLevel = teethService.selectCavityLevel();
				
				// 치아 측정 VO
				webTeethMeasureVO.setUserId(userId);
				// 측정 값 보이는 개수
				webTeethMeasureVO.setLimit(limit);
				// 측정일 목록 조회
				measureDtList = teethService.selectUserMeasureDtList(webTeethMeasureVO);

				if( measureDtList.size() > 0) {
					// 최근 측정일
					measureDt = measureDtList.get(0);
				}else {
//					if(lang.equals("en")) {
//						// 측정일, 측정인원이 없을 경우
//						model.addAttribute("msg", "No measurement records.");
//						model.addAttribute("loginUrl", loginUrl+"/web/logout.do");
//						return "/common/alertMessage";
//					}else if(lang.equals("ko")){
//						// 측정일, 측정인원이 없을 경우
//						model.addAttribute("msg", "측정 기록이 없습니다.");
//						model.addAttribute("loginUrl", loginUrl+"/web/logout.do");
//						return "/common/alertMessage";
//					}
					// 측정일, 측정인원이 없을 경우
					if(userType.equals("PR") || userType.equals("ST") ){
						loginUrl = loginUrl+"/web/statistics/logout.do";
					}else if(userType.equals("DI")){
						loginUrl =  loginUrl+"/web/director/logout.do";
					}
					
					model.addAttribute("loginUrl", loginUrl);
					model.addAttribute("msg", "측정 기록이 없습니다.");
					return "/common/alertMessage";
					
				}
				
				// 피측정자 최근 치아 측정 값 조회
				webTeethMeasureVO = teethService.selectUserMeasureValue(userId, measureDt);
				// 관리자 측 수정 여부 확인
				diagDescriptFl = webTeethMeasureVO.getDiagDescriptFl();
		
				// 치아 측정 값 배열 (혼합치 배열) 
				// 변경전 유치 배열 34~53까지 20개
				babyTeethValueArray[0] = webTeethMeasureVO.getT34();
				babyTeethValueArray[1] = webTeethMeasureVO.getT35();
				babyTeethValueArray[2] = webTeethMeasureVO.getT36();
				// 영구치 T07
				babyTeethValueArray[3] = webTeethMeasureVO.getT37();
				// 영구치 T08
				babyTeethValueArray[4] = webTeethMeasureVO.getT38();
				// 영구치 T09
				babyTeethValueArray[5] = webTeethMeasureVO.getT39();
				// 영구치 T10
				babyTeethValueArray[6] = webTeethMeasureVO.getT40();
				babyTeethValueArray[7] = webTeethMeasureVO.getT41();
				babyTeethValueArray[8] = webTeethMeasureVO.getT42();
				babyTeethValueArray[9] = webTeethMeasureVO.getT43();
				babyTeethValueArray[10] = webTeethMeasureVO.getT46();
				babyTeethValueArray[11] = webTeethMeasureVO.getT47();
				babyTeethValueArray[12] = webTeethMeasureVO.getT48();
				// 영구치 T23
				babyTeethValueArray[13] = webTeethMeasureVO.getT49();
				// 영구치 T24
				babyTeethValueArray[14] = webTeethMeasureVO.getT50();
				// 영구치 T25
				babyTeethValueArray[15] = webTeethMeasureVO.getT51();
				// 영구치 T26
				babyTeethValueArray[16] = webTeethMeasureVO.getT52();
				babyTeethValueArray[17] = webTeethMeasureVO.getT53();
				babyTeethValueArray[18] = webTeethMeasureVO.getT54();
				babyTeethValueArray[19] = webTeethMeasureVO.getT55();
		
				// 영구치 어금니 - 16, 26, 36, 46
				permanentMolarsValueArray[0] = webTeethMeasureVO.getT33();
				permanentMolarsValueArray[1] = webTeethMeasureVO.getT44();
				permanentMolarsValueArray[2] = webTeethMeasureVO.getT45();
				permanentMolarsValueArray[3] = webTeethMeasureVO.getT56();
				
				// 영구치 상악
				permTeethValueArray[0] = webTeethMeasureVO.getT07(); 
				permTeethValueArray[1] = webTeethMeasureVO.getT08(); 
				permTeethValueArray[2] = webTeethMeasureVO.getT09(); 
				permTeethValueArray[3] = webTeethMeasureVO.getT10(); 
				
				// 영구치 하악
				permTeethValueArray[4] = webTeethMeasureVO.getT23(); 
				permTeethValueArray[5] = webTeethMeasureVO.getT24(); 
				permTeethValueArray[6] = webTeethMeasureVO.getT25(); 
				permTeethValueArray[7] = webTeethMeasureVO.getT26(); 
				
				// 갯수 카운팅을 위한 임시 배열
				tmpBabyTeethValueArray[0] = webTeethMeasureVO.getT37();
				tmpBabyTeethValueArray[1] = webTeethMeasureVO.getT38();
				tmpBabyTeethValueArray[2] = webTeethMeasureVO.getT39();
				tmpBabyTeethValueArray[3] = webTeethMeasureVO.getT40();
				tmpBabyTeethValueArray[4] = webTeethMeasureVO.getT49();
				tmpBabyTeethValueArray[5] = webTeethMeasureVO.getT50();
				tmpBabyTeethValueArray[6] = webTeethMeasureVO.getT51();
				tmpBabyTeethValueArray[7] = webTeethMeasureVO.getT52();
				
				// 피측정자 진단 코드 조회
				userDiagCd = webTeethMeasureVO.getDiagCd();
				
				// 충치 단계 조회 (주의, 충치)
				cautionLevel = Integer.parseInt(String.valueOf(cavityLevel.get("CAVITY_CAUTION")));
				dangerLevel = Integer.parseInt(String.valueOf(cavityLevel.get("CAVITY_DANGER")));
				
				
				/** 유치 정상, 주의, 충치 개수 저장 **/
				for (int i = 0; i < babyTeethValueArray.length; i++) { // 측정자가 입력한 주의나 충치 값의 -1000
					if (babyTeethValueArray[i] > 1000) {
						babyTeethValueArray[i] = (int) babyTeethValueArray[i] - 1000;
					}
					if (babyTeethValueArray[i] < cautionLevel) { // 정상 치아는 -99이상 체크
						babyCvNormalCnt++;
					} else if (babyTeethValueArray[i] >= cautionLevel && babyTeethValueArray[i] < dangerLevel) {
						babyCvCautionCnt++;
					} else if (babyTeethValueArray[i] >= dangerLevel) {
						babyCvDangerCnt++;
					}
				}
				
				/** 영구치 상악 하악 정상, 주의, 충치 개수 저장 **/
				for (int i = 0; i < permTeethValueArray.length; i++) { // 측정자가 입력한 주의나 충치 값의 -1000
					if (permTeethValueArray[i] > 1000) {
						permTeethValueArray[i] = (int) permTeethValueArray[i] - 1000;
					}
					if (permTeethValueArray[i] >= cautionLevel && permTeethValueArray[i] < dangerLevel) {
						pmCvCautionCnt++;
					} else if (permTeethValueArray[i] >= dangerLevel) {
						pmCvDangerCnt++;
					}
				}
				
				for(int i=0; i < tmpBabyTeethValueArray.length; i++) {
					if (tmpBabyTeethValueArray[i] > 1000) {
						tmpBabyTeethValueArray[i] = (int) tmpBabyTeethValueArray[i] - 1000;
					}	
				}
				
				/** 영구치와 유치 두 개 다 값이 있을 경우  **/
				for(int i=0; i<8; i++) {
					if(permTeethValueArray[i] > 0 && tmpBabyTeethValueArray[i] > 0) {
						
						if(tmpBabyTeethValueArray[i] >= cautionLevel && tmpBabyTeethValueArray[i] < dangerLevel) { // 유치의 값이 주의 단계와 같거나 크고 주의 단계보다 작을 때 :: 범위는 주의 단계 이므로 유치의 주의 단계의 개수를 차감
							babyCvCautionCnt--;
						}else if(tmpBabyTeethValueArray[i]  > dangerLevel) { // 유치의 값이 위험 단계보다 클때 :: 범위는 위험 단계 이므로 유치의 위험 단계의 개수를 차감
							babyCvDangerCnt--;
						}
					}

					// 영구치 측정 값이 있고 유치 측정값이 0이거나 -99일때 정상치아 갯수를 -1해준다
					if(permTeethValueArray[i] > 0 && tmpBabyTeethValueArray[i] <=0) {
						babyCvNormalCnt--;
					}
				}
				
				/** 영구치 어금니 정상, 주의, 충치 개수 저장 **/
				for (int i = 0; i < permanentMolarsValueArray.length; i++) { // 측정자가 입력한 주의나 충치 값의 -1000
					if (permanentMolarsValueArray[i] > 1000) {
						permanentMolarsValueArray[i] = (int) permanentMolarsValueArray[i] - 1000;
					}
					if (permanentMolarsValueArray[i] >= cautionLevel && permanentMolarsValueArray[i] < dangerLevel) {
						pmCvCautionCnt++;
					} else if (permanentMolarsValueArray[i] >= dangerLevel) {
						pmCvDangerCnt++;
					}
				}
				
				// 관리자 수정 여부 확인
				if (diagDescriptFl.equals("N")) {
					if (userDiagCd != null && !"".equals(userDiagCd)) {
						diagCdArray = userDiagCd.split("\\|");
						List<String> list = new ArrayList<String>();
						diagDescript = "";
						for (int i = 0; i < diagCdArray.length; i++) {
							String[] diagCdStrArray = diagCdArray[i].split(":");
							if ("1".equals(diagCdStrArray[2])) {
								diagCdStr = diagCdStrArray[0] + diagCdStrArray[1];
								list.add(diagCdStr);
								if(diagCdStr.startsWith("A00") || diagCdStr.equals("B006") ||diagCdStr.equals("E003")) {
									list.add("GA1");
								}
								// 진단 코드 존재 유무
								isDiagCnt++;
							}
						}
						// GA1 중복제거
						list = list.stream().distinct().collect(Collectors.toList());
						if(list.contains("GA1")){
							// 중복 제거 후 마지막에 다시 붙여주는 작업
							list.remove("GA1");
							list.add("GA1");
						}
						
						for(int i=0; i<list.size(); i++) {
							diagDescript += "&nbsp;" + diagnosisService.selectDiagDescript(list.get(i), teethType);
						}

						// 진단 키워드를 누르지 않으면 카운팅이 없으므로 이상없음
						if (isDiagCnt == 0) { // 하드코딩
							String diagTitle = diagnosisService.selectDiagTitle("", teethType);
							userDiagCd = userDiagCd.replaceAll("E:003:0", "E:003:1");
							diagDescript += "&nbsp;" + diagnosisService.selectDiagDescript("E003", teethType) + "<br/>";
							diagDescript = diagDescript.replaceAll("			", "");
							webTeethMeasureVO.setDiagTitle(diagTitle);
							webTeethMeasureVO.setDiagCd(userDiagCd);
						}
						diagDescript = diagDescript.replaceAll("			", "");
						webTeethMeasureVO.setDiagDescript(diagDescript);
					}
				}else {
					diagDescript = webTeethMeasureVO.getDiagDescript();
				}
				
				if(diagDescriptFl.equals("N")) {
					//진단 설명 : 진단 코드로 진단명을 검색하여 진단에 대한 설명 등록
					if (userDiagCd != null && !"".equals(userDiagCd)) {
						diagCdTitleArray = userDiagCd.split("\\|");
						for (int i = 0; i < diagCdTitleArray.length; i++) {
							String diagTitle = diagnosisService.selectDiagTitle(diagCdTitleArray[i], teethType);
							if (diagTitle != null) {
								webTeethMeasureVO.setDiagTitle(diagTitle);
								break;
							}
						}
						webTeethMeasureVO.setDiagCd(userDiagCd);
					}
				}
		
				// 진단 화면 표시 유치 정상 개수
				sheetNormalCnt = babyCvNormalCnt + pmCvNomalCnt;
				// 진단 화면 표시 유치 주의 개수
				sheetCautionCnt = babyCvCautionCnt + pmCvCautionCnt;
				// 진단 화면 표시 유치 위험 개수
				sheetdangerCnt = babyCvDangerCnt + pmCvDangerCnt;
		
				// 자녀 이름 등록
				webTeethMeasureVO.setUserName(userName);
		
				// 유치 정상, 주의, 충치 개수 입력
				webTeethMeasureVO.setCavityNormal(babyCvNormalCnt);
				webTeethMeasureVO.setCavityCaution(babyCvCautionCnt);
				webTeethMeasureVO.setCavityDanger(babyCvDangerCnt);
				// 영구치 정상, 주의, 충치 개수 입력
				webTeethMeasureVO.setPermCavityNormal(pmCvNomalCnt);
				webTeethMeasureVO.setPermCavityCaution(pmCvCautionCnt);
				webTeethMeasureVO.setPermCavityDanger(pmCvDangerCnt);
		
				// 진단 상세 정보 업데이트
				teethService.updateDiagDescript(userId, measureDt, diagDescript);
				// ST_STUDENT_USER_DETAIL 테이블에 CavityCnt 업데이트
				teethService.updateUserCavityCntByMeasureDt(webTeethMeasureVO);
				
				returnUrl =  "/web/statistics/general/diagnosis_main";
		
			}
//			else if(teethType.equals("B")) {
//				// 성인용 치아의 결과 진단지는 작업중입니다.
//				//model.addAttribute("urlType", urlType);
//				return "/web/statistics/diagnosis_main_adult";
//			}else if(teethType.equals("P")) {
//				// 성인용 치아의 결과 진단지는 작업중입니다.
//				//model.addAttribute("urlType", urlType);
//				return "/web/statistics/diagnosis_main_adult";
//			}
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		//model.addAttribute("urlType", urlType);
		model.addAttribute("dataList", webTeethMeasureVO);
		model.addAttribute("schoolCode", schoolCode);
		model.addAttribute("measureDtList", measureDtList);
		model.addAttribute("cautionLevel", cautionLevel);
		model.addAttribute("dangerLevel", dangerLevel);
		model.addAttribute("cavityNormal", sheetNormalCnt);
		model.addAttribute("cavityCaution", sheetCautionCnt);
		model.addAttribute("cavityDanger", sheetdangerCnt);
		
		
		// 현재 자문치과 조회는 주석 처리
		// 자문 치과 조회
//		try {
//			webAdviceVO = webOrganService.selectAdviceDentistInfo(schoolCode);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		if (webAdviceVO != null) { // 자문 치과가 있을 경우
//			model.addAttribute("adviceDentistInfo", webAdviceVO);
//			returnUrl = "/web/statistics/diagnosis_advice";
//		} 
		
//		if(lang.equals("en")) {
//			returnUrl = "/web/statistics/diagnosis_main_en";
//		}else if(lang.equals("ko")) {
//			returnUrl =  "/web/statistics/diagnosis_main";
//		}
		
		return returnUrl; 	   
		
	}

	
	
	/**
	 * 기능 : 진단지 조회 (측정일 선택 시 작동)
	 * 작성자 : 정주현
	 * 작성일 : 2022. 11. 28
	 * 수정일 : 2023. 08. 03
	 */
	@PostMapping(value = { "/web/statistics/ajaxDiagnosis"})
	@ResponseBody
	public HashMap<String, Object> ajaxDiagnosis(HttpServletRequest request, HttpSession session, Model model, @RequestBody Map<String, String> paramMap) throws Exception {

		// 리턴 맵
		HashMap<String, Object> hm = new HashMap<String, Object>();
		// 세션 값
		UserVO sessionVO = (UserVO) session.getAttribute("userInfo");
//		// 언어팩
//		String lang = (String)model.getAttribute("lang");
//		// Url 타입
//		String urlType = (String) model.getAttribute("urlType");
//		if(lang == null || lang.equals("")) {
//			lang = "ko";
//		}
		
		if(sessionVO == null) {
			// 세션이 끊겼을 때 code 999를 사용
			hm.put("code", "999");
			return hm;
		}
		
		// 법정대리인 아이디
		String paUserId = null;
		// 피측정자 아이디
		String userId = null;
		// 피측정자 이름
		String userName = null;
		// 피측정자 치아 형태
		String teethType = null;
		// 피측정자 회원 타입
		String userType = sessionVO.getUserType();
		// 기관 코드
		String schoolCode = sessionVO.getSchoolCode();
		// 측정일
		String measureDt = paramMap.get("measureDt");
		
		UserVO userVO = new UserVO();
		

		// 법정대리인이 로그인한 경우
		if(userType.equals("PR")) {
			paUserId = sessionVO.getUserId();
			// 법정대리인 아이디로 피측정자 아이디 조회
			userId = userService.selectChUserId(paUserId);
			// 피측정자 정보 및 상세정보 조회
			userVO = userService.selectUserInfo(userId);
			// 치아 형태 : 유치원 : M
			teethType = userVO.getTeethType();
			userName = userVO.getUserName();
		}else if(userType.equals("DI")){
			userId = paramMap.get("userId");
			userVO = userService.selectUserInfo(userId);
			teethType = userVO.getTeethType();
			userName = userVO.getUserName();
		}else {
			// 본인이 직접 로그인한 경우
			userId = sessionVO.getUserId();
			userVO = userService.selectUserInfo(userId);
			teethType = userVO.getTeethType();
			userName = sessionVO.getUserName();
			
		}

		// 유치 개수 20개
		int[] babyTeethValueArray = null;
		// 영구치 개수 8개
		int[] permTeethValueArray = null;
		// 영구치 어금니 개수 4개
		int[] permanentMolarsValueArray = null;
		// 임시 유치 배열 20개
		int[] tmpBabyTeethValueArray = null;
		
		// 충치단계 값(주의)
		Integer cautionLevel = 0;
		// 충치단계 값(위험)
		Integer dangerLevel = 0;
	
		// 정상 유치 개수
		int babyCvNormalCnt = 0;
		// 주의 유치 개수
		int babyCvCautionCnt = 0;
		// 충치 유치 개수
		int babyCvDangerCnt = 0;
	
		// 정상 영구치 개수
		int pmCvNomalCnt = 0;
		// 주의 영구치 개수
		int pmCvCautionCnt = 0;
		// 충치 영구치 개수
		int pmCvDangerCnt = 0;
	
		// 유치+영구치 개수
		int sheetNormalCnt = 0;
		int sheetCautionCnt = 0;
		int sheetdangerCnt = 0;
		
		// 진단 코드
		String userDiagCd = null;
		// 진단 코드 분리한 배열
		String[] diagCdArray;
		// 진단 코드 문자열
		String diagCdStr = null;
		// 진단 코드 별 제목 배열
		String[] diagCdTitleArray;
		// 진단 코드 설명
		String diagDescript = "";
		// 진단 코드 설명 업데이트 여부
		String diagDescriptFl = "";
		// 진단 코드 카운트
		int isDiagCnt = 0;
	
		// 결제 정보에 따라서 보여지는 양을 다르게 조정 :: 기본 값은 3
		int limit = 100;
		
		TeethMeasureVO webTeethMeasureVO = new TeethMeasureVO();
		List<String> measureDtList = new ArrayList<String>();
		
		// 진단 결과지 
		// 치아 형태 - M(유치원)
		if(teethType.equals("M")) {
			try {
				// 유치 개수 20개
				babyTeethValueArray = new int[20];
				// 영구치 개수 12개
				permTeethValueArray = new int[8];
				// 영구치 어금니 4개
				permanentMolarsValueArray = new int[4];
				// 영구치 위치에 해당하는 임시 유치 배열
				tmpBabyTeethValueArray = new int[8];

				// CAVITY_LEVEL 분류 부분 - 충치 단계별 수치 조회
				HashMap<String, Integer> cavityLevel = teethService.selectCavityLevel();
				
				// 치아 측정 VO
				webTeethMeasureVO.setUserId(userId);
				// 측정 값 보이는 개수
				webTeethMeasureVO.setLimit(limit);
				
				// 피측정자 최근 치아 측정 값 조회
				webTeethMeasureVO = teethService.selectUserMeasureValue(userId, measureDt);
				
				diagDescriptFl = webTeethMeasureVO.getDiagDescriptFl();
		
				babyTeethValueArray[0] = webTeethMeasureVO.getT34();
				babyTeethValueArray[1] = webTeethMeasureVO.getT35();
				babyTeethValueArray[2] = webTeethMeasureVO.getT36();
				// 영구치 T07
				babyTeethValueArray[3] = webTeethMeasureVO.getT37();
				// 영구치 T08
				babyTeethValueArray[4] = webTeethMeasureVO.getT38();
				// 영구치 T09
				babyTeethValueArray[5] = webTeethMeasureVO.getT39();
				// 영구치 T10
				babyTeethValueArray[6] = webTeethMeasureVO.getT40();
				babyTeethValueArray[7] = webTeethMeasureVO.getT41();
				babyTeethValueArray[8] = webTeethMeasureVO.getT42();
				babyTeethValueArray[9] = webTeethMeasureVO.getT43();
				babyTeethValueArray[10] = webTeethMeasureVO.getT46();
				babyTeethValueArray[11] = webTeethMeasureVO.getT47();
				babyTeethValueArray[12] = webTeethMeasureVO.getT48();
				// 영구치 T23
				babyTeethValueArray[13] = webTeethMeasureVO.getT49();
				// 영구치 T24
				babyTeethValueArray[14] = webTeethMeasureVO.getT50();
				// 영구치 T25
				babyTeethValueArray[15] = webTeethMeasureVO.getT51();
				// 영구치 T26
				babyTeethValueArray[16] = webTeethMeasureVO.getT52();
				babyTeethValueArray[17] = webTeethMeasureVO.getT53();
				babyTeethValueArray[18] = webTeethMeasureVO.getT54();
				babyTeethValueArray[19] = webTeethMeasureVO.getT55();
		
				// 영구치 어금니 - 16, 26, 36, 46
				permanentMolarsValueArray[0] = webTeethMeasureVO.getT33();
				permanentMolarsValueArray[1] = webTeethMeasureVO.getT44();
				permanentMolarsValueArray[2] = webTeethMeasureVO.getT45();
				permanentMolarsValueArray[3] = webTeethMeasureVO.getT56();
				
				// 영구치 상악
				permTeethValueArray[0] = webTeethMeasureVO.getT07(); 
				permTeethValueArray[1] = webTeethMeasureVO.getT08(); 
				permTeethValueArray[2] = webTeethMeasureVO.getT09(); 
				permTeethValueArray[3] = webTeethMeasureVO.getT10(); 

				// 영구치 하악
				permTeethValueArray[4] = webTeethMeasureVO.getT23(); 
				permTeethValueArray[5] = webTeethMeasureVO.getT24(); 
				permTeethValueArray[6] = webTeethMeasureVO.getT25(); 
				permTeethValueArray[7] = webTeethMeasureVO.getT26(); 
				
				// 갯수 카운팅을 위한 임시 배열
				tmpBabyTeethValueArray[0] = webTeethMeasureVO.getT37();
				tmpBabyTeethValueArray[1] = webTeethMeasureVO.getT38();
				tmpBabyTeethValueArray[2] = webTeethMeasureVO.getT39();
				tmpBabyTeethValueArray[3] = webTeethMeasureVO.getT40();
				tmpBabyTeethValueArray[4] = webTeethMeasureVO.getT49();
				tmpBabyTeethValueArray[5] = webTeethMeasureVO.getT50();
				tmpBabyTeethValueArray[6] = webTeethMeasureVO.getT51();
				tmpBabyTeethValueArray[7] = webTeethMeasureVO.getT52();
				
				// 피측정자 진단 코드 조회
				userDiagCd = webTeethMeasureVO.getDiagCd();
				
				// 충치 단계 조회 (주의, 충치)
				cautionLevel = Integer.parseInt(String.valueOf(cavityLevel.get("CAVITY_CAUTION")));
				dangerLevel = Integer.parseInt(String.valueOf(cavityLevel.get("CAVITY_DANGER")));
				
				
				/** 유치 정상, 주의, 충치 개수 저장 **/
				for (int i = 0; i < babyTeethValueArray.length; i++) { // 측정자가 입력한 주의나 충치 값의 -1000
					if (babyTeethValueArray[i] > 1000) {
						babyTeethValueArray[i] = (int) babyTeethValueArray[i] - 1000;
					}
					if (babyTeethValueArray[i] < cautionLevel) { // 정상 치아는 -99이상 체크
						babyCvNormalCnt++;
					} else if (babyTeethValueArray[i] >= cautionLevel && babyTeethValueArray[i] < dangerLevel) {
						babyCvCautionCnt++;
					} else if (babyTeethValueArray[i] >= dangerLevel) {
						babyCvDangerCnt++;
					}
				}
				
				/** 영구치 상악 하악 정상, 주의, 충치 개수 저장 **/
				for (int i = 0; i < permTeethValueArray.length; i++) { // 측정자가 입력한 주의나 충치 값의 -1000
					if (permTeethValueArray[i] > 1000) {
						permTeethValueArray[i] = (int) permTeethValueArray[i] - 1000;
					}
					if (permTeethValueArray[i] >= cautionLevel && permTeethValueArray[i] < dangerLevel) {
						pmCvCautionCnt++;
					} else if (permTeethValueArray[i] >= dangerLevel) {
						pmCvDangerCnt++;
					}
				}
				
				for(int i=0; i < tmpBabyTeethValueArray.length; i++) {
					if (tmpBabyTeethValueArray[i] > 1000) {
						tmpBabyTeethValueArray[i] = (int) tmpBabyTeethValueArray[i] - 1000;
					}	
				}
				
				/** 영구치와 유치 두 개 다 값이 있을 경우  **/
				for(int i=0; i<8; i++) {
					if(permTeethValueArray[i] > 0 && tmpBabyTeethValueArray[i] > 0) {
						if(tmpBabyTeethValueArray[i] >= cautionLevel && tmpBabyTeethValueArray[i] < dangerLevel) { // 유치의 값이 주의 단계와 같거나 크고 주의 단계보다 작을 때 :: 범위는 주의 단계 이므로 유치의 주의 단계의 개수를 차감
							babyCvCautionCnt--;
						}else if(tmpBabyTeethValueArray[i]  > dangerLevel) { // 유치의 값이 위험 단계보다 클때 :: 범위는 위험 단계 이므로 유치의 위험 단계의 개수를 차감
							babyCvDangerCnt--;
						}
					}
					// 영구치 측정 값이 있고 유치 측정값이 0이거나 -99일때 정상치아 갯수를 -1해준다
					if(permTeethValueArray[i] > 0 && tmpBabyTeethValueArray[i] <=0) {
						babyCvNormalCnt--;
					}
				}
				
				/** 영구치 어금니 정상, 주의, 충치 개수 저장 **/
				for (int i = 0; i < permanentMolarsValueArray.length; i++) { // 측정자가 입력한 주의나 충치 값의 -1000
					if (permanentMolarsValueArray[i] > 1000) {
						permanentMolarsValueArray[i] = (int) permanentMolarsValueArray[i] - 1000;
					}
					if (permanentMolarsValueArray[i] >= cautionLevel && permanentMolarsValueArray[i] < dangerLevel) {
						pmCvCautionCnt++;
					} else if (permanentMolarsValueArray[i] >= dangerLevel) {
						pmCvDangerCnt++;
					}
				}
				
				if (diagDescriptFl.equals("N")) {
					if (userDiagCd != null && !"".equals(userDiagCd)) {
						diagCdArray = userDiagCd.split("\\|");
						List<String> list = new ArrayList<String>();
						diagDescript = "";
						for (int i = 0; i < diagCdArray.length; i++) {
							String[] diagCdStrArray = diagCdArray[i].split(":");
							if ("1".equals(diagCdStrArray[2])) {
								diagCdStr = diagCdStrArray[0] + diagCdStrArray[1];
								list.add(diagCdStr);
								if(diagCdStr.startsWith("A00") || diagCdStr.equals("B006") ||diagCdStr.equals("E003")) {
									list.add("GA1");
								}
								// 진단 코드 존재 유무
								isDiagCnt++;
							}
						}
						// GA1 중복제거
						list = list.stream().distinct().collect(Collectors.toList());
						if(list.contains("GA1")){
							// 중복 제거 후 마지막에 다시 붙여주는 작업
							list.remove("GA1");
							list.add("GA1");
						}
						
						for(int i=0; i<list.size(); i++) {
							diagDescript += "&nbsp;" + diagnosisService.selectDiagDescript(list.get(i), teethType);
						}
					
						// 진단 키워드를 누르지 않으면 카운팅이 없으므로 이상없음
						if (isDiagCnt == 0) { // 하드코딩
							String diagTitle = diagnosisService.selectDiagTitle("", teethType);
							userDiagCd = userDiagCd.replaceAll("E:003:0", "E:003:1");
							diagDescript += "&nbsp;" + diagnosisService.selectDiagDescript("E003", teethType) + "<br/>";
							diagDescript = diagDescript.replaceAll("			", "");
							webTeethMeasureVO.setDiagTitle(diagTitle);
							webTeethMeasureVO.setDiagCd(userDiagCd);
						}
						diagDescript = diagDescript.replaceAll("			", "");
						webTeethMeasureVO.setDiagDescript(diagDescript);
					}
				}else {
					diagDescript = webTeethMeasureVO.getDiagDescript();
				}
				
				
				if(diagDescriptFl.equals("N")) {
					//진단 설명 : 진단 코드로 진단명을 검색하여 진단에 대한 설명 등록
					if (userDiagCd != null && !"".equals(userDiagCd)) {
						diagCdTitleArray = userDiagCd.split("\\|");
						for (int i = 0; i < diagCdTitleArray.length; i++) {
							String diagTitle = diagnosisService.selectDiagTitle(diagCdTitleArray[i], teethType);
							if (diagTitle != null) {
								webTeethMeasureVO.setDiagTitle(diagTitle);
								break;
							}
						}
						webTeethMeasureVO.setDiagCd(userDiagCd);
					}
				}
		
				// 진단 화면 표시 유치 정상 개수
				sheetNormalCnt = babyCvNormalCnt + pmCvNomalCnt;
				// 진단 화면 표시 유치 주의 개수
				sheetCautionCnt = babyCvCautionCnt + pmCvCautionCnt;
				// 진단 화면 표시 유치 위험 개수
				sheetdangerCnt = babyCvDangerCnt + pmCvDangerCnt;
		
				// 자녀 이름 등록
				webTeethMeasureVO.setUserName(userName);
		
				// 유치 정상, 주의, 충치 개수 입력
				webTeethMeasureVO.setCavityNormal(babyCvNormalCnt);
				webTeethMeasureVO.setCavityCaution(babyCvCautionCnt);
				webTeethMeasureVO.setCavityDanger(babyCvDangerCnt);
				// 영구치 정상, 주의, 충치 개수 입력
				webTeethMeasureVO.setPermCavityNormal(pmCvNomalCnt);
				webTeethMeasureVO.setPermCavityCaution(pmCvCautionCnt);
				webTeethMeasureVO.setPermCavityDanger(pmCvDangerCnt);
		
				// 진단 상세 정보 업데이트
				teethService.updateDiagDescript(userId, measureDt, diagDescript);
				// ST_STUDENT_USER_DETAIL 테이블에 CavityCnt 업데이트
				teethService.updateUserCavityCntByMeasureDt(webTeethMeasureVO);

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
		
		hm.put("code", "000");

		hm.put("dataList", webTeethMeasureVO);
		hm.put("schoolCode", schoolCode);
		hm.put("measureDtList", measureDtList);
		hm.put("cautionLevel", cautionLevel);
		hm.put("dangerLevel", dangerLevel);
		hm.put("cavityNormal", sheetNormalCnt);
		hm.put("cavityCaution", sheetCautionCnt);
		hm.put("cavityDanger", sheetdangerCnt);

		return hm;

	}
	

	
	/**
	 * 기능 : 그래프 조회
	 * 작성자 : 정주현
	 * 작성일 : 2022. 11. 28
	 * 수정일 : 2023. 08. 03
	 */
	@GetMapping(value = { "/web/statistics/graph.do" })
	// location.href로 요청이 들어옴
	public String graph(HttpServletRequest request, HttpSession session, Model model) {

		// 세션 값
		UserVO sessionVO = (UserVO) session.getAttribute("userInfo");
		String returnUrl = null;
//		// 언어팩
//		String lang = (String)session.getAttribute("lang");
		if (sessionVO == null) {
//			if(lang.equals("ko")) {
				return "/web/login/schoolLoginForm";
//			}else if(lang.equals("en")) {
//				return "web/statistics/login/statisticsLoginForm";
//			}
		}
		 
		try {

			String paUserId = null;
			String userId = null;
			String userName = null;
			String userType = sessionVO.getUserType();
			String schoolCode = sessionVO.getSchoolCode();
			
			if(userType.equals("PR")) {
				paUserId = sessionVO.getUserId();
				// 법정대리인 아이디로 피측정자 아이디 조회
				userId = userService.selectChUserId(paUserId);
				// 법정대리인 아이디로 피측정자 이름 조회
				userName = userService.selectChUserName(userId);
			}else {
				userId = sessionVO.getUserId();
				userName = sessionVO.getUserName();
			}
			
			String measureDt = null;
			String schoolName = null;

			double deteriorateMaxScore = 0;

			// 결제 정보에 따라서 보여지는 양을 다르게 조정 :: 기본 값은 3
			int limit = 100;

			TeethMeasureVO webTeethMeasureVO = new TeethMeasureVO();
			List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
			List<String> measureDtList = new ArrayList<String>();
			List<Double> deteriorateScordList = new ArrayList<Double>();
			List<Integer> cavityCntList = new ArrayList<Integer>();
			List<String> userNameList = new ArrayList<String>();
			List<Double> userFearScoreList = new ArrayList<Double>();

			// 기관 코드
			schoolCode = sessionVO.getSchoolCode();
			// 치아 측정 VO
			webTeethMeasureVO.setUserId(userId);
			webTeethMeasureVO.setLimit(limit);
			// 피측정자 치아 측정일 목록
			measureDtList = teethService.selectUserMeasureDtList(webTeethMeasureVO);
			// 최근 측정일
			measureDt = measureDtList.get(0);
			// 기관 내 피측정자 데이터 통계 작업 후 조회
			dataList = teethService.selectUserMeasureStatisticsList(schoolCode, measureDt);
			// 기관명 
			schoolName = (String)dataList.get(0).get("SCHOOL_NAME");
			
			for (int i = 0; i < dataList.size(); i++) {
				// 악화 지수 초기화
				double fearScore = 0;
				// 피측정자 아이디
				String stUserName = (String) dataList.get(i).get("USER_NAME");
				// 피측정자 진단 태그 항목
				String diagCd = (String) dataList.get(i).get("DIAG_CD");
				// 피측정자 개월 수
				long monthcount = (long) dataList.get(i).get("MONTH_COUNT");
				// 피측정자 악화지수
				double deteriorateScore = 0;

				// 유치 및 영구치 >>  주의(caution) 치아 및 충치(danger) 치아 개수
				int cavityDangerCnt = Integer.parseInt(dataList.get(i).get("CAVITY_DANGER").toString());
				int permCavityDangerCnt = Integer.parseInt(dataList.get(i).get("PERM_CAVITY_DANGER").toString());
				
				// 악화 지수
				deteriorateScore = (double) dataList.get(i).get("DETERIORATE_SCORE");
				// 악화 지수 최대값 저장
				if (deteriorateMaxScore < deteriorateScore) {
					deteriorateMaxScore = deteriorateScore;
				}

				// 진단지 조회한 회원과 이름리스트의 이름이 일치하지 않을 경우 = 001~999 로 표기
				if (!userName.equals(stUserName)) {
					stUserName = "\'" + String.format("%03d", i + 1) + "(" + monthcount + ")\'";
				} else {
					stUserName = "\'" + stUserName + "(" + monthcount + ")\'";
				}

				if (deteriorateScore > 0 && diagCd.contains("A:22:1")) {
					// 측정 두려움 항목이 있을 경우 *별표 표시
					stUserName = stUserName.replaceAll("'", "");
					stUserName = "\'" + stUserName + "*\'";
					fearScore = 0;
				} else if (diagCd.contains("A:22:1")) {
					// 측정 두려움 항목만 있을 경우 MAX 값을 지정
					fearScore = Math.round(deteriorateMaxScore);
				}

				if(fearScore % 5 != 0) {
					int calFearScore = (int)fearScore % 5;
					switch(calFearScore) {
					case 1: fearScore = fearScore+4.0;
						break;
					case 2: fearScore = fearScore+3.0;
						break;
					case 3: fearScore = fearScore+2.0;
						break;
					case 4: fearScore = fearScore+1.0;
						break;
					}
				}
				userNameList.add(stUserName);
				userFearScoreList.add(fearScore);
				// 충치에 대한 내용만 그래프에 값으로 표시가 되므로 주의에 대한 값은 필요 없음
				cavityCntList.add(cavityDangerCnt + permCavityDangerCnt);
				deteriorateScordList.add(deteriorateScore);
			}
			
			
			// 피측정자 아이디
			model.addAttribute("userId", userId);
			// 피측정자 이름
			model.addAttribute("userName", userName);
			// 피측정자 소속 기관 코드
			model.addAttribute("schoolCode", schoolCode);
			// 피측정자 소속 기관 이름
			model.addAttribute("schoolName", schoolName);
			
			// 기관 내 측정 인원 수
			model.addAttribute("userCount", dataList.size());
			// 치아 측정일 목록
			model.addAttribute("measureDtList", measureDtList);
			// 기관 내 측정 인원 이름 목록
			model.addAttribute("userNameList", userNameList);
			// 기관 내 측정 인원 악화 지수 목록
			model.addAttribute("deteriorateScordList", deteriorateScordList);
			// 기관 내 측정 인원 충치 개수 목록
			model.addAttribute("cavityCntList", cavityCntList);
			// 기관 내 측정 두려움 인원 목록
			model.addAttribute("userFearScoreList", userFearScoreList);
			
//			if(lang.equals("ko")) {
				returnUrl =  "/web/statistics/graph_main";
//			}else if(lang.equals("en")){
//				returnUrl =  "/web/statistics/graph_main_en";
//			}

		} catch (Exception e) {
			model.addAttribute("msg", "세션시간이 만료되었습니다. 다시 로그인해주시기 바랍니다.");
			model.addAttribute("loginUrl", loginUrl + "/login");
			returnUrl = "/common/alertMessage";
		}
		
		return returnUrl;
		
	}

	
	
	/**
	 * 작성자 : 정주현
	 * 작성일 : 2022. 11. 28
	 * 수정일 : 2023. 08. 03
	 * 기능 : 그래프 조회 - selectbox (파라미터 : 측정일)
	 */
	@SuppressWarnings("unused")
	@PostMapping(value = { "/web/statistics/ajaxGraph"})
	@ResponseBody
	public HashMap<String, Object> ajaxGraph(HttpServletRequest request, HttpSession session, Model model, @RequestBody Map<String, String> paramMap) throws Exception {

		// 세션 값
		UserVO sessionVO = (UserVO) session.getAttribute("userInfo");
		HashMap<String, Object> hm = new HashMap<String, Object>();
		
		try {
			
			// 회원 타입
			String selectUserType = (String)paramMap.get("selectUserType");
			// 피측정자 아이디
			String userId = (String)paramMap.get("userId");
			// 측정일
			String measureDt = (String)paramMap.get("measureDt");
			// 기관코드
			String schoolCode = sessionVO.getSchoolCode();
			
			// 피측정자 이름
			String userName = null;
			// 악화지수 최대 값
			double deteriorateMaxScore = 0;

			TeethMeasureVO webTeethMeasureVO = new TeethMeasureVO();
			List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
			List<Double> deteriorateScordList = new ArrayList<Double>();
			List<Integer> cavityCntList = new ArrayList<Integer>();
			List<String> userNameList = new ArrayList<String>();
			List<Double> userFearScoreList = new ArrayList<Double>();
			
			// 법정대리인 아이디로 피측정자 이름 조회
			userName = userService.selectChUserName(userId);
			// 기관 내 피측정자 데이터 통계 작업 후 조회
			dataList = teethService.selectUserMeasureStatisticsList(schoolCode, measureDt);

			for (int i = 0; i < dataList.size(); i++) {
				// 두려움 지수
				double fearScore = 0;
				// 악화지수 초기화
				double deteriorateScore = 0;
				// 피측정자 아이디
				String stUserId = (String) dataList.get(i).get("USER_ID");
				// 피측정자 이름
				String stUserName = (String) dataList.get(i).get("USER_NAME");
				// 피측정자 진단 태그 항목
				String diagCd = (String) dataList.get(i).get("DIAG_CD");
				// 피측정자 개월 수
				long monthcount = (long) dataList.get(i).get("MONTH_COUNT");

				// 유치 및 영구치 >>  주의(caution) 치아 및 충치(danger) 치아 개수
				int cavityCautionCnt = Integer.parseInt(dataList.get(i).get("CAVITY_CAUTION").toString());
				int cavityDangerCnt = Integer.parseInt(dataList.get(i).get("CAVITY_DANGER").toString());
				int permCavityCautionCnt = Integer.parseInt(dataList.get(i).get("PERM_CAVITY_CAUTION").toString());
				int permCavityDangerCnt = Integer.parseInt(dataList.get(i).get("PERM_CAVITY_DANGER").toString());

				// 악화지수
				deteriorateScore = (double) dataList.get(i).get("DETERIORATE_SCORE");
				// 악화지수 최대값 저장
				if (deteriorateMaxScore < deteriorateScore) {
					deteriorateMaxScore = deteriorateScore;
				}

				if(selectUserType != null && !selectUserType.equals("")) {
					stUserName = stUserName + "(" + monthcount + ")";
				}else {
					// 진단지 조회한 회원과 이름리스트의 이름이 일치하지 않을 경우 = 001~999로 표기
					if (!userName.equals(stUserName)) {
						stUserName = String.format("%03d", i + 1) + "(" + monthcount + ")";
					} else {
						stUserName = stUserName + "(" + monthcount + ")";
					}
				}
				
				// 두려움에 대한 처리 (하드코딩)
				if (deteriorateScore > 0 && diagCd.contains("E:001:1")) {
					// 측정 두려움 항목이 있을 경우 *별표 표시
					stUserName = stUserName.replaceAll("'", "");
					stUserName = "\'" + stUserName + "*\'";
					fearScore = 0;
				} else if (diagCd.contains("E:001:1")) {
					// 측정 두려움 항목만 있을 경우 MAX 값을 지정
					fearScore = Math.round(deteriorateMaxScore);
				}

				if(fearScore % 5 != 0) {
					int calFearScore = (int)fearScore % 5;
					switch(calFearScore) {
					case 1: fearScore = fearScore+4.0;
						break;
					case 2: fearScore = fearScore+3.0;
						break;
					case 3: fearScore = fearScore+2.0;
						break;
					case 4: fearScore = fearScore+1.0;
						break;
					}
				}
				userNameList.add(stUserName);
				userFearScoreList.add(fearScore);
				cavityCntList.add(cavityDangerCnt + permCavityDangerCnt);
				deteriorateScordList.add(deteriorateScore);
			}
			
			// 피측정자 아이디
			hm.put("userId", userId);
			// 피측정자 이름
			hm.put("userName", userName);
			// 피측정자 소속 기관 코드
			hm.put("schoolCode", schoolCode);
			// 기관 내 측정 인원 수
			hm.put("userCount", dataList.size());
			// 기관 내 측정 인원 이름 목록
			hm.put("userNameList", userNameList);
			// 기관 내 측정 인원 악화 지수 목록
			hm.put("deteriorateScordList", deteriorateScordList);
			// 기관 내 측정 인원 충치 개수 목록
			hm.put("cavityCntList", cavityCntList);
			// 기관 내 측정 두려움 인원 목록
			hm.put("userFearScoreList", userFearScoreList);
			
			hm.put("code", "000");
			hm.put("msg", "success");
			return hm;

		} catch (Exception e) {

			e.printStackTrace();
			hm.put("code", "500");
			hm.put("msg", "Server Error");
			return hm;
			
		}
	}
	
	
	
	/**
	 * 기능 : 진단 결과지 (기관장용)
	 * 작성자 : 정주현
	 * 작성일 : 2022. 03. 28
	 * 수정일 : 2023. 08. 01
	 */
	@GetMapping(value = { "/web/statistics/integrateStatistics.do"})
	public String integrateStatistics(HttpServletRequest request, HttpSession session, Model model) throws Exception {
		
		// 세션 값
		UserVO sessionVO = (UserVO) session.getAttribute("userInfo");
		// 언어팩
		// String lang = (String)session.getAttribute("lang");
		// Url 타입
		// String urlType = (String) model.getAttribute("urlType");
		// if(lang == null || lang.equals("")) {
		//	lang = "ko";
		//}
		
		if (sessionVO == null) {
			model.addAttribute("msg", "세션시간이 만료되었습니다. 다시 로그인해주시기 바랍니다.");
			model.addAttribute("loginUrl", loginUrl+"/director/login");
			return "/common/alertMessage";
		}
		
		boolean isMeasureDt = false;
		// 피측정자 아이디
		String userId = null;
		// 피측정자 이름
		String userName = null;
		// 기관 코드
		String schoolCode = sessionVO.getSchoolCode();
		// 기관 이름
		String schoolName = null;
		// 부서코드
		String classCode = null;
		// 피측정자 수
		int userCount = 0;
		
		// 충치단계 값(주의)
		Integer cautionLevel = 0;
		// 충치단계 값(위험)
		Integer dangerLevel = 0;

		// CAVITY_LEVEL 분류 부분 - 충치 단계별 수치 조회
		HashMap<String, Integer> cavityLevel = teethService.selectCavityLevel();
		
		List<HashMap<String, Object>> measureDtListMap = new ArrayList<HashMap<String, Object>> ();
		List<String> organMeasureDtList = new ArrayList<String>();
		
		List<HashMap<String, Object>> departList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		Map<Integer, List<HashMap<String, Object>>> departUserList = new HashMap<Integer, List<HashMap<String, Object>>>();
		
		List<String> userNameList = new ArrayList<String>();
		
		// 데이터베이스의 충치 단계 조회 (주의, 충치)
		cautionLevel = Integer.parseInt(String.valueOf(cavityLevel.get("CAVITY_CAUTION")));
		dangerLevel = Integer.parseInt(String.valueOf(cavityLevel.get("CAVITY_DANGER")));
		
		// 측정일 목록
		measureDtListMap = organService.selectOrganMeasureDtList(schoolCode);
		for(int i=0; i<measureDtListMap.size(); i++) {
			organMeasureDtList.add((String) measureDtListMap.get(i).get("MEASURE_DT"));
		}
		// 최근 측정일
		String measureDt = request.getParameter("measureDt");
		if(measureDt == null || measureDt.equals("")) {
			if( measureDtListMap.size() > 0) {
				measureDt = (String)measureDtListMap.get(0).get("MEASURE_DT");
			}else {
				// 측정일, 측정인원이 없을 경우
				model.addAttribute("msg", "측정 기록이 없습니다.");
				model.addAttribute("loginUrl", loginUrl+"/web/logout.do");
				return "/common/alertMessage";
			}
		}else {
			isMeasureDt = true;
		}
		
		try {
			// 부서 목록
			departList = organService.selectDepartmentList("SCHOOL_CODE", schoolCode);
			// 부서 이름
			schoolName = (String)departList.get(0).get("SCHOOL_NAME");
			// 피측정자 인원 수
			userCount = departList.size();

			for(int i=0; i<departList.size(); i++) {
				// 부서 코드
				classCode = (String) departList.get(i).get("CLASS_CODE");
				// 부서 내 피측정자 인원 목록
				list = userService.selectDepartUserList(classCode, measureDt);
				if(list != null || list.size() > 0) {
					for(int j=0; j<list.size(); j++) {
						userNameList.add((String) list.get(j).get("USER_NAME"));
					}
					departUserList.put(i, list);
				}
			}
			
			// 피측정자 아이디
			userId = (String)departUserList.get(0).get(0).get("USER_ID");
			// 피측정자 이름
			userName = (String)departUserList.get(0).get(0).get("USER_NAME");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		// 첫번째 반 첫번째 학생 정보
		model.addAttribute("userId", userId);
		model.addAttribute("userName", userName);
		
		if(isMeasureDt) {
			model.addAttribute("measureDt", measureDt);
		}
		
		// 메뉴 탭 :: 반목록
		model.addAttribute("departList", departList);
		// 메뉴 탭 :: 학생 목록
		model.addAttribute("departUserList", departUserList);
		
		// 그래프 파라미터
		model.addAttribute("schoolName", schoolName);
		model.addAttribute("userNameList", userNameList);
		model.addAttribute("userCount", userCount);
		
		// 측정일 목록
		model.addAttribute("organMeasureDtList", organMeasureDtList);
		
		// 결과지 파라미터
		model.addAttribute("cautionLevel", cautionLevel);
		model.addAttribute("dangerLevel", dangerLevel);

		return "/web/statistics/director/integrateStatistics";
		

	}

}
