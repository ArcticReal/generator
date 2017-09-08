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
import com.skytala.eCommerce.command.AddOrderContactMech;
import com.skytala.eCommerce.command.DeleteOrderContactMech;
import com.skytala.eCommerce.command.UpdateOrderContactMech;
import com.skytala.eCommerce.entity.OrderContactMech;
import com.skytala.eCommerce.entity.OrderContactMechMapper;
import com.skytala.eCommerce.event.OrderContactMechAdded;
import com.skytala.eCommerce.event.OrderContactMechDeleted;
import com.skytala.eCommerce.event.OrderContactMechFound;
import com.skytala.eCommerce.event.OrderContactMechUpdated;
import com.skytala.eCommerce.query.FindOrderContactMechsBy;

@RestController
@RequestMapping("/api/orderContactMech")
public class OrderContactMechController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderContactMech>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderContactMechController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderContactMech
	 * @return a List with the OrderContactMechs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderContactMech> findOrderContactMechsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderContactMechsBy query = new FindOrderContactMechsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderContactMechController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContactMechFound.class,
				event -> sendOrderContactMechsFoundMessage(((OrderContactMechFound) event).getOrderContactMechs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderContactMechsFoundMessage(List<OrderContactMech> orderContactMechs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderContactMechs);
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
	public boolean createOrderContactMech(HttpServletRequest request) {

		OrderContactMech orderContactMechToBeAdded = new OrderContactMech();
		try {
			orderContactMechToBeAdded = OrderContactMechMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderContactMech(orderContactMechToBeAdded);

	}

	/**
	 * creates a new OrderContactMech entry in the ofbiz database
	 * 
	 * @param orderContactMechToBeAdded
	 *            the OrderContactMech thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderContactMech(OrderContactMech orderContactMechToBeAdded) {

		AddOrderContactMech com = new AddOrderContactMech(orderContactMechToBeAdded);
		int usedTicketId;

		synchronized (OrderContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContactMechAdded.class,
				event -> sendOrderContactMechChangedMessage(((OrderContactMechAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderContactMech(HttpServletRequest request) {

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

		OrderContactMech orderContactMechToBeUpdated = new OrderContactMech();

		try {
			orderContactMechToBeUpdated = OrderContactMechMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderContactMech(orderContactMechToBeUpdated);

	}

	/**
	 * Updates the OrderContactMech with the specific Id
	 * 
	 * @param orderContactMechToBeUpdated the OrderContactMech thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderContactMech(OrderContactMech orderContactMechToBeUpdated) {

		UpdateOrderContactMech com = new UpdateOrderContactMech(orderContactMechToBeUpdated);

		int usedTicketId;

		synchronized (OrderContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContactMechUpdated.class,
				event -> sendOrderContactMechChangedMessage(((OrderContactMechUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderContactMech from the database
	 * 
	 * @param orderContactMechId:
	 *            the id of the OrderContactMech thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderContactMechById(@RequestParam(value = "orderContactMechId") String orderContactMechId) {

		DeleteOrderContactMech com = new DeleteOrderContactMech(orderContactMechId);

		int usedTicketId;

		synchronized (OrderContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContactMechDeleted.class,
				event -> sendOrderContactMechChangedMessage(((OrderContactMechDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderContactMechChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderContactMech/\" plus one of the following: "
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
