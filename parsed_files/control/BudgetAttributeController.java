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
import com.skytala.eCommerce.command.AddBudgetAttribute;
import com.skytala.eCommerce.command.DeleteBudgetAttribute;
import com.skytala.eCommerce.command.UpdateBudgetAttribute;
import com.skytala.eCommerce.entity.BudgetAttribute;
import com.skytala.eCommerce.entity.BudgetAttributeMapper;
import com.skytala.eCommerce.event.BudgetAttributeAdded;
import com.skytala.eCommerce.event.BudgetAttributeDeleted;
import com.skytala.eCommerce.event.BudgetAttributeFound;
import com.skytala.eCommerce.event.BudgetAttributeUpdated;
import com.skytala.eCommerce.query.FindBudgetAttributesBy;

@RestController
@RequestMapping("/api/budgetAttribute")
public class BudgetAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BudgetAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BudgetAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BudgetAttribute
	 * @return a List with the BudgetAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BudgetAttribute> findBudgetAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindBudgetAttributesBy query = new FindBudgetAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (BudgetAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetAttributeFound.class,
				event -> sendBudgetAttributesFoundMessage(((BudgetAttributeFound) event).getBudgetAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBudgetAttributesFoundMessage(List<BudgetAttribute> budgetAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, budgetAttributes);
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
	public boolean createBudgetAttribute(HttpServletRequest request) {

		BudgetAttribute budgetAttributeToBeAdded = new BudgetAttribute();
		try {
			budgetAttributeToBeAdded = BudgetAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBudgetAttribute(budgetAttributeToBeAdded);

	}

	/**
	 * creates a new BudgetAttribute entry in the ofbiz database
	 * 
	 * @param budgetAttributeToBeAdded
	 *            the BudgetAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBudgetAttribute(BudgetAttribute budgetAttributeToBeAdded) {

		AddBudgetAttribute com = new AddBudgetAttribute(budgetAttributeToBeAdded);
		int usedTicketId;

		synchronized (BudgetAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetAttributeAdded.class,
				event -> sendBudgetAttributeChangedMessage(((BudgetAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBudgetAttribute(HttpServletRequest request) {

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

		BudgetAttribute budgetAttributeToBeUpdated = new BudgetAttribute();

		try {
			budgetAttributeToBeUpdated = BudgetAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBudgetAttribute(budgetAttributeToBeUpdated);

	}

	/**
	 * Updates the BudgetAttribute with the specific Id
	 * 
	 * @param budgetAttributeToBeUpdated the BudgetAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBudgetAttribute(BudgetAttribute budgetAttributeToBeUpdated) {

		UpdateBudgetAttribute com = new UpdateBudgetAttribute(budgetAttributeToBeUpdated);

		int usedTicketId;

		synchronized (BudgetAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetAttributeUpdated.class,
				event -> sendBudgetAttributeChangedMessage(((BudgetAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BudgetAttribute from the database
	 * 
	 * @param budgetAttributeId:
	 *            the id of the BudgetAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebudgetAttributeById(@RequestParam(value = "budgetAttributeId") String budgetAttributeId) {

		DeleteBudgetAttribute com = new DeleteBudgetAttribute(budgetAttributeId);

		int usedTicketId;

		synchronized (BudgetAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BudgetAttributeDeleted.class,
				event -> sendBudgetAttributeChangedMessage(((BudgetAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBudgetAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/budgetAttribute/\" plus one of the following: "
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
