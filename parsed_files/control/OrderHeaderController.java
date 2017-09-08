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
import com.skytala.eCommerce.command.AddOrderHeader;
import com.skytala.eCommerce.command.DeleteOrderHeader;
import com.skytala.eCommerce.command.UpdateOrderHeader;
import com.skytala.eCommerce.entity.OrderHeader;
import com.skytala.eCommerce.entity.OrderHeaderMapper;
import com.skytala.eCommerce.event.OrderHeaderAdded;
import com.skytala.eCommerce.event.OrderHeaderDeleted;
import com.skytala.eCommerce.event.OrderHeaderFound;
import com.skytala.eCommerce.event.OrderHeaderUpdated;
import com.skytala.eCommerce.query.FindOrderHeadersBy;

@RestController
@RequestMapping("/api/orderHeader")
public class OrderHeaderController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderHeader>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderHeaderController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderHeader
	 * @return a List with the OrderHeaders
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderHeader> findOrderHeadersBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderHeadersBy query = new FindOrderHeadersBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderHeaderController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderFound.class,
				event -> sendOrderHeadersFoundMessage(((OrderHeaderFound) event).getOrderHeaders(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderHeadersFoundMessage(List<OrderHeader> orderHeaders, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderHeaders);
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
	public boolean createOrderHeader(HttpServletRequest request) {

		OrderHeader orderHeaderToBeAdded = new OrderHeader();
		try {
			orderHeaderToBeAdded = OrderHeaderMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderHeader(orderHeaderToBeAdded);

	}

	/**
	 * creates a new OrderHeader entry in the ofbiz database
	 * 
	 * @param orderHeaderToBeAdded
	 *            the OrderHeader thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderHeader(OrderHeader orderHeaderToBeAdded) {

		AddOrderHeader com = new AddOrderHeader(orderHeaderToBeAdded);
		int usedTicketId;

		synchronized (OrderHeaderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderAdded.class,
				event -> sendOrderHeaderChangedMessage(((OrderHeaderAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderHeader(HttpServletRequest request) {

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

		OrderHeader orderHeaderToBeUpdated = new OrderHeader();

		try {
			orderHeaderToBeUpdated = OrderHeaderMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderHeader(orderHeaderToBeUpdated);

	}

	/**
	 * Updates the OrderHeader with the specific Id
	 * 
	 * @param orderHeaderToBeUpdated the OrderHeader thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderHeader(OrderHeader orderHeaderToBeUpdated) {

		UpdateOrderHeader com = new UpdateOrderHeader(orderHeaderToBeUpdated);

		int usedTicketId;

		synchronized (OrderHeaderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderUpdated.class,
				event -> sendOrderHeaderChangedMessage(((OrderHeaderUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderHeader from the database
	 * 
	 * @param orderHeaderId:
	 *            the id of the OrderHeader thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderHeaderById(@RequestParam(value = "orderHeaderId") String orderHeaderId) {

		DeleteOrderHeader com = new DeleteOrderHeader(orderHeaderId);

		int usedTicketId;

		synchronized (OrderHeaderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderDeleted.class,
				event -> sendOrderHeaderChangedMessage(((OrderHeaderDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderHeaderChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderHeader/\" plus one of the following: "
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
