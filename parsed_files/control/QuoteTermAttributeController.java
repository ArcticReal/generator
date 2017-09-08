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
import com.skytala.eCommerce.command.AddQuoteTermAttribute;
import com.skytala.eCommerce.command.DeleteQuoteTermAttribute;
import com.skytala.eCommerce.command.UpdateQuoteTermAttribute;
import com.skytala.eCommerce.entity.QuoteTermAttribute;
import com.skytala.eCommerce.entity.QuoteTermAttributeMapper;
import com.skytala.eCommerce.event.QuoteTermAttributeAdded;
import com.skytala.eCommerce.event.QuoteTermAttributeDeleted;
import com.skytala.eCommerce.event.QuoteTermAttributeFound;
import com.skytala.eCommerce.event.QuoteTermAttributeUpdated;
import com.skytala.eCommerce.query.FindQuoteTermAttributesBy;

@RestController
@RequestMapping("/api/quoteTermAttribute")
public class QuoteTermAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteTermAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteTermAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteTermAttribute
	 * @return a List with the QuoteTermAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteTermAttribute> findQuoteTermAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteTermAttributesBy query = new FindQuoteTermAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteTermAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTermAttributeFound.class,
				event -> sendQuoteTermAttributesFoundMessage(((QuoteTermAttributeFound) event).getQuoteTermAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteTermAttributesFoundMessage(List<QuoteTermAttribute> quoteTermAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteTermAttributes);
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
	public boolean createQuoteTermAttribute(HttpServletRequest request) {

		QuoteTermAttribute quoteTermAttributeToBeAdded = new QuoteTermAttribute();
		try {
			quoteTermAttributeToBeAdded = QuoteTermAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteTermAttribute(quoteTermAttributeToBeAdded);

	}

	/**
	 * creates a new QuoteTermAttribute entry in the ofbiz database
	 * 
	 * @param quoteTermAttributeToBeAdded
	 *            the QuoteTermAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteTermAttribute(QuoteTermAttribute quoteTermAttributeToBeAdded) {

		AddQuoteTermAttribute com = new AddQuoteTermAttribute(quoteTermAttributeToBeAdded);
		int usedTicketId;

		synchronized (QuoteTermAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTermAttributeAdded.class,
				event -> sendQuoteTermAttributeChangedMessage(((QuoteTermAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteTermAttribute(HttpServletRequest request) {

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

		QuoteTermAttribute quoteTermAttributeToBeUpdated = new QuoteTermAttribute();

		try {
			quoteTermAttributeToBeUpdated = QuoteTermAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteTermAttribute(quoteTermAttributeToBeUpdated);

	}

	/**
	 * Updates the QuoteTermAttribute with the specific Id
	 * 
	 * @param quoteTermAttributeToBeUpdated the QuoteTermAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteTermAttribute(QuoteTermAttribute quoteTermAttributeToBeUpdated) {

		UpdateQuoteTermAttribute com = new UpdateQuoteTermAttribute(quoteTermAttributeToBeUpdated);

		int usedTicketId;

		synchronized (QuoteTermAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTermAttributeUpdated.class,
				event -> sendQuoteTermAttributeChangedMessage(((QuoteTermAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteTermAttribute from the database
	 * 
	 * @param quoteTermAttributeId:
	 *            the id of the QuoteTermAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteTermAttributeById(@RequestParam(value = "quoteTermAttributeId") String quoteTermAttributeId) {

		DeleteQuoteTermAttribute com = new DeleteQuoteTermAttribute(quoteTermAttributeId);

		int usedTicketId;

		synchronized (QuoteTermAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTermAttributeDeleted.class,
				event -> sendQuoteTermAttributeChangedMessage(((QuoteTermAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteTermAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteTermAttribute/\" plus one of the following: "
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
