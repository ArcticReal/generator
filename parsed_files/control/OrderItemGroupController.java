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
import com.skytala.eCommerce.command.AddOrderItemGroup;
import com.skytala.eCommerce.command.DeleteOrderItemGroup;
import com.skytala.eCommerce.command.UpdateOrderItemGroup;
import com.skytala.eCommerce.entity.OrderItemGroup;
import com.skytala.eCommerce.entity.OrderItemGroupMapper;
import com.skytala.eCommerce.event.OrderItemGroupAdded;
import com.skytala.eCommerce.event.OrderItemGroupDeleted;
import com.skytala.eCommerce.event.OrderItemGroupFound;
import com.skytala.eCommerce.event.OrderItemGroupUpdated;
import com.skytala.eCommerce.query.FindOrderItemGroupsBy;

@RestController
@RequestMapping("/api/orderItemGroup")
public class OrderItemGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemGroup
	 * @return a List with the OrderItemGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemGroup> findOrderItemGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemGroupsBy query = new FindOrderItemGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemGroupFound.class,
				event -> sendOrderItemGroupsFoundMessage(((OrderItemGroupFound) event).getOrderItemGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemGroupsFoundMessage(List<OrderItemGroup> orderItemGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemGroups);
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
	public boolean createOrderItemGroup(HttpServletRequest request) {

		OrderItemGroup orderItemGroupToBeAdded = new OrderItemGroup();
		try {
			orderItemGroupToBeAdded = OrderItemGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemGroup(orderItemGroupToBeAdded);

	}

	/**
	 * creates a new OrderItemGroup entry in the ofbiz database
	 * 
	 * @param orderItemGroupToBeAdded
	 *            the OrderItemGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemGroup(OrderItemGroup orderItemGroupToBeAdded) {

		AddOrderItemGroup com = new AddOrderItemGroup(orderItemGroupToBeAdded);
		int usedTicketId;

		synchronized (OrderItemGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemGroupAdded.class,
				event -> sendOrderItemGroupChangedMessage(((OrderItemGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemGroup(HttpServletRequest request) {

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

		OrderItemGroup orderItemGroupToBeUpdated = new OrderItemGroup();

		try {
			orderItemGroupToBeUpdated = OrderItemGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemGroup(orderItemGroupToBeUpdated);

	}

	/**
	 * Updates the OrderItemGroup with the specific Id
	 * 
	 * @param orderItemGroupToBeUpdated the OrderItemGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemGroup(OrderItemGroup orderItemGroupToBeUpdated) {

		UpdateOrderItemGroup com = new UpdateOrderItemGroup(orderItemGroupToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemGroupUpdated.class,
				event -> sendOrderItemGroupChangedMessage(((OrderItemGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemGroup from the database
	 * 
	 * @param orderItemGroupId:
	 *            the id of the OrderItemGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemGroupById(@RequestParam(value = "orderItemGroupId") String orderItemGroupId) {

		DeleteOrderItemGroup com = new DeleteOrderItemGroup(orderItemGroupId);

		int usedTicketId;

		synchronized (OrderItemGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemGroupDeleted.class,
				event -> sendOrderItemGroupChangedMessage(((OrderItemGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemGroup/\" plus one of the following: "
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
