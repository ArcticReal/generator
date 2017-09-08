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
import com.skytala.eCommerce.command.AddMrpEvent;
import com.skytala.eCommerce.command.DeleteMrpEvent;
import com.skytala.eCommerce.command.UpdateMrpEvent;
import com.skytala.eCommerce.entity.MrpEvent;
import com.skytala.eCommerce.entity.MrpEventMapper;
import com.skytala.eCommerce.event.MrpEventAdded;
import com.skytala.eCommerce.event.MrpEventDeleted;
import com.skytala.eCommerce.event.MrpEventFound;
import com.skytala.eCommerce.event.MrpEventUpdated;
import com.skytala.eCommerce.query.FindMrpEventsBy;

@RestController
@RequestMapping("/api/mrpEvent")
public class MrpEventController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<MrpEvent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public MrpEventController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a MrpEvent
	 * @return a List with the MrpEvents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<MrpEvent> findMrpEventsBy(@RequestParam Map<String, String> allRequestParams) {

		FindMrpEventsBy query = new FindMrpEventsBy(allRequestParams);

		int usedTicketId;

		synchronized (MrpEventController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MrpEventFound.class,
				event -> sendMrpEventsFoundMessage(((MrpEventFound) event).getMrpEvents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendMrpEventsFoundMessage(List<MrpEvent> mrpEvents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, mrpEvents);
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
	public boolean createMrpEvent(HttpServletRequest request) {

		MrpEvent mrpEventToBeAdded = new MrpEvent();
		try {
			mrpEventToBeAdded = MrpEventMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createMrpEvent(mrpEventToBeAdded);

	}

	/**
	 * creates a new MrpEvent entry in the ofbiz database
	 * 
	 * @param mrpEventToBeAdded
	 *            the MrpEvent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createMrpEvent(MrpEvent mrpEventToBeAdded) {

		AddMrpEvent com = new AddMrpEvent(mrpEventToBeAdded);
		int usedTicketId;

		synchronized (MrpEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MrpEventAdded.class,
				event -> sendMrpEventChangedMessage(((MrpEventAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateMrpEvent(HttpServletRequest request) {

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

		MrpEvent mrpEventToBeUpdated = new MrpEvent();

		try {
			mrpEventToBeUpdated = MrpEventMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateMrpEvent(mrpEventToBeUpdated);

	}

	/**
	 * Updates the MrpEvent with the specific Id
	 * 
	 * @param mrpEventToBeUpdated the MrpEvent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateMrpEvent(MrpEvent mrpEventToBeUpdated) {

		UpdateMrpEvent com = new UpdateMrpEvent(mrpEventToBeUpdated);

		int usedTicketId;

		synchronized (MrpEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MrpEventUpdated.class,
				event -> sendMrpEventChangedMessage(((MrpEventUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a MrpEvent from the database
	 * 
	 * @param mrpEventId:
	 *            the id of the MrpEvent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletemrpEventById(@RequestParam(value = "mrpEventId") String mrpEventId) {

		DeleteMrpEvent com = new DeleteMrpEvent(mrpEventId);

		int usedTicketId;

		synchronized (MrpEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MrpEventDeleted.class,
				event -> sendMrpEventChangedMessage(((MrpEventDeleted) event).isSuccess(), usedTicketId));

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

	public void sendMrpEventChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/mrpEvent/\" plus one of the following: "
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
