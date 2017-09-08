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
import com.skytala.eCommerce.command.AddQuoteAttribute;
import com.skytala.eCommerce.command.DeleteQuoteAttribute;
import com.skytala.eCommerce.command.UpdateQuoteAttribute;
import com.skytala.eCommerce.entity.QuoteAttribute;
import com.skytala.eCommerce.entity.QuoteAttributeMapper;
import com.skytala.eCommerce.event.QuoteAttributeAdded;
import com.skytala.eCommerce.event.QuoteAttributeDeleted;
import com.skytala.eCommerce.event.QuoteAttributeFound;
import com.skytala.eCommerce.event.QuoteAttributeUpdated;
import com.skytala.eCommerce.query.FindQuoteAttributesBy;

@RestController
@RequestMapping("/api/quoteAttribute")
public class QuoteAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteAttribute
	 * @return a List with the QuoteAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteAttribute> findQuoteAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteAttributesBy query = new FindQuoteAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteAttributeFound.class,
				event -> sendQuoteAttributesFoundMessage(((QuoteAttributeFound) event).getQuoteAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteAttributesFoundMessage(List<QuoteAttribute> quoteAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteAttributes);
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
	public boolean createQuoteAttribute(HttpServletRequest request) {

		QuoteAttribute quoteAttributeToBeAdded = new QuoteAttribute();
		try {
			quoteAttributeToBeAdded = QuoteAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteAttribute(quoteAttributeToBeAdded);

	}

	/**
	 * creates a new QuoteAttribute entry in the ofbiz database
	 * 
	 * @param quoteAttributeToBeAdded
	 *            the QuoteAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteAttribute(QuoteAttribute quoteAttributeToBeAdded) {

		AddQuoteAttribute com = new AddQuoteAttribute(quoteAttributeToBeAdded);
		int usedTicketId;

		synchronized (QuoteAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteAttributeAdded.class,
				event -> sendQuoteAttributeChangedMessage(((QuoteAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteAttribute(HttpServletRequest request) {

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

		QuoteAttribute quoteAttributeToBeUpdated = new QuoteAttribute();

		try {
			quoteAttributeToBeUpdated = QuoteAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteAttribute(quoteAttributeToBeUpdated);

	}

	/**
	 * Updates the QuoteAttribute with the specific Id
	 * 
	 * @param quoteAttributeToBeUpdated the QuoteAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteAttribute(QuoteAttribute quoteAttributeToBeUpdated) {

		UpdateQuoteAttribute com = new UpdateQuoteAttribute(quoteAttributeToBeUpdated);

		int usedTicketId;

		synchronized (QuoteAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteAttributeUpdated.class,
				event -> sendQuoteAttributeChangedMessage(((QuoteAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteAttribute from the database
	 * 
	 * @param quoteAttributeId:
	 *            the id of the QuoteAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteAttributeById(@RequestParam(value = "quoteAttributeId") String quoteAttributeId) {

		DeleteQuoteAttribute com = new DeleteQuoteAttribute(quoteAttributeId);

		int usedTicketId;

		synchronized (QuoteAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteAttributeDeleted.class,
				event -> sendQuoteAttributeChangedMessage(((QuoteAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteAttribute/\" plus one of the following: "
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
