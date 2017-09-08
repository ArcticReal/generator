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
import com.skytala.eCommerce.command.AddBudgetRevision;
import com.skytala.eCommerce.command.DeleteBudgetRevision;
import com.skytala.eCommerce.command.UpdateBudgetRevision;
import com.skytala.eCommerce.entity.BudgetRevision;
import com.skytala.eCommerce.entity.BudgetRevisionMapper;
import com.skytala.eCommerce.event.BudgetRevisionAdded;
import com.skytala.eCommerce.event.BudgetRevisionDeleted;
import com.skytala.eCommerce.event.BudgetRevisionFound;
import com.skytala.eCommerce.event.BudgetRevisionUpdated;
import com.skytala.eCommerce.query.FindBudgetRevisionsBy;

@RestController
@RequestMapping("/api/budgetRevision")
public class BudgetRevisionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetRevision>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetRevisionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetRevision
	 * @return a List with the BudgetRevisions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetRevision> findBudgetRevisionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetRevisionsBy query = new FindBudgetRevisionsBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetRevisionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRevisionFound.class,
				event -> sendBudgetRevisionsFoundMessage(((BudgetRevisionFound) event).getBudgetRevisions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetRevisionsFoundMessage(List<BudgetRevision> budgetRevisions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetRevisions);
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
	public boolean createBudgetRevision(HttpServletRequest request) {

		BudgetRevision budgetRevisionToBeAdded = new BudgetRevision();
		try {
			budgetRevisionToBeAdded = BudgetRevisionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetRevision(budgetRevisionToBeAdded);

	}

	/**
	 * creates a new BudgetRevision entry in the ofbiz database
	 * 
	 * @param budgetRevisionToBeAdded
	 *            the BudgetRevision thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetRevision(BudgetRevision budgetRevisionToBeAdded) {

		AddBudgetRevision com = new AddBudgetRevision(budgetRevisionToBeAdded);
		int usedTicketId;

		synchronized (BudgetRevisionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRevisionAdded.class,
				event -> sendBudgetRevisionChangedMessage(((BudgetRevisionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetRevision(HttpServletRequest request) {

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

		BudgetRevision budgetRevisionToBeUpdated = new BudgetRevision();

		try {
			budgetRevisionToBeUpdated = BudgetRevisionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetRevision(budgetRevisionToBeUpdated);

	}

	/**
	 * Updates the BudgetRevision with the specific Id
	 * 
	 * @param budgetRevisionToBeUpdated the BudgetRevision thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetRevision(BudgetRevision budgetRevisionToBeUpdated) {

		UpdateBudgetRevision com = new UpdateBudgetRevision(budgetRevisionToBeUpdated);

		int usedTicketId;

		synchronized (BudgetRevisionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRevisionUpdated.class,
				event -> sendBudgetRevisionChangedMessage(((BudgetRevisionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetRevision from the database
	 * 
	 * @param budgetRevisionId:
	 *            the id of the BudgetRevision thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetRevisionById(@RequestParam(value = "budgetRevisionId") String budgetRevisionId) {

		DeleteBudgetRevision com = new DeleteBudgetRevision(budgetRevisionId);

		int usedTicketId;

		synchronized (BudgetRevisionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetRevisionDeleted.class,
				event -> sendBudgetRevisionChangedMessage(((BudgetRevisionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetRevisionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetRevision/\" plus one of the following: "
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
