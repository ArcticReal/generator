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
import com.skytala.eCommerce.command.AddBudgetItemType;
import com.skytala.eCommerce.command.DeleteBudgetItemType;
import com.skytala.eCommerce.command.UpdateBudgetItemType;
import com.skytala.eCommerce.entity.BudgetItemType;
import com.skytala.eCommerce.entity.BudgetItemTypeMapper;
import com.skytala.eCommerce.event.BudgetItemTypeAdded;
import com.skytala.eCommerce.event.BudgetItemTypeDeleted;
import com.skytala.eCommerce.event.BudgetItemTypeFound;
import com.skytala.eCommerce.event.BudgetItemTypeUpdated;
import com.skytala.eCommerce.query.FindBudgetItemTypesBy;

@RestController
@RequestMapping("/api/budgetItemType")
public class BudgetItemTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetItemType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetItemTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetItemType
	 * @return a List with the BudgetItemTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetItemType> findBudgetItemTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetItemTypesBy query = new FindBudgetItemTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetItemTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemTypeFound.class,
				event -> sendBudgetItemTypesFoundMessage(((BudgetItemTypeFound) event).getBudgetItemTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetItemTypesFoundMessage(List<BudgetItemType> budgetItemTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetItemTypes);
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
	public boolean createBudgetItemType(HttpServletRequest request) {

		BudgetItemType budgetItemTypeToBeAdded = new BudgetItemType();
		try {
			budgetItemTypeToBeAdded = BudgetItemTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetItemType(budgetItemTypeToBeAdded);

	}

	/**
	 * creates a new BudgetItemType entry in the ofbiz database
	 * 
	 * @param budgetItemTypeToBeAdded
	 *            the BudgetItemType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetItemType(BudgetItemType budgetItemTypeToBeAdded) {

		AddBudgetItemType com = new AddBudgetItemType(budgetItemTypeToBeAdded);
		int usedTicketId;

		synchronized (BudgetItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemTypeAdded.class,
				event -> sendBudgetItemTypeChangedMessage(((BudgetItemTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetItemType(HttpServletRequest request) {

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

		BudgetItemType budgetItemTypeToBeUpdated = new BudgetItemType();

		try {
			budgetItemTypeToBeUpdated = BudgetItemTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetItemType(budgetItemTypeToBeUpdated);

	}

	/**
	 * Updates the BudgetItemType with the specific Id
	 * 
	 * @param budgetItemTypeToBeUpdated the BudgetItemType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetItemType(BudgetItemType budgetItemTypeToBeUpdated) {

		UpdateBudgetItemType com = new UpdateBudgetItemType(budgetItemTypeToBeUpdated);

		int usedTicketId;

		synchronized (BudgetItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemTypeUpdated.class,
				event -> sendBudgetItemTypeChangedMessage(((BudgetItemTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetItemType from the database
	 * 
	 * @param budgetItemTypeId:
	 *            the id of the BudgetItemType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetItemTypeById(@RequestParam(value = "budgetItemTypeId") String budgetItemTypeId) {

		DeleteBudgetItemType com = new DeleteBudgetItemType(budgetItemTypeId);

		int usedTicketId;

		synchronized (BudgetItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemTypeDeleted.class,
				event -> sendBudgetItemTypeChangedMessage(((BudgetItemTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetItemTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetItemType/\" plus one of the following: "
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
