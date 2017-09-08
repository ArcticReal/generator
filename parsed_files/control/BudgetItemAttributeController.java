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
import com.skytala.eCommerce.command.AddBudgetItemAttribute;
import com.skytala.eCommerce.command.DeleteBudgetItemAttribute;
import com.skytala.eCommerce.command.UpdateBudgetItemAttribute;
import com.skytala.eCommerce.entity.BudgetItemAttribute;
import com.skytala.eCommerce.entity.BudgetItemAttributeMapper;
import com.skytala.eCommerce.event.BudgetItemAttributeAdded;
import com.skytala.eCommerce.event.BudgetItemAttributeDeleted;
import com.skytala.eCommerce.event.BudgetItemAttributeFound;
import com.skytala.eCommerce.event.BudgetItemAttributeUpdated;
import com.skytala.eCommerce.query.FindBudgetItemAttributesBy;

@RestController
@RequestMapping("/api/budgetItemAttribute")
public class BudgetItemAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetItemAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetItemAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetItemAttribute
	 * @return a List with the BudgetItemAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetItemAttribute> findBudgetItemAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetItemAttributesBy query = new FindBudgetItemAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetItemAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemAttributeFound.class,
				event -> sendBudgetItemAttributesFoundMessage(((BudgetItemAttributeFound) event).getBudgetItemAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetItemAttributesFoundMessage(List<BudgetItemAttribute> budgetItemAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetItemAttributes);
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
	public boolean createBudgetItemAttribute(HttpServletRequest request) {

		BudgetItemAttribute budgetItemAttributeToBeAdded = new BudgetItemAttribute();
		try {
			budgetItemAttributeToBeAdded = BudgetItemAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetItemAttribute(budgetItemAttributeToBeAdded);

	}

	/**
	 * creates a new BudgetItemAttribute entry in the ofbiz database
	 * 
	 * @param budgetItemAttributeToBeAdded
	 *            the BudgetItemAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetItemAttribute(BudgetItemAttribute budgetItemAttributeToBeAdded) {

		AddBudgetItemAttribute com = new AddBudgetItemAttribute(budgetItemAttributeToBeAdded);
		int usedTicketId;

		synchronized (BudgetItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemAttributeAdded.class,
				event -> sendBudgetItemAttributeChangedMessage(((BudgetItemAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetItemAttribute(HttpServletRequest request) {

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

		BudgetItemAttribute budgetItemAttributeToBeUpdated = new BudgetItemAttribute();

		try {
			budgetItemAttributeToBeUpdated = BudgetItemAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetItemAttribute(budgetItemAttributeToBeUpdated);

	}

	/**
	 * Updates the BudgetItemAttribute with the specific Id
	 * 
	 * @param budgetItemAttributeToBeUpdated the BudgetItemAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetItemAttribute(BudgetItemAttribute budgetItemAttributeToBeUpdated) {

		UpdateBudgetItemAttribute com = new UpdateBudgetItemAttribute(budgetItemAttributeToBeUpdated);

		int usedTicketId;

		synchronized (BudgetItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemAttributeUpdated.class,
				event -> sendBudgetItemAttributeChangedMessage(((BudgetItemAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetItemAttribute from the database
	 * 
	 * @param budgetItemAttributeId:
	 *            the id of the BudgetItemAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetItemAttributeById(@RequestParam(value = "budgetItemAttributeId") String budgetItemAttributeId) {

		DeleteBudgetItemAttribute com = new DeleteBudgetItemAttribute(budgetItemAttributeId);

		int usedTicketId;

		synchronized (BudgetItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetItemAttributeDeleted.class,
				event -> sendBudgetItemAttributeChangedMessage(((BudgetItemAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetItemAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetItemAttribute/\" plus one of the following: "
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
