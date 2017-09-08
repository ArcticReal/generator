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
import com.skytala.eCommerce.command.AddQuoteCoefficient;
import com.skytala.eCommerce.command.DeleteQuoteCoefficient;
import com.skytala.eCommerce.command.UpdateQuoteCoefficient;
import com.skytala.eCommerce.entity.QuoteCoefficient;
import com.skytala.eCommerce.entity.QuoteCoefficientMapper;
import com.skytala.eCommerce.event.QuoteCoefficientAdded;
import com.skytala.eCommerce.event.QuoteCoefficientDeleted;
import com.skytala.eCommerce.event.QuoteCoefficientFound;
import com.skytala.eCommerce.event.QuoteCoefficientUpdated;
import com.skytala.eCommerce.query.FindQuoteCoefficientsBy;

@RestController
@RequestMapping("/api/quoteCoefficient")
public class QuoteCoefficientController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteCoefficient>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteCoefficientController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteCoefficient
	 * @return a List with the QuoteCoefficients
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteCoefficient> findQuoteCoefficientsBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteCoefficientsBy query = new FindQuoteCoefficientsBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteCoefficientController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteCoefficientFound.class,
				event -> sendQuoteCoefficientsFoundMessage(((QuoteCoefficientFound) event).getQuoteCoefficients(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteCoefficientsFoundMessage(List<QuoteCoefficient> quoteCoefficients, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteCoefficients);
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
	public boolean createQuoteCoefficient(HttpServletRequest request) {

		QuoteCoefficient quoteCoefficientToBeAdded = new QuoteCoefficient();
		try {
			quoteCoefficientToBeAdded = QuoteCoefficientMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteCoefficient(quoteCoefficientToBeAdded);

	}

	/**
	 * creates a new QuoteCoefficient entry in the ofbiz database
	 * 
	 * @param quoteCoefficientToBeAdded
	 *            the QuoteCoefficient thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteCoefficient(QuoteCoefficient quoteCoefficientToBeAdded) {

		AddQuoteCoefficient com = new AddQuoteCoefficient(quoteCoefficientToBeAdded);
		int usedTicketId;

		synchronized (QuoteCoefficientController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteCoefficientAdded.class,
				event -> sendQuoteCoefficientChangedMessage(((QuoteCoefficientAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteCoefficient(HttpServletRequest request) {

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

		QuoteCoefficient quoteCoefficientToBeUpdated = new QuoteCoefficient();

		try {
			quoteCoefficientToBeUpdated = QuoteCoefficientMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteCoefficient(quoteCoefficientToBeUpdated);

	}

	/**
	 * Updates the QuoteCoefficient with the specific Id
	 * 
	 * @param quoteCoefficientToBeUpdated the QuoteCoefficient thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteCoefficient(QuoteCoefficient quoteCoefficientToBeUpdated) {

		UpdateQuoteCoefficient com = new UpdateQuoteCoefficient(quoteCoefficientToBeUpdated);

		int usedTicketId;

		synchronized (QuoteCoefficientController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteCoefficientUpdated.class,
				event -> sendQuoteCoefficientChangedMessage(((QuoteCoefficientUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteCoefficient from the database
	 * 
	 * @param quoteCoefficientId:
	 *            the id of the QuoteCoefficient thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteCoefficientById(@RequestParam(value = "quoteCoefficientId") String quoteCoefficientId) {

		DeleteQuoteCoefficient com = new DeleteQuoteCoefficient(quoteCoefficientId);

		int usedTicketId;

		synchronized (QuoteCoefficientController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteCoefficientDeleted.class,
				event -> sendQuoteCoefficientChangedMessage(((QuoteCoefficientDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteCoefficientChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteCoefficient/\" plus one of the following: "
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
