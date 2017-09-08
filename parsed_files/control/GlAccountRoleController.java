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
import com.skytala.eCommerce.command.AddGlAccountRole;
import com.skytala.eCommerce.command.DeleteGlAccountRole;
import com.skytala.eCommerce.command.UpdateGlAccountRole;
import com.skytala.eCommerce.entity.GlAccountRole;
import com.skytala.eCommerce.entity.GlAccountRoleMapper;
import com.skytala.eCommerce.event.GlAccountRoleAdded;
import com.skytala.eCommerce.event.GlAccountRoleDeleted;
import com.skytala.eCommerce.event.GlAccountRoleFound;
import com.skytala.eCommerce.event.GlAccountRoleUpdated;
import com.skytala.eCommerce.query.FindGlAccountRolesBy;

@RestController
@RequestMapping("/api/glAccountRole")
public class GlAccountRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccountRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccountRole
	 * @return a List with the GlAccountRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccountRole> findGlAccountRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountRolesBy query = new FindGlAccountRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountRoleFound.class,
				event -> sendGlAccountRolesFoundMessage(((GlAccountRoleFound) event).getGlAccountRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountRolesFoundMessage(List<GlAccountRole> glAccountRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccountRoles);
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
	public boolean createGlAccountRole(HttpServletRequest request) {

		GlAccountRole glAccountRoleToBeAdded = new GlAccountRole();
		try {
			glAccountRoleToBeAdded = GlAccountRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccountRole(glAccountRoleToBeAdded);

	}

	/**
	 * creates a new GlAccountRole entry in the ofbiz database
	 * 
	 * @param glAccountRoleToBeAdded
	 *            the GlAccountRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccountRole(GlAccountRole glAccountRoleToBeAdded) {

		AddGlAccountRole com = new AddGlAccountRole(glAccountRoleToBeAdded);
		int usedTicketId;

		synchronized (GlAccountRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountRoleAdded.class,
				event -> sendGlAccountRoleChangedMessage(((GlAccountRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccountRole(HttpServletRequest request) {

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

		GlAccountRole glAccountRoleToBeUpdated = new GlAccountRole();

		try {
			glAccountRoleToBeUpdated = GlAccountRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccountRole(glAccountRoleToBeUpdated);

	}

	/**
	 * Updates the GlAccountRole with the specific Id
	 * 
	 * @param glAccountRoleToBeUpdated the GlAccountRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccountRole(GlAccountRole glAccountRoleToBeUpdated) {

		UpdateGlAccountRole com = new UpdateGlAccountRole(glAccountRoleToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountRoleUpdated.class,
				event -> sendGlAccountRoleChangedMessage(((GlAccountRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccountRole from the database
	 * 
	 * @param glAccountRoleId:
	 *            the id of the GlAccountRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountRoleById(@RequestParam(value = "glAccountRoleId") String glAccountRoleId) {

		DeleteGlAccountRole com = new DeleteGlAccountRole(glAccountRoleId);

		int usedTicketId;

		synchronized (GlAccountRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountRoleDeleted.class,
				event -> sendGlAccountRoleChangedMessage(((GlAccountRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccountRole/\" plus one of the following: "
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
