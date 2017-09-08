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
import com.skytala.eCommerce.command.AddOrderAdjustmentTypeAttr;
import com.skytala.eCommerce.command.DeleteOrderAdjustmentTypeAttr;
import com.skytala.eCommerce.command.UpdateOrderAdjustmentTypeAttr;
import com.skytala.eCommerce.entity.OrderAdjustmentTypeAttr;
import com.skytala.eCommerce.entity.OrderAdjustmentTypeAttrMapper;
import com.skytala.eCommerce.event.OrderAdjustmentTypeAttrAdded;
import com.skytala.eCommerce.event.OrderAdjustmentTypeAttrDeleted;
import com.skytala.eCommerce.event.OrderAdjustmentTypeAttrFound;
import com.skytala.eCommerce.event.OrderAdjustmentTypeAttrUpdated;
import com.skytala.eCommerce.query.FindOrderAdjustmentTypeAttrsBy;

@RestController
@RequestMapping("/api/orderAdjustmentTypeAttr")
public class OrderAdjustmentTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderAdjustmentTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderAdjustmentTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderAdjustmentTypeAttr
	 * @return a List with the OrderAdjustmentTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderAdjustmentTypeAttr> findOrderAdjustmentTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderAdjustmentTypeAttrsBy query = new FindOrderAdjustmentTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderAdjustmentTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentTypeAttrFound.class,
				event -> sendOrderAdjustmentTypeAttrsFoundMessage(((OrderAdjustmentTypeAttrFound) event).getOrderAdjustmentTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderAdjustmentTypeAttrsFoundMessage(List<OrderAdjustmentTypeAttr> orderAdjustmentTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderAdjustmentTypeAttrs);
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
	public boolean createOrderAdjustmentTypeAttr(HttpServletRequest request) {

		OrderAdjustmentTypeAttr orderAdjustmentTypeAttrToBeAdded = new OrderAdjustmentTypeAttr();
		try {
			orderAdjustmentTypeAttrToBeAdded = OrderAdjustmentTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderAdjustmentTypeAttr(orderAdjustmentTypeAttrToBeAdded);

	}

	/**
	 * creates a new OrderAdjustmentTypeAttr entry in the ofbiz database
	 * 
	 * @param orderAdjustmentTypeAttrToBeAdded
	 *            the OrderAdjustmentTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderAdjustmentTypeAttr(OrderAdjustmentTypeAttr orderAdjustmentTypeAttrToBeAdded) {

		AddOrderAdjustmentTypeAttr com = new AddOrderAdjustmentTypeAttr(orderAdjustmentTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (OrderAdjustmentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentTypeAttrAdded.class,
				event -> sendOrderAdjustmentTypeAttrChangedMessage(((OrderAdjustmentTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderAdjustmentTypeAttr(HttpServletRequest request) {

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

		OrderAdjustmentTypeAttr orderAdjustmentTypeAttrToBeUpdated = new OrderAdjustmentTypeAttr();

		try {
			orderAdjustmentTypeAttrToBeUpdated = OrderAdjustmentTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderAdjustmentTypeAttr(orderAdjustmentTypeAttrToBeUpdated);

	}

	/**
	 * Updates the OrderAdjustmentTypeAttr with the specific Id
	 * 
	 * @param orderAdjustmentTypeAttrToBeUpdated the OrderAdjustmentTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderAdjustmentTypeAttr(OrderAdjustmentTypeAttr orderAdjustmentTypeAttrToBeUpdated) {

		UpdateOrderAdjustmentTypeAttr com = new UpdateOrderAdjustmentTypeAttr(orderAdjustmentTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (OrderAdjustmentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentTypeAttrUpdated.class,
				event -> sendOrderAdjustmentTypeAttrChangedMessage(((OrderAdjustmentTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderAdjustmentTypeAttr from the database
	 * 
	 * @param orderAdjustmentTypeAttrId:
	 *            the id of the OrderAdjustmentTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderAdjustmentTypeAttrById(@RequestParam(value = "orderAdjustmentTypeAttrId") String orderAdjustmentTypeAttrId) {

		DeleteOrderAdjustmentTypeAttr com = new DeleteOrderAdjustmentTypeAttr(orderAdjustmentTypeAttrId);

		int usedTicketId;

		synchronized (OrderAdjustmentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentTypeAttrDeleted.class,
				event -> sendOrderAdjustmentTypeAttrChangedMessage(((OrderAdjustmentTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderAdjustmentTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderAdjustmentTypeAttr/\" plus one of the following: "
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
