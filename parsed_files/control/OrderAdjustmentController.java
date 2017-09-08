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
import com.skytala.eCommerce.command.AddOrderAdjustment;
import com.skytala.eCommerce.command.DeleteOrderAdjustment;
import com.skytala.eCommerce.command.UpdateOrderAdjustment;
import com.skytala.eCommerce.entity.OrderAdjustment;
import com.skytala.eCommerce.entity.OrderAdjustmentMapper;
import com.skytala.eCommerce.event.OrderAdjustmentAdded;
import com.skytala.eCommerce.event.OrderAdjustmentDeleted;
import com.skytala.eCommerce.event.OrderAdjustmentFound;
import com.skytala.eCommerce.event.OrderAdjustmentUpdated;
import com.skytala.eCommerce.query.FindOrderAdjustmentsBy;

@RestController
@RequestMapping("/api/orderAdjustment")
public class OrderAdjustmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderAdjustment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderAdjustmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderAdjustment
	 * @return a List with the OrderAdjustments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderAdjustment> findOrderAdjustmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderAdjustmentsBy query = new FindOrderAdjustmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderAdjustmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentFound.class,
				event -> sendOrderAdjustmentsFoundMessage(((OrderAdjustmentFound) event).getOrderAdjustments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderAdjustmentsFoundMessage(List<OrderAdjustment> orderAdjustments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderAdjustments);
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
	public boolean createOrderAdjustment(HttpServletRequest request) {

		OrderAdjustment orderAdjustmentToBeAdded = new OrderAdjustment();
		try {
			orderAdjustmentToBeAdded = OrderAdjustmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderAdjustment(orderAdjustmentToBeAdded);

	}

	/**
	 * creates a new OrderAdjustment entry in the ofbiz database
	 * 
	 * @param orderAdjustmentToBeAdded
	 *            the OrderAdjustment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderAdjustment(OrderAdjustment orderAdjustmentToBeAdded) {

		AddOrderAdjustment com = new AddOrderAdjustment(orderAdjustmentToBeAdded);
		int usedTicketId;

		synchronized (OrderAdjustmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentAdded.class,
				event -> sendOrderAdjustmentChangedMessage(((OrderAdjustmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderAdjustment(HttpServletRequest request) {

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

		OrderAdjustment orderAdjustmentToBeUpdated = new OrderAdjustment();

		try {
			orderAdjustmentToBeUpdated = OrderAdjustmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderAdjustment(orderAdjustmentToBeUpdated);

	}

	/**
	 * Updates the OrderAdjustment with the specific Id
	 * 
	 * @param orderAdjustmentToBeUpdated the OrderAdjustment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderAdjustment(OrderAdjustment orderAdjustmentToBeUpdated) {

		UpdateOrderAdjustment com = new UpdateOrderAdjustment(orderAdjustmentToBeUpdated);

		int usedTicketId;

		synchronized (OrderAdjustmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentUpdated.class,
				event -> sendOrderAdjustmentChangedMessage(((OrderAdjustmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderAdjustment from the database
	 * 
	 * @param orderAdjustmentId:
	 *            the id of the OrderAdjustment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderAdjustmentById(@RequestParam(value = "orderAdjustmentId") String orderAdjustmentId) {

		DeleteOrderAdjustment com = new DeleteOrderAdjustment(orderAdjustmentId);

		int usedTicketId;

		synchronized (OrderAdjustmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentDeleted.class,
				event -> sendOrderAdjustmentChangedMessage(((OrderAdjustmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderAdjustmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderAdjustment/\" plus one of the following: "
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
