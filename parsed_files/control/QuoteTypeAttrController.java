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
import com.skytala.eCommerce.command.AddQuoteTypeAttr;
import com.skytala.eCommerce.command.DeleteQuoteTypeAttr;
import com.skytala.eCommerce.command.UpdateQuoteTypeAttr;
import com.skytala.eCommerce.entity.QuoteTypeAttr;
import com.skytala.eCommerce.entity.QuoteTypeAttrMapper;
import com.skytala.eCommerce.event.QuoteTypeAttrAdded;
import com.skytala.eCommerce.event.QuoteTypeAttrDeleted;
import com.skytala.eCommerce.event.QuoteTypeAttrFound;
import com.skytala.eCommerce.event.QuoteTypeAttrUpdated;
import com.skytala.eCommerce.query.FindQuoteTypeAttrsBy;

@RestController
@RequestMapping("/api/quoteTypeAttr")
public class QuoteTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteTypeAttr
	 * @return a List with the QuoteTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteTypeAttr> findQuoteTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteTypeAttrsBy query = new FindQuoteTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTypeAttrFound.class,
				event -> sendQuoteTypeAttrsFoundMessage(((QuoteTypeAttrFound) event).getQuoteTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteTypeAttrsFoundMessage(List<QuoteTypeAttr> quoteTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteTypeAttrs);
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
	public boolean createQuoteTypeAttr(HttpServletRequest request) {

		QuoteTypeAttr quoteTypeAttrToBeAdded = new QuoteTypeAttr();
		try {
			quoteTypeAttrToBeAdded = QuoteTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteTypeAttr(quoteTypeAttrToBeAdded);

	}

	/**
	 * creates a new QuoteTypeAttr entry in the ofbiz database
	 * 
	 * @param quoteTypeAttrToBeAdded
	 *            the QuoteTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteTypeAttr(QuoteTypeAttr quoteTypeAttrToBeAdded) {

		AddQuoteTypeAttr com = new AddQuoteTypeAttr(quoteTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (QuoteTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTypeAttrAdded.class,
				event -> sendQuoteTypeAttrChangedMessage(((QuoteTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteTypeAttr(HttpServletRequest request) {

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

		QuoteTypeAttr quoteTypeAttrToBeUpdated = new QuoteTypeAttr();

		try {
			quoteTypeAttrToBeUpdated = QuoteTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteTypeAttr(quoteTypeAttrToBeUpdated);

	}

	/**
	 * Updates the QuoteTypeAttr with the specific Id
	 * 
	 * @param quoteTypeAttrToBeUpdated the QuoteTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteTypeAttr(QuoteTypeAttr quoteTypeAttrToBeUpdated) {

		UpdateQuoteTypeAttr com = new UpdateQuoteTypeAttr(quoteTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (QuoteTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTypeAttrUpdated.class,
				event -> sendQuoteTypeAttrChangedMessage(((QuoteTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteTypeAttr from the database
	 * 
	 * @param quoteTypeAttrId:
	 *            the id of the QuoteTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteTypeAttrById(@RequestParam(value = "quoteTypeAttrId") String quoteTypeAttrId) {

		DeleteQuoteTypeAttr com = new DeleteQuoteTypeAttr(quoteTypeAttrId);

		int usedTicketId;

		synchronized (QuoteTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteTypeAttrDeleted.class,
				event -> sendQuoteTypeAttrChangedMessage(((QuoteTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteTypeAttr/\" plus one of the following: "
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
