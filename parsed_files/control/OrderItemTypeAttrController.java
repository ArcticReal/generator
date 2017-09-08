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
import com.skytala.eCommerce.command.AddOrderItemTypeAttr;
import com.skytala.eCommerce.command.DeleteOrderItemTypeAttr;
import com.skytala.eCommerce.command.UpdateOrderItemTypeAttr;
import com.skytala.eCommerce.entity.OrderItemTypeAttr;
import com.skytala.eCommerce.entity.OrderItemTypeAttrMapper;
import com.skytala.eCommerce.event.OrderItemTypeAttrAdded;
import com.skytala.eCommerce.event.OrderItemTypeAttrDeleted;
import com.skytala.eCommerce.event.OrderItemTypeAttrFound;
import com.skytala.eCommerce.event.OrderItemTypeAttrUpdated;
import com.skytala.eCommerce.query.FindOrderItemTypeAttrsBy;

@RestController
@RequestMapping("/api/orderItemTypeAttr")
public class OrderItemTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemTypeAttr
	 * @return a List with the OrderItemTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemTypeAttr> findOrderItemTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemTypeAttrsBy query = new FindOrderItemTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemTypeAttrFound.class,
				event -> sendOrderItemTypeAttrsFoundMessage(((OrderItemTypeAttrFound) event).getOrderItemTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemTypeAttrsFoundMessage(List<OrderItemTypeAttr> orderItemTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemTypeAttrs);
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
	public boolean createOrderItemTypeAttr(HttpServletRequest request) {

		OrderItemTypeAttr orderItemTypeAttrToBeAdded = new OrderItemTypeAttr();
		try {
			orderItemTypeAttrToBeAdded = OrderItemTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemTypeAttr(orderItemTypeAttrToBeAdded);

	}

	/**
	 * creates a new OrderItemTypeAttr entry in the ofbiz database
	 * 
	 * @param orderItemTypeAttrToBeAdded
	 *            the OrderItemTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemTypeAttr(OrderItemTypeAttr orderItemTypeAttrToBeAdded) {

		AddOrderItemTypeAttr com = new AddOrderItemTypeAttr(orderItemTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (OrderItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemTypeAttrAdded.class,
				event -> sendOrderItemTypeAttrChangedMessage(((OrderItemTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemTypeAttr(HttpServletRequest request) {

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

		OrderItemTypeAttr orderItemTypeAttrToBeUpdated = new OrderItemTypeAttr();

		try {
			orderItemTypeAttrToBeUpdated = OrderItemTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemTypeAttr(orderItemTypeAttrToBeUpdated);

	}

	/**
	 * Updates the OrderItemTypeAttr with the specific Id
	 * 
	 * @param orderItemTypeAttrToBeUpdated the OrderItemTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemTypeAttr(OrderItemTypeAttr orderItemTypeAttrToBeUpdated) {

		UpdateOrderItemTypeAttr com = new UpdateOrderItemTypeAttr(orderItemTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemTypeAttrUpdated.class,
				event -> sendOrderItemTypeAttrChangedMessage(((OrderItemTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemTypeAttr from the database
	 * 
	 * @param orderItemTypeAttrId:
	 *            the id of the OrderItemTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemTypeAttrById(@RequestParam(value = "orderItemTypeAttrId") String orderItemTypeAttrId) {

		DeleteOrderItemTypeAttr com = new DeleteOrderItemTypeAttr(orderItemTypeAttrId);

		int usedTicketId;

		synchronized (OrderItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemTypeAttrDeleted.class,
				event -> sendOrderItemTypeAttrChangedMessage(((OrderItemTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemTypeAttr/\" plus one of the following: "
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
