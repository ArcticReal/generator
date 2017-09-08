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
import com.skytala.eCommerce.command.AddQuote;
import com.skytala.eCommerce.command.DeleteQuote;
import com.skytala.eCommerce.command.UpdateQuote;
import com.skytala.eCommerce.entity.Quote;
import com.skytala.eCommerce.entity.QuoteMapper;
import com.skytala.eCommerce.event.QuoteAdded;
import com.skytala.eCommerce.event.QuoteDeleted;
import com.skytala.eCommerce.event.QuoteFound;
import com.skytala.eCommerce.event.QuoteUpdated;
import com.skytala.eCommerce.query.FindQuotesBy;

@RestController
@RequestMapping("/api/quote")
public class QuoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Quote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Quote
	 * @return a List with the Quotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Quote> findQuotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuotesBy query = new FindQuotesBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteFound.class,
				event -> sendQuotesFoundMessage(((QuoteFound) event).getQuotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuotesFoundMessage(List<Quote> quotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quotes);
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
	public boolean createQuote(HttpServletRequest request) {

		Quote quoteToBeAdded = new Quote();
		try {
			quoteToBeAdded = QuoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuote(quoteToBeAdded);

	}

	/**
	 * creates a new Quote entry in the ofbiz database
	 * 
	 * @param quoteToBeAdded
	 *            the Quote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuote(Quote quoteToBeAdded) {

		AddQuote com = new AddQuote(quoteToBeAdded);
		int usedTicketId;

		synchronized (QuoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteAdded.class,
				event -> sendQuoteChangedMessage(((QuoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuote(HttpServletRequest request) {

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

		Quote quoteToBeUpdated = new Quote();

		try {
			quoteToBeUpdated = QuoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuote(quoteToBeUpdated);

	}

	/**
	 * Updates the Quote with the specific Id
	 * 
	 * @param quoteToBeUpdated the Quote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuote(Quote quoteToBeUpdated) {

		UpdateQuote com = new UpdateQuote(quoteToBeUpdated);

		int usedTicketId;

		synchronized (QuoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteUpdated.class,
				event -> sendQuoteChangedMessage(((QuoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Quote from the database
	 * 
	 * @param quoteId:
	 *            the id of the Quote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteById(@RequestParam(value = "quoteId") String quoteId) {

		DeleteQuote com = new DeleteQuote(quoteId);

		int usedTicketId;

		synchronized (QuoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteDeleted.class,
				event -> sendQuoteChangedMessage(((QuoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quote/\" plus one of the following: "
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
