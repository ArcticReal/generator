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
import com.skytala.eCommerce.command.AddBudgetItem;
import com.skytala.eCommerce.command.DeleteBudgetItem;
import com.skytala.eCommerce.command.UpdateBudgetItem;
import com.skytala.eCommerce.entity.BudgetItem;
import com.skytala.eCommerce.entity.BudgetItemMapper;
import com.skytala.eCommerce.event.BudgetItemAdded;
import com.skytala.eCommerce.event.BudgetItemDeleted;
import com.skytala.eCommerce.event.BudgetItemFound;
import com.skytala.eCommerce.event.BudgetItemUpdated;
import com.skytala.eCommerce.query.FindBudgetItemsBy;

@RestController
@RequestMapping("/api/budgetItem")
public class BudgetItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetItem
	 * @return a List with the BudgetItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetItem> findBudgetItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetItemsBy query = new FindBudgetItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemFound.class,
				event -> sendBudgetItemsFoundMessage(((BudgetItemFound) event).getBudgetItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetItemsFoundMessage(List<BudgetItem> budgetItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetItems);
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
	public boolean createBudgetItem(HttpServletRequest request) {

		BudgetItem budgetItemToBeAdded = new BudgetItem();
		try {
			budgetItemToBeAdded = BudgetItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetItem(budgetItemToBeAdded);

	}

	/**
	 * creates a new BudgetItem entry in the ofbiz database
	 * 
	 * @param budgetItemToBeAdded
	 *            the BudgetItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetItem(BudgetItem budgetItemToBeAdded) {

		AddBudgetItem com = new AddBudgetItem(budgetItemToBeAdded);
		int usedTicketId;

		synchronized (BudgetItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemAdded.class,
				event -> sendBudgetItemChangedMessage(((BudgetItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetItem(HttpServletRequest request) {

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

		BudgetItem budgetItemToBeUpdated = new BudgetItem();

		try {
			budgetItemToBeUpdated = BudgetItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetItem(budgetItemToBeUpdated);

	}

	/**
	 * Updates the BudgetItem with the specific Id
	 * 
	 * @param budgetItemToBeUpdated the BudgetItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetItem(BudgetItem budgetItemToBeUpdated) {

		UpdateBudgetItem com = new UpdateBudgetItem(budgetItemToBeUpdated);

		int usedTicketId;

		synchronized (BudgetItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemUpdated.class,
				event -> sendBudgetItemChangedMessage(((BudgetItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetItem from the database
	 * 
	 * @param budgetItemId:
	 *            the id of the BudgetItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetItemById(@RequestParam(value = "budgetItemId") String budgetItemId) {

		DeleteBudgetItem com = new DeleteBudgetItem(budgetItemId);

		int usedTicketId;

		synchronized (BudgetItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemDeleted.class,
				event -> sendBudgetItemChangedMessage(((BudgetItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetItem/\" plus one of the following: "
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
