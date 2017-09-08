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
import com.skytala.eCommerce.command.AddRoleType;
import com.skytala.eCommerce.command.DeleteRoleType;
import com.skytala.eCommerce.command.UpdateRoleType;
import com.skytala.eCommerce.entity.RoleType;
import com.skytala.eCommerce.entity.RoleTypeMapper;
import com.skytala.eCommerce.event.RoleTypeAdded;
import com.skytala.eCommerce.event.RoleTypeDeleted;
import com.skytala.eCommerce.event.RoleTypeFound;
import com.skytala.eCommerce.event.RoleTypeUpdated;
import com.skytala.eCommerce.query.FindRoleTypesBy;

@RestController
@RequestMapping("/api/roleType")
public class RoleTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RoleType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RoleTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RoleType
	 * @return a List with the RoleTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RoleType> findRoleTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindRoleTypesBy query = new FindRoleTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (RoleTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RoleTypeFound.class,
				event -> sendRoleTypesFoundMessage(((RoleTypeFound) event).getRoleTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRoleTypesFoundMessage(List<RoleType> roleTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, roleTypes);
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
	public boolean createRoleType(HttpServletRequest request) {

		RoleType roleTypeToBeAdded = new RoleType();
		try {
			roleTypeToBeAdded = RoleTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRoleType(roleTypeToBeAdded);

	}

	/**
	 * creates a new RoleType entry in the ofbiz database
	 * 
	 * @param roleTypeToBeAdded
	 *            the RoleType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRoleType(RoleType roleTypeToBeAdded) {

		AddRoleType com = new AddRoleType(roleTypeToBeAdded);
		int usedTicketId;

		synchronized (RoleTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RoleTypeAdded.class,
				event -> sendRoleTypeChangedMessage(((RoleTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRoleType(HttpServletRequest request) {

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

		RoleType roleTypeToBeUpdated = new RoleType();

		try {
			roleTypeToBeUpdated = RoleTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRoleType(roleTypeToBeUpdated);

	}

	/**
	 * Updates the RoleType with the specific Id
	 * 
	 * @param roleTypeToBeUpdated the RoleType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRoleType(RoleType roleTypeToBeUpdated) {

		UpdateRoleType com = new UpdateRoleType(roleTypeToBeUpdated);

		int usedTicketId;

		synchronized (RoleTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RoleTypeUpdated.class,
				event -> sendRoleTypeChangedMessage(((RoleTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RoleType from the database
	 * 
	 * @param roleTypeId:
	 *            the id of the RoleType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteroleTypeById(@RequestParam(value = "roleTypeId") String roleTypeId) {

		DeleteRoleType com = new DeleteRoleType(roleTypeId);

		int usedTicketId;

		synchronized (RoleTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RoleTypeDeleted.class,
				event -> sendRoleTypeChangedMessage(((RoleTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRoleTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/roleType/\" plus one of the following: "
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
