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
import com.skytala.eCommerce.command.AddSalesOpportunityHistory;
import com.skytala.eCommerce.command.DeleteSalesOpportunityHistory;
import com.skytala.eCommerce.command.UpdateSalesOpportunityHistory;
import com.skytala.eCommerce.entity.SalesOpportunityHistory;
import com.skytala.eCommerce.entity.SalesOpportunityHistoryMapper;
import com.skytala.eCommerce.event.SalesOpportunityHistoryAdded;
import com.skytala.eCommerce.event.SalesOpportunityHistoryDeleted;
import com.skytala.eCommerce.event.SalesOpportunityHistoryFound;
import com.skytala.eCommerce.event.SalesOpportunityHistoryUpdated;
import com.skytala.eCommerce.query.FindSalesOpportunityHistorysBy;

@RestController
@RequestMapping("/api/salesOpportunityHistory")
public class SalesOpportunityHistoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesOpportunityHistory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesOpportunityHistoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesOpportunityHistory
	 * @return a List with the SalesOpportunityHistorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesOpportunityHistory> findSalesOpportunityHistorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesOpportunityHistorysBy query = new FindSalesOpportunityHistorysBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesOpportunityHistoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityHistoryFound.class,
				event -> sendSalesOpportunityHistorysFoundMessage(((SalesOpportunityHistoryFound) event).getSalesOpportunityHistorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesOpportunityHistorysFoundMessage(List<SalesOpportunityHistory> salesOpportunityHistorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesOpportunityHistorys);
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
	public boolean createSalesOpportunityHistory(HttpServletRequest request) {

		SalesOpportunityHistory salesOpportunityHistoryToBeAdded = new SalesOpportunityHistory();
		try {
			salesOpportunityHistoryToBeAdded = SalesOpportunityHistoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesOpportunityHistory(salesOpportunityHistoryToBeAdded);

	}

	/**
	 * creates a new SalesOpportunityHistory entry in the ofbiz database
	 * 
	 * @param salesOpportunityHistoryToBeAdded
	 *            the SalesOpportunityHistory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesOpportunityHistory(SalesOpportunityHistory salesOpportunityHistoryToBeAdded) {

		AddSalesOpportunityHistory com = new AddSalesOpportunityHistory(salesOpportunityHistoryToBeAdded);
		int usedTicketId;

		synchronized (SalesOpportunityHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityHistoryAdded.class,
				event -> sendSalesOpportunityHistoryChangedMessage(((SalesOpportunityHistoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesOpportunityHistory(HttpServletRequest request) {

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

		SalesOpportunityHistory salesOpportunityHistoryToBeUpdated = new SalesOpportunityHistory();

		try {
			salesOpportunityHistoryToBeUpdated = SalesOpportunityHistoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesOpportunityHistory(salesOpportunityHistoryToBeUpdated);

	}

	/**
	 * Updates the SalesOpportunityHistory with the specific Id
	 * 
	 * @param salesOpportunityHistoryToBeUpdated the SalesOpportunityHistory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesOpportunityHistory(SalesOpportunityHistory salesOpportunityHistoryToBeUpdated) {

		UpdateSalesOpportunityHistory com = new UpdateSalesOpportunityHistory(salesOpportunityHistoryToBeUpdated);

		int usedTicketId;

		synchronized (SalesOpportunityHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityHistoryUpdated.class,
				event -> sendSalesOpportunityHistoryChangedMessage(((SalesOpportunityHistoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesOpportunityHistory from the database
	 * 
	 * @param salesOpportunityHistoryId:
	 *            the id of the SalesOpportunityHistory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesOpportunityHistoryById(@RequestParam(value = "salesOpportunityHistoryId") String salesOpportunityHistoryId) {

		DeleteSalesOpportunityHistory com = new DeleteSalesOpportunityHistory(salesOpportunityHistoryId);

		int usedTicketId;

		synchronized (SalesOpportunityHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityHistoryDeleted.class,
				event -> sendSalesOpportunityHistoryChangedMessage(((SalesOpportunityHistoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesOpportunityHistoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesOpportunityHistory/\" plus one of the following: "
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
