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
import com.skytala.eCommerce.command.AddCommunicationEventType;
import com.skytala.eCommerce.command.DeleteCommunicationEventType;
import com.skytala.eCommerce.command.UpdateCommunicationEventType;
import com.skytala.eCommerce.entity.CommunicationEventType;
import com.skytala.eCommerce.entity.CommunicationEventTypeMapper;
import com.skytala.eCommerce.event.CommunicationEventTypeAdded;
import com.skytala.eCommerce.event.CommunicationEventTypeDeleted;
import com.skytala.eCommerce.event.CommunicationEventTypeFound;
import com.skytala.eCommerce.event.CommunicationEventTypeUpdated;
import com.skytala.eCommerce.query.FindCommunicationEventTypesBy;

@RestController
@RequestMapping("/api/communicationEventType")
public class CommunicationEventTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommunicationEventType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommunicationEventTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommunicationEventType
	 * @return a List with the CommunicationEventTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommunicationEventType> findCommunicationEventTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommunicationEventTypesBy query = new FindCommunicationEventTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (CommunicationEventTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventTypeFound.class,
				event -> sendCommunicationEventTypesFoundMessage(((CommunicationEventTypeFound) event).getCommunicationEventTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommunicationEventTypesFoundMessage(List<CommunicationEventType> communicationEventTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, communicationEventTypes);
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
	public boolean createCommunicationEventType(HttpServletRequest request) {

		CommunicationEventType communicationEventTypeToBeAdded = new CommunicationEventType();
		try {
			communicationEventTypeToBeAdded = CommunicationEventTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommunicationEventType(communicationEventTypeToBeAdded);

	}

	/**
	 * creates a new CommunicationEventType entry in the ofbiz database
	 * 
	 * @param communicationEventTypeToBeAdded
	 *            the CommunicationEventType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommunicationEventType(CommunicationEventType communicationEventTypeToBeAdded) {

		AddCommunicationEventType com = new AddCommunicationEventType(communicationEventTypeToBeAdded);
		int usedTicketId;

		synchronized (CommunicationEventTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventTypeAdded.class,
				event -> sendCommunicationEventTypeChangedMessage(((CommunicationEventTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCommunicationEventType(HttpServletRequest request) {

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

		CommunicationEventType communicationEventTypeToBeUpdated = new CommunicationEventType();

		try {
			communicationEventTypeToBeUpdated = CommunicationEventTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommunicationEventType(communicationEventTypeToBeUpdated);

	}

	/**
	 * Updates the CommunicationEventType with the specific Id
	 * 
	 * @param communicationEventTypeToBeUpdated the CommunicationEventType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommunicationEventType(CommunicationEventType communicationEventTypeToBeUpdated) {

		UpdateCommunicationEventType com = new UpdateCommunicationEventType(communicationEventTypeToBeUpdated);

		int usedTicketId;

		synchronized (CommunicationEventTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventTypeUpdated.class,
				event -> sendCommunicationEventTypeChangedMessage(((CommunicationEventTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CommunicationEventType from the database
	 * 
	 * @param communicationEventTypeId:
	 *            the id of the CommunicationEventType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommunicationEventTypeById(@RequestParam(value = "communicationEventTypeId") String communicationEventTypeId) {

		DeleteCommunicationEventType com = new DeleteCommunicationEventType(communicationEventTypeId);

		int usedTicketId;

		synchronized (CommunicationEventTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventTypeDeleted.class,
				event -> sendCommunicationEventTypeChangedMessage(((CommunicationEventTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCommunicationEventTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/communicationEventType/\" plus one of the following: "
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
