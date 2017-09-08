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
import com.skytala.eCommerce.command.AddOrderTermAttribute;
import com.skytala.eCommerce.command.DeleteOrderTermAttribute;
import com.skytala.eCommerce.command.UpdateOrderTermAttribute;
import com.skytala.eCommerce.entity.OrderTermAttribute;
import com.skytala.eCommerce.entity.OrderTermAttributeMapper;
import com.skytala.eCommerce.event.OrderTermAttributeAdded;
import com.skytala.eCommerce.event.OrderTermAttributeDeleted;
import com.skytala.eCommerce.event.OrderTermAttributeFound;
import com.skytala.eCommerce.event.OrderTermAttributeUpdated;
import com.skytala.eCommerce.query.FindOrderTermAttributesBy;

@RestController
@RequestMapping("/api/orderTermAttribute")
public class OrderTermAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderTermAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderTermAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderTermAttribute
	 * @return a List with the OrderTermAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderTermAttribute> findOrderTermAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderTermAttributesBy query = new FindOrderTermAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderTermAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTermAttributeFound.class,
				event -> sendOrderTermAttributesFoundMessage(((OrderTermAttributeFound) event).getOrderTermAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderTermAttributesFoundMessage(List<OrderTermAttribute> orderTermAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderTermAttributes);
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
	public boolean createOrderTermAttribute(HttpServletRequest request) {

		OrderTermAttribute orderTermAttributeToBeAdded = new OrderTermAttribute();
		try {
			orderTermAttributeToBeAdded = OrderTermAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderTermAttribute(orderTermAttributeToBeAdded);

	}

	/**
	 * creates a new OrderTermAttribute entry in the ofbiz database
	 * 
	 * @param orderTermAttributeToBeAdded
	 *            the OrderTermAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderTermAttribute(OrderTermAttribute orderTermAttributeToBeAdded) {

		AddOrderTermAttribute com = new AddOrderTermAttribute(orderTermAttributeToBeAdded);
		int usedTicketId;

		synchronized (OrderTermAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTermAttributeAdded.class,
				event -> sendOrderTermAttributeChangedMessage(((OrderTermAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderTermAttribute(HttpServletRequest request) {

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

		OrderTermAttribute orderTermAttributeToBeUpdated = new OrderTermAttribute();

		try {
			orderTermAttributeToBeUpdated = OrderTermAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderTermAttribute(orderTermAttributeToBeUpdated);

	}

	/**
	 * Updates the OrderTermAttribute with the specific Id
	 * 
	 * @param orderTermAttributeToBeUpdated the OrderTermAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderTermAttribute(OrderTermAttribute orderTermAttributeToBeUpdated) {

		UpdateOrderTermAttribute com = new UpdateOrderTermAttribute(orderTermAttributeToBeUpdated);

		int usedTicketId;

		synchronized (OrderTermAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTermAttributeUpdated.class,
				event -> sendOrderTermAttributeChangedMessage(((OrderTermAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderTermAttribute from the database
	 * 
	 * @param orderTermAttributeId:
	 *            the id of the OrderTermAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderTermAttributeById(@RequestParam(value = "orderTermAttributeId") String orderTermAttributeId) {

		DeleteOrderTermAttribute com = new DeleteOrderTermAttribute(orderTermAttributeId);

		int usedTicketId;

		synchronized (OrderTermAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTermAttributeDeleted.class,
				event -> sendOrderTermAttributeChangedMessage(((OrderTermAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderTermAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderTermAttribute/\" plus one of the following: "
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
