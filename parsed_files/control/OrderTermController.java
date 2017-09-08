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
import com.skytala.eCommerce.command.AddOrderTerm;
import com.skytala.eCommerce.command.DeleteOrderTerm;
import com.skytala.eCommerce.command.UpdateOrderTerm;
import com.skytala.eCommerce.entity.OrderTerm;
import com.skytala.eCommerce.entity.OrderTermMapper;
import com.skytala.eCommerce.event.OrderTermAdded;
import com.skytala.eCommerce.event.OrderTermDeleted;
import com.skytala.eCommerce.event.OrderTermFound;
import com.skytala.eCommerce.event.OrderTermUpdated;
import com.skytala.eCommerce.query.FindOrderTermsBy;

@RestController
@RequestMapping("/api/orderTerm")
public class OrderTermController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderTerm>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderTermController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderTerm
	 * @return a List with the OrderTerms
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderTerm> findOrderTermsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderTermsBy query = new FindOrderTermsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderTermController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTermFound.class,
				event -> sendOrderTermsFoundMessage(((OrderTermFound) event).getOrderTerms(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderTermsFoundMessage(List<OrderTerm> orderTerms, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderTerms);
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
	public boolean createOrderTerm(HttpServletRequest request) {

		OrderTerm orderTermToBeAdded = new OrderTerm();
		try {
			orderTermToBeAdded = OrderTermMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderTerm(orderTermToBeAdded);

	}

	/**
	 * creates a new OrderTerm entry in the ofbiz database
	 * 
	 * @param orderTermToBeAdded
	 *            the OrderTerm thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderTerm(OrderTerm orderTermToBeAdded) {

		AddOrderTerm com = new AddOrderTerm(orderTermToBeAdded);
		int usedTicketId;

		synchronized (OrderTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTermAdded.class,
				event -> sendOrderTermChangedMessage(((OrderTermAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderTerm(HttpServletRequest request) {

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

		OrderTerm orderTermToBeUpdated = new OrderTerm();

		try {
			orderTermToBeUpdated = OrderTermMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderTerm(orderTermToBeUpdated);

	}

	/**
	 * Updates the OrderTerm with the specific Id
	 * 
	 * @param orderTermToBeUpdated the OrderTerm thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderTerm(OrderTerm orderTermToBeUpdated) {

		UpdateOrderTerm com = new UpdateOrderTerm(orderTermToBeUpdated);

		int usedTicketId;

		synchronized (OrderTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTermUpdated.class,
				event -> sendOrderTermChangedMessage(((OrderTermUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderTerm from the database
	 * 
	 * @param orderTermId:
	 *            the id of the OrderTerm thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderTermById(@RequestParam(value = "orderTermId") String orderTermId) {

		DeleteOrderTerm com = new DeleteOrderTerm(orderTermId);

		int usedTicketId;

		synchronized (OrderTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderTermDeleted.class,
				event -> sendOrderTermChangedMessage(((OrderTermDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderTermChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderTerm/\" plus one of the following: "
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
