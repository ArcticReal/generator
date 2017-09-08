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
import com.skytala.eCommerce.command.AddOrderContentType;
import com.skytala.eCommerce.command.DeleteOrderContentType;
import com.skytala.eCommerce.command.UpdateOrderContentType;
import com.skytala.eCommerce.entity.OrderContentType;
import com.skytala.eCommerce.entity.OrderContentTypeMapper;
import com.skytala.eCommerce.event.OrderContentTypeAdded;
import com.skytala.eCommerce.event.OrderContentTypeDeleted;
import com.skytala.eCommerce.event.OrderContentTypeFound;
import com.skytala.eCommerce.event.OrderContentTypeUpdated;
import com.skytala.eCommerce.query.FindOrderContentTypesBy;

@RestController
@RequestMapping("/api/orderContentType")
public class OrderContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderContentType
	 * @return a List with the OrderContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderContentType> findOrderContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderContentTypesBy query = new FindOrderContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContentTypeFound.class,
				event -> sendOrderContentTypesFoundMessage(((OrderContentTypeFound) event).getOrderContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderContentTypesFoundMessage(List<OrderContentType> orderContentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderContentTypes);
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
	public boolean createOrderContentType(HttpServletRequest request) {

		OrderContentType orderContentTypeToBeAdded = new OrderContentType();
		try {
			orderContentTypeToBeAdded = OrderContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderContentType(orderContentTypeToBeAdded);

	}

	/**
	 * creates a new OrderContentType entry in the ofbiz database
	 * 
	 * @param orderContentTypeToBeAdded
	 *            the OrderContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderContentType(OrderContentType orderContentTypeToBeAdded) {

		AddOrderContentType com = new AddOrderContentType(orderContentTypeToBeAdded);
		int usedTicketId;

		synchronized (OrderContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContentTypeAdded.class,
				event -> sendOrderContentTypeChangedMessage(((OrderContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderContentType(HttpServletRequest request) {

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

		OrderContentType orderContentTypeToBeUpdated = new OrderContentType();

		try {
			orderContentTypeToBeUpdated = OrderContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderContentType(orderContentTypeToBeUpdated);

	}

	/**
	 * Updates the OrderContentType with the specific Id
	 * 
	 * @param orderContentTypeToBeUpdated the OrderContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderContentType(OrderContentType orderContentTypeToBeUpdated) {

		UpdateOrderContentType com = new UpdateOrderContentType(orderContentTypeToBeUpdated);

		int usedTicketId;

		synchronized (OrderContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContentTypeUpdated.class,
				event -> sendOrderContentTypeChangedMessage(((OrderContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderContentType from the database
	 * 
	 * @param orderContentTypeId:
	 *            the id of the OrderContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderContentTypeById(@RequestParam(value = "orderContentTypeId") String orderContentTypeId) {

		DeleteOrderContentType com = new DeleteOrderContentType(orderContentTypeId);

		int usedTicketId;

		synchronized (OrderContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderContentTypeDeleted.class,
				event -> sendOrderContentTypeChangedMessage(((OrderContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderContentType/\" plus one of the following: "
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
