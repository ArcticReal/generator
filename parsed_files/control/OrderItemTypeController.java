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
import com.skytala.eCommerce.command.AddOrderItemType;
import com.skytala.eCommerce.command.DeleteOrderItemType;
import com.skytala.eCommerce.command.UpdateOrderItemType;
import com.skytala.eCommerce.entity.OrderItemType;
import com.skytala.eCommerce.entity.OrderItemTypeMapper;
import com.skytala.eCommerce.event.OrderItemTypeAdded;
import com.skytala.eCommerce.event.OrderItemTypeDeleted;
import com.skytala.eCommerce.event.OrderItemTypeFound;
import com.skytala.eCommerce.event.OrderItemTypeUpdated;
import com.skytala.eCommerce.query.FindOrderItemTypesBy;

@RestController
@RequestMapping("/api/orderItemType")
public class OrderItemTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemType
	 * @return a List with the OrderItemTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemType> findOrderItemTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemTypesBy query = new FindOrderItemTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemTypeFound.class,
				event -> sendOrderItemTypesFoundMessage(((OrderItemTypeFound) event).getOrderItemTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemTypesFoundMessage(List<OrderItemType> orderItemTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemTypes);
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
	public boolean createOrderItemType(HttpServletRequest request) {

		OrderItemType orderItemTypeToBeAdded = new OrderItemType();
		try {
			orderItemTypeToBeAdded = OrderItemTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemType(orderItemTypeToBeAdded);

	}

	/**
	 * creates a new OrderItemType entry in the ofbiz database
	 * 
	 * @param orderItemTypeToBeAdded
	 *            the OrderItemType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemType(OrderItemType orderItemTypeToBeAdded) {

		AddOrderItemType com = new AddOrderItemType(orderItemTypeToBeAdded);
		int usedTicketId;

		synchronized (OrderItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemTypeAdded.class,
				event -> sendOrderItemTypeChangedMessage(((OrderItemTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemType(HttpServletRequest request) {

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

		OrderItemType orderItemTypeToBeUpdated = new OrderItemType();

		try {
			orderItemTypeToBeUpdated = OrderItemTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemType(orderItemTypeToBeUpdated);

	}

	/**
	 * Updates the OrderItemType with the specific Id
	 * 
	 * @param orderItemTypeToBeUpdated the OrderItemType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemType(OrderItemType orderItemTypeToBeUpdated) {

		UpdateOrderItemType com = new UpdateOrderItemType(orderItemTypeToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemTypeUpdated.class,
				event -> sendOrderItemTypeChangedMessage(((OrderItemTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemType from the database
	 * 
	 * @param orderItemTypeId:
	 *            the id of the OrderItemType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemTypeById(@RequestParam(value = "orderItemTypeId") String orderItemTypeId) {

		DeleteOrderItemType com = new DeleteOrderItemType(orderItemTypeId);

		int usedTicketId;

		synchronized (OrderItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemTypeDeleted.class,
				event -> sendOrderItemTypeChangedMessage(((OrderItemTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemType/\" plus one of the following: "
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
