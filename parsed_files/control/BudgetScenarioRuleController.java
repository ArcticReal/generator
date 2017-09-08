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
import com.skytala.eCommerce.command.AddBudgetScenarioRule;
import com.skytala.eCommerce.command.DeleteBudgetScenarioRule;
import com.skytala.eCommerce.command.UpdateBudgetScenarioRule;
import com.skytala.eCommerce.entity.BudgetScenarioRule;
import com.skytala.eCommerce.entity.BudgetScenarioRuleMapper;
import com.skytala.eCommerce.event.BudgetScenarioRuleAdded;
import com.skytala.eCommerce.event.BudgetScenarioRuleDeleted;
import com.skytala.eCommerce.event.BudgetScenarioRuleFound;
import com.skytala.eCommerce.event.BudgetScenarioRuleUpdated;
import com.skytala.eCommerce.query.FindBudgetScenarioRulesBy;

@RestController
@RequestMapping("/api/budgetScenarioRule")
public class BudgetScenarioRuleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetScenarioRule>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetScenarioRuleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetScenarioRule
	 * @return a List with the BudgetScenarioRules
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetScenarioRule> findBudgetScenarioRulesBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetScenarioRulesBy query = new FindBudgetScenarioRulesBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetScenarioRuleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioRuleFound.class,
				event -> sendBudgetScenarioRulesFoundMessage(((BudgetScenarioRuleFound) event).getBudgetScenarioRules(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetScenarioRulesFoundMessage(List<BudgetScenarioRule> budgetScenarioRules, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetScenarioRules);
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
	public boolean createBudgetScenarioRule(HttpServletRequest request) {

		BudgetScenarioRule budgetScenarioRuleToBeAdded = new BudgetScenarioRule();
		try {
			budgetScenarioRuleToBeAdded = BudgetScenarioRuleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetScenarioRule(budgetScenarioRuleToBeAdded);

	}

	/**
	 * creates a new BudgetScenarioRule entry in the ofbiz database
	 * 
	 * @param budgetScenarioRuleToBeAdded
	 *            the BudgetScenarioRule thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetScenarioRule(BudgetScenarioRule budgetScenarioRuleToBeAdded) {

		AddBudgetScenarioRule com = new AddBudgetScenarioRule(budgetScenarioRuleToBeAdded);
		int usedTicketId;

		synchronized (BudgetScenarioRuleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioRuleAdded.class,
				event -> sendBudgetScenarioRuleChangedMessage(((BudgetScenarioRuleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetScenarioRule(HttpServletRequest request) {

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

		BudgetScenarioRule budgetScenarioRuleToBeUpdated = new BudgetScenarioRule();

		try {
			budgetScenarioRuleToBeUpdated = BudgetScenarioRuleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetScenarioRule(budgetScenarioRuleToBeUpdated);

	}

	/**
	 * Updates the BudgetScenarioRule with the specific Id
	 * 
	 * @param budgetScenarioRuleToBeUpdated the BudgetScenarioRule thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetScenarioRule(BudgetScenarioRule budgetScenarioRuleToBeUpdated) {

		UpdateBudgetScenarioRule com = new UpdateBudgetScenarioRule(budgetScenarioRuleToBeUpdated);

		int usedTicketId;

		synchronized (BudgetScenarioRuleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioRuleUpdated.class,
				event -> sendBudgetScenarioRuleChangedMessage(((BudgetScenarioRuleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetScenarioRule from the database
	 * 
	 * @param budgetScenarioRuleId:
	 *            the id of the BudgetScenarioRule thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetScenarioRuleById(@RequestParam(value = "budgetScenarioRuleId") String budgetScenarioRuleId) {

		DeleteBudgetScenarioRule com = new DeleteBudgetScenarioRule(budgetScenarioRuleId);

		int usedTicketId;

		synchronized (BudgetScenarioRuleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioRuleDeleted.class,
				event -> sendBudgetScenarioRuleChangedMessage(((BudgetScenarioRuleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetScenarioRuleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetScenarioRule/\" plus one of the following: "
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
