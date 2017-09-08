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
import com.skytala.eCommerce.command.AddOrderHeaderNote;
import com.skytala.eCommerce.command.DeleteOrderHeaderNote;
import com.skytala.eCommerce.command.UpdateOrderHeaderNote;
import com.skytala.eCommerce.entity.OrderHeaderNote;
import com.skytala.eCommerce.entity.OrderHeaderNoteMapper;
import com.skytala.eCommerce.event.OrderHeaderNoteAdded;
import com.skytala.eCommerce.event.OrderHeaderNoteDeleted;
import com.skytala.eCommerce.event.OrderHeaderNoteFound;
import com.skytala.eCommerce.event.OrderHeaderNoteUpdated;
import com.skytala.eCommerce.query.FindOrderHeaderNotesBy;

@RestController
@RequestMapping("/api/orderHeaderNote")
public class OrderHeaderNoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OrderHeaderNote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OrderHeaderNoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OrderHeaderNote
	 * @return a List with the OrderHeaderNotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OrderHeaderNote> findOrderHeaderNotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOrderHeaderNotesBy query = new FindOrderHeaderNotesBy(allRequestParams);

		int usedTicketId;

		synchronized (OrderHeaderNoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderNoteFound.class,
				event -> sendOrderHeaderNotesFoundMessage(((OrderHeaderNoteFound) event).getOrderHeaderNotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOrderHeaderNotesFoundMessage(List<OrderHeaderNote> orderHeaderNotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, orderHeaderNotes);
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
	public boolean createOrderHeaderNote(HttpServletRequest request) {

		OrderHeaderNote orderHeaderNoteToBeAdded = new OrderHeaderNote();
		try {
			orderHeaderNoteToBeAdded = OrderHeaderNoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOrderHeaderNote(orderHeaderNoteToBeAdded);

	}

	/**
	 * creates a new OrderHeaderNote entry in the ofbiz database
	 * 
	 * @param orderHeaderNoteToBeAdded
	 *            the OrderHeaderNote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOrderHeaderNote(OrderHeaderNote orderHeaderNoteToBeAdded) {

		AddOrderHeaderNote com = new AddOrderHeaderNote(orderHeaderNoteToBeAdded);
		int usedTicketId;

		synchronized (OrderHeaderNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderNoteAdded.class,
				event -> sendOrderHeaderNoteChangedMessage(((OrderHeaderNoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOrderHeaderNote(HttpServletRequest request) {

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

		OrderHeaderNote orderHeaderNoteToBeUpdated = new OrderHeaderNote();

		try {
			orderHeaderNoteToBeUpdated = OrderHeaderNoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOrderHeaderNote(orderHeaderNoteToBeUpdated);

	}

	/**
	 * Updates the OrderHeaderNote with the specific Id
	 * 
	 * @param orderHeaderNoteToBeUpdated the OrderHeaderNote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOrderHeaderNote(OrderHeaderNote orderHeaderNoteToBeUpdated) {

		UpdateOrderHeaderNote com = new UpdateOrderHeaderNote(orderHeaderNoteToBeUpdated);

		int usedTicketId;

		synchronized (OrderHeaderNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderNoteUpdated.class,
				event -> sendOrderHeaderNoteChangedMessage(((OrderHeaderNoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OrderHeaderNote from the database
	 * 
	 * @param orderHeaderNoteId:
	 *            the id of the OrderHeaderNote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteorderHeaderNoteById(@RequestParam(value = "orderHeaderNoteId") String orderHeaderNoteId) {

		DeleteOrderHeaderNote com = new DeleteOrderHeaderNote(orderHeaderNoteId);

		int usedTicketId;

		synchronized (OrderHeaderNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OrderHeaderNoteDeleted.class,
				event -> sendOrderHeaderNoteChangedMessage(((OrderHeaderNoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOrderHeaderNoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/orderHeaderNote/\" plus one of the following: "
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
