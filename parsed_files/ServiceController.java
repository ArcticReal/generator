package com.skytala.eCommerce.control;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service")
public class ServiceController{

	@RequestMapping("/userLogin")
	public ResponseEntity<Object> userLogin(HttpSession session, @RequestParam(value="login.username", required=false) String loginusername, @RequestParam(value="userLogin", required=false) org.apache.ofbiz.entity.GenericValue userLogin, @RequestParam(value="visitId", required=false) String visitId, @RequestParam(value="isServiceAuth", required=false) Boolean isServiceAuth, @RequestParam(value="timeZone", required=false) java.util.TimeZone timeZone, @RequestParam(value="login.password", required=false) String loginpassword, @RequestParam(value="locale", required=false) java.util.Locale locale) {
		
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("login.username",loginusername);
		paramMap.put("userLogin",userLogin);
		paramMap.put("visitId",visitId);
		paramMap.put("isServiceAuth",isServiceAuth);
		paramMap.put("timeZone",timeZone);
		paramMap.put("login.password",loginpassword);
		paramMap.put("locale",locale);

		Map<String, Object> result = new HashMap<>();
		LocalDispatcher dispatcher = (LocalDispatcher) session.getServletContext().getAttribute("dispatcher");
		try {
			result = dispatcher.runSync("userLogin", paramMap);
		} catch (ServiceAuthException e) {

			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(null);
		}
		return ResponseEntity.ok().body(result);
	}

	@RequestMapping("/findProductById")
	public ResponseEntity<Object> findProductById(HttpSession session, @RequestParam(value="idToFind") String idToFind, @RequestParam(value="searchAllId", required=false) String searchAllId, @RequestParam(value="goodIdentificationTypeId", required=false) String goodIdentificationTypeId, @RequestParam(value="searchProductFirst", required=false) String searchProductFirst) {
		
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("idToFind",idToFind);
		paramMap.put("searchAllId",searchAllId);
		paramMap.put("goodIdentificationTypeId",goodIdentificationTypeId);
		paramMap.put("searchProductFirst",searchProductFirst);
		paramMap.put("userLogin", session.getAttribute("userLogin"));

		Map<String, Object> result = new HashMap<>();
		LocalDispatcher dispatcher = (LocalDispatcher) session.getServletContext().getAttribute("dispatcher");
		try {
			result = dispatcher.runSync("findProductById", paramMap);
		} catch (ServiceAuthException e) {

			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(null);
		}
		return ResponseEntity.ok().body(result);
	}

	@RequestMapping(value = (" * "))
	public ResponseEntity<Object> returnErrorPage() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Requested service does not exist.");
	}

}
