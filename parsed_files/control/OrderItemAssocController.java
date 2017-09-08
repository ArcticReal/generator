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
import com.skytala.eCommerce.command.AddOrderItemAssoc;
import com.skytala.eCommerce.command.DeleteOrderItemAssoc;
import com.skytala.eCommerce.command.UpdateOrderItemAssoc;
import com.skytala.eCommerce.entity.OrderItemAssoc;
import com.skytala.eCommerce.entity.OrderItemAssocMapper;
import com.skytala.eCommerce.event.OrderItemAssocAdded;
import com.skytala.eCommerce.event.OrderItemAssocDeleted;
import com.skytala.eCommerce.event.OrderItemAssocFound;
import com.skytala.eCommerce.event.OrderItemAssocUpdated;
import com.skytala.eCommerce.query.FindOrderItemAssocsBy;

@RestController
@RequestMapping("/api/orderItemAssoc")
public class OrderItemAssocController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemAssoc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemAssocController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemAssoc
	 * @return a List with the OrderItemAssocs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemAssoc> findOrderItemAssocsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemAssocsBy query = new FindOrderItemAssocsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemAssocController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAssocFound.class,
				event -> sendOrderItemAssocsFoundMessage(((OrderItemAssocFound) event).getOrderItemAssocs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemAssocsFoundMessage(List<OrderItemAssoc> orderItemAssocs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemAssocs);
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
	public boolean createOrderItemAssoc(HttpServletRequest request) {

		OrderItemAssoc orderItemAssocToBeAdded = new OrderItemAssoc();
		try {
			orderItemAssocToBeAdded = OrderItemAssocMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemAssoc(orderItemAssocToBeAdded);

	}

	/**
	 * creates a new OrderItemAssoc entry in the ofbiz database
	 * 
	 * @param orderItemAssocToBeAdded
	 *            the OrderItemAssoc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemAssoc(OrderItemAssoc orderItemAssocToBeAdded) {

		AddOrderItemAssoc com = new AddOrderItemAssoc(orderItemAssocToBeAdded);
		int usedTicketId;

		synchronized (OrderItemAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAssocAdded.class,
				event -> sendOrderItemAssocChangedMessage(((OrderItemAssocAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemAssoc(HttpServletRequest request) {

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

		OrderItemAssoc orderItemAssocToBeUpdated = new OrderItemAssoc();

		try {
			orderItemAssocToBeUpdated = OrderItemAssocMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemAssoc(orderItemAssocToBeUpdated);

	}

	/**
	 * Updates the OrderItemAssoc with the specific Id
	 * 
	 * @param orderItemAssocToBeUpdated the OrderItemAssoc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemAssoc(OrderItemAssoc orderItemAssocToBeUpdated) {

		UpdateOrderItemAssoc com = new UpdateOrderItemAssoc(orderItemAssocToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAssocUpdated.class,
				event -> sendOrderItemAssocChangedMessage(((OrderItemAssocUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemAssoc from the database
	 * 
	 * @param orderItemAssocId:
	 *            the id of the OrderItemAssoc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemAssocById(@RequestParam(value = "orderItemAssocId") String orderItemAssocId) {

		DeleteOrderItemAssoc com = new DeleteOrderItemAssoc(orderItemAssocId);

		int usedTicketId;

		synchronized (OrderItemAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemAssocDeleted.class,
				event -> sendOrderItemAssocChangedMessage(((OrderItemAssocDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemAssocChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemAssoc/\" plus one of the following: "
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
