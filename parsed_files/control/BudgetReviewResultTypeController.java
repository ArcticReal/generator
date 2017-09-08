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
import com.skytala.eCommerce.command.AddBudgetReviewResultType;
import com.skytala.eCommerce.command.DeleteBudgetReviewResultType;
import com.skytala.eCommerce.command.UpdateBudgetReviewResultType;
import com.skytala.eCommerce.entity.BudgetReviewResultType;
import com.skytala.eCommerce.entity.BudgetReviewResultTypeMapper;
import com.skytala.eCommerce.event.BudgetReviewResultTypeAdded;
import com.skytala.eCommerce.event.BudgetReviewResultTypeDeleted;
import com.skytala.eCommerce.event.BudgetReviewResultTypeFound;
import com.skytala.eCommerce.event.BudgetReviewResultTypeUpdated;
import com.skytala.eCommerce.query.FindBudgetReviewResultTypesBy;

@RestController
@RequestMapping("/api/budgetReviewResultType")
public class BudgetReviewResultTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetReviewResultType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetReviewResultTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetReviewResultType
	 * @return a List with the BudgetReviewResultTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetReviewResultType> findBudgetReviewResultTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetReviewResultTypesBy query = new FindBudgetReviewResultTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetReviewResultTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetReviewResultTypeFound.class,
				event -> sendBudgetReviewResultTypesFoundMessage(((BudgetReviewResultTypeFound) event).getBudgetReviewResultTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetReviewResultTypesFoundMessage(List<BudgetReviewResultType> budgetReviewResultTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetReviewResultTypes);
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
	public boolean createBudgetReviewResultType(HttpServletRequest request) {

		BudgetReviewResultType budgetReviewResultTypeToBeAdded = new BudgetReviewResultType();
		try {
			budgetReviewResultTypeToBeAdded = BudgetReviewResultTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetReviewResultType(budgetReviewResultTypeToBeAdded);

	}

	/**
	 * creates a new BudgetReviewResultType entry in the ofbiz database
	 * 
	 * @param budgetReviewResultTypeToBeAdded
	 *            the BudgetReviewResultType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetReviewResultType(BudgetReviewResultType budgetReviewResultTypeToBeAdded) {

		AddBudgetReviewResultType com = new AddBudgetReviewResultType(budgetReviewResultTypeToBeAdded);
		int usedTicketId;

		synchronized (BudgetReviewResultTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetReviewResultTypeAdded.class,
				event -> sendBudgetReviewResultTypeChangedMessage(((BudgetReviewResultTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetReviewResultType(HttpServletRequest request) {

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

		BudgetReviewResultType budgetReviewResultTypeToBeUpdated = new BudgetReviewResultType();

		try {
			budgetReviewResultTypeToBeUpdated = BudgetReviewResultTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetReviewResultType(budgetReviewResultTypeToBeUpdated);

	}

	/**
	 * Updates the BudgetReviewResultType with the specific Id
	 * 
	 * @param budgetReviewResultTypeToBeUpdated the BudgetReviewResultType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetReviewResultType(BudgetReviewResultType budgetReviewResultTypeToBeUpdated) {

		UpdateBudgetReviewResultType com = new UpdateBudgetReviewResultType(budgetReviewResultTypeToBeUpdated);

		int usedTicketId;

		synchronized (BudgetReviewResultTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetReviewResultTypeUpdated.class,
				event -> sendBudgetReviewResultTypeChangedMessage(((BudgetReviewResultTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetReviewResultType from the database
	 * 
	 * @param budgetReviewResultTypeId:
	 *            the id of the BudgetReviewResultType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetReviewResultTypeById(@RequestParam(value = "budgetReviewResultTypeId") String budgetReviewResultTypeId) {

		DeleteBudgetReviewResultType com = new DeleteBudgetReviewResultType(budgetReviewResultTypeId);

		int usedTicketId;

		synchronized (BudgetReviewResultTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetReviewResultTypeDeleted.class,
				event -> sendBudgetReviewResultTypeChangedMessage(((BudgetReviewResultTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetReviewResultTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetReviewResultType/\" plus one of the following: "
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
