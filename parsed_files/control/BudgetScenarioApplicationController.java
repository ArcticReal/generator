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
import com.skytala.eCommerce.command.AddBudgetScenarioApplication;
import com.skytala.eCommerce.command.DeleteBudgetScenarioApplication;
import com.skytala.eCommerce.command.UpdateBudgetScenarioApplication;
import com.skytala.eCommerce.entity.BudgetScenarioApplication;
import com.skytala.eCommerce.entity.BudgetScenarioApplicationMapper;
import com.skytala.eCommerce.event.BudgetScenarioApplicationAdded;
import com.skytala.eCommerce.event.BudgetScenarioApplicationDeleted;
import com.skytala.eCommerce.event.BudgetScenarioApplicationFound;
import com.skytala.eCommerce.event.BudgetScenarioApplicationUpdated;
import com.skytala.eCommerce.query.FindBudgetScenarioApplicationsBy;

@RestController
@RequestMapping("/api/budgetScenarioApplication")
public class BudgetScenarioApplicationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetScenarioApplication>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetScenarioApplicationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetScenarioApplication
	 * @return a List with the BudgetScenarioApplications
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetScenarioApplication> findBudgetScenarioApplicationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetScenarioApplicationsBy query = new FindBudgetScenarioApplicationsBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetScenarioApplicationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioApplicationFound.class,
				event -> sendBudgetScenarioApplicationsFoundMessage(((BudgetScenarioApplicationFound) event).getBudgetScenarioApplications(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetScenarioApplicationsFoundMessage(List<BudgetScenarioApplication> budgetScenarioApplications, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetScenarioApplications);
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
	public boolean createBudgetScenarioApplication(HttpServletRequest request) {

		BudgetScenarioApplication budgetScenarioApplicationToBeAdded = new BudgetScenarioApplication();
		try {
			budgetScenarioApplicationToBeAdded = BudgetScenarioApplicationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetScenarioApplication(budgetScenarioApplicationToBeAdded);

	}

	/**
	 * creates a new BudgetScenarioApplication entry in the ofbiz database
	 * 
	 * @param budgetScenarioApplicationToBeAdded
	 *            the BudgetScenarioApplication thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetScenarioApplication(BudgetScenarioApplication budgetScenarioApplicationToBeAdded) {

		AddBudgetScenarioApplication com = new AddBudgetScenarioApplication(budgetScenarioApplicationToBeAdded);
		int usedTicketId;

		synchronized (BudgetScenarioApplicationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioApplicationAdded.class,
				event -> sendBudgetScenarioApplicationChangedMessage(((BudgetScenarioApplicationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetScenarioApplication(HttpServletRequest request) {

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

		BudgetScenarioApplication budgetScenarioApplicationToBeUpdated = new BudgetScenarioApplication();

		try {
			budgetScenarioApplicationToBeUpdated = BudgetScenarioApplicationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetScenarioApplication(budgetScenarioApplicationToBeUpdated);

	}

	/**
	 * Updates the BudgetScenarioApplication with the specific Id
	 * 
	 * @param budgetScenarioApplicationToBeUpdated the BudgetScenarioApplication thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetScenarioApplication(BudgetScenarioApplication budgetScenarioApplicationToBeUpdated) {

		UpdateBudgetScenarioApplication com = new UpdateBudgetScenarioApplication(budgetScenarioApplicationToBeUpdated);

		int usedTicketId;

		synchronized (BudgetScenarioApplicationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioApplicationUpdated.class,
				event -> sendBudgetScenarioApplicationChangedMessage(((BudgetScenarioApplicationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetScenarioApplication from the database
	 * 
	 * @param budgetScenarioApplicationId:
	 *            the id of the BudgetScenarioApplication thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetScenarioApplicationById(@RequestParam(value = "budgetScenarioApplicationId") String budgetScenarioApplicationId) {

		DeleteBudgetScenarioApplication com = new DeleteBudgetScenarioApplication(budgetScenarioApplicationId);

		int usedTicketId;

		synchronized (BudgetScenarioApplicationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioApplicationDeleted.class,
				event -> sendBudgetScenarioApplicationChangedMessage(((BudgetScenarioApplicationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetScenarioApplicationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetScenarioApplication/\" plus one of the following: "
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
