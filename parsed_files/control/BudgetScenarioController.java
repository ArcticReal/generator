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
import com.skytala.eCommerce.command.AddBudgetScenario;
import com.skytala.eCommerce.command.DeleteBudgetScenario;
import com.skytala.eCommerce.command.UpdateBudgetScenario;
import com.skytala.eCommerce.entity.BudgetScenario;
import com.skytala.eCommerce.entity.BudgetScenarioMapper;
import com.skytala.eCommerce.event.BudgetScenarioAdded;
import com.skytala.eCommerce.event.BudgetScenarioDeleted;
import com.skytala.eCommerce.event.BudgetScenarioFound;
import com.skytala.eCommerce.event.BudgetScenarioUpdated;
import com.skytala.eCommerce.query.FindBudgetScenariosBy;

@RestController
@RequestMapping("/api/budgetScenario")
public class BudgetScenarioController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetScenario>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetScenarioController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetScenario
	 * @return a List with the BudgetScenarios
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetScenario> findBudgetScenariosBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetScenariosBy query = new FindBudgetScenariosBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetScenarioController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioFound.class,
				event -> sendBudgetScenariosFoundMessage(((BudgetScenarioFound) event).getBudgetScenarios(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetScenariosFoundMessage(List<BudgetScenario> budgetScenarios, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetScenarios);
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
	public boolean createBudgetScenario(HttpServletRequest request) {

		BudgetScenario budgetScenarioToBeAdded = new BudgetScenario();
		try {
			budgetScenarioToBeAdded = BudgetScenarioMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetScenario(budgetScenarioToBeAdded);

	}

	/**
	 * creates a new BudgetScenario entry in the ofbiz database
	 * 
	 * @param budgetScenarioToBeAdded
	 *            the BudgetScenario thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetScenario(BudgetScenario budgetScenarioToBeAdded) {

		AddBudgetScenario com = new AddBudgetScenario(budgetScenarioToBeAdded);
		int usedTicketId;

		synchronized (BudgetScenarioController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioAdded.class,
				event -> sendBudgetScenarioChangedMessage(((BudgetScenarioAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetScenario(HttpServletRequest request) {

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

		BudgetScenario budgetScenarioToBeUpdated = new BudgetScenario();

		try {
			budgetScenarioToBeUpdated = BudgetScenarioMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetScenario(budgetScenarioToBeUpdated);

	}

	/**
	 * Updates the BudgetScenario with the specific Id
	 * 
	 * @param budgetScenarioToBeUpdated the BudgetScenario thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetScenario(BudgetScenario budgetScenarioToBeUpdated) {

		UpdateBudgetScenario com = new UpdateBudgetScenario(budgetScenarioToBeUpdated);

		int usedTicketId;

		synchronized (BudgetScenarioController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioUpdated.class,
				event -> sendBudgetScenarioChangedMessage(((BudgetScenarioUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetScenario from the database
	 * 
	 * @param budgetScenarioId:
	 *            the id of the BudgetScenario thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetScenarioById(@RequestParam(value = "budgetScenarioId") String budgetScenarioId) {

		DeleteBudgetScenario com = new DeleteBudgetScenario(budgetScenarioId);

		int usedTicketId;

		synchronized (BudgetScenarioController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetScenarioDeleted.class,
				event -> sendBudgetScenarioChangedMessage(((BudgetScenarioDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetScenarioChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetScenario/\" plus one of the following: "
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
