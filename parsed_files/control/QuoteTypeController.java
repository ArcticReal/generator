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
import com.skytala.eCommerce.command.AddQuoteType;
import com.skytala.eCommerce.command.DeleteQuoteType;
import com.skytala.eCommerce.command.UpdateQuoteType;
import com.skytala.eCommerce.entity.QuoteType;
import com.skytala.eCommerce.entity.QuoteTypeMapper;
import com.skytala.eCommerce.event.QuoteTypeAdded;
import com.skytala.eCommerce.event.QuoteTypeDeleted;
import com.skytala.eCommerce.event.QuoteTypeFound;
import com.skytala.eCommerce.event.QuoteTypeUpdated;
import com.skytala.eCommerce.query.FindQuoteTypesBy;

@RestController
@RequestMapping("/api/quoteType")
public class QuoteTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteType
	 * @return a List with the QuoteTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteType> findQuoteTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteTypesBy query = new FindQuoteTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTypeFound.class,
				event -> sendQuoteTypesFoundMessage(((QuoteTypeFound) event).getQuoteTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteTypesFoundMessage(List<QuoteType> quoteTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteTypes);
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
	public boolean createQuoteType(HttpServletRequest request) {

		QuoteType quoteTypeToBeAdded = new QuoteType();
		try {
			quoteTypeToBeAdded = QuoteTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteType(quoteTypeToBeAdded);

	}

	/**
	 * creates a new QuoteType entry in the ofbiz database
	 * 
	 * @param quoteTypeToBeAdded
	 *            the QuoteType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteType(QuoteType quoteTypeToBeAdded) {

		AddQuoteType com = new AddQuoteType(quoteTypeToBeAdded);
		int usedTicketId;

		synchronized (QuoteTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTypeAdded.class,
				event -> sendQuoteTypeChangedMessage(((QuoteTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteType(HttpServletRequest request) {

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

		QuoteType quoteTypeToBeUpdated = new QuoteType();

		try {
			quoteTypeToBeUpdated = QuoteTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteType(quoteTypeToBeUpdated);

	}

	/**
	 * Updates the QuoteType with the specific Id
	 * 
	 * @param quoteTypeToBeUpdated the QuoteType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteType(QuoteType quoteTypeToBeUpdated) {

		UpdateQuoteType com = new UpdateQuoteType(quoteTypeToBeUpdated);

		int usedTicketId;

		synchronized (QuoteTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTypeUpdated.class,
				event -> sendQuoteTypeChangedMessage(((QuoteTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteType from the database
	 * 
	 * @param quoteTypeId:
	 *            the id of the QuoteType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteTypeById(@RequestParam(value = "quoteTypeId") String quoteTypeId) {

		DeleteQuoteType com = new DeleteQuoteType(quoteTypeId);

		int usedTicketId;

		synchronized (QuoteTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTypeDeleted.class,
				event -> sendQuoteTypeChangedMessage(((QuoteTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteType/\" plus one of the following: "
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
