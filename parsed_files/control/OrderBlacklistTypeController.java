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
import com.skytala.eCommerce.command.AddOrderBlacklistType;
import com.skytala.eCommerce.command.DeleteOrderBlacklistType;
import com.skytala.eCommerce.command.UpdateOrderBlacklistType;
import com.skytala.eCommerce.entity.OrderBlacklistType;
import com.skytala.eCommerce.entity.OrderBlacklistTypeMapper;
import com.skytala.eCommerce.event.OrderBlacklistTypeAdded;
import com.skytala.eCommerce.event.OrderBlacklistTypeDeleted;
import com.skytala.eCommerce.event.OrderBlacklistTypeFound;
import com.skytala.eCommerce.event.OrderBlacklistTypeUpdated;
import com.skytala.eCommerce.query.FindOrderBlacklistTypesBy;

@RestController
@RequestMapping("/api/orderBlacklistType")
public class OrderBlacklistTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderBlacklistType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderBlacklistTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderBlacklistType
	 * @return a List with the OrderBlacklistTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderBlacklistType> findOrderBlacklistTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderBlacklistTypesBy query = new FindOrderBlacklistTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderBlacklistTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderBlacklistTypeFound.class,
				event -> sendOrderBlacklistTypesFoundMessage(((OrderBlacklistTypeFound) event).getOrderBlacklistTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderBlacklistTypesFoundMessage(List<OrderBlacklistType> orderBlacklistTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderBlacklistTypes);
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
	public boolean createOrderBlacklistType(HttpServletRequest request) {

		OrderBlacklistType orderBlacklistTypeToBeAdded = new OrderBlacklistType();
		try {
			orderBlacklistTypeToBeAdded = OrderBlacklistTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderBlacklistType(orderBlacklistTypeToBeAdded);

	}

	/**
	 * creates a new OrderBlacklistType entry in the ofbiz database
	 * 
	 * @param orderBlacklistTypeToBeAdded
	 *            the OrderBlacklistType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderBlacklistType(OrderBlacklistType orderBlacklistTypeToBeAdded) {

		AddOrderBlacklistType com = new AddOrderBlacklistType(orderBlacklistTypeToBeAdded);
		int usedTicketId;

		synchronized (OrderBlacklistTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderBlacklistTypeAdded.class,
				event -> sendOrderBlacklistTypeChangedMessage(((OrderBlacklistTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderBlacklistType(HttpServletRequest request) {

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

		OrderBlacklistType orderBlacklistTypeToBeUpdated = new OrderBlacklistType();

		try {
			orderBlacklistTypeToBeUpdated = OrderBlacklistTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderBlacklistType(orderBlacklistTypeToBeUpdated);

	}

	/**
	 * Updates the OrderBlacklistType with the specific Id
	 * 
	 * @param orderBlacklistTypeToBeUpdated the OrderBlacklistType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderBlacklistType(OrderBlacklistType orderBlacklistTypeToBeUpdated) {

		UpdateOrderBlacklistType com = new UpdateOrderBlacklistType(orderBlacklistTypeToBeUpdated);

		int usedTicketId;

		synchronized (OrderBlacklistTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderBlacklistTypeUpdated.class,
				event -> sendOrderBlacklistTypeChangedMessage(((OrderBlacklistTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderBlacklistType from the database
	 * 
	 * @param orderBlacklistTypeId:
	 *            the id of the OrderBlacklistType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderBlacklistTypeById(@RequestParam(value = "orderBlacklistTypeId") String orderBlacklistTypeId) {

		DeleteOrderBlacklistType com = new DeleteOrderBlacklistType(orderBlacklistTypeId);

		int usedTicketId;

		synchronized (OrderBlacklistTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderBlacklistTypeDeleted.class,
				event -> sendOrderBlacklistTypeChangedMessage(((OrderBlacklistTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderBlacklistTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderBlacklistType/\" plus one of the following: "
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
