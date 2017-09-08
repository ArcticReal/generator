package com.skytala.eCommerce.control;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service")
public class ServiceController{

	@RequestMapping("/countProductView")
	public Map<String, Object> invokeTestService(HttpSession session, @RequestParam(value="productId") String productId) {
		
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("productId",productId);
		Map<String, Object> result = new HashMap<>();
		LocalDispatcher dispatcher = (LocalDispatcher) session.getServletContext().getAttribute("dispatcher");
		try {
			result = dispatcher.runSync("countProductView", paramMap);
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}

		return result;
	}
}
