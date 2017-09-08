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
import com.skytala.eCommerce.command.AddQuoteItem;
import com.skytala.eCommerce.command.DeleteQuoteItem;
import com.skytala.eCommerce.command.UpdateQuoteItem;
import com.skytala.eCommerce.entity.QuoteItem;
import com.skytala.eCommerce.entity.QuoteItemMapper;
import com.skytala.eCommerce.event.QuoteItemAdded;
import com.skytala.eCommerce.event.QuoteItemDeleted;
import com.skytala.eCommerce.event.QuoteItemFound;
import com.skytala.eCommerce.event.QuoteItemUpdated;
import com.skytala.eCommerce.query.FindQuoteItemsBy;

@RestController
@RequestMapping("/api/quoteItem")
public class QuoteItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteItem
	 * @return a List with the QuoteItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteItem> findQuoteItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteItemsBy query = new FindQuoteItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteItemFound.class,
				event -> sendQuoteItemsFoundMessage(((QuoteItemFound) event).getQuoteItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteItemsFoundMessage(List<QuoteItem> quoteItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteItems);
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
	public boolean createQuoteItem(HttpServletRequest request) {

		QuoteItem quoteItemToBeAdded = new QuoteItem();
		try {
			quoteItemToBeAdded = QuoteItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteItem(quoteItemToBeAdded);

	}

	/**
	 * creates a new QuoteItem entry in the ofbiz database
	 * 
	 * @param quoteItemToBeAdded
	 *            the QuoteItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteItem(QuoteItem quoteItemToBeAdded) {

		AddQuoteItem com = new AddQuoteItem(quoteItemToBeAdded);
		int usedTicketId;

		synchronized (QuoteItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteItemAdded.class,
				event -> sendQuoteItemChangedMessage(((QuoteItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteItem(HttpServletRequest request) {

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

		QuoteItem quoteItemToBeUpdated = new QuoteItem();

		try {
			quoteItemToBeUpdated = QuoteItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteItem(quoteItemToBeUpdated);

	}

	/**
	 * Updates the QuoteItem with the specific Id
	 * 
	 * @param quoteItemToBeUpdated the QuoteItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteItem(QuoteItem quoteItemToBeUpdated) {

		UpdateQuoteItem com = new UpdateQuoteItem(quoteItemToBeUpdated);

		int usedTicketId;

		synchronized (QuoteItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteItemUpdated.class,
				event -> sendQuoteItemChangedMessage(((QuoteItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteItem from the database
	 * 
	 * @param quoteItemId:
	 *            the id of the QuoteItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteItemById(@RequestParam(value = "quoteItemId") String quoteItemId) {

		DeleteQuoteItem com = new DeleteQuoteItem(quoteItemId);

		int usedTicketId;

		synchronized (QuoteItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteItemDeleted.class,
				event -> sendQuoteItemChangedMessage(((QuoteItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteItem/\" plus one of the following: "
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
