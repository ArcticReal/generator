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
import com.skytala.eCommerce.command.AddOrderAdjustmentAttribute;
import com.skytala.eCommerce.command.DeleteOrderAdjustmentAttribute;
import com.skytala.eCommerce.command.UpdateOrderAdjustmentAttribute;
import com.skytala.eCommerce.entity.OrderAdjustmentAttribute;
import com.skytala.eCommerce.entity.OrderAdjustmentAttributeMapper;
import com.skytala.eCommerce.event.OrderAdjustmentAttributeAdded;
import com.skytala.eCommerce.event.OrderAdjustmentAttributeDeleted;
import com.skytala.eCommerce.event.OrderAdjustmentAttributeFound;
import com.skytala.eCommerce.event.OrderAdjustmentAttributeUpdated;
import com.skytala.eCommerce.query.FindOrderAdjustmentAttributesBy;

@RestController
@RequestMapping("/api/orderAdjustmentAttribute")
public class OrderAdjustmentAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderAdjustmentAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderAdjustmentAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderAdjustmentAttribute
	 * @return a List with the OrderAdjustmentAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderAdjustmentAttribute> findOrderAdjustmentAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderAdjustmentAttributesBy query = new FindOrderAdjustmentAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderAdjustmentAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentAttributeFound.class,
				event -> sendOrderAdjustmentAttributesFoundMessage(((OrderAdjustmentAttributeFound) event).getOrderAdjustmentAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderAdjustmentAttributesFoundMessage(List<OrderAdjustmentAttribute> orderAdjustmentAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderAdjustmentAttributes);
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
	public boolean createOrderAdjustmentAttribute(HttpServletRequest request) {

		OrderAdjustmentAttribute orderAdjustmentAttributeToBeAdded = new OrderAdjustmentAttribute();
		try {
			orderAdjustmentAttributeToBeAdded = OrderAdjustmentAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderAdjustmentAttribute(orderAdjustmentAttributeToBeAdded);

	}

	/**
	 * creates a new OrderAdjustmentAttribute entry in the ofbiz database
	 * 
	 * @param orderAdjustmentAttributeToBeAdded
	 *            the OrderAdjustmentAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderAdjustmentAttribute(OrderAdjustmentAttribute orderAdjustmentAttributeToBeAdded) {

		AddOrderAdjustmentAttribute com = new AddOrderAdjustmentAttribute(orderAdjustmentAttributeToBeAdded);
		int usedTicketId;

		synchronized (OrderAdjustmentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentAttributeAdded.class,
				event -> sendOrderAdjustmentAttributeChangedMessage(((OrderAdjustmentAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderAdjustmentAttribute(HttpServletRequest request) {

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

		OrderAdjustmentAttribute orderAdjustmentAttributeToBeUpdated = new OrderAdjustmentAttribute();

		try {
			orderAdjustmentAttributeToBeUpdated = OrderAdjustmentAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderAdjustmentAttribute(orderAdjustmentAttributeToBeUpdated);

	}

	/**
	 * Updates the OrderAdjustmentAttribute with the specific Id
	 * 
	 * @param orderAdjustmentAttributeToBeUpdated the OrderAdjustmentAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderAdjustmentAttribute(OrderAdjustmentAttribute orderAdjustmentAttributeToBeUpdated) {

		UpdateOrderAdjustmentAttribute com = new UpdateOrderAdjustmentAttribute(orderAdjustmentAttributeToBeUpdated);

		int usedTicketId;

		synchronized (OrderAdjustmentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentAttributeUpdated.class,
				event -> sendOrderAdjustmentAttributeChangedMessage(((OrderAdjustmentAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderAdjustmentAttribute from the database
	 * 
	 * @param orderAdjustmentAttributeId:
	 *            the id of the OrderAdjustmentAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderAdjustmentAttributeById(@RequestParam(value = "orderAdjustmentAttributeId") String orderAdjustmentAttributeId) {

		DeleteOrderAdjustmentAttribute com = new DeleteOrderAdjustmentAttribute(orderAdjustmentAttributeId);

		int usedTicketId;

		synchronized (OrderAdjustmentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentAttributeDeleted.class,
				event -> sendOrderAdjustmentAttributeChangedMessage(((OrderAdjustmentAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderAdjustmentAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderAdjustmentAttribute/\" plus one of the following: "
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
