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
import com.skytala.eCommerce.command.AddOrderRequirementCommitment;
import com.skytala.eCommerce.command.DeleteOrderRequirementCommitment;
import com.skytala.eCommerce.command.UpdateOrderRequirementCommitment;
import com.skytala.eCommerce.entity.OrderRequirementCommitment;
import com.skytala.eCommerce.entity.OrderRequirementCommitmentMapper;
import com.skytala.eCommerce.event.OrderRequirementCommitmentAdded;
import com.skytala.eCommerce.event.OrderRequirementCommitmentDeleted;
import com.skytala.eCommerce.event.OrderRequirementCommitmentFound;
import com.skytala.eCommerce.event.OrderRequirementCommitmentUpdated;
import com.skytala.eCommerce.query.FindOrderRequirementCommitmentsBy;

@RestController
@RequestMapping("/api/orderRequirementCommitment")
public class OrderRequirementCommitmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderRequirementCommitment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderRequirementCommitmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderRequirementCommitment
	 * @return a List with the OrderRequirementCommitments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderRequirementCommitment> findOrderRequirementCommitmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderRequirementCommitmentsBy query = new FindOrderRequirementCommitmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderRequirementCommitmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderRequirementCommitmentFound.class,
				event -> sendOrderRequirementCommitmentsFoundMessage(((OrderRequirementCommitmentFound) event).getOrderRequirementCommitments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderRequirementCommitmentsFoundMessage(List<OrderRequirementCommitment> orderRequirementCommitments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderRequirementCommitments);
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
	public boolean createOrderRequirementCommitment(HttpServletRequest request) {

		OrderRequirementCommitment orderRequirementCommitmentToBeAdded = new OrderRequirementCommitment();
		try {
			orderRequirementCommitmentToBeAdded = OrderRequirementCommitmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderRequirementCommitment(orderRequirementCommitmentToBeAdded);

	}

	/**
	 * creates a new OrderRequirementCommitment entry in the ofbiz database
	 * 
	 * @param orderRequirementCommitmentToBeAdded
	 *            the OrderRequirementCommitment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderRequirementCommitment(OrderRequirementCommitment orderRequirementCommitmentToBeAdded) {

		AddOrderRequirementCommitment com = new AddOrderRequirementCommitment(orderRequirementCommitmentToBeAdded);
		int usedTicketId;

		synchronized (OrderRequirementCommitmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderRequirementCommitmentAdded.class,
				event -> sendOrderRequirementCommitmentChangedMessage(((OrderRequirementCommitmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderRequirementCommitment(HttpServletRequest request) {

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

		OrderRequirementCommitment orderRequirementCommitmentToBeUpdated = new OrderRequirementCommitment();

		try {
			orderRequirementCommitmentToBeUpdated = OrderRequirementCommitmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderRequirementCommitment(orderRequirementCommitmentToBeUpdated);

	}

	/**
	 * Updates the OrderRequirementCommitment with the specific Id
	 * 
	 * @param orderRequirementCommitmentToBeUpdated the OrderRequirementCommitment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderRequirementCommitment(OrderRequirementCommitment orderRequirementCommitmentToBeUpdated) {

		UpdateOrderRequirementCommitment com = new UpdateOrderRequirementCommitment(orderRequirementCommitmentToBeUpdated);

		int usedTicketId;

		synchronized (OrderRequirementCommitmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderRequirementCommitmentUpdated.class,
				event -> sendOrderRequirementCommitmentChangedMessage(((OrderRequirementCommitmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderRequirementCommitment from the database
	 * 
	 * @param orderRequirementCommitmentId:
	 *            the id of the OrderRequirementCommitment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderRequirementCommitmentById(@RequestParam(value = "orderRequirementCommitmentId") String orderRequirementCommitmentId) {

		DeleteOrderRequirementCommitment com = new DeleteOrderRequirementCommitment(orderRequirementCommitmentId);

		int usedTicketId;

		synchronized (OrderRequirementCommitmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderRequirementCommitmentDeleted.class,
				event -> sendOrderRequirementCommitmentChangedMessage(((OrderRequirementCommitmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderRequirementCommitmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderRequirementCommitment/\" plus one of the following: "
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
