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
import com.skytala.eCommerce.command.AddOrderPaymentPreference;
import com.skytala.eCommerce.command.DeleteOrderPaymentPreference;
import com.skytala.eCommerce.command.UpdateOrderPaymentPreference;
import com.skytala.eCommerce.entity.OrderPaymentPreference;
import com.skytala.eCommerce.entity.OrderPaymentPreferenceMapper;
import com.skytala.eCommerce.event.OrderPaymentPreferenceAdded;
import com.skytala.eCommerce.event.OrderPaymentPreferenceDeleted;
import com.skytala.eCommerce.event.OrderPaymentPreferenceFound;
import com.skytala.eCommerce.event.OrderPaymentPreferenceUpdated;
import com.skytala.eCommerce.query.FindOrderPaymentPreferencesBy;

@RestController
@RequestMapping("/api/orderPaymentPreference")
public class OrderPaymentPreferenceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderPaymentPreference>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderPaymentPreferenceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderPaymentPreference
	 * @return a List with the OrderPaymentPreferences
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderPaymentPreference> findOrderPaymentPreferencesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderPaymentPreferencesBy query = new FindOrderPaymentPreferencesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderPaymentPreferenceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderPaymentPreferenceFound.class,
				event -> sendOrderPaymentPreferencesFoundMessage(((OrderPaymentPreferenceFound) event).getOrderPaymentPreferences(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderPaymentPreferencesFoundMessage(List<OrderPaymentPreference> orderPaymentPreferences, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderPaymentPreferences);
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
	public boolean createOrderPaymentPreference(HttpServletRequest request) {

		OrderPaymentPreference orderPaymentPreferenceToBeAdded = new OrderPaymentPreference();
		try {
			orderPaymentPreferenceToBeAdded = OrderPaymentPreferenceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderPaymentPreference(orderPaymentPreferenceToBeAdded);

	}

	/**
	 * creates a new OrderPaymentPreference entry in the ofbiz database
	 * 
	 * @param orderPaymentPreferenceToBeAdded
	 *            the OrderPaymentPreference thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderPaymentPreference(OrderPaymentPreference orderPaymentPreferenceToBeAdded) {

		AddOrderPaymentPreference com = new AddOrderPaymentPreference(orderPaymentPreferenceToBeAdded);
		int usedTicketId;

		synchronized (OrderPaymentPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderPaymentPreferenceAdded.class,
				event -> sendOrderPaymentPreferenceChangedMessage(((OrderPaymentPreferenceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderPaymentPreference(HttpServletRequest request) {

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

		OrderPaymentPreference orderPaymentPreferenceToBeUpdated = new OrderPaymentPreference();

		try {
			orderPaymentPreferenceToBeUpdated = OrderPaymentPreferenceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderPaymentPreference(orderPaymentPreferenceToBeUpdated);

	}

	/**
	 * Updates the OrderPaymentPreference with the specific Id
	 * 
	 * @param orderPaymentPreferenceToBeUpdated the OrderPaymentPreference thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderPaymentPreference(OrderPaymentPreference orderPaymentPreferenceToBeUpdated) {

		UpdateOrderPaymentPreference com = new UpdateOrderPaymentPreference(orderPaymentPreferenceToBeUpdated);

		int usedTicketId;

		synchronized (OrderPaymentPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderPaymentPreferenceUpdated.class,
				event -> sendOrderPaymentPreferenceChangedMessage(((OrderPaymentPreferenceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderPaymentPreference from the database
	 * 
	 * @param orderPaymentPreferenceId:
	 *            the id of the OrderPaymentPreference thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderPaymentPreferenceById(@RequestParam(value = "orderPaymentPreferenceId") String orderPaymentPreferenceId) {

		DeleteOrderPaymentPreference com = new DeleteOrderPaymentPreference(orderPaymentPreferenceId);

		int usedTicketId;

		synchronized (OrderPaymentPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderPaymentPreferenceDeleted.class,
				event -> sendOrderPaymentPreferenceChangedMessage(((OrderPaymentPreferenceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderPaymentPreferenceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderPaymentPreference/\" plus one of the following: "
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
