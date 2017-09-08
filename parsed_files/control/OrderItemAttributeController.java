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
import com.skytala.eCommerce.command.AddOrderItemAttribute;
import com.skytala.eCommerce.command.DeleteOrderItemAttribute;
import com.skytala.eCommerce.command.UpdateOrderItemAttribute;
import com.skytala.eCommerce.entity.OrderItemAttribute;
import com.skytala.eCommerce.entity.OrderItemAttributeMapper;
import com.skytala.eCommerce.event.OrderItemAttributeAdded;
import com.skytala.eCommerce.event.OrderItemAttributeDeleted;
import com.skytala.eCommerce.event.OrderItemAttributeFound;
import com.skytala.eCommerce.event.OrderItemAttributeUpdated;
import com.skytala.eCommerce.query.FindOrderItemAttributesBy;

@RestController
@RequestMapping("/api/orderItemAttribute")
public class OrderItemAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemAttribute
	 * @return a List with the OrderItemAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemAttribute> findOrderItemAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemAttributesBy query = new FindOrderItemAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAttributeFound.class,
				event -> sendOrderItemAttributesFoundMessage(((OrderItemAttributeFound) event).getOrderItemAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemAttributesFoundMessage(List<OrderItemAttribute> orderItemAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemAttributes);
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
	public boolean createOrderItemAttribute(HttpServletRequest request) {

		OrderItemAttribute orderItemAttributeToBeAdded = new OrderItemAttribute();
		try {
			orderItemAttributeToBeAdded = OrderItemAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemAttribute(orderItemAttributeToBeAdded);

	}

	/**
	 * creates a new OrderItemAttribute entry in the ofbiz database
	 * 
	 * @param orderItemAttributeToBeAdded
	 *            the OrderItemAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemAttribute(OrderItemAttribute orderItemAttributeToBeAdded) {

		AddOrderItemAttribute com = new AddOrderItemAttribute(orderItemAttributeToBeAdded);
		int usedTicketId;

		synchronized (OrderItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAttributeAdded.class,
				event -> sendOrderItemAttributeChangedMessage(((OrderItemAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemAttribute(HttpServletRequest request) {

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

		OrderItemAttribute orderItemAttributeToBeUpdated = new OrderItemAttribute();

		try {
			orderItemAttributeToBeUpdated = OrderItemAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemAttribute(orderItemAttributeToBeUpdated);

	}

	/**
	 * Updates the OrderItemAttribute with the specific Id
	 * 
	 * @param orderItemAttributeToBeUpdated the OrderItemAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemAttribute(OrderItemAttribute orderItemAttributeToBeUpdated) {

		UpdateOrderItemAttribute com = new UpdateOrderItemAttribute(orderItemAttributeToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAttributeUpdated.class,
				event -> sendOrderItemAttributeChangedMessage(((OrderItemAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemAttribute from the database
	 * 
	 * @param orderItemAttributeId:
	 *            the id of the OrderItemAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemAttributeById(@RequestParam(value = "orderItemAttributeId") String orderItemAttributeId) {

		DeleteOrderItemAttribute com = new DeleteOrderItemAttribute(orderItemAttributeId);

		int usedTicketId;

		synchronized (OrderItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAttributeDeleted.class,
				event -> sendOrderItemAttributeChangedMessage(((OrderItemAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemAttribute/\" plus one of the following: "
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
