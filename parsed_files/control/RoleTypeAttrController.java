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
import com.skytala.eCommerce.command.AddRoleTypeAttr;
import com.skytala.eCommerce.command.DeleteRoleTypeAttr;
import com.skytala.eCommerce.command.UpdateRoleTypeAttr;
import com.skytala.eCommerce.entity.RoleTypeAttr;
import com.skytala.eCommerce.entity.RoleTypeAttrMapper;
import com.skytala.eCommerce.event.RoleTypeAttrAdded;
import com.skytala.eCommerce.event.RoleTypeAttrDeleted;
import com.skytala.eCommerce.event.RoleTypeAttrFound;
import com.skytala.eCommerce.event.RoleTypeAttrUpdated;
import com.skytala.eCommerce.query.FindRoleTypeAttrsBy;

@RestController
@RequestMapping("/api/roleTypeAttr")
public class RoleTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RoleTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RoleTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RoleTypeAttr
	 * @return a List with the RoleTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RoleTypeAttr> findRoleTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindRoleTypeAttrsBy query = new FindRoleTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (RoleTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RoleTypeAttrFound.class,
				event -> sendRoleTypeAttrsFoundMessage(((RoleTypeAttrFound) event).getRoleTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRoleTypeAttrsFoundMessage(List<RoleTypeAttr> roleTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, roleTypeAttrs);
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
	public boolean createRoleTypeAttr(HttpServletRequest request) {

		RoleTypeAttr roleTypeAttrToBeAdded = new RoleTypeAttr();
		try {
			roleTypeAttrToBeAdded = RoleTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRoleTypeAttr(roleTypeAttrToBeAdded);

	}

	/**
	 * creates a new RoleTypeAttr entry in the ofbiz database
	 * 
	 * @param roleTypeAttrToBeAdded
	 *            the RoleTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRoleTypeAttr(RoleTypeAttr roleTypeAttrToBeAdded) {

		AddRoleTypeAttr com = new AddRoleTypeAttr(roleTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (RoleTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RoleTypeAttrAdded.class,
				event -> sendRoleTypeAttrChangedMessage(((RoleTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRoleTypeAttr(HttpServletRequest request) {

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

		RoleTypeAttr roleTypeAttrToBeUpdated = new RoleTypeAttr();

		try {
			roleTypeAttrToBeUpdated = RoleTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRoleTypeAttr(roleTypeAttrToBeUpdated);

	}

	/**
	 * Updates the RoleTypeAttr with the specific Id
	 * 
	 * @param roleTypeAttrToBeUpdated the RoleTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRoleTypeAttr(RoleTypeAttr roleTypeAttrToBeUpdated) {

		UpdateRoleTypeAttr com = new UpdateRoleTypeAttr(roleTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (RoleTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RoleTypeAttrUpdated.class,
				event -> sendRoleTypeAttrChangedMessage(((RoleTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RoleTypeAttr from the database
	 * 
	 * @param roleTypeAttrId:
	 *            the id of the RoleTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteroleTypeAttrById(@RequestParam(value = "roleTypeAttrId") String roleTypeAttrId) {

		DeleteRoleTypeAttr com = new DeleteRoleTypeAttr(roleTypeAttrId);

		int usedTicketId;

		synchronized (RoleTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RoleTypeAttrDeleted.class,
				event -> sendRoleTypeAttrChangedMessage(((RoleTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRoleTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/roleTypeAttr/\" plus one of the following: "
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
