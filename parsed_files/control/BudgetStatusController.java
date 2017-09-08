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
import com.skytala.eCommerce.command.AddBudgetStatus;
import com.skytala.eCommerce.command.DeleteBudgetStatus;
import com.skytala.eCommerce.command.UpdateBudgetStatus;
import com.skytala.eCommerce.entity.BudgetStatus;
import com.skytala.eCommerce.entity.BudgetStatusMapper;
import com.skytala.eCommerce.event.BudgetStatusAdded;
import com.skytala.eCommerce.event.BudgetStatusDeleted;
import com.skytala.eCommerce.event.BudgetStatusFound;
import com.skytala.eCommerce.event.BudgetStatusUpdated;
import com.skytala.eCommerce.query.FindBudgetStatussBy;

@RestController
@RequestMapping("/api/budgetStatus")
public class BudgetStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetStatus
	 * @return a List with the BudgetStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetStatus> findBudgetStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetStatussBy query = new FindBudgetStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetStatusFound.class,
				event -> sendBudgetStatussFoundMessage(((BudgetStatusFound) event).getBudgetStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetStatussFoundMessage(List<BudgetStatus> budgetStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetStatuss);
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
	public boolean createBudgetStatus(HttpServletRequest request) {

		BudgetStatus budgetStatusToBeAdded = new BudgetStatus();
		try {
			budgetStatusToBeAdded = BudgetStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetStatus(budgetStatusToBeAdded);

	}

	/**
	 * creates a new BudgetStatus entry in the ofbiz database
	 * 
	 * @param budgetStatusToBeAdded
	 *            the BudgetStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetStatus(BudgetStatus budgetStatusToBeAdded) {

		AddBudgetStatus com = new AddBudgetStatus(budgetStatusToBeAdded);
		int usedTicketId;

		synchronized (BudgetStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetStatusAdded.class,
				event -> sendBudgetStatusChangedMessage(((BudgetStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetStatus(HttpServletRequest request) {

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

		BudgetStatus budgetStatusToBeUpdated = new BudgetStatus();

		try {
			budgetStatusToBeUpdated = BudgetStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetStatus(budgetStatusToBeUpdated);

	}

	/**
	 * Updates the BudgetStatus with the specific Id
	 * 
	 * @param budgetStatusToBeUpdated the BudgetStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetStatus(BudgetStatus budgetStatusToBeUpdated) {

		UpdateBudgetStatus com = new UpdateBudgetStatus(budgetStatusToBeUpdated);

		int usedTicketId;

		synchronized (BudgetStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetStatusUpdated.class,
				event -> sendBudgetStatusChangedMessage(((BudgetStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetStatus from the database
	 * 
	 * @param budgetStatusId:
	 *            the id of the BudgetStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetStatusById(@RequestParam(value = "budgetStatusId") String budgetStatusId) {

		DeleteBudgetStatus com = new DeleteBudgetStatus(budgetStatusId);

		int usedTicketId;

		synchronized (BudgetStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetStatusDeleted.class,
				event -> sendBudgetStatusChangedMessage(((BudgetStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetStatus/\" plus one of the following: "
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
