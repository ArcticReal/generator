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
import com.skytala.eCommerce.command.AddOrderItem;
import com.skytala.eCommerce.command.DeleteOrderItem;
import com.skytala.eCommerce.command.UpdateOrderItem;
import com.skytala.eCommerce.entity.OrderItem;
import com.skytala.eCommerce.entity.OrderItemMapper;
import com.skytala.eCommerce.event.OrderItemAdded;
import com.skytala.eCommerce.event.OrderItemDeleted;
import com.skytala.eCommerce.event.OrderItemFound;
import com.skytala.eCommerce.event.OrderItemUpdated;
import com.skytala.eCommerce.query.FindOrderItemsBy;

@RestController
@RequestMapping("/api/orderItem")
public class OrderItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItem
	 * @return a List with the OrderItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItem> findOrderItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemsBy query = new FindOrderItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemFound.class,
				event -> sendOrderItemsFoundMessage(((OrderItemFound) event).getOrderItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemsFoundMessage(List<OrderItem> orderItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItems);
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
	public boolean createOrderItem(HttpServletRequest request) {

		OrderItem orderItemToBeAdded = new OrderItem();
		try {
			orderItemToBeAdded = OrderItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItem(orderItemToBeAdded);

	}

	/**
	 * creates a new OrderItem entry in the ofbiz database
	 * 
	 * @param orderItemToBeAdded
	 *            the OrderItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItem(OrderItem orderItemToBeAdded) {

		AddOrderItem com = new AddOrderItem(orderItemToBeAdded);
		int usedTicketId;

		synchronized (OrderItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAdded.class,
				event -> sendOrderItemChangedMessage(((OrderItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItem(HttpServletRequest request) {

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

		OrderItem orderItemToBeUpdated = new OrderItem();

		try {
			orderItemToBeUpdated = OrderItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItem(orderItemToBeUpdated);

	}

	/**
	 * Updates the OrderItem with the specific Id
	 * 
	 * @param orderItemToBeUpdated the OrderItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItem(OrderItem orderItemToBeUpdated) {

		UpdateOrderItem com = new UpdateOrderItem(orderItemToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemUpdated.class,
				event -> sendOrderItemChangedMessage(((OrderItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItem from the database
	 * 
	 * @param orderItemId:
	 *            the id of the OrderItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemById(@RequestParam(value = "orderItemId") String orderItemId) {

		DeleteOrderItem com = new DeleteOrderItem(orderItemId);

		int usedTicketId;

		synchronized (OrderItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemDeleted.class,
				event -> sendOrderItemChangedMessage(((OrderItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItem/\" plus one of the following: "
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
