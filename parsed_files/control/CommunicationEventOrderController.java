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
import com.skytala.eCommerce.command.AddCommunicationEventOrder;
import com.skytala.eCommerce.command.DeleteCommunicationEventOrder;
import com.skytala.eCommerce.command.UpdateCommunicationEventOrder;
import com.skytala.eCommerce.entity.CommunicationEventOrder;
import com.skytala.eCommerce.entity.CommunicationEventOrderMapper;
import com.skytala.eCommerce.event.CommunicationEventOrderAdded;
import com.skytala.eCommerce.event.CommunicationEventOrderDeleted;
import com.skytala.eCommerce.event.CommunicationEventOrderFound;
import com.skytala.eCommerce.event.CommunicationEventOrderUpdated;
import com.skytala.eCommerce.query.FindCommunicationEventOrdersBy;

@RestController
@RequestMapping("/api/communicationEventOrder")
public class CommunicationEventOrderController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommunicationEventOrder>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommunicationEventOrderController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommunicationEventOrder
	 * @return a List with the CommunicationEventOrders
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommunicationEventOrder> findCommunicationEventOrdersBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommunicationEventOrdersBy query = new FindCommunicationEventOrdersBy(allRequestParams);

		int usedTicketId;

		synchronized (CommunicationEventOrderController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventOrderFound.class,
				event -> sendCommunicationEventOrdersFoundMessage(((CommunicationEventOrderFound) event).getCommunicationEventOrders(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommunicationEventOrdersFoundMessage(List<CommunicationEventOrder> communicationEventOrders, int usedTicketId) {
		queryReturnVal.put(usedTicketId, communicationEventOrders);
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
	public boolean createCommunicationEventOrder(HttpServletRequest request) {

		CommunicationEventOrder communicationEventOrderToBeAdded = new CommunicationEventOrder();
		try {
			communicationEventOrderToBeAdded = CommunicationEventOrderMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommunicationEventOrder(communicationEventOrderToBeAdded);

	}

	/**
	 * creates a new CommunicationEventOrder entry in the ofbiz database
	 * 
	 * @param communicationEventOrderToBeAdded
	 *            the CommunicationEventOrder thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommunicationEventOrder(CommunicationEventOrder communicationEventOrderToBeAdded) {

		AddCommunicationEventOrder com = new AddCommunicationEventOrder(communicationEventOrderToBeAdded);
		int usedTicketId;

		synchronized (CommunicationEventOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventOrderAdded.class,
				event -> sendCommunicationEventOrderChangedMessage(((CommunicationEventOrderAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCommunicationEventOrder(HttpServletRequest request) {

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

		CommunicationEventOrder communicationEventOrderToBeUpdated = new CommunicationEventOrder();

		try {
			communicationEventOrderToBeUpdated = CommunicationEventOrderMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommunicationEventOrder(communicationEventOrderToBeUpdated);

	}

	/**
	 * Updates the CommunicationEventOrder with the specific Id
	 * 
	 * @param communicationEventOrderToBeUpdated the CommunicationEventOrder thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommunicationEventOrder(CommunicationEventOrder communicationEventOrderToBeUpdated) {

		UpdateCommunicationEventOrder com = new UpdateCommunicationEventOrder(communicationEventOrderToBeUpdated);

		int usedTicketId;

		synchronized (CommunicationEventOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventOrderUpdated.class,
				event -> sendCommunicationEventOrderChangedMessage(((CommunicationEventOrderUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CommunicationEventOrder from the database
	 * 
	 * @param communicationEventOrderId:
	 *            the id of the CommunicationEventOrder thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommunicationEventOrderById(@RequestParam(value = "communicationEventOrderId") String communicationEventOrderId) {

		DeleteCommunicationEventOrder com = new DeleteCommunicationEventOrder(communicationEventOrderId);

		int usedTicketId;

		synchronized (CommunicationEventOrderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventOrderDeleted.class,
				event -> sendCommunicationEventOrderChangedMessage(((CommunicationEventOrderDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCommunicationEventOrderChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/communicationEventOrder/\" plus one of the following: "
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
