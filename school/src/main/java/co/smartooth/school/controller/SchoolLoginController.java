package co.smartooth.school.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import co.smartooth.school.service.SchoolAuthService;
import co.smartooth.school.service.SchoolLogService;
import co.smartooth.school.service.SchoolUserService;
import co.smartooth.school.utils.JwtTokenUtil;
import co.smartooth.school.vo.SchoolAuthVO;
import co.smartooth.school.vo.SchoolUserVO;
import co.smartooth.web.utils.AES256Util;
import lombok.extern.slf4j.Slf4j;


/**
 * 작성자 : 정주현
 * 작성일 : 2023. 08. 25
 * 수정일 : 2023. 08. 28
 * 서버분리 : 2023. 08. 01
 */
@Slf4j
@Controller
@PropertySource({ "classpath:application.yml" })
public class SchoolLoginController {

	
	@Autowired(required = false)
	private SchoolAuthService schoolAuthService;

	
	@Autowired(required = false)
	private SchoolUserService schoolUserService;

	
	@Autowired(required = false)
	private SchoolLogService schoolLogService;
	
	
	
	
	/**
	 * 기능   : 로그인 API
	 * 작성자 : 정주현 
	 * 작성일 : 2023. 08. 25
	 * 수정일 : 2023. 08. 28
	 */
	@PostMapping(value = {"/school/login.do"})
	@ResponseBody
	public HashMap<String,Object> login(@RequestParam HashMap<String, Object> paramMap) {
       
	    Logger logger = LoggerFactory.getLogger(getClass());

	    logger.debug("========== School.LoginController ========== /school/login.do ==========");
	    logger.debug("========== School.LoginController ========== /school/login.do ==========");
	    logger.debug("========== School.LoginController ========== /school/login.do ==========");
	    logger.debug("========== School.LoginController ========== /school/login.do ==========");
	    
	    // String lang = (String)paramMap.get("lang");
	    // 하드코딩
	    // String lang = "ko";
	    String userId = null;
		String userPwd = null;
		String loginIp = null;
		String userAuthToken = null;
		
		int loginChkByIdPwd = 0;
		int isIdExist = 0;
		
		
		HashMap<String,Object> hm = new HashMap<String,Object>();
		List<String> childrenUserIdList = new ArrayList<String>();
		List<SchoolUserVO> childUserInfoList = new ArrayList<SchoolUserVO>();
		
		// 로그인 회원 인증 VO
		SchoolAuthVO AuthVO = new SchoolAuthVO();
		// 법정대리인 회원 VO
		SchoolUserVO legalRepresentativeUserVO = new SchoolUserVO();
		
		// 오늘 일자 계산
		Date tmpDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
		String sysDate = sdf.format(tmpDate);
		
		// 파라미터 >> 값 setting
		userId= (String)paramMap.get("userId");
		loginIp = (String)paramMap.get("loginIp"); 
		
		// 비밀번호 암호화 
		AES256Util aes256Util = new AES256Util();
		userPwd = aes256Util.aesEncode((String)paramMap.get("userPwd"));
		
		if(userPwd.equals("false")) { // 암호화에 실패할 경우
			hm.put("code", "500");
			//if(lang.equals("ko")) {
				hm.put("msg", "서버 에러가 발생했습니다.\n관리자에게 문의해주시기 바랍니다.");
			//}else if(lang.equals("en")) {
			//	hm.put("msg", "Server Error");
			//}
			return hm;
		}
		
		// 회원 인증 VO
		AuthVO.setUserId(userId);
		AuthVO.setUserPwd(userPwd);
		AuthVO.setLoginDt(sysDate);
		AuthVO.setLoginIp(loginIp);
		
		
		try {
			
			// 로그인 확인
			loginChkByIdPwd = schoolAuthService.loginChkByIdPwd(AuthVO);
			if(loginChkByIdPwd == 0){ // 0일 경우는 Database에 ID와 비밀번호가 틀린 것
				isIdExist = schoolAuthService.isIdExist(AuthVO.getUserId());
				if(isIdExist == 0) { // ID가 존재하지 않을 경우
					//if(lang.equals("ko")) {
						hm.put("msg", "해당 아이디가 존재하지 않습니다");
					//}else if(lang.equals("en")) {
					//	hm.put("msg", "This ID does not exist");
					//}
				}else { // PWD가 틀렸을 경우
					hm.put("code", "406");
					//if(lang.equals("ko")) {
						hm.put("msg", "비밀번호가 틀렸습니다");
					//}else if(lang.equals("en")) {
					//	hm.put("msg", "The password is wrong");
					//}
				}
			}else { 
				
				// JWT token 발행
				JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
				userAuthToken = jwtTokenUtil.createToken(AuthVO);

				// 법정대리인 계정 정보 조회
				legalRepresentativeUserVO = schoolUserService.selectUserInfo(userId);
				// 법정대리인 - 피측정자 아이디 목록 조회
				childrenUserIdList = schoolUserService.selectChildUserIdList(userId);
				
				if(childrenUserIdList.size()>0) {
					
					// 피측정자 아이디로 정보 조회 후 리턴
					for(int i=0; i<childrenUserIdList.size(); i++) {
						String childUserId = childrenUserIdList.get(i);
						childUserInfoList.add(schoolUserService.selectChildUserInfo(childUserId)); 
					}
				}
				
				// 로그인 일자 업데이트
				schoolLogService.updateLoginDt(AuthVO);
				// 로그인 Log 등록
				schoolLogService.insertUserLoginHistory(AuthVO);
				
			}
		} catch (Exception e) {
			hm.put("code", "500");
			//if(lang.equals("ko")) {
				hm.put("msg", "서버 에러가 발생했습니다.\n관리자에게 문의해주시기 바랍니다.");
			//}else if(lang.equals("en")) {
			//	hm.put("msg", "Server Error");
			//}
			e.printStackTrace();
		}
		
		hm.put("code", "000");
		//if(lang.equals("ko")) {
		hm.put("msg", "로그인 성공");
		//}else if(lang.equals("en")) {
		//	hm.put("msg", "Login Success");
		//}
		// 로그인 토큰
		hm.put("userAuthToken", userAuthToken);
		// 로그인 회원 정보
		hm.put("userInfo", legalRepresentativeUserVO);
		// 피측정자 회원 정보 - 목록으로 리턴
		hm.put("childUserInfoList", childUserInfoList);
		
		
		return hm;
	}
	
	
	
}
