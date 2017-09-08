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
import com.skytala.eCommerce.command.AddCommunicationEvent;
import com.skytala.eCommerce.command.DeleteCommunicationEvent;
import com.skytala.eCommerce.command.UpdateCommunicationEvent;
import com.skytala.eCommerce.entity.CommunicationEvent;
import com.skytala.eCommerce.entity.CommunicationEventMapper;
import com.skytala.eCommerce.event.CommunicationEventAdded;
import com.skytala.eCommerce.event.CommunicationEventDeleted;
import com.skytala.eCommerce.event.CommunicationEventFound;
import com.skytala.eCommerce.event.CommunicationEventUpdated;
import com.skytala.eCommerce.query.FindCommunicationEventsBy;

@RestController
@RequestMapping("/api/communicationEvent")
public class CommunicationEventController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommunicationEvent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommunicationEventController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommunicationEvent
	 * @return a List with the CommunicationEvents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommunicationEvent> findCommunicationEventsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommunicationEventsBy query = new FindCommunicationEventsBy(allRequestParams);

		int usedTicketId;

		synchronized (CommunicationEventController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventFound.class,
				event -> sendCommunicationEventsFoundMessage(((CommunicationEventFound) event).getCommunicationEvents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommunicationEventsFoundMessage(List<CommunicationEvent> communicationEvents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, communicationEvents);
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
	public boolean createCommunicationEvent(HttpServletRequest request) {

		CommunicationEvent communicationEventToBeAdded = new CommunicationEvent();
		try {
			communicationEventToBeAdded = CommunicationEventMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommunicationEvent(communicationEventToBeAdded);

	}

	/**
	 * creates a new CommunicationEvent entry in the ofbiz database
	 * 
	 * @param communicationEventToBeAdded
	 *            the CommunicationEvent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommunicationEvent(CommunicationEvent communicationEventToBeAdded) {

		AddCommunicationEvent com = new AddCommunicationEvent(communicationEventToBeAdded);
		int usedTicketId;

		synchronized (CommunicationEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventAdded.class,
				event -> sendCommunicationEventChangedMessage(((CommunicationEventAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCommunicationEvent(HttpServletRequest request) {

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

		CommunicationEvent communicationEventToBeUpdated = new CommunicationEvent();

		try {
			communicationEventToBeUpdated = CommunicationEventMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommunicationEvent(communicationEventToBeUpdated);

	}

	/**
	 * Updates the CommunicationEvent with the specific Id
	 * 
	 * @param communicationEventToBeUpdated the CommunicationEvent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommunicationEvent(CommunicationEvent communicationEventToBeUpdated) {

		UpdateCommunicationEvent com = new UpdateCommunicationEvent(communicationEventToBeUpdated);

		int usedTicketId;

		synchronized (CommunicationEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventUpdated.class,
				event -> sendCommunicationEventChangedMessage(((CommunicationEventUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CommunicationEvent from the database
	 * 
	 * @param communicationEventId:
	 *            the id of the CommunicationEvent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommunicationEventById(@RequestParam(value = "communicationEventId") String communicationEventId) {

		DeleteCommunicationEvent com = new DeleteCommunicationEvent(communicationEventId);

		int usedTicketId;

		synchronized (CommunicationEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventDeleted.class,
				event -> sendCommunicationEventChangedMessage(((CommunicationEventDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCommunicationEventChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/communicationEvent/\" plus one of the following: "
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
