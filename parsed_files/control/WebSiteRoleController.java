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
import com.skytala.eCommerce.command.AddWebSiteRole;
import com.skytala.eCommerce.command.DeleteWebSiteRole;
import com.skytala.eCommerce.command.UpdateWebSiteRole;
import com.skytala.eCommerce.entity.WebSiteRole;
import com.skytala.eCommerce.entity.WebSiteRoleMapper;
import com.skytala.eCommerce.event.WebSiteRoleAdded;
import com.skytala.eCommerce.event.WebSiteRoleDeleted;
import com.skytala.eCommerce.event.WebSiteRoleFound;
import com.skytala.eCommerce.event.WebSiteRoleUpdated;
import com.skytala.eCommerce.query.FindWebSiteRolesBy;

@RestController
@RequestMapping("/api/webSiteRole")
public class WebSiteRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WebSiteRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WebSiteRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WebSiteRole
	 * @return a List with the WebSiteRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WebSiteRole> findWebSiteRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWebSiteRolesBy query = new FindWebSiteRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (WebSiteRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteRoleFound.class,
				event -> sendWebSiteRolesFoundMessage(((WebSiteRoleFound) event).getWebSiteRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWebSiteRolesFoundMessage(List<WebSiteRole> webSiteRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, webSiteRoles);
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
	public boolean createWebSiteRole(HttpServletRequest request) {

		WebSiteRole webSiteRoleToBeAdded = new WebSiteRole();
		try {
			webSiteRoleToBeAdded = WebSiteRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWebSiteRole(webSiteRoleToBeAdded);

	}

	/**
	 * creates a new WebSiteRole entry in the ofbiz database
	 * 
	 * @param webSiteRoleToBeAdded
	 *            the WebSiteRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWebSiteRole(WebSiteRole webSiteRoleToBeAdded) {

		AddWebSiteRole com = new AddWebSiteRole(webSiteRoleToBeAdded);
		int usedTicketId;

		synchronized (WebSiteRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteRoleAdded.class,
				event -> sendWebSiteRoleChangedMessage(((WebSiteRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWebSiteRole(HttpServletRequest request) {

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

		WebSiteRole webSiteRoleToBeUpdated = new WebSiteRole();

		try {
			webSiteRoleToBeUpdated = WebSiteRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWebSiteRole(webSiteRoleToBeUpdated);

	}

	/**
	 * Updates the WebSiteRole with the specific Id
	 * 
	 * @param webSiteRoleToBeUpdated the WebSiteRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWebSiteRole(WebSiteRole webSiteRoleToBeUpdated) {

		UpdateWebSiteRole com = new UpdateWebSiteRole(webSiteRoleToBeUpdated);

		int usedTicketId;

		synchronized (WebSiteRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteRoleUpdated.class,
				event -> sendWebSiteRoleChangedMessage(((WebSiteRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WebSiteRole from the database
	 * 
	 * @param webSiteRoleId:
	 *            the id of the WebSiteRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletewebSiteRoleById(@RequestParam(value = "webSiteRoleId") String webSiteRoleId) {

		DeleteWebSiteRole com = new DeleteWebSiteRole(webSiteRoleId);

		int usedTicketId;

		synchronized (WebSiteRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteRoleDeleted.class,
				event -> sendWebSiteRoleChangedMessage(((WebSiteRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWebSiteRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/webSiteRole/\" plus one of the following: "
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
