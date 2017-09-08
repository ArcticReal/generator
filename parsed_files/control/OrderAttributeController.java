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
import com.skytala.eCommerce.command.AddOrderAttribute;
import com.skytala.eCommerce.command.DeleteOrderAttribute;
import com.skytala.eCommerce.command.UpdateOrderAttribute;
import com.skytala.eCommerce.entity.OrderAttribute;
import com.skytala.eCommerce.entity.OrderAttributeMapper;
import com.skytala.eCommerce.event.OrderAttributeAdded;
import com.skytala.eCommerce.event.OrderAttributeDeleted;
import com.skytala.eCommerce.event.OrderAttributeFound;
import com.skytala.eCommerce.event.OrderAttributeUpdated;
import com.skytala.eCommerce.query.FindOrderAttributesBy;

@RestController
@RequestMapping("/api/orderAttribute")
public class OrderAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderAttribute
	 * @return a List with the OrderAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderAttribute> findOrderAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderAttributesBy query = new FindOrderAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAttributeFound.class,
				event -> sendOrderAttributesFoundMessage(((OrderAttributeFound) event).getOrderAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderAttributesFoundMessage(List<OrderAttribute> orderAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderAttributes);
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
	public boolean createOrderAttribute(HttpServletRequest request) {

		OrderAttribute orderAttributeToBeAdded = new OrderAttribute();
		try {
			orderAttributeToBeAdded = OrderAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderAttribute(orderAttributeToBeAdded);

	}

	/**
	 * creates a new OrderAttribute entry in the ofbiz database
	 * 
	 * @param orderAttributeToBeAdded
	 *            the OrderAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderAttribute(OrderAttribute orderAttributeToBeAdded) {

		AddOrderAttribute com = new AddOrderAttribute(orderAttributeToBeAdded);
		int usedTicketId;

		synchronized (OrderAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAttributeAdded.class,
				event -> sendOrderAttributeChangedMessage(((OrderAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderAttribute(HttpServletRequest request) {

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

		OrderAttribute orderAttributeToBeUpdated = new OrderAttribute();

		try {
			orderAttributeToBeUpdated = OrderAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderAttribute(orderAttributeToBeUpdated);

	}

	/**
	 * Updates the OrderAttribute with the specific Id
	 * 
	 * @param orderAttributeToBeUpdated the OrderAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderAttribute(OrderAttribute orderAttributeToBeUpdated) {

		UpdateOrderAttribute com = new UpdateOrderAttribute(orderAttributeToBeUpdated);

		int usedTicketId;

		synchronized (OrderAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAttributeUpdated.class,
				event -> sendOrderAttributeChangedMessage(((OrderAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderAttribute from the database
	 * 
	 * @param orderAttributeId:
	 *            the id of the OrderAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderAttributeById(@RequestParam(value = "orderAttributeId") String orderAttributeId) {

		DeleteOrderAttribute com = new DeleteOrderAttribute(orderAttributeId);

		int usedTicketId;

		synchronized (OrderAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderAttributeDeleted.class,
				event -> sendOrderAttributeChangedMessage(((OrderAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderAttribute/\" plus one of the following: "
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
