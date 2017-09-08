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
import com.skytala.eCommerce.command.AddPriorityType;
import com.skytala.eCommerce.command.DeletePriorityType;
import com.skytala.eCommerce.command.UpdatePriorityType;
import com.skytala.eCommerce.entity.PriorityType;
import com.skytala.eCommerce.entity.PriorityTypeMapper;
import com.skytala.eCommerce.event.PriorityTypeAdded;
import com.skytala.eCommerce.event.PriorityTypeDeleted;
import com.skytala.eCommerce.event.PriorityTypeFound;
import com.skytala.eCommerce.event.PriorityTypeUpdated;
import com.skytala.eCommerce.query.FindPriorityTypesBy;

@RestController
@RequestMapping("/api/priorityType")
public class PriorityTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PriorityType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PriorityTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PriorityType
	 * @return a List with the PriorityTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PriorityType> findPriorityTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPriorityTypesBy query = new FindPriorityTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PriorityTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PriorityTypeFound.class,
				event -> sendPriorityTypesFoundMessage(((PriorityTypeFound) event).getPriorityTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPriorityTypesFoundMessage(List<PriorityType> priorityTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, priorityTypes);
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
	public boolean createPriorityType(HttpServletRequest request) {

		PriorityType priorityTypeToBeAdded = new PriorityType();
		try {
			priorityTypeToBeAdded = PriorityTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPriorityType(priorityTypeToBeAdded);

	}

	/**
	 * creates a new PriorityType entry in the ofbiz database
	 * 
	 * @param priorityTypeToBeAdded
	 *            the PriorityType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPriorityType(PriorityType priorityTypeToBeAdded) {

		AddPriorityType com = new AddPriorityType(priorityTypeToBeAdded);
		int usedTicketId;

		synchronized (PriorityTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PriorityTypeAdded.class,
				event -> sendPriorityTypeChangedMessage(((PriorityTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePriorityType(HttpServletRequest request) {

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

		PriorityType priorityTypeToBeUpdated = new PriorityType();

		try {
			priorityTypeToBeUpdated = PriorityTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePriorityType(priorityTypeToBeUpdated);

	}

	/**
	 * Updates the PriorityType with the specific Id
	 * 
	 * @param priorityTypeToBeUpdated the PriorityType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePriorityType(PriorityType priorityTypeToBeUpdated) {

		UpdatePriorityType com = new UpdatePriorityType(priorityTypeToBeUpdated);

		int usedTicketId;

		synchronized (PriorityTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PriorityTypeUpdated.class,
				event -> sendPriorityTypeChangedMessage(((PriorityTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PriorityType from the database
	 * 
	 * @param priorityTypeId:
	 *            the id of the PriorityType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepriorityTypeById(@RequestParam(value = "priorityTypeId") String priorityTypeId) {

		DeletePriorityType com = new DeletePriorityType(priorityTypeId);

		int usedTicketId;

		synchronized (PriorityTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PriorityTypeDeleted.class,
				event -> sendPriorityTypeChangedMessage(((PriorityTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPriorityTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/priorityType/\" plus one of the following: "
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
