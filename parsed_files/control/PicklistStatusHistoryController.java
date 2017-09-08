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
import com.skytala.eCommerce.command.AddPicklistStatusHistory;
import com.skytala.eCommerce.command.DeletePicklistStatusHistory;
import com.skytala.eCommerce.command.UpdatePicklistStatusHistory;
import com.skytala.eCommerce.entity.PicklistStatusHistory;
import com.skytala.eCommerce.entity.PicklistStatusHistoryMapper;
import com.skytala.eCommerce.event.PicklistStatusHistoryAdded;
import com.skytala.eCommerce.event.PicklistStatusHistoryDeleted;
import com.skytala.eCommerce.event.PicklistStatusHistoryFound;
import com.skytala.eCommerce.event.PicklistStatusHistoryUpdated;
import com.skytala.eCommerce.query.FindPicklistStatusHistorysBy;

@RestController
@RequestMapping("/api/picklistStatusHistory")
public class PicklistStatusHistoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PicklistStatusHistory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PicklistStatusHistoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PicklistStatusHistory
	 * @return a List with the PicklistStatusHistorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PicklistStatusHistory> findPicklistStatusHistorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindPicklistStatusHistorysBy query = new FindPicklistStatusHistorysBy(allRequestParams);

		int usedTicketId;

		synchronized (PicklistStatusHistoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistStatusHistoryFound.class,
				event -> sendPicklistStatusHistorysFoundMessage(((PicklistStatusHistoryFound) event).getPicklistStatusHistorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPicklistStatusHistorysFoundMessage(List<PicklistStatusHistory> picklistStatusHistorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, picklistStatusHistorys);
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
	public boolean createPicklistStatusHistory(HttpServletRequest request) {

		PicklistStatusHistory picklistStatusHistoryToBeAdded = new PicklistStatusHistory();
		try {
			picklistStatusHistoryToBeAdded = PicklistStatusHistoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPicklistStatusHistory(picklistStatusHistoryToBeAdded);

	}

	/**
	 * creates a new PicklistStatusHistory entry in the ofbiz database
	 * 
	 * @param picklistStatusHistoryToBeAdded
	 *            the PicklistStatusHistory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPicklistStatusHistory(PicklistStatusHistory picklistStatusHistoryToBeAdded) {

		AddPicklistStatusHistory com = new AddPicklistStatusHistory(picklistStatusHistoryToBeAdded);
		int usedTicketId;

		synchronized (PicklistStatusHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistStatusHistoryAdded.class,
				event -> sendPicklistStatusHistoryChangedMessage(((PicklistStatusHistoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePicklistStatusHistory(HttpServletRequest request) {

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

		PicklistStatusHistory picklistStatusHistoryToBeUpdated = new PicklistStatusHistory();

		try {
			picklistStatusHistoryToBeUpdated = PicklistStatusHistoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePicklistStatusHistory(picklistStatusHistoryToBeUpdated);

	}

	/**
	 * Updates the PicklistStatusHistory with the specific Id
	 * 
	 * @param picklistStatusHistoryToBeUpdated the PicklistStatusHistory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePicklistStatusHistory(PicklistStatusHistory picklistStatusHistoryToBeUpdated) {

		UpdatePicklistStatusHistory com = new UpdatePicklistStatusHistory(picklistStatusHistoryToBeUpdated);

		int usedTicketId;

		synchronized (PicklistStatusHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistStatusHistoryUpdated.class,
				event -> sendPicklistStatusHistoryChangedMessage(((PicklistStatusHistoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PicklistStatusHistory from the database
	 * 
	 * @param picklistStatusHistoryId:
	 *            the id of the PicklistStatusHistory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepicklistStatusHistoryById(@RequestParam(value = "picklistStatusHistoryId") String picklistStatusHistoryId) {

		DeletePicklistStatusHistory com = new DeletePicklistStatusHistory(picklistStatusHistoryId);

		int usedTicketId;

		synchronized (PicklistStatusHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistStatusHistoryDeleted.class,
				event -> sendPicklistStatusHistoryChangedMessage(((PicklistStatusHistoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPicklistStatusHistoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/picklistStatusHistory/\" plus one of the following: "
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
