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
import com.skytala.eCommerce.command.AddOrderSummaryEntry;
import com.skytala.eCommerce.command.DeleteOrderSummaryEntry;
import com.skytala.eCommerce.command.UpdateOrderSummaryEntry;
import com.skytala.eCommerce.entity.OrderSummaryEntry;
import com.skytala.eCommerce.entity.OrderSummaryEntryMapper;
import com.skytala.eCommerce.event.OrderSummaryEntryAdded;
import com.skytala.eCommerce.event.OrderSummaryEntryDeleted;
import com.skytala.eCommerce.event.OrderSummaryEntryFound;
import com.skytala.eCommerce.event.OrderSummaryEntryUpdated;
import com.skytala.eCommerce.query.FindOrderSummaryEntrysBy;

@RestController
@RequestMapping("/api/orderSummaryEntry")
public class OrderSummaryEntryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderSummaryEntry>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderSummaryEntryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderSummaryEntry
	 * @return a List with the OrderSummaryEntrys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderSummaryEntry> findOrderSummaryEntrysBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderSummaryEntrysBy query = new FindOrderSummaryEntrysBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderSummaryEntryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderSummaryEntryFound.class,
				event -> sendOrderSummaryEntrysFoundMessage(((OrderSummaryEntryFound) event).getOrderSummaryEntrys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderSummaryEntrysFoundMessage(List<OrderSummaryEntry> orderSummaryEntrys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderSummaryEntrys);
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
	public boolean createOrderSummaryEntry(HttpServletRequest request) {

		OrderSummaryEntry orderSummaryEntryToBeAdded = new OrderSummaryEntry();
		try {
			orderSummaryEntryToBeAdded = OrderSummaryEntryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderSummaryEntry(orderSummaryEntryToBeAdded);

	}

	/**
	 * creates a new OrderSummaryEntry entry in the ofbiz database
	 * 
	 * @param orderSummaryEntryToBeAdded
	 *            the OrderSummaryEntry thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderSummaryEntry(OrderSummaryEntry orderSummaryEntryToBeAdded) {

		AddOrderSummaryEntry com = new AddOrderSummaryEntry(orderSummaryEntryToBeAdded);
		int usedTicketId;

		synchronized (OrderSummaryEntryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderSummaryEntryAdded.class,
				event -> sendOrderSummaryEntryChangedMessage(((OrderSummaryEntryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderSummaryEntry(HttpServletRequest request) {

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

		OrderSummaryEntry orderSummaryEntryToBeUpdated = new OrderSummaryEntry();

		try {
			orderSummaryEntryToBeUpdated = OrderSummaryEntryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderSummaryEntry(orderSummaryEntryToBeUpdated);

	}

	/**
	 * Updates the OrderSummaryEntry with the specific Id
	 * 
	 * @param orderSummaryEntryToBeUpdated the OrderSummaryEntry thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderSummaryEntry(OrderSummaryEntry orderSummaryEntryToBeUpdated) {

		UpdateOrderSummaryEntry com = new UpdateOrderSummaryEntry(orderSummaryEntryToBeUpdated);

		int usedTicketId;

		synchronized (OrderSummaryEntryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderSummaryEntryUpdated.class,
				event -> sendOrderSummaryEntryChangedMessage(((OrderSummaryEntryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderSummaryEntry from the database
	 * 
	 * @param orderSummaryEntryId:
	 *            the id of the OrderSummaryEntry thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderSummaryEntryById(@RequestParam(value = "orderSummaryEntryId") String orderSummaryEntryId) {

		DeleteOrderSummaryEntry com = new DeleteOrderSummaryEntry(orderSummaryEntryId);

		int usedTicketId;

		synchronized (OrderSummaryEntryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderSummaryEntryDeleted.class,
				event -> sendOrderSummaryEntryChangedMessage(((OrderSummaryEntryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderSummaryEntryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderSummaryEntry/\" plus one of the following: "
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
