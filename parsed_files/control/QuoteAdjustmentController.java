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
import com.skytala.eCommerce.command.AddQuoteAdjustment;
import com.skytala.eCommerce.command.DeleteQuoteAdjustment;
import com.skytala.eCommerce.command.UpdateQuoteAdjustment;
import com.skytala.eCommerce.entity.QuoteAdjustment;
import com.skytala.eCommerce.entity.QuoteAdjustmentMapper;
import com.skytala.eCommerce.event.QuoteAdjustmentAdded;
import com.skytala.eCommerce.event.QuoteAdjustmentDeleted;
import com.skytala.eCommerce.event.QuoteAdjustmentFound;
import com.skytala.eCommerce.event.QuoteAdjustmentUpdated;
import com.skytala.eCommerce.query.FindQuoteAdjustmentsBy;

@RestController
@RequestMapping("/api/quoteAdjustment")
public class QuoteAdjustmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteAdjustment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteAdjustmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteAdjustment
	 * @return a List with the QuoteAdjustments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteAdjustment> findQuoteAdjustmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteAdjustmentsBy query = new FindQuoteAdjustmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteAdjustmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteAdjustmentFound.class,
				event -> sendQuoteAdjustmentsFoundMessage(((QuoteAdjustmentFound) event).getQuoteAdjustments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteAdjustmentsFoundMessage(List<QuoteAdjustment> quoteAdjustments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteAdjustments);
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
	public boolean createQuoteAdjustment(HttpServletRequest request) {

		QuoteAdjustment quoteAdjustmentToBeAdded = new QuoteAdjustment();
		try {
			quoteAdjustmentToBeAdded = QuoteAdjustmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteAdjustment(quoteAdjustmentToBeAdded);

	}

	/**
	 * creates a new QuoteAdjustment entry in the ofbiz database
	 * 
	 * @param quoteAdjustmentToBeAdded
	 *            the QuoteAdjustment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteAdjustment(QuoteAdjustment quoteAdjustmentToBeAdded) {

		AddQuoteAdjustment com = new AddQuoteAdjustment(quoteAdjustmentToBeAdded);
		int usedTicketId;

		synchronized (QuoteAdjustmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteAdjustmentAdded.class,
				event -> sendQuoteAdjustmentChangedMessage(((QuoteAdjustmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteAdjustment(HttpServletRequest request) {

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

		QuoteAdjustment quoteAdjustmentToBeUpdated = new QuoteAdjustment();

		try {
			quoteAdjustmentToBeUpdated = QuoteAdjustmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteAdjustment(quoteAdjustmentToBeUpdated);

	}

	/**
	 * Updates the QuoteAdjustment with the specific Id
	 * 
	 * @param quoteAdjustmentToBeUpdated the QuoteAdjustment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteAdjustment(QuoteAdjustment quoteAdjustmentToBeUpdated) {

		UpdateQuoteAdjustment com = new UpdateQuoteAdjustment(quoteAdjustmentToBeUpdated);

		int usedTicketId;

		synchronized (QuoteAdjustmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteAdjustmentUpdated.class,
				event -> sendQuoteAdjustmentChangedMessage(((QuoteAdjustmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteAdjustment from the database
	 * 
	 * @param quoteAdjustmentId:
	 *            the id of the QuoteAdjustment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteAdjustmentById(@RequestParam(value = "quoteAdjustmentId") String quoteAdjustmentId) {

		DeleteQuoteAdjustment com = new DeleteQuoteAdjustment(quoteAdjustmentId);

		int usedTicketId;

		synchronized (QuoteAdjustmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteAdjustmentDeleted.class,
				event -> sendQuoteAdjustmentChangedMessage(((QuoteAdjustmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteAdjustmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteAdjustment/\" plus one of the following: "
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
