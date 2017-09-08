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
import com.skytala.eCommerce.command.AddOrderAdjustmentBilling;
import com.skytala.eCommerce.command.DeleteOrderAdjustmentBilling;
import com.skytala.eCommerce.command.UpdateOrderAdjustmentBilling;
import com.skytala.eCommerce.entity.OrderAdjustmentBilling;
import com.skytala.eCommerce.entity.OrderAdjustmentBillingMapper;
import com.skytala.eCommerce.event.OrderAdjustmentBillingAdded;
import com.skytala.eCommerce.event.OrderAdjustmentBillingDeleted;
import com.skytala.eCommerce.event.OrderAdjustmentBillingFound;
import com.skytala.eCommerce.event.OrderAdjustmentBillingUpdated;
import com.skytala.eCommerce.query.FindOrderAdjustmentBillingsBy;

@RestController
@RequestMapping("/api/orderAdjustmentBilling")
public class OrderAdjustmentBillingController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderAdjustmentBilling>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderAdjustmentBillingController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderAdjustmentBilling
	 * @return a List with the OrderAdjustmentBillings
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderAdjustmentBilling> findOrderAdjustmentBillingsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderAdjustmentBillingsBy query = new FindOrderAdjustmentBillingsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderAdjustmentBillingController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentBillingFound.class,
				event -> sendOrderAdjustmentBillingsFoundMessage(((OrderAdjustmentBillingFound) event).getOrderAdjustmentBillings(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderAdjustmentBillingsFoundMessage(List<OrderAdjustmentBilling> orderAdjustmentBillings, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderAdjustmentBillings);
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
	public boolean createOrderAdjustmentBilling(HttpServletRequest request) {

		OrderAdjustmentBilling orderAdjustmentBillingToBeAdded = new OrderAdjustmentBilling();
		try {
			orderAdjustmentBillingToBeAdded = OrderAdjustmentBillingMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderAdjustmentBilling(orderAdjustmentBillingToBeAdded);

	}

	/**
	 * creates a new OrderAdjustmentBilling entry in the ofbiz database
	 * 
	 * @param orderAdjustmentBillingToBeAdded
	 *            the OrderAdjustmentBilling thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderAdjustmentBilling(OrderAdjustmentBilling orderAdjustmentBillingToBeAdded) {

		AddOrderAdjustmentBilling com = new AddOrderAdjustmentBilling(orderAdjustmentBillingToBeAdded);
		int usedTicketId;

		synchronized (OrderAdjustmentBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentBillingAdded.class,
				event -> sendOrderAdjustmentBillingChangedMessage(((OrderAdjustmentBillingAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderAdjustmentBilling(HttpServletRequest request) {

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

		OrderAdjustmentBilling orderAdjustmentBillingToBeUpdated = new OrderAdjustmentBilling();

		try {
			orderAdjustmentBillingToBeUpdated = OrderAdjustmentBillingMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderAdjustmentBilling(orderAdjustmentBillingToBeUpdated);

	}

	/**
	 * Updates the OrderAdjustmentBilling with the specific Id
	 * 
	 * @param orderAdjustmentBillingToBeUpdated the OrderAdjustmentBilling thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderAdjustmentBilling(OrderAdjustmentBilling orderAdjustmentBillingToBeUpdated) {

		UpdateOrderAdjustmentBilling com = new UpdateOrderAdjustmentBilling(orderAdjustmentBillingToBeUpdated);

		int usedTicketId;

		synchronized (OrderAdjustmentBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentBillingUpdated.class,
				event -> sendOrderAdjustmentBillingChangedMessage(((OrderAdjustmentBillingUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderAdjustmentBilling from the database
	 * 
	 * @param orderAdjustmentBillingId:
	 *            the id of the OrderAdjustmentBilling thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderAdjustmentBillingById(@RequestParam(value = "orderAdjustmentBillingId") String orderAdjustmentBillingId) {

		DeleteOrderAdjustmentBilling com = new DeleteOrderAdjustmentBilling(orderAdjustmentBillingId);

		int usedTicketId;

		synchronized (OrderAdjustmentBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAdjustmentBillingDeleted.class,
				event -> sendOrderAdjustmentBillingChangedMessage(((OrderAdjustmentBillingDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderAdjustmentBillingChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderAdjustmentBilling/\" plus one of the following: "
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
