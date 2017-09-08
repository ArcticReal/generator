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
import com.skytala.eCommerce.command.AddOrderHeaderWorkEffort;
import com.skytala.eCommerce.command.DeleteOrderHeaderWorkEffort;
import com.skytala.eCommerce.command.UpdateOrderHeaderWorkEffort;
import com.skytala.eCommerce.entity.OrderHeaderWorkEffort;
import com.skytala.eCommerce.entity.OrderHeaderWorkEffortMapper;
import com.skytala.eCommerce.event.OrderHeaderWorkEffortAdded;
import com.skytala.eCommerce.event.OrderHeaderWorkEffortDeleted;
import com.skytala.eCommerce.event.OrderHeaderWorkEffortFound;
import com.skytala.eCommerce.event.OrderHeaderWorkEffortUpdated;
import com.skytala.eCommerce.query.FindOrderHeaderWorkEffortsBy;

@RestController
@RequestMapping("/api/orderHeaderWorkEffort")
public class OrderHeaderWorkEffortController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderHeaderWorkEffort>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderHeaderWorkEffortController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderHeaderWorkEffort
	 * @return a List with the OrderHeaderWorkEfforts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderHeaderWorkEffort> findOrderHeaderWorkEffortsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderHeaderWorkEffortsBy query = new FindOrderHeaderWorkEffortsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderHeaderWorkEffortController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderWorkEffortFound.class,
				event -> sendOrderHeaderWorkEffortsFoundMessage(((OrderHeaderWorkEffortFound) event).getOrderHeaderWorkEfforts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderHeaderWorkEffortsFoundMessage(List<OrderHeaderWorkEffort> orderHeaderWorkEfforts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderHeaderWorkEfforts);
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
	public boolean createOrderHeaderWorkEffort(HttpServletRequest request) {

		OrderHeaderWorkEffort orderHeaderWorkEffortToBeAdded = new OrderHeaderWorkEffort();
		try {
			orderHeaderWorkEffortToBeAdded = OrderHeaderWorkEffortMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderHeaderWorkEffort(orderHeaderWorkEffortToBeAdded);

	}

	/**
	 * creates a new OrderHeaderWorkEffort entry in the ofbiz database
	 * 
	 * @param orderHeaderWorkEffortToBeAdded
	 *            the OrderHeaderWorkEffort thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderHeaderWorkEffort(OrderHeaderWorkEffort orderHeaderWorkEffortToBeAdded) {

		AddOrderHeaderWorkEffort com = new AddOrderHeaderWorkEffort(orderHeaderWorkEffortToBeAdded);
		int usedTicketId;

		synchronized (OrderHeaderWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderWorkEffortAdded.class,
				event -> sendOrderHeaderWorkEffortChangedMessage(((OrderHeaderWorkEffortAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderHeaderWorkEffort(HttpServletRequest request) {

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

		OrderHeaderWorkEffort orderHeaderWorkEffortToBeUpdated = new OrderHeaderWorkEffort();

		try {
			orderHeaderWorkEffortToBeUpdated = OrderHeaderWorkEffortMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderHeaderWorkEffort(orderHeaderWorkEffortToBeUpdated);

	}

	/**
	 * Updates the OrderHeaderWorkEffort with the specific Id
	 * 
	 * @param orderHeaderWorkEffortToBeUpdated the OrderHeaderWorkEffort thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderHeaderWorkEffort(OrderHeaderWorkEffort orderHeaderWorkEffortToBeUpdated) {

		UpdateOrderHeaderWorkEffort com = new UpdateOrderHeaderWorkEffort(orderHeaderWorkEffortToBeUpdated);

		int usedTicketId;

		synchronized (OrderHeaderWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderWorkEffortUpdated.class,
				event -> sendOrderHeaderWorkEffortChangedMessage(((OrderHeaderWorkEffortUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderHeaderWorkEffort from the database
	 * 
	 * @param orderHeaderWorkEffortId:
	 *            the id of the OrderHeaderWorkEffort thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderHeaderWorkEffortById(@RequestParam(value = "orderHeaderWorkEffortId") String orderHeaderWorkEffortId) {

		DeleteOrderHeaderWorkEffort com = new DeleteOrderHeaderWorkEffort(orderHeaderWorkEffortId);

		int usedTicketId;

		synchronized (OrderHeaderWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderWorkEffortDeleted.class,
				event -> sendOrderHeaderWorkEffortChangedMessage(((OrderHeaderWorkEffortDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderHeaderWorkEffortChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderHeaderWorkEffort/\" plus one of the following: "
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
