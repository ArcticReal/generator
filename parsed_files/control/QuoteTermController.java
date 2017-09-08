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
import com.skytala.eCommerce.command.AddQuoteTerm;
import com.skytala.eCommerce.command.DeleteQuoteTerm;
import com.skytala.eCommerce.command.UpdateQuoteTerm;
import com.skytala.eCommerce.entity.QuoteTerm;
import com.skytala.eCommerce.entity.QuoteTermMapper;
import com.skytala.eCommerce.event.QuoteTermAdded;
import com.skytala.eCommerce.event.QuoteTermDeleted;
import com.skytala.eCommerce.event.QuoteTermFound;
import com.skytala.eCommerce.event.QuoteTermUpdated;
import com.skytala.eCommerce.query.FindQuoteTermsBy;

@RestController
@RequestMapping("/api/quoteTerm")
public class QuoteTermController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteTerm>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteTermController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteTerm
	 * @return a List with the QuoteTerms
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteTerm> findQuoteTermsBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteTermsBy query = new FindQuoteTermsBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteTermController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTermFound.class,
				event -> sendQuoteTermsFoundMessage(((QuoteTermFound) event).getQuoteTerms(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteTermsFoundMessage(List<QuoteTerm> quoteTerms, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteTerms);
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
	public boolean createQuoteTerm(HttpServletRequest request) {

		QuoteTerm quoteTermToBeAdded = new QuoteTerm();
		try {
			quoteTermToBeAdded = QuoteTermMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteTerm(quoteTermToBeAdded);

	}

	/**
	 * creates a new QuoteTerm entry in the ofbiz database
	 * 
	 * @param quoteTermToBeAdded
	 *            the QuoteTerm thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteTerm(QuoteTerm quoteTermToBeAdded) {

		AddQuoteTerm com = new AddQuoteTerm(quoteTermToBeAdded);
		int usedTicketId;

		synchronized (QuoteTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTermAdded.class,
				event -> sendQuoteTermChangedMessage(((QuoteTermAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteTerm(HttpServletRequest request) {

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

		QuoteTerm quoteTermToBeUpdated = new QuoteTerm();

		try {
			quoteTermToBeUpdated = QuoteTermMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteTerm(quoteTermToBeUpdated);

	}

	/**
	 * Updates the QuoteTerm with the specific Id
	 * 
	 * @param quoteTermToBeUpdated the QuoteTerm thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteTerm(QuoteTerm quoteTermToBeUpdated) {

		UpdateQuoteTerm com = new UpdateQuoteTerm(quoteTermToBeUpdated);

		int usedTicketId;

		synchronized (QuoteTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTermUpdated.class,
				event -> sendQuoteTermChangedMessage(((QuoteTermUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteTerm from the database
	 * 
	 * @param quoteTermId:
	 *            the id of the QuoteTerm thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteTermById(@RequestParam(value = "quoteTermId") String quoteTermId) {

		DeleteQuoteTerm com = new DeleteQuoteTerm(quoteTermId);

		int usedTicketId;

		synchronized (QuoteTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTermDeleted.class,
				event -> sendQuoteTermChangedMessage(((QuoteTermDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteTermChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteTerm/\" plus one of the following: "
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
