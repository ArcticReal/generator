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
import com.skytala.eCommerce.command.AddFinAccountRole;
import com.skytala.eCommerce.command.DeleteFinAccountRole;
import com.skytala.eCommerce.command.UpdateFinAccountRole;
import com.skytala.eCommerce.entity.FinAccountRole;
import com.skytala.eCommerce.entity.FinAccountRoleMapper;
import com.skytala.eCommerce.event.FinAccountRoleAdded;
import com.skytala.eCommerce.event.FinAccountRoleDeleted;
import com.skytala.eCommerce.event.FinAccountRoleFound;
import com.skytala.eCommerce.event.FinAccountRoleUpdated;
import com.skytala.eCommerce.query.FindFinAccountRolesBy;

@RestController
@RequestMapping("/api/finAccountRole")
public class FinAccountRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccountRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccountRole
	 * @return a List with the FinAccountRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccountRole> findFinAccountRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountRolesBy query = new FindFinAccountRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountRoleFound.class,
				event -> sendFinAccountRolesFoundMessage(((FinAccountRoleFound) event).getFinAccountRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountRolesFoundMessage(List<FinAccountRole> finAccountRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccountRoles);
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
	public boolean createFinAccountRole(HttpServletRequest request) {

		FinAccountRole finAccountRoleToBeAdded = new FinAccountRole();
		try {
			finAccountRoleToBeAdded = FinAccountRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccountRole(finAccountRoleToBeAdded);

	}

	/**
	 * creates a new FinAccountRole entry in the ofbiz database
	 * 
	 * @param finAccountRoleToBeAdded
	 *            the FinAccountRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccountRole(FinAccountRole finAccountRoleToBeAdded) {

		AddFinAccountRole com = new AddFinAccountRole(finAccountRoleToBeAdded);
		int usedTicketId;

		synchronized (FinAccountRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountRoleAdded.class,
				event -> sendFinAccountRoleChangedMessage(((FinAccountRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccountRole(HttpServletRequest request) {

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

		FinAccountRole finAccountRoleToBeUpdated = new FinAccountRole();

		try {
			finAccountRoleToBeUpdated = FinAccountRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccountRole(finAccountRoleToBeUpdated);

	}

	/**
	 * Updates the FinAccountRole with the specific Id
	 * 
	 * @param finAccountRoleToBeUpdated the FinAccountRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccountRole(FinAccountRole finAccountRoleToBeUpdated) {

		UpdateFinAccountRole com = new UpdateFinAccountRole(finAccountRoleToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountRoleUpdated.class,
				event -> sendFinAccountRoleChangedMessage(((FinAccountRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccountRole from the database
	 * 
	 * @param finAccountRoleId:
	 *            the id of the FinAccountRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountRoleById(@RequestParam(value = "finAccountRoleId") String finAccountRoleId) {

		DeleteFinAccountRole com = new DeleteFinAccountRole(finAccountRoleId);

		int usedTicketId;

		synchronized (FinAccountRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountRoleDeleted.class,
				event -> sendFinAccountRoleChangedMessage(((FinAccountRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccountRole/\" plus one of the following: "
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
