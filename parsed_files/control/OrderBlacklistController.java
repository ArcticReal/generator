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
import com.skytala.eCommerce.command.AddOrderBlacklist;
import com.skytala.eCommerce.command.DeleteOrderBlacklist;
import com.skytala.eCommerce.command.UpdateOrderBlacklist;
import com.skytala.eCommerce.entity.OrderBlacklist;
import com.skytala.eCommerce.entity.OrderBlacklistMapper;
import com.skytala.eCommerce.event.OrderBlacklistAdded;
import com.skytala.eCommerce.event.OrderBlacklistDeleted;
import com.skytala.eCommerce.event.OrderBlacklistFound;
import com.skytala.eCommerce.event.OrderBlacklistUpdated;
import com.skytala.eCommerce.query.FindOrderBlacklistsBy;

@RestController
@RequestMapping("/api/orderBlacklist")
public class OrderBlacklistController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderBlacklist>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderBlacklistController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderBlacklist
	 * @return a List with the OrderBlacklists
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderBlacklist> findOrderBlacklistsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderBlacklistsBy query = new FindOrderBlacklistsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderBlacklistController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderBlacklistFound.class,
				event -> sendOrderBlacklistsFoundMessage(((OrderBlacklistFound) event).getOrderBlacklists(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderBlacklistsFoundMessage(List<OrderBlacklist> orderBlacklists, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderBlacklists);
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
	public boolean createOrderBlacklist(HttpServletRequest request) {

		OrderBlacklist orderBlacklistToBeAdded = new OrderBlacklist();
		try {
			orderBlacklistToBeAdded = OrderBlacklistMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderBlacklist(orderBlacklistToBeAdded);

	}

	/**
	 * creates a new OrderBlacklist entry in the ofbiz database
	 * 
	 * @param orderBlacklistToBeAdded
	 *            the OrderBlacklist thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderBlacklist(OrderBlacklist orderBlacklistToBeAdded) {

		AddOrderBlacklist com = new AddOrderBlacklist(orderBlacklistToBeAdded);
		int usedTicketId;

		synchronized (OrderBlacklistController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderBlacklistAdded.class,
				event -> sendOrderBlacklistChangedMessage(((OrderBlacklistAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderBlacklist(HttpServletRequest request) {

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

		OrderBlacklist orderBlacklistToBeUpdated = new OrderBlacklist();

		try {
			orderBlacklistToBeUpdated = OrderBlacklistMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderBlacklist(orderBlacklistToBeUpdated);

	}

	/**
	 * Updates the OrderBlacklist with the specific Id
	 * 
	 * @param orderBlacklistToBeUpdated the OrderBlacklist thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderBlacklist(OrderBlacklist orderBlacklistToBeUpdated) {

		UpdateOrderBlacklist com = new UpdateOrderBlacklist(orderBlacklistToBeUpdated);

		int usedTicketId;

		synchronized (OrderBlacklistController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderBlacklistUpdated.class,
				event -> sendOrderBlacklistChangedMessage(((OrderBlacklistUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderBlacklist from the database
	 * 
	 * @param orderBlacklistId:
	 *            the id of the OrderBlacklist thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderBlacklistById(@RequestParam(value = "orderBlacklistId") String orderBlacklistId) {

		DeleteOrderBlacklist com = new DeleteOrderBlacklist(orderBlacklistId);

		int usedTicketId;

		synchronized (OrderBlacklistController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderBlacklistDeleted.class,
				event -> sendOrderBlacklistChangedMessage(((OrderBlacklistDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderBlacklistChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderBlacklist/\" plus one of the following: "
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
