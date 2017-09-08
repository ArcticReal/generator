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
import com.skytala.eCommerce.command.AddQuoteWorkEffort;
import com.skytala.eCommerce.command.DeleteQuoteWorkEffort;
import com.skytala.eCommerce.command.UpdateQuoteWorkEffort;
import com.skytala.eCommerce.entity.QuoteWorkEffort;
import com.skytala.eCommerce.entity.QuoteWorkEffortMapper;
import com.skytala.eCommerce.event.QuoteWorkEffortAdded;
import com.skytala.eCommerce.event.QuoteWorkEffortDeleted;
import com.skytala.eCommerce.event.QuoteWorkEffortFound;
import com.skytala.eCommerce.event.QuoteWorkEffortUpdated;
import com.skytala.eCommerce.query.FindQuoteWorkEffortsBy;

@RestController
@RequestMapping("/api/quoteWorkEffort")
public class QuoteWorkEffortController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteWorkEffort>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteWorkEffortController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteWorkEffort
	 * @return a List with the QuoteWorkEfforts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteWorkEffort> findQuoteWorkEffortsBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteWorkEffortsBy query = new FindQuoteWorkEffortsBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteWorkEffortController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteWorkEffortFound.class,
				event -> sendQuoteWorkEffortsFoundMessage(((QuoteWorkEffortFound) event).getQuoteWorkEfforts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteWorkEffortsFoundMessage(List<QuoteWorkEffort> quoteWorkEfforts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteWorkEfforts);
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
	public boolean createQuoteWorkEffort(HttpServletRequest request) {

		QuoteWorkEffort quoteWorkEffortToBeAdded = new QuoteWorkEffort();
		try {
			quoteWorkEffortToBeAdded = QuoteWorkEffortMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteWorkEffort(quoteWorkEffortToBeAdded);

	}

	/**
	 * creates a new QuoteWorkEffort entry in the ofbiz database
	 * 
	 * @param quoteWorkEffortToBeAdded
	 *            the QuoteWorkEffort thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteWorkEffort(QuoteWorkEffort quoteWorkEffortToBeAdded) {

		AddQuoteWorkEffort com = new AddQuoteWorkEffort(quoteWorkEffortToBeAdded);
		int usedTicketId;

		synchronized (QuoteWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteWorkEffortAdded.class,
				event -> sendQuoteWorkEffortChangedMessage(((QuoteWorkEffortAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteWorkEffort(HttpServletRequest request) {

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

		QuoteWorkEffort quoteWorkEffortToBeUpdated = new QuoteWorkEffort();

		try {
			quoteWorkEffortToBeUpdated = QuoteWorkEffortMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteWorkEffort(quoteWorkEffortToBeUpdated);

	}

	/**
	 * Updates the QuoteWorkEffort with the specific Id
	 * 
	 * @param quoteWorkEffortToBeUpdated the QuoteWorkEffort thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteWorkEffort(QuoteWorkEffort quoteWorkEffortToBeUpdated) {

		UpdateQuoteWorkEffort com = new UpdateQuoteWorkEffort(quoteWorkEffortToBeUpdated);

		int usedTicketId;

		synchronized (QuoteWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteWorkEffortUpdated.class,
				event -> sendQuoteWorkEffortChangedMessage(((QuoteWorkEffortUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteWorkEffort from the database
	 * 
	 * @param quoteWorkEffortId:
	 *            the id of the QuoteWorkEffort thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteWorkEffortById(@RequestParam(value = "quoteWorkEffortId") String quoteWorkEffortId) {

		DeleteQuoteWorkEffort com = new DeleteQuoteWorkEffort(quoteWorkEffortId);

		int usedTicketId;

		synchronized (QuoteWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteWorkEffortDeleted.class,
				event -> sendQuoteWorkEffortChangedMessage(((QuoteWorkEffortDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteWorkEffortChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteWorkEffort/\" plus one of the following: "
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
