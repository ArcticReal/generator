package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddSecurityGroupPermission;
import com.skytala.eCommerce.command.DeleteSecurityGroupPermission;
import com.skytala.eCommerce.command.UpdateSecurityGroupPermission;
import com.skytala.eCommerce.entity.SecurityGroupPermission;
import com.skytala.eCommerce.entity.SecurityGroupPermissionMapper;
import com.skytala.eCommerce.event.SecurityGroupPermissionAdded;
import com.skytala.eCommerce.event.SecurityGroupPermissionDeleted;
import com.skytala.eCommerce.event.SecurityGroupPermissionFound;
import com.skytala.eCommerce.event.SecurityGroupPermissionUpdated;
import com.skytala.eCommerce.query.FindSecurityGroupPermissionsBy;

@RestController
@RequestMapping("/api/securityGroupPermission")
public class SecurityGroupPermissionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SecurityGroupPermission>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SecurityGroupPermissionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SecurityGroupPermission
	 * @return a List with the SecurityGroupPermissions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SecurityGroupPermission> findSecurityGroupPermissionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSecurityGroupPermissionsBy query = new FindSecurityGroupPermissionsBy(allRequestParams);

		int usedTicketId;

		synchronized (SecurityGroupPermissionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SecurityGroupPermissionFound.class,
				event -> sendSecurityGroupPermissionsFoundMessage(((SecurityGroupPermissionFound) event).getSecurityGroupPermissions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSecurityGroupPermissionsFoundMessage(List<SecurityGroupPermission> securityGroupPermissions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, securityGroupPermissions);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createSecurityGroupPermission(HttpServletRequest request) {

		SecurityGroupPermission securityGroupPermissionToBeAdded = new SecurityGroupPermission();
		try {
			securityGroupPermissionToBeAdded = SecurityGroupPermissionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSecurityGroupPermission(securityGroupPermissionToBeAdded);

	}

	/**
	 * creates a new SecurityGroupPermission entry in the ofbiz database
	 * 
	 * @param securityGroupPermissionToBeAdded
	 *            the SecurityGroupPermission thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSecurityGroupPermission(SecurityGroupPermission securityGroupPermissionToBeAdded) {

		AddSecurityGroupPermission com = new AddSecurityGroupPermission(securityGroupPermissionToBeAdded);
		int usedTicketId;

		synchronized (SecurityGroupPermissionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SecurityGroupPermissionAdded.class,
				event -> sendSecurityGroupPermissionChangedMessage(((SecurityGroupPermissionAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateSecurityGroupPermission(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		SecurityGroupPermission securityGroupPermissionToBeUpdated = new SecurityGroupPermission();

		try {
			securityGroupPermissionToBeUpdated = SecurityGroupPermissionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSecurityGroupPermission(securityGroupPermissionToBeUpdated);

	}

	/**
	 * Updates the SecurityGroupPermission with the specific Id
	 * 
	 * @param securityGroupPermissionToBeUpdated the SecurityGroupPermission thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSecurityGroupPermission(SecurityGroupPermission securityGroupPermissionToBeUpdated) {

		UpdateSecurityGroupPermission com = new UpdateSecurityGroupPermission(securityGroupPermissionToBeUpdated);

		int usedTicketId;

		synchronized (SecurityGroupPermissionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SecurityGroupPermissionUpdated.class,
				event -> sendSecurityGroupPermissionChangedMessage(((SecurityGroupPermissionUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a SecurityGroupPermission from the database
	 * 
	 * @param securityGroupPermissionId:
	 *            the id of the SecurityGroupPermission thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesecurityGroupPermissionById(@RequestParam(value = "securityGroupPermissionId") String securityGroupPermissionId) {

		DeleteSecurityGroupPermission com = new DeleteSecurityGroupPermission(securityGroupPermissionId);

		int usedTicketId;

		synchronized (SecurityGroupPermissionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SecurityGroupPermissionDeleted.class,
				event -> sendSecurityGroupPermissionChangedMessage(((SecurityGroupPermissionDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendSecurityGroupPermissionChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/securityGroupPermission/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}
