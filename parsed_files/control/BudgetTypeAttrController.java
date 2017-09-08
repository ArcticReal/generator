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
import com.skytala.eCommerce.command.AddBudgetTypeAttr;
import com.skytala.eCommerce.command.DeleteBudgetTypeAttr;
import com.skytala.eCommerce.command.UpdateBudgetTypeAttr;
import com.skytala.eCommerce.entity.BudgetTypeAttr;
import com.skytala.eCommerce.entity.BudgetTypeAttrMapper;
import com.skytala.eCommerce.event.BudgetTypeAttrAdded;
import com.skytala.eCommerce.event.BudgetTypeAttrDeleted;
import com.skytala.eCommerce.event.BudgetTypeAttrFound;
import com.skytala.eCommerce.event.BudgetTypeAttrUpdated;
import com.skytala.eCommerce.query.FindBudgetTypeAttrsBy;

@RestController
@RequestMapping("/api/budgetTypeAttr")
public class BudgetTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetTypeAttr
	 * @return a List with the BudgetTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetTypeAttr> findBudgetTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetTypeAttrsBy query = new FindBudgetTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetTypeAttrFound.class,
				event -> sendBudgetTypeAttrsFoundMessage(((BudgetTypeAttrFound) event).getBudgetTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetTypeAttrsFoundMessage(List<BudgetTypeAttr> budgetTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetTypeAttrs);
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
	public boolean createBudgetTypeAttr(HttpServletRequest request) {

		BudgetTypeAttr budgetTypeAttrToBeAdded = new BudgetTypeAttr();
		try {
			budgetTypeAttrToBeAdded = BudgetTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetTypeAttr(budgetTypeAttrToBeAdded);

	}

	/**
	 * creates a new BudgetTypeAttr entry in the ofbiz database
	 * 
	 * @param budgetTypeAttrToBeAdded
	 *            the BudgetTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetTypeAttr(BudgetTypeAttr budgetTypeAttrToBeAdded) {

		AddBudgetTypeAttr com = new AddBudgetTypeAttr(budgetTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (BudgetTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetTypeAttrAdded.class,
				event -> sendBudgetTypeAttrChangedMessage(((BudgetTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetTypeAttr(HttpServletRequest request) {

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

		BudgetTypeAttr budgetTypeAttrToBeUpdated = new BudgetTypeAttr();

		try {
			budgetTypeAttrToBeUpdated = BudgetTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetTypeAttr(budgetTypeAttrToBeUpdated);

	}

	/**
	 * Updates the BudgetTypeAttr with the specific Id
	 * 
	 * @param budgetTypeAttrToBeUpdated the BudgetTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetTypeAttr(BudgetTypeAttr budgetTypeAttrToBeUpdated) {

		UpdateBudgetTypeAttr com = new UpdateBudgetTypeAttr(budgetTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (BudgetTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetTypeAttrUpdated.class,
				event -> sendBudgetTypeAttrChangedMessage(((BudgetTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetTypeAttr from the database
	 * 
	 * @param budgetTypeAttrId:
	 *            the id of the BudgetTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetTypeAttrById(@RequestParam(value = "budgetTypeAttrId") String budgetTypeAttrId) {

		DeleteBudgetTypeAttr com = new DeleteBudgetTypeAttr(budgetTypeAttrId);

		int usedTicketId;

		synchronized (BudgetTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetTypeAttrDeleted.class,
				event -> sendBudgetTypeAttrChangedMessage(((BudgetTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetTypeAttr/\" plus one of the following: "
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
