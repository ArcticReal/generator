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
import com.skytala.eCommerce.command.AddOrderAdjustmentType;
import com.skytala.eCommerce.command.DeleteOrderAdjustmentType;
import com.skytala.eCommerce.command.UpdateOrderAdjustmentType;
import com.skytala.eCommerce.entity.OrderAdjustmentType;
import com.skytala.eCommerce.entity.OrderAdjustmentTypeMapper;
import com.skytala.eCommerce.event.OrderAdjustmentTypeAdded;
import com.skytala.eCommerce.event.OrderAdjustmentTypeDeleted;
import com.skytala.eCommerce.event.OrderAdjustmentTypeFound;
import com.skytala.eCommerce.event.OrderAdjustmentTypeUpdated;
import com.skytala.eCommerce.query.FindOrderAdjustmentTypesBy;

@RestController
@RequestMapping("/api/orderAdjustmentType")
public class OrderAdjustmentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderAdjustmentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderAdjustmentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderAdjustmentType
	 * @return a List with the OrderAdjustmentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderAdjustmentType> findOrderAdjustmentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderAdjustmentTypesBy query = new FindOrderAdjustmentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderAdjustmentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentTypeFound.class,
				event -> sendOrderAdjustmentTypesFoundMessage(((OrderAdjustmentTypeFound) event).getOrderAdjustmentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderAdjustmentTypesFoundMessage(List<OrderAdjustmentType> orderAdjustmentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderAdjustmentTypes);
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
	public boolean createOrderAdjustmentType(HttpServletRequest request) {

		OrderAdjustmentType orderAdjustmentTypeToBeAdded = new OrderAdjustmentType();
		try {
			orderAdjustmentTypeToBeAdded = OrderAdjustmentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderAdjustmentType(orderAdjustmentTypeToBeAdded);

	}

	/**
	 * creates a new OrderAdjustmentType entry in the ofbiz database
	 * 
	 * @param orderAdjustmentTypeToBeAdded
	 *            the OrderAdjustmentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderAdjustmentType(OrderAdjustmentType orderAdjustmentTypeToBeAdded) {

		AddOrderAdjustmentType com = new AddOrderAdjustmentType(orderAdjustmentTypeToBeAdded);
		int usedTicketId;

		synchronized (OrderAdjustmentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentTypeAdded.class,
				event -> sendOrderAdjustmentTypeChangedMessage(((OrderAdjustmentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderAdjustmentType(HttpServletRequest request) {

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

		OrderAdjustmentType orderAdjustmentTypeToBeUpdated = new OrderAdjustmentType();

		try {
			orderAdjustmentTypeToBeUpdated = OrderAdjustmentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderAdjustmentType(orderAdjustmentTypeToBeUpdated);

	}

	/**
	 * Updates the OrderAdjustmentType with the specific Id
	 * 
	 * @param orderAdjustmentTypeToBeUpdated the OrderAdjustmentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderAdjustmentType(OrderAdjustmentType orderAdjustmentTypeToBeUpdated) {

		UpdateOrderAdjustmentType com = new UpdateOrderAdjustmentType(orderAdjustmentTypeToBeUpdated);

		int usedTicketId;

		synchronized (OrderAdjustmentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentTypeUpdated.class,
				event -> sendOrderAdjustmentTypeChangedMessage(((OrderAdjustmentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderAdjustmentType from the database
	 * 
	 * @param orderAdjustmentTypeId:
	 *            the id of the OrderAdjustmentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderAdjustmentTypeById(@RequestParam(value = "orderAdjustmentTypeId") String orderAdjustmentTypeId) {

		DeleteOrderAdjustmentType com = new DeleteOrderAdjustmentType(orderAdjustmentTypeId);

		int usedTicketId;

		synchronized (OrderAdjustmentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentTypeDeleted.class,
				event -> sendOrderAdjustmentTypeChangedMessage(((OrderAdjustmentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderAdjustmentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderAdjustmentType/\" plus one of the following: "
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
