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
import com.skytala.eCommerce.command.AddOrderItemShipGroup;
import com.skytala.eCommerce.command.DeleteOrderItemShipGroup;
import com.skytala.eCommerce.command.UpdateOrderItemShipGroup;
import com.skytala.eCommerce.entity.OrderItemShipGroup;
import com.skytala.eCommerce.entity.OrderItemShipGroupMapper;
import com.skytala.eCommerce.event.OrderItemShipGroupAdded;
import com.skytala.eCommerce.event.OrderItemShipGroupDeleted;
import com.skytala.eCommerce.event.OrderItemShipGroupFound;
import com.skytala.eCommerce.event.OrderItemShipGroupUpdated;
import com.skytala.eCommerce.query.FindOrderItemShipGroupsBy;

@RestController
@RequestMapping("/api/orderItemShipGroup")
public class OrderItemShipGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemShipGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemShipGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemShipGroup
	 * @return a List with the OrderItemShipGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemShipGroup> findOrderItemShipGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemShipGroupsBy query = new FindOrderItemShipGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemShipGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGroupFound.class,
				event -> sendOrderItemShipGroupsFoundMessage(((OrderItemShipGroupFound) event).getOrderItemShipGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemShipGroupsFoundMessage(List<OrderItemShipGroup> orderItemShipGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemShipGroups);
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
	public boolean createOrderItemShipGroup(HttpServletRequest request) {

		OrderItemShipGroup orderItemShipGroupToBeAdded = new OrderItemShipGroup();
		try {
			orderItemShipGroupToBeAdded = OrderItemShipGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemShipGroup(orderItemShipGroupToBeAdded);

	}

	/**
	 * creates a new OrderItemShipGroup entry in the ofbiz database
	 * 
	 * @param orderItemShipGroupToBeAdded
	 *            the OrderItemShipGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemShipGroup(OrderItemShipGroup orderItemShipGroupToBeAdded) {

		AddOrderItemShipGroup com = new AddOrderItemShipGroup(orderItemShipGroupToBeAdded);
		int usedTicketId;

		synchronized (OrderItemShipGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGroupAdded.class,
				event -> sendOrderItemShipGroupChangedMessage(((OrderItemShipGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemShipGroup(HttpServletRequest request) {

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

		OrderItemShipGroup orderItemShipGroupToBeUpdated = new OrderItemShipGroup();

		try {
			orderItemShipGroupToBeUpdated = OrderItemShipGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemShipGroup(orderItemShipGroupToBeUpdated);

	}

	/**
	 * Updates the OrderItemShipGroup with the specific Id
	 * 
	 * @param orderItemShipGroupToBeUpdated the OrderItemShipGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemShipGroup(OrderItemShipGroup orderItemShipGroupToBeUpdated) {

		UpdateOrderItemShipGroup com = new UpdateOrderItemShipGroup(orderItemShipGroupToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemShipGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGroupUpdated.class,
				event -> sendOrderItemShipGroupChangedMessage(((OrderItemShipGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemShipGroup from the database
	 * 
	 * @param orderItemShipGroupId:
	 *            the id of the OrderItemShipGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemShipGroupById(@RequestParam(value = "orderItemShipGroupId") String orderItemShipGroupId) {

		DeleteOrderItemShipGroup com = new DeleteOrderItemShipGroup(orderItemShipGroupId);

		int usedTicketId;

		synchronized (OrderItemShipGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGroupDeleted.class,
				event -> sendOrderItemShipGroupChangedMessage(((OrderItemShipGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemShipGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemShipGroup/\" plus one of the following: "
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
