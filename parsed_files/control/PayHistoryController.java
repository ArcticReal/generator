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
import com.skytala.eCommerce.command.AddPayHistory;
import com.skytala.eCommerce.command.DeletePayHistory;
import com.skytala.eCommerce.command.UpdatePayHistory;
import com.skytala.eCommerce.entity.PayHistory;
import com.skytala.eCommerce.entity.PayHistoryMapper;
import com.skytala.eCommerce.event.PayHistoryAdded;
import com.skytala.eCommerce.event.PayHistoryDeleted;
import com.skytala.eCommerce.event.PayHistoryFound;
import com.skytala.eCommerce.event.PayHistoryUpdated;
import com.skytala.eCommerce.query.FindPayHistorysBy;

@RestController
@RequestMapping("/api/payHistory")
public class PayHistoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PayHistory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PayHistoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PayHistory
	 * @return a List with the PayHistorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PayHistory> findPayHistorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindPayHistorysBy query = new FindPayHistorysBy(allRequestParams);

		int usedTicketId;

		synchronized (PayHistoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayHistoryFound.class,
				event -> sendPayHistorysFoundMessage(((PayHistoryFound) event).getPayHistorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPayHistorysFoundMessage(List<PayHistory> payHistorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, payHistorys);
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
	public boolean createPayHistory(HttpServletRequest request) {

		PayHistory payHistoryToBeAdded = new PayHistory();
		try {
			payHistoryToBeAdded = PayHistoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPayHistory(payHistoryToBeAdded);

	}

	/**
	 * creates a new PayHistory entry in the ofbiz database
	 * 
	 * @param payHistoryToBeAdded
	 *            the PayHistory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPayHistory(PayHistory payHistoryToBeAdded) {

		AddPayHistory com = new AddPayHistory(payHistoryToBeAdded);
		int usedTicketId;

		synchronized (PayHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayHistoryAdded.class,
				event -> sendPayHistoryChangedMessage(((PayHistoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePayHistory(HttpServletRequest request) {

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

		PayHistory payHistoryToBeUpdated = new PayHistory();

		try {
			payHistoryToBeUpdated = PayHistoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePayHistory(payHistoryToBeUpdated);

	}

	/**
	 * Updates the PayHistory with the specific Id
	 * 
	 * @param payHistoryToBeUpdated the PayHistory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePayHistory(PayHistory payHistoryToBeUpdated) {

		UpdatePayHistory com = new UpdatePayHistory(payHistoryToBeUpdated);

		int usedTicketId;

		synchronized (PayHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayHistoryUpdated.class,
				event -> sendPayHistoryChangedMessage(((PayHistoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PayHistory from the database
	 * 
	 * @param payHistoryId:
	 *            the id of the PayHistory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepayHistoryById(@RequestParam(value = "payHistoryId") String payHistoryId) {

		DeletePayHistory com = new DeletePayHistory(payHistoryId);

		int usedTicketId;

		synchronized (PayHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayHistoryDeleted.class,
				event -> sendPayHistoryChangedMessage(((PayHistoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPayHistoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/payHistory/\" plus one of the following: "
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
