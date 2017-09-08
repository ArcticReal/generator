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
import com.skytala.eCommerce.command.AddOrderItemGroupOrder;
import com.skytala.eCommerce.command.DeleteOrderItemGroupOrder;
import com.skytala.eCommerce.command.UpdateOrderItemGroupOrder;
import com.skytala.eCommerce.entity.OrderItemGroupOrder;
import com.skytala.eCommerce.entity.OrderItemGroupOrderMapper;
import com.skytala.eCommerce.event.OrderItemGroupOrderAdded;
import com.skytala.eCommerce.event.OrderItemGroupOrderDeleted;
import com.skytala.eCommerce.event.OrderItemGroupOrderFound;
import com.skytala.eCommerce.event.OrderItemGroupOrderUpdated;
import com.skytala.eCommerce.query.FindOrderItemGroupOrdersBy;

@RestController
@RequestMapping("/api/orderItemGroupOrder")
public class OrderItemGroupOrderController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemGroupOrder>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemGroupOrderController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemGroupOrder
	 * @return a List with the OrderItemGroupOrders
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemGroupOrder> findOrderItemGroupOrdersBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemGroupOrdersBy query = new FindOrderItemGroupOrdersBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemGroupOrderController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemGroupOrderFound.class,
				event -> sendOrderItemGroupOrdersFoundMessage(((OrderItemGroupOrderFound) event).getOrderItemGroupOrders(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemGroupOrdersFoundMessage(List<OrderItemGroupOrder> orderItemGroupOrders, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemGroupOrders);
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
	public boolean createOrderItemGroupOrder(HttpServletRequest request) {

		OrderItemGroupOrder orderItemGroupOrderToBeAdded = new OrderItemGroupOrder();
		try {
			orderItemGroupOrderToBeAdded = OrderItemGroupOrderMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemGroupOrder(orderItemGroupOrderToBeAdded);

	}

	/**
	 * creates a new OrderItemGroupOrder entry in the ofbiz database
	 * 
	 * @param orderItemGroupOrderToBeAdded
	 *            the OrderItemGroupOrder thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemGroupOrder(OrderItemGroupOrder orderItemGroupOrderToBeAdded) {

		AddOrderItemGroupOrder com = new AddOrderItemGroupOrder(orderItemGroupOrderToBeAdded);
		int usedTicketId;

		synchronized (OrderItemGroupOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemGroupOrderAdded.class,
				event -> sendOrderItemGroupOrderChangedMessage(((OrderItemGroupOrderAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemGroupOrder(HttpServletRequest request) {

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

		OrderItemGroupOrder orderItemGroupOrderToBeUpdated = new OrderItemGroupOrder();

		try {
			orderItemGroupOrderToBeUpdated = OrderItemGroupOrderMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemGroupOrder(orderItemGroupOrderToBeUpdated);

	}

	/**
	 * Updates the OrderItemGroupOrder with the specific Id
	 * 
	 * @param orderItemGroupOrderToBeUpdated the OrderItemGroupOrder thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemGroupOrder(OrderItemGroupOrder orderItemGroupOrderToBeUpdated) {

		UpdateOrderItemGroupOrder com = new UpdateOrderItemGroupOrder(orderItemGroupOrderToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemGroupOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemGroupOrderUpdated.class,
				event -> sendOrderItemGroupOrderChangedMessage(((OrderItemGroupOrderUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemGroupOrder from the database
	 * 
	 * @param orderItemGroupOrderId:
	 *            the id of the OrderItemGroupOrder thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemGroupOrderById(@RequestParam(value = "orderItemGroupOrderId") String orderItemGroupOrderId) {

		DeleteOrderItemGroupOrder com = new DeleteOrderItemGroupOrder(orderItemGroupOrderId);

		int usedTicketId;

		synchronized (OrderItemGroupOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemGroupOrderDeleted.class,
				event -> sendOrderItemGroupOrderChangedMessage(((OrderItemGroupOrderDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemGroupOrderChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemGroupOrder/\" plus one of the following: "
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
