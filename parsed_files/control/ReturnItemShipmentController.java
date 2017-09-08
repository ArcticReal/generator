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
import com.skytala.eCommerce.command.AddReturnItemShipment;
import com.skytala.eCommerce.command.DeleteReturnItemShipment;
import com.skytala.eCommerce.command.UpdateReturnItemShipment;
import com.skytala.eCommerce.entity.ReturnItemShipment;
import com.skytala.eCommerce.entity.ReturnItemShipmentMapper;
import com.skytala.eCommerce.event.ReturnItemShipmentAdded;
import com.skytala.eCommerce.event.ReturnItemShipmentDeleted;
import com.skytala.eCommerce.event.ReturnItemShipmentFound;
import com.skytala.eCommerce.event.ReturnItemShipmentUpdated;
import com.skytala.eCommerce.query.FindReturnItemShipmentsBy;

@RestController
@RequestMapping("/api/returnItemShipment")
public class ReturnItemShipmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnItemShipment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnItemShipmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnItemShipment
	 * @return a List with the ReturnItemShipments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnItemShipment> findReturnItemShipmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnItemShipmentsBy query = new FindReturnItemShipmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnItemShipmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemShipmentFound.class,
				event -> sendReturnItemShipmentsFoundMessage(((ReturnItemShipmentFound) event).getReturnItemShipments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnItemShipmentsFoundMessage(List<ReturnItemShipment> returnItemShipments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnItemShipments);
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
	public boolean createReturnItemShipment(HttpServletRequest request) {

		ReturnItemShipment returnItemShipmentToBeAdded = new ReturnItemShipment();
		try {
			returnItemShipmentToBeAdded = ReturnItemShipmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnItemShipment(returnItemShipmentToBeAdded);

	}

	/**
	 * creates a new ReturnItemShipment entry in the ofbiz database
	 * 
	 * @param returnItemShipmentToBeAdded
	 *            the ReturnItemShipment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnItemShipment(ReturnItemShipment returnItemShipmentToBeAdded) {

		AddReturnItemShipment com = new AddReturnItemShipment(returnItemShipmentToBeAdded);
		int usedTicketId;

		synchronized (ReturnItemShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemShipmentAdded.class,
				event -> sendReturnItemShipmentChangedMessage(((ReturnItemShipmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnItemShipment(HttpServletRequest request) {

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

		ReturnItemShipment returnItemShipmentToBeUpdated = new ReturnItemShipment();

		try {
			returnItemShipmentToBeUpdated = ReturnItemShipmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnItemShipment(returnItemShipmentToBeUpdated);

	}

	/**
	 * Updates the ReturnItemShipment with the specific Id
	 * 
	 * @param returnItemShipmentToBeUpdated the ReturnItemShipment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnItemShipment(ReturnItemShipment returnItemShipmentToBeUpdated) {

		UpdateReturnItemShipment com = new UpdateReturnItemShipment(returnItemShipmentToBeUpdated);

		int usedTicketId;

		synchronized (ReturnItemShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemShipmentUpdated.class,
				event -> sendReturnItemShipmentChangedMessage(((ReturnItemShipmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnItemShipment from the database
	 * 
	 * @param returnItemShipmentId:
	 *            the id of the ReturnItemShipment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnItemShipmentById(@RequestParam(value = "returnItemShipmentId") String returnItemShipmentId) {

		DeleteReturnItemShipment com = new DeleteReturnItemShipment(returnItemShipmentId);

		int usedTicketId;

		synchronized (ReturnItemShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemShipmentDeleted.class,
				event -> sendReturnItemShipmentChangedMessage(((ReturnItemShipmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnItemShipmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnItemShipment/\" plus one of the following: "
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
