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
import com.skytala.eCommerce.command.AddBudgetRole;
import com.skytala.eCommerce.command.DeleteBudgetRole;
import com.skytala.eCommerce.command.UpdateBudgetRole;
import com.skytala.eCommerce.entity.BudgetRole;
import com.skytala.eCommerce.entity.BudgetRoleMapper;
import com.skytala.eCommerce.event.BudgetRoleAdded;
import com.skytala.eCommerce.event.BudgetRoleDeleted;
import com.skytala.eCommerce.event.BudgetRoleFound;
import com.skytala.eCommerce.event.BudgetRoleUpdated;
import com.skytala.eCommerce.query.FindBudgetRolesBy;

@RestController
@RequestMapping("/api/budgetRole")
public class BudgetRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetRole
	 * @return a List with the BudgetRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetRole> findBudgetRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetRolesBy query = new FindBudgetRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRoleFound.class,
				event -> sendBudgetRolesFoundMessage(((BudgetRoleFound) event).getBudgetRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetRolesFoundMessage(List<BudgetRole> budgetRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetRoles);
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
	public boolean createBudgetRole(HttpServletRequest request) {

		BudgetRole budgetRoleToBeAdded = new BudgetRole();
		try {
			budgetRoleToBeAdded = BudgetRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetRole(budgetRoleToBeAdded);

	}

	/**
	 * creates a new BudgetRole entry in the ofbiz database
	 * 
	 * @param budgetRoleToBeAdded
	 *            the BudgetRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetRole(BudgetRole budgetRoleToBeAdded) {

		AddBudgetRole com = new AddBudgetRole(budgetRoleToBeAdded);
		int usedTicketId;

		synchronized (BudgetRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRoleAdded.class,
				event -> sendBudgetRoleChangedMessage(((BudgetRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetRole(HttpServletRequest request) {

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

		BudgetRole budgetRoleToBeUpdated = new BudgetRole();

		try {
			budgetRoleToBeUpdated = BudgetRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetRole(budgetRoleToBeUpdated);

	}

	/**
	 * Updates the BudgetRole with the specific Id
	 * 
	 * @param budgetRoleToBeUpdated the BudgetRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetRole(BudgetRole budgetRoleToBeUpdated) {

		UpdateBudgetRole com = new UpdateBudgetRole(budgetRoleToBeUpdated);

		int usedTicketId;

		synchronized (BudgetRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRoleUpdated.class,
				event -> sendBudgetRoleChangedMessage(((BudgetRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetRole from the database
	 * 
	 * @param budgetRoleId:
	 *            the id of the BudgetRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetRoleById(@RequestParam(value = "budgetRoleId") String budgetRoleId) {

		DeleteBudgetRole com = new DeleteBudgetRole(budgetRoleId);

		int usedTicketId;

		synchronized (BudgetRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRoleDeleted.class,
				event -> sendBudgetRoleChangedMessage(((BudgetRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetRole/\" plus one of the following: "
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
