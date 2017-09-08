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
import com.skytala.eCommerce.command.AddOrderItemChange;
import com.skytala.eCommerce.command.DeleteOrderItemChange;
import com.skytala.eCommerce.command.UpdateOrderItemChange;
import com.skytala.eCommerce.entity.OrderItemChange;
import com.skytala.eCommerce.entity.OrderItemChangeMapper;
import com.skytala.eCommerce.event.OrderItemChangeAdded;
import com.skytala.eCommerce.event.OrderItemChangeDeleted;
import com.skytala.eCommerce.event.OrderItemChangeFound;
import com.skytala.eCommerce.event.OrderItemChangeUpdated;
import com.skytala.eCommerce.query.FindOrderItemChangesBy;

@RestController
@RequestMapping("/api/orderItemChange")
public class OrderItemChangeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemChange>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemChangeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemChange
	 * @return a List with the OrderItemChanges
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemChange> findOrderItemChangesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemChangesBy query = new FindOrderItemChangesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemChangeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemChangeFound.class,
				event -> sendOrderItemChangesFoundMessage(((OrderItemChangeFound) event).getOrderItemChanges(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemChangesFoundMessage(List<OrderItemChange> orderItemChanges, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemChanges);
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
	public boolean createOrderItemChange(HttpServletRequest request) {

		OrderItemChange orderItemChangeToBeAdded = new OrderItemChange();
		try {
			orderItemChangeToBeAdded = OrderItemChangeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemChange(orderItemChangeToBeAdded);

	}

	/**
	 * creates a new OrderItemChange entry in the ofbiz database
	 * 
	 * @param orderItemChangeToBeAdded
	 *            the OrderItemChange thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemChange(OrderItemChange orderItemChangeToBeAdded) {

		AddOrderItemChange com = new AddOrderItemChange(orderItemChangeToBeAdded);
		int usedTicketId;

		synchronized (OrderItemChangeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemChangeAdded.class,
				event -> sendOrderItemChangeChangedMessage(((OrderItemChangeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemChange(HttpServletRequest request) {

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

		OrderItemChange orderItemChangeToBeUpdated = new OrderItemChange();

		try {
			orderItemChangeToBeUpdated = OrderItemChangeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemChange(orderItemChangeToBeUpdated);

	}

	/**
	 * Updates the OrderItemChange with the specific Id
	 * 
	 * @param orderItemChangeToBeUpdated the OrderItemChange thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemChange(OrderItemChange orderItemChangeToBeUpdated) {

		UpdateOrderItemChange com = new UpdateOrderItemChange(orderItemChangeToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemChangeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemChangeUpdated.class,
				event -> sendOrderItemChangeChangedMessage(((OrderItemChangeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemChange from the database
	 * 
	 * @param orderItemChangeId:
	 *            the id of the OrderItemChange thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemChangeById(@RequestParam(value = "orderItemChangeId") String orderItemChangeId) {

		DeleteOrderItemChange com = new DeleteOrderItemChange(orderItemChangeId);

		int usedTicketId;

		synchronized (OrderItemChangeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemChangeDeleted.class,
				event -> sendOrderItemChangeChangedMessage(((OrderItemChangeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemChangeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemChange/\" plus one of the following: "
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
