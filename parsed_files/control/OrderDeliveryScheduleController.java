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
import com.skytala.eCommerce.command.AddOrderDeliverySchedule;
import com.skytala.eCommerce.command.DeleteOrderDeliverySchedule;
import com.skytala.eCommerce.command.UpdateOrderDeliverySchedule;
import com.skytala.eCommerce.entity.OrderDeliverySchedule;
import com.skytala.eCommerce.entity.OrderDeliveryScheduleMapper;
import com.skytala.eCommerce.event.OrderDeliveryScheduleAdded;
import com.skytala.eCommerce.event.OrderDeliveryScheduleDeleted;
import com.skytala.eCommerce.event.OrderDeliveryScheduleFound;
import com.skytala.eCommerce.event.OrderDeliveryScheduleUpdated;
import com.skytala.eCommerce.query.FindOrderDeliverySchedulesBy;

@RestController
@RequestMapping("/api/orderDeliverySchedule")
public class OrderDeliveryScheduleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderDeliverySchedule>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderDeliveryScheduleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderDeliverySchedule
	 * @return a List with the OrderDeliverySchedules
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderDeliverySchedule> findOrderDeliverySchedulesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderDeliverySchedulesBy query = new FindOrderDeliverySchedulesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderDeliveryScheduleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderDeliveryScheduleFound.class,
				event -> sendOrderDeliverySchedulesFoundMessage(((OrderDeliveryScheduleFound) event).getOrderDeliverySchedules(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderDeliverySchedulesFoundMessage(List<OrderDeliverySchedule> orderDeliverySchedules, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderDeliverySchedules);
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
	public boolean createOrderDeliverySchedule(HttpServletRequest request) {

		OrderDeliverySchedule orderDeliveryScheduleToBeAdded = new OrderDeliverySchedule();
		try {
			orderDeliveryScheduleToBeAdded = OrderDeliveryScheduleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderDeliverySchedule(orderDeliveryScheduleToBeAdded);

	}

	/**
	 * creates a new OrderDeliverySchedule entry in the ofbiz database
	 * 
	 * @param orderDeliveryScheduleToBeAdded
	 *            the OrderDeliverySchedule thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderDeliverySchedule(OrderDeliverySchedule orderDeliveryScheduleToBeAdded) {

		AddOrderDeliverySchedule com = new AddOrderDeliverySchedule(orderDeliveryScheduleToBeAdded);
		int usedTicketId;

		synchronized (OrderDeliveryScheduleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderDeliveryScheduleAdded.class,
				event -> sendOrderDeliveryScheduleChangedMessage(((OrderDeliveryScheduleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderDeliverySchedule(HttpServletRequest request) {

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

		OrderDeliverySchedule orderDeliveryScheduleToBeUpdated = new OrderDeliverySchedule();

		try {
			orderDeliveryScheduleToBeUpdated = OrderDeliveryScheduleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderDeliverySchedule(orderDeliveryScheduleToBeUpdated);

	}

	/**
	 * Updates the OrderDeliverySchedule with the specific Id
	 * 
	 * @param orderDeliveryScheduleToBeUpdated the OrderDeliverySchedule thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderDeliverySchedule(OrderDeliverySchedule orderDeliveryScheduleToBeUpdated) {

		UpdateOrderDeliverySchedule com = new UpdateOrderDeliverySchedule(orderDeliveryScheduleToBeUpdated);

		int usedTicketId;

		synchronized (OrderDeliveryScheduleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderDeliveryScheduleUpdated.class,
				event -> sendOrderDeliveryScheduleChangedMessage(((OrderDeliveryScheduleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderDeliverySchedule from the database
	 * 
	 * @param orderDeliveryScheduleId:
	 *            the id of the OrderDeliverySchedule thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderDeliveryScheduleById(@RequestParam(value = "orderDeliveryScheduleId") String orderDeliveryScheduleId) {

		DeleteOrderDeliverySchedule com = new DeleteOrderDeliverySchedule(orderDeliveryScheduleId);

		int usedTicketId;

		synchronized (OrderDeliveryScheduleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderDeliveryScheduleDeleted.class,
				event -> sendOrderDeliveryScheduleChangedMessage(((OrderDeliveryScheduleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderDeliveryScheduleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderDeliverySchedule/\" plus one of the following: "
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
