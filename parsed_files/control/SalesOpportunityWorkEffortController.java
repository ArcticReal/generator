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
import com.skytala.eCommerce.command.AddSalesOpportunityWorkEffort;
import com.skytala.eCommerce.command.DeleteSalesOpportunityWorkEffort;
import com.skytala.eCommerce.command.UpdateSalesOpportunityWorkEffort;
import com.skytala.eCommerce.entity.SalesOpportunityWorkEffort;
import com.skytala.eCommerce.entity.SalesOpportunityWorkEffortMapper;
import com.skytala.eCommerce.event.SalesOpportunityWorkEffortAdded;
import com.skytala.eCommerce.event.SalesOpportunityWorkEffortDeleted;
import com.skytala.eCommerce.event.SalesOpportunityWorkEffortFound;
import com.skytala.eCommerce.event.SalesOpportunityWorkEffortUpdated;
import com.skytala.eCommerce.query.FindSalesOpportunityWorkEffortsBy;

@RestController
@RequestMapping("/api/salesOpportunityWorkEffort")
public class SalesOpportunityWorkEffortController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesOpportunityWorkEffort>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesOpportunityWorkEffortController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesOpportunityWorkEffort
	 * @return a List with the SalesOpportunityWorkEfforts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesOpportunityWorkEffort> findSalesOpportunityWorkEffortsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesOpportunityWorkEffortsBy query = new FindSalesOpportunityWorkEffortsBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesOpportunityWorkEffortController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityWorkEffortFound.class,
				event -> sendSalesOpportunityWorkEffortsFoundMessage(((SalesOpportunityWorkEffortFound) event).getSalesOpportunityWorkEfforts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesOpportunityWorkEffortsFoundMessage(List<SalesOpportunityWorkEffort> salesOpportunityWorkEfforts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesOpportunityWorkEfforts);
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
	public boolean createSalesOpportunityWorkEffort(HttpServletRequest request) {

		SalesOpportunityWorkEffort salesOpportunityWorkEffortToBeAdded = new SalesOpportunityWorkEffort();
		try {
			salesOpportunityWorkEffortToBeAdded = SalesOpportunityWorkEffortMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesOpportunityWorkEffort(salesOpportunityWorkEffortToBeAdded);

	}

	/**
	 * creates a new SalesOpportunityWorkEffort entry in the ofbiz database
	 * 
	 * @param salesOpportunityWorkEffortToBeAdded
	 *            the SalesOpportunityWorkEffort thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesOpportunityWorkEffort(SalesOpportunityWorkEffort salesOpportunityWorkEffortToBeAdded) {

		AddSalesOpportunityWorkEffort com = new AddSalesOpportunityWorkEffort(salesOpportunityWorkEffortToBeAdded);
		int usedTicketId;

		synchronized (SalesOpportunityWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityWorkEffortAdded.class,
				event -> sendSalesOpportunityWorkEffortChangedMessage(((SalesOpportunityWorkEffortAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesOpportunityWorkEffort(HttpServletRequest request) {

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

		SalesOpportunityWorkEffort salesOpportunityWorkEffortToBeUpdated = new SalesOpportunityWorkEffort();

		try {
			salesOpportunityWorkEffortToBeUpdated = SalesOpportunityWorkEffortMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesOpportunityWorkEffort(salesOpportunityWorkEffortToBeUpdated);

	}

	/**
	 * Updates the SalesOpportunityWorkEffort with the specific Id
	 * 
	 * @param salesOpportunityWorkEffortToBeUpdated the SalesOpportunityWorkEffort thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesOpportunityWorkEffort(SalesOpportunityWorkEffort salesOpportunityWorkEffortToBeUpdated) {

		UpdateSalesOpportunityWorkEffort com = new UpdateSalesOpportunityWorkEffort(salesOpportunityWorkEffortToBeUpdated);

		int usedTicketId;

		synchronized (SalesOpportunityWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityWorkEffortUpdated.class,
				event -> sendSalesOpportunityWorkEffortChangedMessage(((SalesOpportunityWorkEffortUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesOpportunityWorkEffort from the database
	 * 
	 * @param salesOpportunityWorkEffortId:
	 *            the id of the SalesOpportunityWorkEffort thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesOpportunityWorkEffortById(@RequestParam(value = "salesOpportunityWorkEffortId") String salesOpportunityWorkEffortId) {

		DeleteSalesOpportunityWorkEffort com = new DeleteSalesOpportunityWorkEffort(salesOpportunityWorkEffortId);

		int usedTicketId;

		synchronized (SalesOpportunityWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityWorkEffortDeleted.class,
				event -> sendSalesOpportunityWorkEffortChangedMessage(((SalesOpportunityWorkEffortDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesOpportunityWorkEffortChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesOpportunityWorkEffort/\" plus one of the following: "
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
