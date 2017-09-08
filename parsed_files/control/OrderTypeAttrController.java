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
import com.skytala.eCommerce.command.AddOrderTypeAttr;
import com.skytala.eCommerce.command.DeleteOrderTypeAttr;
import com.skytala.eCommerce.command.UpdateOrderTypeAttr;
import com.skytala.eCommerce.entity.OrderTypeAttr;
import com.skytala.eCommerce.entity.OrderTypeAttrMapper;
import com.skytala.eCommerce.event.OrderTypeAttrAdded;
import com.skytala.eCommerce.event.OrderTypeAttrDeleted;
import com.skytala.eCommerce.event.OrderTypeAttrFound;
import com.skytala.eCommerce.event.OrderTypeAttrUpdated;
import com.skytala.eCommerce.query.FindOrderTypeAttrsBy;

@RestController
@RequestMapping("/api/orderTypeAttr")
public class OrderTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderTypeAttr
	 * @return a List with the OrderTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderTypeAttr> findOrderTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderTypeAttrsBy query = new FindOrderTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTypeAttrFound.class,
				event -> sendOrderTypeAttrsFoundMessage(((OrderTypeAttrFound) event).getOrderTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderTypeAttrsFoundMessage(List<OrderTypeAttr> orderTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderTypeAttrs);
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
	public boolean createOrderTypeAttr(HttpServletRequest request) {

		OrderTypeAttr orderTypeAttrToBeAdded = new OrderTypeAttr();
		try {
			orderTypeAttrToBeAdded = OrderTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderTypeAttr(orderTypeAttrToBeAdded);

	}

	/**
	 * creates a new OrderTypeAttr entry in the ofbiz database
	 * 
	 * @param orderTypeAttrToBeAdded
	 *            the OrderTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderTypeAttr(OrderTypeAttr orderTypeAttrToBeAdded) {

		AddOrderTypeAttr com = new AddOrderTypeAttr(orderTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (OrderTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTypeAttrAdded.class,
				event -> sendOrderTypeAttrChangedMessage(((OrderTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderTypeAttr(HttpServletRequest request) {

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

		OrderTypeAttr orderTypeAttrToBeUpdated = new OrderTypeAttr();

		try {
			orderTypeAttrToBeUpdated = OrderTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderTypeAttr(orderTypeAttrToBeUpdated);

	}

	/**
	 * Updates the OrderTypeAttr with the specific Id
	 * 
	 * @param orderTypeAttrToBeUpdated the OrderTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderTypeAttr(OrderTypeAttr orderTypeAttrToBeUpdated) {

		UpdateOrderTypeAttr com = new UpdateOrderTypeAttr(orderTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (OrderTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTypeAttrUpdated.class,
				event -> sendOrderTypeAttrChangedMessage(((OrderTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderTypeAttr from the database
	 * 
	 * @param orderTypeAttrId:
	 *            the id of the OrderTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderTypeAttrById(@RequestParam(value = "orderTypeAttrId") String orderTypeAttrId) {

		DeleteOrderTypeAttr com = new DeleteOrderTypeAttr(orderTypeAttrId);

		int usedTicketId;

		synchronized (OrderTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTypeAttrDeleted.class,
				event -> sendOrderTypeAttrChangedMessage(((OrderTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderTypeAttr/\" plus one of the following: "
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
