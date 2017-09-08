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
import com.skytala.eCommerce.command.AddBudget;
import com.skytala.eCommerce.command.DeleteBudget;
import com.skytala.eCommerce.command.UpdateBudget;
import com.skytala.eCommerce.entity.Budget;
import com.skytala.eCommerce.entity.BudgetMapper;
import com.skytala.eCommerce.event.BudgetAdded;
import com.skytala.eCommerce.event.BudgetDeleted;
import com.skytala.eCommerce.event.BudgetFound;
import com.skytala.eCommerce.event.BudgetUpdated;
import com.skytala.eCommerce.query.FindBudgetsBy;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Budget>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Budget
	 * @return a List with the Budgets
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Budget> findBudgetsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetsBy query = new FindBudgetsBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetFound.class,
				event -> sendBudgetsFoundMessage(((BudgetFound) event).getBudgets(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetsFoundMessage(List<Budget> budgets, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgets);
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
	public boolean createBudget(HttpServletRequest request) {

		Budget budgetToBeAdded = new Budget();
		try {
			budgetToBeAdded = BudgetMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudget(budgetToBeAdded);

	}

	/**
	 * creates a new Budget entry in the ofbiz database
	 * 
	 * @param budgetToBeAdded
	 *            the Budget thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudget(Budget budgetToBeAdded) {

		AddBudget com = new AddBudget(budgetToBeAdded);
		int usedTicketId;

		synchronized (BudgetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetAdded.class,
				event -> sendBudgetChangedMessage(((BudgetAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudget(HttpServletRequest request) {

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

		Budget budgetToBeUpdated = new Budget();

		try {
			budgetToBeUpdated = BudgetMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudget(budgetToBeUpdated);

	}

	/**
	 * Updates the Budget with the specific Id
	 * 
	 * @param budgetToBeUpdated the Budget thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudget(Budget budgetToBeUpdated) {

		UpdateBudget com = new UpdateBudget(budgetToBeUpdated);

		int usedTicketId;

		synchronized (BudgetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetUpdated.class,
				event -> sendBudgetChangedMessage(((BudgetUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Budget from the database
	 * 
	 * @param budgetId:
	 *            the id of the Budget thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetById(@RequestParam(value = "budgetId") String budgetId) {

		DeleteBudget com = new DeleteBudget(budgetId);

		int usedTicketId;

		synchronized (BudgetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetDeleted.class,
				event -> sendBudgetChangedMessage(((BudgetDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budget/\" plus one of the following: "
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
