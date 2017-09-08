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
import com.skytala.eCommerce.command.AddEmplPositionFulfillment;
import com.skytala.eCommerce.command.DeleteEmplPositionFulfillment;
import com.skytala.eCommerce.command.UpdateEmplPositionFulfillment;
import com.skytala.eCommerce.entity.EmplPositionFulfillment;
import com.skytala.eCommerce.entity.EmplPositionFulfillmentMapper;
import com.skytala.eCommerce.event.EmplPositionFulfillmentAdded;
import com.skytala.eCommerce.event.EmplPositionFulfillmentDeleted;
import com.skytala.eCommerce.event.EmplPositionFulfillmentFound;
import com.skytala.eCommerce.event.EmplPositionFulfillmentUpdated;
import com.skytala.eCommerce.query.FindEmplPositionFulfillmentsBy;

@RestController
@RequestMapping("/api/emplPositionFulfillment")
public class EmplPositionFulfillmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmplPositionFulfillment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmplPositionFulfillmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmplPositionFulfillment
	 * @return a List with the EmplPositionFulfillments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmplPositionFulfillment> findEmplPositionFulfillmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmplPositionFulfillmentsBy query = new FindEmplPositionFulfillmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (EmplPositionFulfillmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionFulfillmentFound.class,
				event -> sendEmplPositionFulfillmentsFoundMessage(((EmplPositionFulfillmentFound) event).getEmplPositionFulfillments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmplPositionFulfillmentsFoundMessage(List<EmplPositionFulfillment> emplPositionFulfillments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emplPositionFulfillments);
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
	public boolean createEmplPositionFulfillment(HttpServletRequest request) {

		EmplPositionFulfillment emplPositionFulfillmentToBeAdded = new EmplPositionFulfillment();
		try {
			emplPositionFulfillmentToBeAdded = EmplPositionFulfillmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmplPositionFulfillment(emplPositionFulfillmentToBeAdded);

	}

	/**
	 * creates a new EmplPositionFulfillment entry in the ofbiz database
	 * 
	 * @param emplPositionFulfillmentToBeAdded
	 *            the EmplPositionFulfillment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmplPositionFulfillment(EmplPositionFulfillment emplPositionFulfillmentToBeAdded) {

		AddEmplPositionFulfillment com = new AddEmplPositionFulfillment(emplPositionFulfillmentToBeAdded);
		int usedTicketId;

		synchronized (EmplPositionFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionFulfillmentAdded.class,
				event -> sendEmplPositionFulfillmentChangedMessage(((EmplPositionFulfillmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmplPositionFulfillment(HttpServletRequest request) {

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

		EmplPositionFulfillment emplPositionFulfillmentToBeUpdated = new EmplPositionFulfillment();

		try {
			emplPositionFulfillmentToBeUpdated = EmplPositionFulfillmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmplPositionFulfillment(emplPositionFulfillmentToBeUpdated);

	}

	/**
	 * Updates the EmplPositionFulfillment with the specific Id
	 * 
	 * @param emplPositionFulfillmentToBeUpdated the EmplPositionFulfillment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmplPositionFulfillment(EmplPositionFulfillment emplPositionFulfillmentToBeUpdated) {

		UpdateEmplPositionFulfillment com = new UpdateEmplPositionFulfillment(emplPositionFulfillmentToBeUpdated);

		int usedTicketId;

		synchronized (EmplPositionFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionFulfillmentUpdated.class,
				event -> sendEmplPositionFulfillmentChangedMessage(((EmplPositionFulfillmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmplPositionFulfillment from the database
	 * 
	 * @param emplPositionFulfillmentId:
	 *            the id of the EmplPositionFulfillment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemplPositionFulfillmentById(@RequestParam(value = "emplPositionFulfillmentId") String emplPositionFulfillmentId) {

		DeleteEmplPositionFulfillment com = new DeleteEmplPositionFulfillment(emplPositionFulfillmentId);

		int usedTicketId;

		synchronized (EmplPositionFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionFulfillmentDeleted.class,
				event -> sendEmplPositionFulfillmentChangedMessage(((EmplPositionFulfillmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmplPositionFulfillmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emplPositionFulfillment/\" plus one of the following: "
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
