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
import com.skytala.eCommerce.command.AddSalesOpportunityQuote;
import com.skytala.eCommerce.command.DeleteSalesOpportunityQuote;
import com.skytala.eCommerce.command.UpdateSalesOpportunityQuote;
import com.skytala.eCommerce.entity.SalesOpportunityQuote;
import com.skytala.eCommerce.entity.SalesOpportunityQuoteMapper;
import com.skytala.eCommerce.event.SalesOpportunityQuoteAdded;
import com.skytala.eCommerce.event.SalesOpportunityQuoteDeleted;
import com.skytala.eCommerce.event.SalesOpportunityQuoteFound;
import com.skytala.eCommerce.event.SalesOpportunityQuoteUpdated;
import com.skytala.eCommerce.query.FindSalesOpportunityQuotesBy;

@RestController
@RequestMapping("/api/salesOpportunityQuote")
public class SalesOpportunityQuoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesOpportunityQuote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesOpportunityQuoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesOpportunityQuote
	 * @return a List with the SalesOpportunityQuotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesOpportunityQuote> findSalesOpportunityQuotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesOpportunityQuotesBy query = new FindSalesOpportunityQuotesBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesOpportunityQuoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityQuoteFound.class,
				event -> sendSalesOpportunityQuotesFoundMessage(((SalesOpportunityQuoteFound) event).getSalesOpportunityQuotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesOpportunityQuotesFoundMessage(List<SalesOpportunityQuote> salesOpportunityQuotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesOpportunityQuotes);
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
	public boolean createSalesOpportunityQuote(HttpServletRequest request) {

		SalesOpportunityQuote salesOpportunityQuoteToBeAdded = new SalesOpportunityQuote();
		try {
			salesOpportunityQuoteToBeAdded = SalesOpportunityQuoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesOpportunityQuote(salesOpportunityQuoteToBeAdded);

	}

	/**
	 * creates a new SalesOpportunityQuote entry in the ofbiz database
	 * 
	 * @param salesOpportunityQuoteToBeAdded
	 *            the SalesOpportunityQuote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesOpportunityQuote(SalesOpportunityQuote salesOpportunityQuoteToBeAdded) {

		AddSalesOpportunityQuote com = new AddSalesOpportunityQuote(salesOpportunityQuoteToBeAdded);
		int usedTicketId;

		synchronized (SalesOpportunityQuoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityQuoteAdded.class,
				event -> sendSalesOpportunityQuoteChangedMessage(((SalesOpportunityQuoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesOpportunityQuote(HttpServletRequest request) {

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

		SalesOpportunityQuote salesOpportunityQuoteToBeUpdated = new SalesOpportunityQuote();

		try {
			salesOpportunityQuoteToBeUpdated = SalesOpportunityQuoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesOpportunityQuote(salesOpportunityQuoteToBeUpdated);

	}

	/**
	 * Updates the SalesOpportunityQuote with the specific Id
	 * 
	 * @param salesOpportunityQuoteToBeUpdated the SalesOpportunityQuote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesOpportunityQuote(SalesOpportunityQuote salesOpportunityQuoteToBeUpdated) {

		UpdateSalesOpportunityQuote com = new UpdateSalesOpportunityQuote(salesOpportunityQuoteToBeUpdated);

		int usedTicketId;

		synchronized (SalesOpportunityQuoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityQuoteUpdated.class,
				event -> sendSalesOpportunityQuoteChangedMessage(((SalesOpportunityQuoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesOpportunityQuote from the database
	 * 
	 * @param salesOpportunityQuoteId:
	 *            the id of the SalesOpportunityQuote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesOpportunityQuoteById(@RequestParam(value = "salesOpportunityQuoteId") String salesOpportunityQuoteId) {

		DeleteSalesOpportunityQuote com = new DeleteSalesOpportunityQuote(salesOpportunityQuoteId);

		int usedTicketId;

		synchronized (SalesOpportunityQuoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityQuoteDeleted.class,
				event -> sendSalesOpportunityQuoteChangedMessage(((SalesOpportunityQuoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesOpportunityQuoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesOpportunityQuote/\" plus one of the following: "
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
