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
import com.skytala.eCommerce.command.AddOrderItemShipGroupAssoc;
import com.skytala.eCommerce.command.DeleteOrderItemShipGroupAssoc;
import com.skytala.eCommerce.command.UpdateOrderItemShipGroupAssoc;
import com.skytala.eCommerce.entity.OrderItemShipGroupAssoc;
import com.skytala.eCommerce.entity.OrderItemShipGroupAssocMapper;
import com.skytala.eCommerce.event.OrderItemShipGroupAssocAdded;
import com.skytala.eCommerce.event.OrderItemShipGroupAssocDeleted;
import com.skytala.eCommerce.event.OrderItemShipGroupAssocFound;
import com.skytala.eCommerce.event.OrderItemShipGroupAssocUpdated;
import com.skytala.eCommerce.query.FindOrderItemShipGroupAssocsBy;

@RestController
@RequestMapping("/api/orderItemShipGroupAssoc")
public class OrderItemShipGroupAssocController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemShipGroupAssoc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemShipGroupAssocController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemShipGroupAssoc
	 * @return a List with the OrderItemShipGroupAssocs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemShipGroupAssoc> findOrderItemShipGroupAssocsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemShipGroupAssocsBy query = new FindOrderItemShipGroupAssocsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemShipGroupAssocController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGroupAssocFound.class,
				event -> sendOrderItemShipGroupAssocsFoundMessage(((OrderItemShipGroupAssocFound) event).getOrderItemShipGroupAssocs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemShipGroupAssocsFoundMessage(List<OrderItemShipGroupAssoc> orderItemShipGroupAssocs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemShipGroupAssocs);
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
	public boolean createOrderItemShipGroupAssoc(HttpServletRequest request) {

		OrderItemShipGroupAssoc orderItemShipGroupAssocToBeAdded = new OrderItemShipGroupAssoc();
		try {
			orderItemShipGroupAssocToBeAdded = OrderItemShipGroupAssocMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemShipGroupAssoc(orderItemShipGroupAssocToBeAdded);

	}

	/**
	 * creates a new OrderItemShipGroupAssoc entry in the ofbiz database
	 * 
	 * @param orderItemShipGroupAssocToBeAdded
	 *            the OrderItemShipGroupAssoc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemShipGroupAssoc(OrderItemShipGroupAssoc orderItemShipGroupAssocToBeAdded) {

		AddOrderItemShipGroupAssoc com = new AddOrderItemShipGroupAssoc(orderItemShipGroupAssocToBeAdded);
		int usedTicketId;

		synchronized (OrderItemShipGroupAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGroupAssocAdded.class,
				event -> sendOrderItemShipGroupAssocChangedMessage(((OrderItemShipGroupAssocAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemShipGroupAssoc(HttpServletRequest request) {

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

		OrderItemShipGroupAssoc orderItemShipGroupAssocToBeUpdated = new OrderItemShipGroupAssoc();

		try {
			orderItemShipGroupAssocToBeUpdated = OrderItemShipGroupAssocMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemShipGroupAssoc(orderItemShipGroupAssocToBeUpdated);

	}

	/**
	 * Updates the OrderItemShipGroupAssoc with the specific Id
	 * 
	 * @param orderItemShipGroupAssocToBeUpdated the OrderItemShipGroupAssoc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemShipGroupAssoc(OrderItemShipGroupAssoc orderItemShipGroupAssocToBeUpdated) {

		UpdateOrderItemShipGroupAssoc com = new UpdateOrderItemShipGroupAssoc(orderItemShipGroupAssocToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemShipGroupAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGroupAssocUpdated.class,
				event -> sendOrderItemShipGroupAssocChangedMessage(((OrderItemShipGroupAssocUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemShipGroupAssoc from the database
	 * 
	 * @param orderItemShipGroupAssocId:
	 *            the id of the OrderItemShipGroupAssoc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemShipGroupAssocById(@RequestParam(value = "orderItemShipGroupAssocId") String orderItemShipGroupAssocId) {

		DeleteOrderItemShipGroupAssoc com = new DeleteOrderItemShipGroupAssoc(orderItemShipGroupAssocId);

		int usedTicketId;

		synchronized (OrderItemShipGroupAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemShipGroupAssocDeleted.class,
				event -> sendOrderItemShipGroupAssocChangedMessage(((OrderItemShipGroupAssocDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemShipGroupAssocChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemShipGroupAssoc/\" plus one of the following: "
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
