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
import com.skytala.eCommerce.command.AddSalesOpportunityCompetitor;
import com.skytala.eCommerce.command.DeleteSalesOpportunityCompetitor;
import com.skytala.eCommerce.command.UpdateSalesOpportunityCompetitor;
import com.skytala.eCommerce.entity.SalesOpportunityCompetitor;
import com.skytala.eCommerce.entity.SalesOpportunityCompetitorMapper;
import com.skytala.eCommerce.event.SalesOpportunityCompetitorAdded;
import com.skytala.eCommerce.event.SalesOpportunityCompetitorDeleted;
import com.skytala.eCommerce.event.SalesOpportunityCompetitorFound;
import com.skytala.eCommerce.event.SalesOpportunityCompetitorUpdated;
import com.skytala.eCommerce.query.FindSalesOpportunityCompetitorsBy;

@RestController
@RequestMapping("/api/salesOpportunityCompetitor")
public class SalesOpportunityCompetitorController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesOpportunityCompetitor>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesOpportunityCompetitorController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesOpportunityCompetitor
	 * @return a List with the SalesOpportunityCompetitors
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesOpportunityCompetitor> findSalesOpportunityCompetitorsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesOpportunityCompetitorsBy query = new FindSalesOpportunityCompetitorsBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesOpportunityCompetitorController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityCompetitorFound.class,
				event -> sendSalesOpportunityCompetitorsFoundMessage(((SalesOpportunityCompetitorFound) event).getSalesOpportunityCompetitors(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesOpportunityCompetitorsFoundMessage(List<SalesOpportunityCompetitor> salesOpportunityCompetitors, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesOpportunityCompetitors);
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
	public boolean createSalesOpportunityCompetitor(HttpServletRequest request) {

		SalesOpportunityCompetitor salesOpportunityCompetitorToBeAdded = new SalesOpportunityCompetitor();
		try {
			salesOpportunityCompetitorToBeAdded = SalesOpportunityCompetitorMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesOpportunityCompetitor(salesOpportunityCompetitorToBeAdded);

	}

	/**
	 * creates a new SalesOpportunityCompetitor entry in the ofbiz database
	 * 
	 * @param salesOpportunityCompetitorToBeAdded
	 *            the SalesOpportunityCompetitor thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesOpportunityCompetitor(SalesOpportunityCompetitor salesOpportunityCompetitorToBeAdded) {

		AddSalesOpportunityCompetitor com = new AddSalesOpportunityCompetitor(salesOpportunityCompetitorToBeAdded);
		int usedTicketId;

		synchronized (SalesOpportunityCompetitorController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityCompetitorAdded.class,
				event -> sendSalesOpportunityCompetitorChangedMessage(((SalesOpportunityCompetitorAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesOpportunityCompetitor(HttpServletRequest request) {

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

		SalesOpportunityCompetitor salesOpportunityCompetitorToBeUpdated = new SalesOpportunityCompetitor();

		try {
			salesOpportunityCompetitorToBeUpdated = SalesOpportunityCompetitorMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesOpportunityCompetitor(salesOpportunityCompetitorToBeUpdated);

	}

	/**
	 * Updates the SalesOpportunityCompetitor with the specific Id
	 * 
	 * @param salesOpportunityCompetitorToBeUpdated the SalesOpportunityCompetitor thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesOpportunityCompetitor(SalesOpportunityCompetitor salesOpportunityCompetitorToBeUpdated) {

		UpdateSalesOpportunityCompetitor com = new UpdateSalesOpportunityCompetitor(salesOpportunityCompetitorToBeUpdated);

		int usedTicketId;

		synchronized (SalesOpportunityCompetitorController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityCompetitorUpdated.class,
				event -> sendSalesOpportunityCompetitorChangedMessage(((SalesOpportunityCompetitorUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesOpportunityCompetitor from the database
	 * 
	 * @param salesOpportunityCompetitorId:
	 *            the id of the SalesOpportunityCompetitor thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesOpportunityCompetitorById(@RequestParam(value = "salesOpportunityCompetitorId") String salesOpportunityCompetitorId) {

		DeleteSalesOpportunityCompetitor com = new DeleteSalesOpportunityCompetitor(salesOpportunityCompetitorId);

		int usedTicketId;

		synchronized (SalesOpportunityCompetitorController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityCompetitorDeleted.class,
				event -> sendSalesOpportunityCompetitorChangedMessage(((SalesOpportunityCompetitorDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesOpportunityCompetitorChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesOpportunityCompetitor/\" plus one of the following: "
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
