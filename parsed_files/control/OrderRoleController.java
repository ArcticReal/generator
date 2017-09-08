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
import com.skytala.eCommerce.command.AddOrderRole;
import com.skytala.eCommerce.command.DeleteOrderRole;
import com.skytala.eCommerce.command.UpdateOrderRole;
import com.skytala.eCommerce.entity.OrderRole;
import com.skytala.eCommerce.entity.OrderRoleMapper;
import com.skytala.eCommerce.event.OrderRoleAdded;
import com.skytala.eCommerce.event.OrderRoleDeleted;
import com.skytala.eCommerce.event.OrderRoleFound;
import com.skytala.eCommerce.event.OrderRoleUpdated;
import com.skytala.eCommerce.query.FindOrderRolesBy;

@RestController
@RequestMapping("/api/orderRole")
public class OrderRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderRole
	 * @return a List with the OrderRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderRole> findOrderRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderRolesBy query = new FindOrderRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderRoleFound.class,
				event -> sendOrderRolesFoundMessage(((OrderRoleFound) event).getOrderRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderRolesFoundMessage(List<OrderRole> orderRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderRoles);
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
	public boolean createOrderRole(HttpServletRequest request) {

		OrderRole orderRoleToBeAdded = new OrderRole();
		try {
			orderRoleToBeAdded = OrderRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderRole(orderRoleToBeAdded);

	}

	/**
	 * creates a new OrderRole entry in the ofbiz database
	 * 
	 * @param orderRoleToBeAdded
	 *            the OrderRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderRole(OrderRole orderRoleToBeAdded) {

		AddOrderRole com = new AddOrderRole(orderRoleToBeAdded);
		int usedTicketId;

		synchronized (OrderRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderRoleAdded.class,
				event -> sendOrderRoleChangedMessage(((OrderRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderRole(HttpServletRequest request) {

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

		OrderRole orderRoleToBeUpdated = new OrderRole();

		try {
			orderRoleToBeUpdated = OrderRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderRole(orderRoleToBeUpdated);

	}

	/**
	 * Updates the OrderRole with the specific Id
	 * 
	 * @param orderRoleToBeUpdated the OrderRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderRole(OrderRole orderRoleToBeUpdated) {

		UpdateOrderRole com = new UpdateOrderRole(orderRoleToBeUpdated);

		int usedTicketId;

		synchronized (OrderRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderRoleUpdated.class,
				event -> sendOrderRoleChangedMessage(((OrderRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderRole from the database
	 * 
	 * @param orderRoleId:
	 *            the id of the OrderRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderRoleById(@RequestParam(value = "orderRoleId") String orderRoleId) {

		DeleteOrderRole com = new DeleteOrderRole(orderRoleId);

		int usedTicketId;

		synchronized (OrderRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderRoleDeleted.class,
				event -> sendOrderRoleChangedMessage(((OrderRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderRole/\" plus one of the following: "
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
