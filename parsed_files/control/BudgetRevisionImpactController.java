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
import com.skytala.eCommerce.command.AddBudgetRevisionImpact;
import com.skytala.eCommerce.command.DeleteBudgetRevisionImpact;
import com.skytala.eCommerce.command.UpdateBudgetRevisionImpact;
import com.skytala.eCommerce.entity.BudgetRevisionImpact;
import com.skytala.eCommerce.entity.BudgetRevisionImpactMapper;
import com.skytala.eCommerce.event.BudgetRevisionImpactAdded;
import com.skytala.eCommerce.event.BudgetRevisionImpactDeleted;
import com.skytala.eCommerce.event.BudgetRevisionImpactFound;
import com.skytala.eCommerce.event.BudgetRevisionImpactUpdated;
import com.skytala.eCommerce.query.FindBudgetRevisionImpactsBy;

@RestController
@RequestMapping("/api/budgetRevisionImpact")
public class BudgetRevisionImpactController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetRevisionImpact>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetRevisionImpactController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetRevisionImpact
	 * @return a List with the BudgetRevisionImpacts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetRevisionImpact> findBudgetRevisionImpactsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetRevisionImpactsBy query = new FindBudgetRevisionImpactsBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetRevisionImpactController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRevisionImpactFound.class,
				event -> sendBudgetRevisionImpactsFoundMessage(((BudgetRevisionImpactFound) event).getBudgetRevisionImpacts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetRevisionImpactsFoundMessage(List<BudgetRevisionImpact> budgetRevisionImpacts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetRevisionImpacts);
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
	public boolean createBudgetRevisionImpact(HttpServletRequest request) {

		BudgetRevisionImpact budgetRevisionImpactToBeAdded = new BudgetRevisionImpact();
		try {
			budgetRevisionImpactToBeAdded = BudgetRevisionImpactMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetRevisionImpact(budgetRevisionImpactToBeAdded);

	}

	/**
	 * creates a new BudgetRevisionImpact entry in the ofbiz database
	 * 
	 * @param budgetRevisionImpactToBeAdded
	 *            the BudgetRevisionImpact thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetRevisionImpact(BudgetRevisionImpact budgetRevisionImpactToBeAdded) {

		AddBudgetRevisionImpact com = new AddBudgetRevisionImpact(budgetRevisionImpactToBeAdded);
		int usedTicketId;

		synchronized (BudgetRevisionImpactController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRevisionImpactAdded.class,
				event -> sendBudgetRevisionImpactChangedMessage(((BudgetRevisionImpactAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetRevisionImpact(HttpServletRequest request) {

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

		BudgetRevisionImpact budgetRevisionImpactToBeUpdated = new BudgetRevisionImpact();

		try {
			budgetRevisionImpactToBeUpdated = BudgetRevisionImpactMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetRevisionImpact(budgetRevisionImpactToBeUpdated);

	}

	/**
	 * Updates the BudgetRevisionImpact with the specific Id
	 * 
	 * @param budgetRevisionImpactToBeUpdated the BudgetRevisionImpact thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetRevisionImpact(BudgetRevisionImpact budgetRevisionImpactToBeUpdated) {

		UpdateBudgetRevisionImpact com = new UpdateBudgetRevisionImpact(budgetRevisionImpactToBeUpdated);

		int usedTicketId;

		synchronized (BudgetRevisionImpactController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRevisionImpactUpdated.class,
				event -> sendBudgetRevisionImpactChangedMessage(((BudgetRevisionImpactUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetRevisionImpact from the database
	 * 
	 * @param budgetRevisionImpactId:
	 *            the id of the BudgetRevisionImpact thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetRevisionImpactById(@RequestParam(value = "budgetRevisionImpactId") String budgetRevisionImpactId) {

		DeleteBudgetRevisionImpact com = new DeleteBudgetRevisionImpact(budgetRevisionImpactId);

		int usedTicketId;

		synchronized (BudgetRevisionImpactController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRevisionImpactDeleted.class,
				event -> sendBudgetRevisionImpactChangedMessage(((BudgetRevisionImpactDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetRevisionImpactChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetRevisionImpact/\" plus one of the following: "
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
