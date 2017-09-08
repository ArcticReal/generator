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
import com.skytala.eCommerce.command.AddOrderShipment;
import com.skytala.eCommerce.command.DeleteOrderShipment;
import com.skytala.eCommerce.command.UpdateOrderShipment;
import com.skytala.eCommerce.entity.OrderShipment;
import com.skytala.eCommerce.entity.OrderShipmentMapper;
import com.skytala.eCommerce.event.OrderShipmentAdded;
import com.skytala.eCommerce.event.OrderShipmentDeleted;
import com.skytala.eCommerce.event.OrderShipmentFound;
import com.skytala.eCommerce.event.OrderShipmentUpdated;
import com.skytala.eCommerce.query.FindOrderShipmentsBy;

@RestController
@RequestMapping("/api/orderShipment")
public class OrderShipmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderShipment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderShipmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderShipment
	 * @return a List with the OrderShipments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderShipment> findOrderShipmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderShipmentsBy query = new FindOrderShipmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderShipmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderShipmentFound.class,
				event -> sendOrderShipmentsFoundMessage(((OrderShipmentFound) event).getOrderShipments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderShipmentsFoundMessage(List<OrderShipment> orderShipments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderShipments);
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
	public boolean createOrderShipment(HttpServletRequest request) {

		OrderShipment orderShipmentToBeAdded = new OrderShipment();
		try {
			orderShipmentToBeAdded = OrderShipmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderShipment(orderShipmentToBeAdded);

	}

	/**
	 * creates a new OrderShipment entry in the ofbiz database
	 * 
	 * @param orderShipmentToBeAdded
	 *            the OrderShipment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderShipment(OrderShipment orderShipmentToBeAdded) {

		AddOrderShipment com = new AddOrderShipment(orderShipmentToBeAdded);
		int usedTicketId;

		synchronized (OrderShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderShipmentAdded.class,
				event -> sendOrderShipmentChangedMessage(((OrderShipmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderShipment(HttpServletRequest request) {

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

		OrderShipment orderShipmentToBeUpdated = new OrderShipment();

		try {
			orderShipmentToBeUpdated = OrderShipmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderShipment(orderShipmentToBeUpdated);

	}

	/**
	 * Updates the OrderShipment with the specific Id
	 * 
	 * @param orderShipmentToBeUpdated the OrderShipment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderShipment(OrderShipment orderShipmentToBeUpdated) {

		UpdateOrderShipment com = new UpdateOrderShipment(orderShipmentToBeUpdated);

		int usedTicketId;

		synchronized (OrderShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderShipmentUpdated.class,
				event -> sendOrderShipmentChangedMessage(((OrderShipmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderShipment from the database
	 * 
	 * @param orderShipmentId:
	 *            the id of the OrderShipment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderShipmentById(@RequestParam(value = "orderShipmentId") String orderShipmentId) {

		DeleteOrderShipment com = new DeleteOrderShipment(orderShipmentId);

		int usedTicketId;

		synchronized (OrderShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderShipmentDeleted.class,
				event -> sendOrderShipmentChangedMessage(((OrderShipmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderShipmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderShipment/\" plus one of the following: "
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
