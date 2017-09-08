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
import com.skytala.eCommerce.command.AddValidContactMechRole;
import com.skytala.eCommerce.command.DeleteValidContactMechRole;
import com.skytala.eCommerce.command.UpdateValidContactMechRole;
import com.skytala.eCommerce.entity.ValidContactMechRole;
import com.skytala.eCommerce.entity.ValidContactMechRoleMapper;
import com.skytala.eCommerce.event.ValidContactMechRoleAdded;
import com.skytala.eCommerce.event.ValidContactMechRoleDeleted;
import com.skytala.eCommerce.event.ValidContactMechRoleFound;
import com.skytala.eCommerce.event.ValidContactMechRoleUpdated;
import com.skytala.eCommerce.query.FindValidContactMechRolesBy;

@RestController
@RequestMapping("/api/validContactMechRole")
public class ValidContactMechRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ValidContactMechRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ValidContactMechRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ValidContactMechRole
	 * @return a List with the ValidContactMechRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ValidContactMechRole> findValidContactMechRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindValidContactMechRolesBy query = new FindValidContactMechRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (ValidContactMechRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValidContactMechRoleFound.class,
				event -> sendValidContactMechRolesFoundMessage(((ValidContactMechRoleFound) event).getValidContactMechRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendValidContactMechRolesFoundMessage(List<ValidContactMechRole> validContactMechRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, validContactMechRoles);
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
	public boolean createValidContactMechRole(HttpServletRequest request) {

		ValidContactMechRole validContactMechRoleToBeAdded = new ValidContactMechRole();
		try {
			validContactMechRoleToBeAdded = ValidContactMechRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createValidContactMechRole(validContactMechRoleToBeAdded);

	}

	/**
	 * creates a new ValidContactMechRole entry in the ofbiz database
	 * 
	 * @param validContactMechRoleToBeAdded
	 *            the ValidContactMechRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createValidContactMechRole(ValidContactMechRole validContactMechRoleToBeAdded) {

		AddValidContactMechRole com = new AddValidContactMechRole(validContactMechRoleToBeAdded);
		int usedTicketId;

		synchronized (ValidContactMechRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValidContactMechRoleAdded.class,
				event -> sendValidContactMechRoleChangedMessage(((ValidContactMechRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateValidContactMechRole(HttpServletRequest request) {

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

		ValidContactMechRole validContactMechRoleToBeUpdated = new ValidContactMechRole();

		try {
			validContactMechRoleToBeUpdated = ValidContactMechRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateValidContactMechRole(validContactMechRoleToBeUpdated);

	}

	/**
	 * Updates the ValidContactMechRole with the specific Id
	 * 
	 * @param validContactMechRoleToBeUpdated the ValidContactMechRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateValidContactMechRole(ValidContactMechRole validContactMechRoleToBeUpdated) {

		UpdateValidContactMechRole com = new UpdateValidContactMechRole(validContactMechRoleToBeUpdated);

		int usedTicketId;

		synchronized (ValidContactMechRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValidContactMechRoleUpdated.class,
				event -> sendValidContactMechRoleChangedMessage(((ValidContactMechRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ValidContactMechRole from the database
	 * 
	 * @param validContactMechRoleId:
	 *            the id of the ValidContactMechRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletevalidContactMechRoleById(@RequestParam(value = "validContactMechRoleId") String validContactMechRoleId) {

		DeleteValidContactMechRole com = new DeleteValidContactMechRole(validContactMechRoleId);

		int usedTicketId;

		synchronized (ValidContactMechRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValidContactMechRoleDeleted.class,
				event -> sendValidContactMechRoleChangedMessage(((ValidContactMechRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendValidContactMechRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/validContactMechRole/\" plus one of the following: "
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
