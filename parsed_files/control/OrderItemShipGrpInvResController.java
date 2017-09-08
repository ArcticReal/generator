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
import com.skytala.eCommerce.command.AddOrderItemShipGrpInvRes;
import com.skytala.eCommerce.command.DeleteOrderItemShipGrpInvRes;
import com.skytala.eCommerce.command.UpdateOrderItemShipGrpInvRes;
import com.skytala.eCommerce.entity.OrderItemShipGrpInvRes;
import com.skytala.eCommerce.entity.OrderItemShipGrpInvResMapper;
import com.skytala.eCommerce.event.OrderItemShipGrpInvResAdded;
import com.skytala.eCommerce.event.OrderItemShipGrpInvResDeleted;
import com.skytala.eCommerce.event.OrderItemShipGrpInvResFound;
import com.skytala.eCommerce.event.OrderItemShipGrpInvResUpdated;
import com.skytala.eCommerce.query.FindOrderItemShipGrpInvRessBy;

@RestController
@RequestMapping("/api/orderItemShipGrpInvRes")
public class OrderItemShipGrpInvResController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemShipGrpInvRes>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemShipGrpInvResController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemShipGrpInvRes
	 * @return a List with the OrderItemShipGrpInvRess
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemShipGrpInvRes> findOrderItemShipGrpInvRessBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemShipGrpInvRessBy query = new FindOrderItemShipGrpInvRessBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemShipGrpInvResController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGrpInvResFound.class,
				event -> sendOrderItemShipGrpInvRessFoundMessage(((OrderItemShipGrpInvResFound) event).getOrderItemShipGrpInvRess(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemShipGrpInvRessFoundMessage(List<OrderItemShipGrpInvRes> orderItemShipGrpInvRess, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemShipGrpInvRess);
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
	public boolean createOrderItemShipGrpInvRes(HttpServletRequest request) {

		OrderItemShipGrpInvRes orderItemShipGrpInvResToBeAdded = new OrderItemShipGrpInvRes();
		try {
			orderItemShipGrpInvResToBeAdded = OrderItemShipGrpInvResMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemShipGrpInvRes(orderItemShipGrpInvResToBeAdded);

	}

	/**
	 * creates a new OrderItemShipGrpInvRes entry in the ofbiz database
	 * 
	 * @param orderItemShipGrpInvResToBeAdded
	 *            the OrderItemShipGrpInvRes thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemShipGrpInvRes(OrderItemShipGrpInvRes orderItemShipGrpInvResToBeAdded) {

		AddOrderItemShipGrpInvRes com = new AddOrderItemShipGrpInvRes(orderItemShipGrpInvResToBeAdded);
		int usedTicketId;

		synchronized (OrderItemShipGrpInvResController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGrpInvResAdded.class,
				event -> sendOrderItemShipGrpInvResChangedMessage(((OrderItemShipGrpInvResAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemShipGrpInvRes(HttpServletRequest request) {

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

		OrderItemShipGrpInvRes orderItemShipGrpInvResToBeUpdated = new OrderItemShipGrpInvRes();

		try {
			orderItemShipGrpInvResToBeUpdated = OrderItemShipGrpInvResMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemShipGrpInvRes(orderItemShipGrpInvResToBeUpdated);

	}

	/**
	 * Updates the OrderItemShipGrpInvRes with the specific Id
	 * 
	 * @param orderItemShipGrpInvResToBeUpdated the OrderItemShipGrpInvRes thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemShipGrpInvRes(OrderItemShipGrpInvRes orderItemShipGrpInvResToBeUpdated) {

		UpdateOrderItemShipGrpInvRes com = new UpdateOrderItemShipGrpInvRes(orderItemShipGrpInvResToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemShipGrpInvResController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGrpInvResUpdated.class,
				event -> sendOrderItemShipGrpInvResChangedMessage(((OrderItemShipGrpInvResUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemShipGrpInvRes from the database
	 * 
	 * @param orderItemShipGrpInvResId:
	 *            the id of the OrderItemShipGrpInvRes thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemShipGrpInvResById(@RequestParam(value = "orderItemShipGrpInvResId") String orderItemShipGrpInvResId) {

		DeleteOrderItemShipGrpInvRes com = new DeleteOrderItemShipGrpInvRes(orderItemShipGrpInvResId);

		int usedTicketId;

		synchronized (OrderItemShipGrpInvResController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGrpInvResDeleted.class,
				event -> sendOrderItemShipGrpInvResChangedMessage(((OrderItemShipGrpInvResDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemShipGrpInvResChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemShipGrpInvRes/\" plus one of the following: "
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
