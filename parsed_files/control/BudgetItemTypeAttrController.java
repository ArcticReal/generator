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
import com.skytala.eCommerce.command.AddBudgetItemTypeAttr;
import com.skytala.eCommerce.command.DeleteBudgetItemTypeAttr;
import com.skytala.eCommerce.command.UpdateBudgetItemTypeAttr;
import com.skytala.eCommerce.entity.BudgetItemTypeAttr;
import com.skytala.eCommerce.entity.BudgetItemTypeAttrMapper;
import com.skytala.eCommerce.event.BudgetItemTypeAttrAdded;
import com.skytala.eCommerce.event.BudgetItemTypeAttrDeleted;
import com.skytala.eCommerce.event.BudgetItemTypeAttrFound;
import com.skytala.eCommerce.event.BudgetItemTypeAttrUpdated;
import com.skytala.eCommerce.query.FindBudgetItemTypeAttrsBy;

@RestController
@RequestMapping("/api/budgetItemTypeAttr")
public class BudgetItemTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetItemTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetItemTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetItemTypeAttr
	 * @return a List with the BudgetItemTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetItemTypeAttr> findBudgetItemTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetItemTypeAttrsBy query = new FindBudgetItemTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetItemTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemTypeAttrFound.class,
				event -> sendBudgetItemTypeAttrsFoundMessage(((BudgetItemTypeAttrFound) event).getBudgetItemTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetItemTypeAttrsFoundMessage(List<BudgetItemTypeAttr> budgetItemTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetItemTypeAttrs);
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
	public boolean createBudgetItemTypeAttr(HttpServletRequest request) {

		BudgetItemTypeAttr budgetItemTypeAttrToBeAdded = new BudgetItemTypeAttr();
		try {
			budgetItemTypeAttrToBeAdded = BudgetItemTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetItemTypeAttr(budgetItemTypeAttrToBeAdded);

	}

	/**
	 * creates a new BudgetItemTypeAttr entry in the ofbiz database
	 * 
	 * @param budgetItemTypeAttrToBeAdded
	 *            the BudgetItemTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetItemTypeAttr(BudgetItemTypeAttr budgetItemTypeAttrToBeAdded) {

		AddBudgetItemTypeAttr com = new AddBudgetItemTypeAttr(budgetItemTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (BudgetItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemTypeAttrAdded.class,
				event -> sendBudgetItemTypeAttrChangedMessage(((BudgetItemTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetItemTypeAttr(HttpServletRequest request) {

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

		BudgetItemTypeAttr budgetItemTypeAttrToBeUpdated = new BudgetItemTypeAttr();

		try {
			budgetItemTypeAttrToBeUpdated = BudgetItemTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetItemTypeAttr(budgetItemTypeAttrToBeUpdated);

	}

	/**
	 * Updates the BudgetItemTypeAttr with the specific Id
	 * 
	 * @param budgetItemTypeAttrToBeUpdated the BudgetItemTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetItemTypeAttr(BudgetItemTypeAttr budgetItemTypeAttrToBeUpdated) {

		UpdateBudgetItemTypeAttr com = new UpdateBudgetItemTypeAttr(budgetItemTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (BudgetItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemTypeAttrUpdated.class,
				event -> sendBudgetItemTypeAttrChangedMessage(((BudgetItemTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetItemTypeAttr from the database
	 * 
	 * @param budgetItemTypeAttrId:
	 *            the id of the BudgetItemTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetItemTypeAttrById(@RequestParam(value = "budgetItemTypeAttrId") String budgetItemTypeAttrId) {

		DeleteBudgetItemTypeAttr com = new DeleteBudgetItemTypeAttr(budgetItemTypeAttrId);

		int usedTicketId;

		synchronized (BudgetItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemTypeAttrDeleted.class,
				event -> sendBudgetItemTypeAttrChangedMessage(((BudgetItemTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetItemTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetItemTypeAttr/\" plus one of the following: "
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
