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
import com.skytala.eCommerce.command.AddOrderContent;
import com.skytala.eCommerce.command.DeleteOrderContent;
import com.skytala.eCommerce.command.UpdateOrderContent;
import com.skytala.eCommerce.entity.OrderContent;
import com.skytala.eCommerce.entity.OrderContentMapper;
import com.skytala.eCommerce.event.OrderContentAdded;
import com.skytala.eCommerce.event.OrderContentDeleted;
import com.skytala.eCommerce.event.OrderContentFound;
import com.skytala.eCommerce.event.OrderContentUpdated;
import com.skytala.eCommerce.query.FindOrderContentsBy;

@RestController
@RequestMapping("/api/orderContent")
public class OrderContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderContent
	 * @return a List with the OrderContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderContent> findOrderContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderContentsBy query = new FindOrderContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContentFound.class,
				event -> sendOrderContentsFoundMessage(((OrderContentFound) event).getOrderContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderContentsFoundMessage(List<OrderContent> orderContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderContents);
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
	public boolean createOrderContent(HttpServletRequest request) {

		OrderContent orderContentToBeAdded = new OrderContent();
		try {
			orderContentToBeAdded = OrderContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderContent(orderContentToBeAdded);

	}

	/**
	 * creates a new OrderContent entry in the ofbiz database
	 * 
	 * @param orderContentToBeAdded
	 *            the OrderContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderContent(OrderContent orderContentToBeAdded) {

		AddOrderContent com = new AddOrderContent(orderContentToBeAdded);
		int usedTicketId;

		synchronized (OrderContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContentAdded.class,
				event -> sendOrderContentChangedMessage(((OrderContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderContent(HttpServletRequest request) {

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

		OrderContent orderContentToBeUpdated = new OrderContent();

		try {
			orderContentToBeUpdated = OrderContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderContent(orderContentToBeUpdated);

	}

	/**
	 * Updates the OrderContent with the specific Id
	 * 
	 * @param orderContentToBeUpdated the OrderContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderContent(OrderContent orderContentToBeUpdated) {

		UpdateOrderContent com = new UpdateOrderContent(orderContentToBeUpdated);

		int usedTicketId;

		synchronized (OrderContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContentUpdated.class,
				event -> sendOrderContentChangedMessage(((OrderContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderContent from the database
	 * 
	 * @param orderContentId:
	 *            the id of the OrderContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderContentById(@RequestParam(value = "orderContentId") String orderContentId) {

		DeleteOrderContent com = new DeleteOrderContent(orderContentId);

		int usedTicketId;

		synchronized (OrderContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContentDeleted.class,
				event -> sendOrderContentChangedMessage(((OrderContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderContent/\" plus one of the following: "
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
