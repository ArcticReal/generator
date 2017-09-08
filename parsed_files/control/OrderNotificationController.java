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
import com.skytala.eCommerce.command.AddOrderNotification;
import com.skytala.eCommerce.command.DeleteOrderNotification;
import com.skytala.eCommerce.command.UpdateOrderNotification;
import com.skytala.eCommerce.entity.OrderNotification;
import com.skytala.eCommerce.entity.OrderNotificationMapper;
import com.skytala.eCommerce.event.OrderNotificationAdded;
import com.skytala.eCommerce.event.OrderNotificationDeleted;
import com.skytala.eCommerce.event.OrderNotificationFound;
import com.skytala.eCommerce.event.OrderNotificationUpdated;
import com.skytala.eCommerce.query.FindOrderNotificationsBy;

@RestController
@RequestMapping("/api/orderNotification")
public class OrderNotificationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderNotification>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderNotificationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderNotification
	 * @return a List with the OrderNotifications
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderNotification> findOrderNotificationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderNotificationsBy query = new FindOrderNotificationsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderNotificationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderNotificationFound.class,
				event -> sendOrderNotificationsFoundMessage(((OrderNotificationFound) event).getOrderNotifications(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderNotificationsFoundMessage(List<OrderNotification> orderNotifications, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderNotifications);
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
	public boolean createOrderNotification(HttpServletRequest request) {

		OrderNotification orderNotificationToBeAdded = new OrderNotification();
		try {
			orderNotificationToBeAdded = OrderNotificationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderNotification(orderNotificationToBeAdded);

	}

	/**
	 * creates a new OrderNotification entry in the ofbiz database
	 * 
	 * @param orderNotificationToBeAdded
	 *            the OrderNotification thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderNotification(OrderNotification orderNotificationToBeAdded) {

		AddOrderNotification com = new AddOrderNotification(orderNotificationToBeAdded);
		int usedTicketId;

		synchronized (OrderNotificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderNotificationAdded.class,
				event -> sendOrderNotificationChangedMessage(((OrderNotificationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderNotification(HttpServletRequest request) {

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

		OrderNotification orderNotificationToBeUpdated = new OrderNotification();

		try {
			orderNotificationToBeUpdated = OrderNotificationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderNotification(orderNotificationToBeUpdated);

	}

	/**
	 * Updates the OrderNotification with the specific Id
	 * 
	 * @param orderNotificationToBeUpdated the OrderNotification thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderNotification(OrderNotification orderNotificationToBeUpdated) {

		UpdateOrderNotification com = new UpdateOrderNotification(orderNotificationToBeUpdated);

		int usedTicketId;

		synchronized (OrderNotificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderNotificationUpdated.class,
				event -> sendOrderNotificationChangedMessage(((OrderNotificationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderNotification from the database
	 * 
	 * @param orderNotificationId:
	 *            the id of the OrderNotification thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderNotificationById(@RequestParam(value = "orderNotificationId") String orderNotificationId) {

		DeleteOrderNotification com = new DeleteOrderNotification(orderNotificationId);

		int usedTicketId;

		synchronized (OrderNotificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderNotificationDeleted.class,
				event -> sendOrderNotificationChangedMessage(((OrderNotificationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderNotificationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderNotification/\" plus one of the following: "
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
