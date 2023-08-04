package co.smartooth.web.controller;

import java.util.HashMap;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import co.smartooth.web.service.MailAuthService;

/**
 * 작성자 : 정주현 
 * 작성일 : 2022. 04. 28
 * 수정일 : 2023. 08. 02
 * 서버분리 : 2023. 08. 01
 * @RestController를 쓰지 않는 이유는 몇 안되는 mapping에 jsp를 반환해줘야하는게 있으므로 @Controller를 사용한다.
 * @RestAPI로 반환해야할 경우 @ResponseBody를 사용하여 HashMap으로 return 해준다.
 */
@Controller
public class MailAuthController {

	
	@Autowired(required = false)
	private MailAuthService mailAuthService;

	

	/**
	 * 기능   : 인증 메일 발송
	 * 작성자 : 정주현 
	 * 작성일 : 2022. 04. 27
	 * 수정일 : 2023. 08. 03
	 */
	@PostMapping(value = {"/web/auth/emailAuth.do"})
	@ResponseBody
	public HashMap<String,Object> mailAuth(HttpSession session, @RequestBody HashMap<String, String> paramMap) {

		// Parameter :: userId
		String userId = (String)session.getAttribute("userId");
		String authEmail = (String)paramMap.get("email");

		HashMap<String,Object> hm = new HashMap<String,Object>();
		
		try {
			// 인증 메일 요청한 아이디와 메일 주소
			mailAuthService.sendMail(userId, authEmail);
		} catch (Exception e) {
			hm.put("code", "500");
			hm.put("msg", "메일이 전송되지 않았습니다.\n관리자에게 문의해주시기 바랍니다.");
			e.printStackTrace();
			return hm;
		}
		hm.put("code", "000");
		hm.put("msg", "메일 전송");

		return hm;
	}

	
	
	/**
	 * 기능   : 인증 번호 (이메일) 일치 확인
	 * 작성자 : 정주현 
	 * 작성일 : 2022. 04. 27
	 * 수정일 : 2023. 08. 03
	 */
	@PostMapping(value = {"/web/auth/isAuthKeyMatch.do"})
	@ResponseBody
	public HashMap<String,Object> isAuthKeyMatch(HttpSession session, @RequestBody HashMap<String, String> paramMap) {
		
 		String userId = (String)session.getAttribute("userId");
		String authKey = paramMap.get("authKey");
		// 인증번호 검증
		int isAuthKeyMatch = 0;
		
		HashMap<String,Object> hm = new HashMap<String,Object>();
		
		try {
			// 메일 인증 번호 확인
			isAuthKeyMatch = mailAuthService.isAuthKeyMatch(userId, authKey);
			// 메일 인증 번호 일치
			if(isAuthKeyMatch > 0 ) {
				// 인증 여부 N -> Y
				mailAuthService.updateAuthStatusY(userId);
				hm.put("code", "000");
				hm.put("msg", "메일 인증 성공");
			}else {
				hm.put("code", "400");
 				hm.put("msg", "메일 인증 실패");
			}
			
		} catch (Exception e) {
			hm.put("code", "500");
			hm.put("msg", "메일 인증 시 문제가 발생하였습니다.\n관리자에게 문의해주시기 바랍니다.");
			e.printStackTrace();
			return hm;
		}
		return hm;
	}
	
	
	
}
