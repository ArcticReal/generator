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
import com.skytala.eCommerce.command.AddBudgetType;
import com.skytala.eCommerce.command.DeleteBudgetType;
import com.skytala.eCommerce.command.UpdateBudgetType;
import com.skytala.eCommerce.entity.BudgetType;
import com.skytala.eCommerce.entity.BudgetTypeMapper;
import com.skytala.eCommerce.event.BudgetTypeAdded;
import com.skytala.eCommerce.event.BudgetTypeDeleted;
import com.skytala.eCommerce.event.BudgetTypeFound;
import com.skytala.eCommerce.event.BudgetTypeUpdated;
import com.skytala.eCommerce.query.FindBudgetTypesBy;

@RestController
@RequestMapping("/api/budgetType")
public class BudgetTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetType
	 * @return a List with the BudgetTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetType> findBudgetTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetTypesBy query = new FindBudgetTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetTypeFound.class,
				event -> sendBudgetTypesFoundMessage(((BudgetTypeFound) event).getBudgetTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetTypesFoundMessage(List<BudgetType> budgetTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetTypes);
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
	public boolean createBudgetType(HttpServletRequest request) {

		BudgetType budgetTypeToBeAdded = new BudgetType();
		try {
			budgetTypeToBeAdded = BudgetTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetType(budgetTypeToBeAdded);

	}

	/**
	 * creates a new BudgetType entry in the ofbiz database
	 * 
	 * @param budgetTypeToBeAdded
	 *            the BudgetType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetType(BudgetType budgetTypeToBeAdded) {

		AddBudgetType com = new AddBudgetType(budgetTypeToBeAdded);
		int usedTicketId;

		synchronized (BudgetTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetTypeAdded.class,
				event -> sendBudgetTypeChangedMessage(((BudgetTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetType(HttpServletRequest request) {

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

		BudgetType budgetTypeToBeUpdated = new BudgetType();

		try {
			budgetTypeToBeUpdated = BudgetTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetType(budgetTypeToBeUpdated);

	}

	/**
	 * Updates the BudgetType with the specific Id
	 * 
	 * @param budgetTypeToBeUpdated the BudgetType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetType(BudgetType budgetTypeToBeUpdated) {

		UpdateBudgetType com = new UpdateBudgetType(budgetTypeToBeUpdated);

		int usedTicketId;

		synchronized (BudgetTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetTypeUpdated.class,
				event -> sendBudgetTypeChangedMessage(((BudgetTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetType from the database
	 * 
	 * @param budgetTypeId:
	 *            the id of the BudgetType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetTypeById(@RequestParam(value = "budgetTypeId") String budgetTypeId) {

		DeleteBudgetType com = new DeleteBudgetType(budgetTypeId);

		int usedTicketId;

		synchronized (BudgetTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetTypeDeleted.class,
				event -> sendBudgetTypeChangedMessage(((BudgetTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetType/\" plus one of the following: "
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
