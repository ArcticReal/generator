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
import com.skytala.eCommerce.command.AddReturnItemBilling;
import com.skytala.eCommerce.command.DeleteReturnItemBilling;
import com.skytala.eCommerce.command.UpdateReturnItemBilling;
import com.skytala.eCommerce.entity.ReturnItemBilling;
import com.skytala.eCommerce.entity.ReturnItemBillingMapper;
import com.skytala.eCommerce.event.ReturnItemBillingAdded;
import com.skytala.eCommerce.event.ReturnItemBillingDeleted;
import com.skytala.eCommerce.event.ReturnItemBillingFound;
import com.skytala.eCommerce.event.ReturnItemBillingUpdated;
import com.skytala.eCommerce.query.FindReturnItemBillingsBy;

@RestController
@RequestMapping("/api/returnItemBilling")
public class ReturnItemBillingController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnItemBilling>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnItemBillingController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnItemBilling
	 * @return a List with the ReturnItemBillings
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnItemBilling> findReturnItemBillingsBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnItemBillingsBy query = new FindReturnItemBillingsBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnItemBillingController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemBillingFound.class,
				event -> sendReturnItemBillingsFoundMessage(((ReturnItemBillingFound) event).getReturnItemBillings(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnItemBillingsFoundMessage(List<ReturnItemBilling> returnItemBillings, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnItemBillings);
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
	public boolean createReturnItemBilling(HttpServletRequest request) {

		ReturnItemBilling returnItemBillingToBeAdded = new ReturnItemBilling();
		try {
			returnItemBillingToBeAdded = ReturnItemBillingMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnItemBilling(returnItemBillingToBeAdded);

	}

	/**
	 * creates a new ReturnItemBilling entry in the ofbiz database
	 * 
	 * @param returnItemBillingToBeAdded
	 *            the ReturnItemBilling thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnItemBilling(ReturnItemBilling returnItemBillingToBeAdded) {

		AddReturnItemBilling com = new AddReturnItemBilling(returnItemBillingToBeAdded);
		int usedTicketId;

		synchronized (ReturnItemBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemBillingAdded.class,
				event -> sendReturnItemBillingChangedMessage(((ReturnItemBillingAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnItemBilling(HttpServletRequest request) {

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

		ReturnItemBilling returnItemBillingToBeUpdated = new ReturnItemBilling();

		try {
			returnItemBillingToBeUpdated = ReturnItemBillingMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnItemBilling(returnItemBillingToBeUpdated);

	}

	/**
	 * Updates the ReturnItemBilling with the specific Id
	 * 
	 * @param returnItemBillingToBeUpdated the ReturnItemBilling thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnItemBilling(ReturnItemBilling returnItemBillingToBeUpdated) {

		UpdateReturnItemBilling com = new UpdateReturnItemBilling(returnItemBillingToBeUpdated);

		int usedTicketId;

		synchronized (ReturnItemBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemBillingUpdated.class,
				event -> sendReturnItemBillingChangedMessage(((ReturnItemBillingUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnItemBilling from the database
	 * 
	 * @param returnItemBillingId:
	 *            the id of the ReturnItemBilling thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnItemBillingById(@RequestParam(value = "returnItemBillingId") String returnItemBillingId) {

		DeleteReturnItemBilling com = new DeleteReturnItemBilling(returnItemBillingId);

		int usedTicketId;

		synchronized (ReturnItemBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemBillingDeleted.class,
				event -> sendReturnItemBillingChangedMessage(((ReturnItemBillingDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnItemBillingChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnItemBilling/\" plus one of the following: "
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
