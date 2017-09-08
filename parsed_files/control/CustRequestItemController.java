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
import com.skytala.eCommerce.command.AddCustRequestItem;
import com.skytala.eCommerce.command.DeleteCustRequestItem;
import com.skytala.eCommerce.command.UpdateCustRequestItem;
import com.skytala.eCommerce.entity.CustRequestItem;
import com.skytala.eCommerce.entity.CustRequestItemMapper;
import com.skytala.eCommerce.event.CustRequestItemAdded;
import com.skytala.eCommerce.event.CustRequestItemDeleted;
import com.skytala.eCommerce.event.CustRequestItemFound;
import com.skytala.eCommerce.event.CustRequestItemUpdated;
import com.skytala.eCommerce.query.FindCustRequestItemsBy;

@RestController
@RequestMapping("/api/custRequestItem")
public class CustRequestItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestItem
	 * @return a List with the CustRequestItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestItem> findCustRequestItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestItemsBy query = new FindCustRequestItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemFound.class,
				event -> sendCustRequestItemsFoundMessage(((CustRequestItemFound) event).getCustRequestItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestItemsFoundMessage(List<CustRequestItem> custRequestItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestItems);
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
	public boolean createCustRequestItem(HttpServletRequest request) {

		CustRequestItem custRequestItemToBeAdded = new CustRequestItem();
		try {
			custRequestItemToBeAdded = CustRequestItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestItem(custRequestItemToBeAdded);

	}

	/**
	 * creates a new CustRequestItem entry in the ofbiz database
	 * 
	 * @param custRequestItemToBeAdded
	 *            the CustRequestItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestItem(CustRequestItem custRequestItemToBeAdded) {

		AddCustRequestItem com = new AddCustRequestItem(custRequestItemToBeAdded);
		int usedTicketId;

		synchronized (CustRequestItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemAdded.class,
				event -> sendCustRequestItemChangedMessage(((CustRequestItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestItem(HttpServletRequest request) {

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

		CustRequestItem custRequestItemToBeUpdated = new CustRequestItem();

		try {
			custRequestItemToBeUpdated = CustRequestItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestItem(custRequestItemToBeUpdated);

	}

	/**
	 * Updates the CustRequestItem with the specific Id
	 * 
	 * @param custRequestItemToBeUpdated the CustRequestItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestItem(CustRequestItem custRequestItemToBeUpdated) {

		UpdateCustRequestItem com = new UpdateCustRequestItem(custRequestItemToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemUpdated.class,
				event -> sendCustRequestItemChangedMessage(((CustRequestItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestItem from the database
	 * 
	 * @param custRequestItemId:
	 *            the id of the CustRequestItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestItemById(@RequestParam(value = "custRequestItemId") String custRequestItemId) {

		DeleteCustRequestItem com = new DeleteCustRequestItem(custRequestItemId);

		int usedTicketId;

		synchronized (CustRequestItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemDeleted.class,
				event -> sendCustRequestItemChangedMessage(((CustRequestItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestItem/\" plus one of the following: "
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
