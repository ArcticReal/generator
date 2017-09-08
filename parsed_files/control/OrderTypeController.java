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
import com.skytala.eCommerce.command.AddOrderType;
import com.skytala.eCommerce.command.DeleteOrderType;
import com.skytala.eCommerce.command.UpdateOrderType;
import com.skytala.eCommerce.entity.OrderType;
import com.skytala.eCommerce.entity.OrderTypeMapper;
import com.skytala.eCommerce.event.OrderTypeAdded;
import com.skytala.eCommerce.event.OrderTypeDeleted;
import com.skytala.eCommerce.event.OrderTypeFound;
import com.skytala.eCommerce.event.OrderTypeUpdated;
import com.skytala.eCommerce.query.FindOrderTypesBy;

@RestController
@RequestMapping("/api/orderType")
public class OrderTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderType
	 * @return a List with the OrderTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderType> findOrderTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderTypesBy query = new FindOrderTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTypeFound.class,
				event -> sendOrderTypesFoundMessage(((OrderTypeFound) event).getOrderTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderTypesFoundMessage(List<OrderType> orderTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderTypes);
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
	public boolean createOrderType(HttpServletRequest request) {

		OrderType orderTypeToBeAdded = new OrderType();
		try {
			orderTypeToBeAdded = OrderTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderType(orderTypeToBeAdded);

	}

	/**
	 * creates a new OrderType entry in the ofbiz database
	 * 
	 * @param orderTypeToBeAdded
	 *            the OrderType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderType(OrderType orderTypeToBeAdded) {

		AddOrderType com = new AddOrderType(orderTypeToBeAdded);
		int usedTicketId;

		synchronized (OrderTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTypeAdded.class,
				event -> sendOrderTypeChangedMessage(((OrderTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderType(HttpServletRequest request) {

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

		OrderType orderTypeToBeUpdated = new OrderType();

		try {
			orderTypeToBeUpdated = OrderTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderType(orderTypeToBeUpdated);

	}

	/**
	 * Updates the OrderType with the specific Id
	 * 
	 * @param orderTypeToBeUpdated the OrderType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderType(OrderType orderTypeToBeUpdated) {

		UpdateOrderType com = new UpdateOrderType(orderTypeToBeUpdated);

		int usedTicketId;

		synchronized (OrderTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTypeUpdated.class,
				event -> sendOrderTypeChangedMessage(((OrderTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderType from the database
	 * 
	 * @param orderTypeId:
	 *            the id of the OrderType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderTypeById(@RequestParam(value = "orderTypeId") String orderTypeId) {

		DeleteOrderType com = new DeleteOrderType(orderTypeId);

		int usedTicketId;

		synchronized (OrderTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTypeDeleted.class,
				event -> sendOrderTypeChangedMessage(((OrderTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderType/\" plus one of the following: "
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
