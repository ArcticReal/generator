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
import com.skytala.eCommerce.command.AddOrderStatus;
import com.skytala.eCommerce.command.DeleteOrderStatus;
import com.skytala.eCommerce.command.UpdateOrderStatus;
import com.skytala.eCommerce.entity.OrderStatus;
import com.skytala.eCommerce.entity.OrderStatusMapper;
import com.skytala.eCommerce.event.OrderStatusAdded;
import com.skytala.eCommerce.event.OrderStatusDeleted;
import com.skytala.eCommerce.event.OrderStatusFound;
import com.skytala.eCommerce.event.OrderStatusUpdated;
import com.skytala.eCommerce.query.FindOrderStatussBy;

@RestController
@RequestMapping("/api/orderStatus")
public class OrderStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderStatus
	 * @return a List with the OrderStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderStatus> findOrderStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderStatussBy query = new FindOrderStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderStatusFound.class,
				event -> sendOrderStatussFoundMessage(((OrderStatusFound) event).getOrderStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderStatussFoundMessage(List<OrderStatus> orderStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderStatuss);
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
	public boolean createOrderStatus(HttpServletRequest request) {

		OrderStatus orderStatusToBeAdded = new OrderStatus();
		try {
			orderStatusToBeAdded = OrderStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderStatus(orderStatusToBeAdded);

	}

	/**
	 * creates a new OrderStatus entry in the ofbiz database
	 * 
	 * @param orderStatusToBeAdded
	 *            the OrderStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderStatus(OrderStatus orderStatusToBeAdded) {

		AddOrderStatus com = new AddOrderStatus(orderStatusToBeAdded);
		int usedTicketId;

		synchronized (OrderStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderStatusAdded.class,
				event -> sendOrderStatusChangedMessage(((OrderStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderStatus(HttpServletRequest request) {

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

		OrderStatus orderStatusToBeUpdated = new OrderStatus();

		try {
			orderStatusToBeUpdated = OrderStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderStatus(orderStatusToBeUpdated);

	}

	/**
	 * Updates the OrderStatus with the specific Id
	 * 
	 * @param orderStatusToBeUpdated the OrderStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderStatus(OrderStatus orderStatusToBeUpdated) {

		UpdateOrderStatus com = new UpdateOrderStatus(orderStatusToBeUpdated);

		int usedTicketId;

		synchronized (OrderStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderStatusUpdated.class,
				event -> sendOrderStatusChangedMessage(((OrderStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderStatus from the database
	 * 
	 * @param orderStatusId:
	 *            the id of the OrderStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderStatusById(@RequestParam(value = "orderStatusId") String orderStatusId) {

		DeleteOrderStatus com = new DeleteOrderStatus(orderStatusId);

		int usedTicketId;

		synchronized (OrderStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderStatusDeleted.class,
				event -> sendOrderStatusChangedMessage(((OrderStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderStatus/\" plus one of the following: "
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
