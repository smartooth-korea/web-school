package co.smartooth.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.smartooth.school.service.SchoolLocationService;
import co.smartooth.school.service.SchoolOrganService;
import co.smartooth.school.service.SchoolTeethService;
import co.smartooth.school.service.SchoolUserService;
import co.smartooth.school.vo.SchoolLocationVO;
import co.smartooth.school.vo.SchoolOrganVO;
import co.smartooth.school.vo.SchoolTeethInfoVO;
import co.smartooth.school.vo.SchoolUserVO;
import co.smartooth.web.utils.AES256Util;


/**
 * 작성자 : 정주현
 * 작성일 : 2023. 08. 25
 * 수정일 : 2023. 08. 25
 */

@Controller
public class SchoolUserController {

	@Value("${loginUrl}")
	private String loginUrl;

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired(required = false)
	private SchoolUserService schoolUserService;

//	@Autowired(required = false)
//	private SchoolTeethService schoolTeethService;

//	@Autowired(required = false)
//	private SchoolOrganService schoolOrganService;

//	@Autowired(required = false)
//	private SchoolLocationService schoolLocationService;
	
	
	
	// 아이디 중복 확인
	@PostMapping(value = {"/school/user/duplicateChkId"})
	@ResponseBody
	public HashMap<String,Object> duplicateChkId(@RequestParam Map<String, String> paramMap) throws Exception {
		
		HashMap<String, Object> hm = new HashMap<String, Object>();

		// 중복 여부
		int duplicateChkId = 0; 
		// 법정대리인 아이디
		String userId = paramMap.get("userId");

		if(userId == null || userId.equals("")) {
			hm.put("code", "401");
			hm.put("msg", "파라미터(아이디)가 없습니다.");
			return hm;
		}
		
		// 법정대리인 중복 확인 
		duplicateChkId = schoolUserService.duplicateChkId(userId);
		
		if(duplicateChkId > 0) {
			hm.put("code", "000");
			hm.put("msg", "사용 가능한 이메일입니다.");
		}else {
			hm.put("code", "402");
			hm.put("msg", "해당 이메일은 존재합니다. 다른 이메일을 입력해주시기 바랍니다.");
		}
		return hm;
	}
	
	
	
	/**
	 * 작성자 : 정주현
	 * 작성일 : 2023. 08. 25
	 * 수정일 : 2023. 08. 25
	 * 기능 : 조회앱의 개인정보 동의서로 회원가입
	 * 비고 : 에러 발생 시 Transaction으로 Database insert 취소
	 */
	@Transactional
	@PostMapping(value = { "/school/user/register.do" })
	@ResponseBody
	public HashMap<String,Object> insertUserInfo(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		
		HashMap<String,Object> hm = new HashMap<String,Object>();
		
		// 법정대리인
		SchoolUserVO userVO = new SchoolUserVO();
		// 암호화 클래스
		AES256Util aes256Util = new AES256Util();
		// 법정대리인 아이디 - 이메일 사용
		String userId = (String)paramMap.get("userId");
		// 법정대리인 비밀번호
		String userPwd  = (String)paramMap.get("userPwd");
		// 법정대리인 이름
		String userName = (String)paramMap.get("userName");
		// 법정대리인 전화번호
		// String userTelNo =  (String)paramMap.get("userTelNo");
		
		try {
			// 법정대리인 회원 정보 VO
			userVO.setUserId(userId);
			userVO.setUserName(userName);
			userVO.setUserPwd(userPwd);
			userVO.setUserType("PR");
			// 법정대리인 계정 정보 등록
			schoolUserService.insertUserInfo(userVO);
			
		} catch (Exception e) {
			hm.put("code", "500");
			hm.put("msg", "계정 등록에 실패했습니다.\n관리자에게 문의해주시기 바랍니다.");
			e.printStackTrace();
		}
		hm.put("code", "000");
		hm.put("msg", "등록이 완료되었습니다.");
		
		return hm;

	}
	
	
	
	/**
	 * 작성자 : 정주현
	 * 작성일 : 2023. 08. 28
	 * 수정일 : 2023. 08. 28
	 * 기능 : 조회앱의 피측정자(자녀) 등록
	 * 비고 : 에러 발생 시 Transaction으로 Database insert 취소
	 */
	@Transactional
	@PostMapping(value = { "/school/user/insertChildUserInfo.do" })
	@ResponseBody
	public HashMap<String,Object> insertChildUserInfo(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		
		HashMap<String,Object> hm = new HashMap<String,Object>();
		
		// 법정대리인
		SchoolUserVO userVO = new SchoolUserVO();
		// 암호화 클래스
		AES256Util aes256Util = new AES256Util();
		// 법정대리인 아이디 - 이메일 사용
		String userId = (String)paramMap.get("userId");
		// 법정대리인 비밀번호
		String userPwd  = aes256Util.aesEncode((String)paramMap.get("userPwd"));
		// 법정대리인 이름
		String userName = (String)paramMap.get("userName");
		// 법정대리인 전화번호
		// String userTelNo =  (String)paramMap.get("userTelNo");
		
		try {
			// 법정대리인 회원 정보 VO
			userVO.setUserId(userId);
			userVO.setUserName(userName);
			userVO.setUserPwd(userPwd);
			userVO.setUserType("PR");
			// 법정대리인 계정 정보 등록
			schoolUserService.insertUserInfo(userVO);
			
		} catch (Exception e) {
			hm.put("code", "500");
			hm.put("msg", "계정 등록에 실패했습니다.\n관리자에게 문의해주시기 바랍니다.");
			e.printStackTrace();
		}
		hm.put("code", "000");
		hm.put("msg", "등록이 완료되었습니다.");
		
		return hm;

	}
	
	
	
	
	
	

}
