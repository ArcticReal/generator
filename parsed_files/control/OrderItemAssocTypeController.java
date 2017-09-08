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
import com.skytala.eCommerce.command.AddOrderItemAssocType;
import com.skytala.eCommerce.command.DeleteOrderItemAssocType;
import com.skytala.eCommerce.command.UpdateOrderItemAssocType;
import com.skytala.eCommerce.entity.OrderItemAssocType;
import com.skytala.eCommerce.entity.OrderItemAssocTypeMapper;
import com.skytala.eCommerce.event.OrderItemAssocTypeAdded;
import com.skytala.eCommerce.event.OrderItemAssocTypeDeleted;
import com.skytala.eCommerce.event.OrderItemAssocTypeFound;
import com.skytala.eCommerce.event.OrderItemAssocTypeUpdated;
import com.skytala.eCommerce.query.FindOrderItemAssocTypesBy;

@RestController
@RequestMapping("/api/orderItemAssocType")
public class OrderItemAssocTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemAssocType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemAssocTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemAssocType
	 * @return a List with the OrderItemAssocTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemAssocType> findOrderItemAssocTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemAssocTypesBy query = new FindOrderItemAssocTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemAssocTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAssocTypeFound.class,
				event -> sendOrderItemAssocTypesFoundMessage(((OrderItemAssocTypeFound) event).getOrderItemAssocTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemAssocTypesFoundMessage(List<OrderItemAssocType> orderItemAssocTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemAssocTypes);
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
	public boolean createOrderItemAssocType(HttpServletRequest request) {

		OrderItemAssocType orderItemAssocTypeToBeAdded = new OrderItemAssocType();
		try {
			orderItemAssocTypeToBeAdded = OrderItemAssocTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemAssocType(orderItemAssocTypeToBeAdded);

	}

	/**
	 * creates a new OrderItemAssocType entry in the ofbiz database
	 * 
	 * @param orderItemAssocTypeToBeAdded
	 *            the OrderItemAssocType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemAssocType(OrderItemAssocType orderItemAssocTypeToBeAdded) {

		AddOrderItemAssocType com = new AddOrderItemAssocType(orderItemAssocTypeToBeAdded);
		int usedTicketId;

		synchronized (OrderItemAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAssocTypeAdded.class,
				event -> sendOrderItemAssocTypeChangedMessage(((OrderItemAssocTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemAssocType(HttpServletRequest request) {

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

		OrderItemAssocType orderItemAssocTypeToBeUpdated = new OrderItemAssocType();

		try {
			orderItemAssocTypeToBeUpdated = OrderItemAssocTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemAssocType(orderItemAssocTypeToBeUpdated);

	}

	/**
	 * Updates the OrderItemAssocType with the specific Id
	 * 
	 * @param orderItemAssocTypeToBeUpdated the OrderItemAssocType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemAssocType(OrderItemAssocType orderItemAssocTypeToBeUpdated) {

		UpdateOrderItemAssocType com = new UpdateOrderItemAssocType(orderItemAssocTypeToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAssocTypeUpdated.class,
				event -> sendOrderItemAssocTypeChangedMessage(((OrderItemAssocTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemAssocType from the database
	 * 
	 * @param orderItemAssocTypeId:
	 *            the id of the OrderItemAssocType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemAssocTypeById(@RequestParam(value = "orderItemAssocTypeId") String orderItemAssocTypeId) {

		DeleteOrderItemAssocType com = new DeleteOrderItemAssocType(orderItemAssocTypeId);

		int usedTicketId;

		synchronized (OrderItemAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAssocTypeDeleted.class,
				event -> sendOrderItemAssocTypeChangedMessage(((OrderItemAssocTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemAssocTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemAssocType/\" plus one of the following: "
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
