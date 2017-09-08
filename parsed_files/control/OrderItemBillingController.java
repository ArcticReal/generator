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
import com.skytala.eCommerce.command.AddOrderItemBilling;
import com.skytala.eCommerce.command.DeleteOrderItemBilling;
import com.skytala.eCommerce.command.UpdateOrderItemBilling;
import com.skytala.eCommerce.entity.OrderItemBilling;
import com.skytala.eCommerce.entity.OrderItemBillingMapper;
import com.skytala.eCommerce.event.OrderItemBillingAdded;
import com.skytala.eCommerce.event.OrderItemBillingDeleted;
import com.skytala.eCommerce.event.OrderItemBillingFound;
import com.skytala.eCommerce.event.OrderItemBillingUpdated;
import com.skytala.eCommerce.query.FindOrderItemBillingsBy;

@RestController
@RequestMapping("/api/orderItemBilling")
public class OrderItemBillingController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderItemBilling>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderItemBillingController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderItemBilling
	 * @return a List with the OrderItemBillings
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderItemBilling> findOrderItemBillingsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderItemBillingsBy query = new FindOrderItemBillingsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderItemBillingController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemBillingFound.class,
				event -> sendOrderItemBillingsFoundMessage(((OrderItemBillingFound) event).getOrderItemBillings(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderItemBillingsFoundMessage(List<OrderItemBilling> orderItemBillings, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderItemBillings);
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
	public boolean createOrderItemBilling(HttpServletRequest request) {

		OrderItemBilling orderItemBillingToBeAdded = new OrderItemBilling();
		try {
			orderItemBillingToBeAdded = OrderItemBillingMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderItemBilling(orderItemBillingToBeAdded);

	}

	/**
	 * creates a new OrderItemBilling entry in the ofbiz database
	 * 
	 * @param orderItemBillingToBeAdded
	 *            the OrderItemBilling thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderItemBilling(OrderItemBilling orderItemBillingToBeAdded) {

		AddOrderItemBilling com = new AddOrderItemBilling(orderItemBillingToBeAdded);
		int usedTicketId;

		synchronized (OrderItemBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemBillingAdded.class,
				event -> sendOrderItemBillingChangedMessage(((OrderItemBillingAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderItemBilling(HttpServletRequest request) {

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

		OrderItemBilling orderItemBillingToBeUpdated = new OrderItemBilling();

		try {
			orderItemBillingToBeUpdated = OrderItemBillingMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderItemBilling(orderItemBillingToBeUpdated);

	}

	/**
	 * Updates the OrderItemBilling with the specific Id
	 * 
	 * @param orderItemBillingToBeUpdated the OrderItemBilling thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderItemBilling(OrderItemBilling orderItemBillingToBeUpdated) {

		UpdateOrderItemBilling com = new UpdateOrderItemBilling(orderItemBillingToBeUpdated);

		int usedTicketId;

		synchronized (OrderItemBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemBillingUpdated.class,
				event -> sendOrderItemBillingChangedMessage(((OrderItemBillingUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderItemBilling from the database
	 * 
	 * @param orderItemBillingId:
	 *            the id of the OrderItemBilling thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderItemBillingById(@RequestParam(value = "orderItemBillingId") String orderItemBillingId) {

		DeleteOrderItemBilling com = new DeleteOrderItemBilling(orderItemBillingId);

		int usedTicketId;

		synchronized (OrderItemBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderItemBillingDeleted.class,
				event -> sendOrderItemBillingChangedMessage(((OrderItemBillingDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderItemBillingChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderItemBilling/\" plus one of the following: "
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
