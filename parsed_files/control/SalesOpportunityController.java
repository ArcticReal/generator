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
import com.skytala.eCommerce.command.AddSalesOpportunity;
import com.skytala.eCommerce.command.DeleteSalesOpportunity;
import com.skytala.eCommerce.command.UpdateSalesOpportunity;
import com.skytala.eCommerce.entity.SalesOpportunity;
import com.skytala.eCommerce.entity.SalesOpportunityMapper;
import com.skytala.eCommerce.event.SalesOpportunityAdded;
import com.skytala.eCommerce.event.SalesOpportunityDeleted;
import com.skytala.eCommerce.event.SalesOpportunityFound;
import com.skytala.eCommerce.event.SalesOpportunityUpdated;
import com.skytala.eCommerce.query.FindSalesOpportunitysBy;

@RestController
@RequestMapping("/api/salesOpportunity")
public class SalesOpportunityController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesOpportunity>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesOpportunityController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesOpportunity
	 * @return a List with the SalesOpportunitys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesOpportunity> findSalesOpportunitysBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesOpportunitysBy query = new FindSalesOpportunitysBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesOpportunityController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityFound.class,
				event -> sendSalesOpportunitysFoundMessage(((SalesOpportunityFound) event).getSalesOpportunitys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesOpportunitysFoundMessage(List<SalesOpportunity> salesOpportunitys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesOpportunitys);
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
	public boolean createSalesOpportunity(HttpServletRequest request) {

		SalesOpportunity salesOpportunityToBeAdded = new SalesOpportunity();
		try {
			salesOpportunityToBeAdded = SalesOpportunityMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesOpportunity(salesOpportunityToBeAdded);

	}

	/**
	 * creates a new SalesOpportunity entry in the ofbiz database
	 * 
	 * @param salesOpportunityToBeAdded
	 *            the SalesOpportunity thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesOpportunity(SalesOpportunity salesOpportunityToBeAdded) {

		AddSalesOpportunity com = new AddSalesOpportunity(salesOpportunityToBeAdded);
		int usedTicketId;

		synchronized (SalesOpportunityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityAdded.class,
				event -> sendSalesOpportunityChangedMessage(((SalesOpportunityAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesOpportunity(HttpServletRequest request) {

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

		SalesOpportunity salesOpportunityToBeUpdated = new SalesOpportunity();

		try {
			salesOpportunityToBeUpdated = SalesOpportunityMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesOpportunity(salesOpportunityToBeUpdated);

	}

	/**
	 * Updates the SalesOpportunity with the specific Id
	 * 
	 * @param salesOpportunityToBeUpdated the SalesOpportunity thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesOpportunity(SalesOpportunity salesOpportunityToBeUpdated) {

		UpdateSalesOpportunity com = new UpdateSalesOpportunity(salesOpportunityToBeUpdated);

		int usedTicketId;

		synchronized (SalesOpportunityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityUpdated.class,
				event -> sendSalesOpportunityChangedMessage(((SalesOpportunityUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesOpportunity from the database
	 * 
	 * @param salesOpportunityId:
	 *            the id of the SalesOpportunity thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesOpportunityById(@RequestParam(value = "salesOpportunityId") String salesOpportunityId) {

		DeleteSalesOpportunity com = new DeleteSalesOpportunity(salesOpportunityId);

		int usedTicketId;

		synchronized (SalesOpportunityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityDeleted.class,
				event -> sendSalesOpportunityChangedMessage(((SalesOpportunityDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesOpportunityChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesOpportunity/\" plus one of the following: "
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
