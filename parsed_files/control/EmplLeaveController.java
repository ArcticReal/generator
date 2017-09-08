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
import com.skytala.eCommerce.command.AddEmplLeave;
import com.skytala.eCommerce.command.DeleteEmplLeave;
import com.skytala.eCommerce.command.UpdateEmplLeave;
import com.skytala.eCommerce.entity.EmplLeave;
import com.skytala.eCommerce.entity.EmplLeaveMapper;
import com.skytala.eCommerce.event.EmplLeaveAdded;
import com.skytala.eCommerce.event.EmplLeaveDeleted;
import com.skytala.eCommerce.event.EmplLeaveFound;
import com.skytala.eCommerce.event.EmplLeaveUpdated;
import com.skytala.eCommerce.query.FindEmplLeavesBy;

@RestController
@RequestMapping("/api/emplLeave")
public class EmplLeaveController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmplLeave>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmplLeaveController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmplLeave
	 * @return a List with the EmplLeaves
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmplLeave> findEmplLeavesBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmplLeavesBy query = new FindEmplLeavesBy(allRequestParams);

		int usedTicketId;

		synchronized (EmplLeaveController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplLeaveFound.class,
				event -> sendEmplLeavesFoundMessage(((EmplLeaveFound) event).getEmplLeaves(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmplLeavesFoundMessage(List<EmplLeave> emplLeaves, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emplLeaves);
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
	public boolean createEmplLeave(HttpServletRequest request) {

		EmplLeave emplLeaveToBeAdded = new EmplLeave();
		try {
			emplLeaveToBeAdded = EmplLeaveMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmplLeave(emplLeaveToBeAdded);

	}

	/**
	 * creates a new EmplLeave entry in the ofbiz database
	 * 
	 * @param emplLeaveToBeAdded
	 *            the EmplLeave thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmplLeave(EmplLeave emplLeaveToBeAdded) {

		AddEmplLeave com = new AddEmplLeave(emplLeaveToBeAdded);
		int usedTicketId;

		synchronized (EmplLeaveController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplLeaveAdded.class,
				event -> sendEmplLeaveChangedMessage(((EmplLeaveAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmplLeave(HttpServletRequest request) {

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

		EmplLeave emplLeaveToBeUpdated = new EmplLeave();

		try {
			emplLeaveToBeUpdated = EmplLeaveMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmplLeave(emplLeaveToBeUpdated);

	}

	/**
	 * Updates the EmplLeave with the specific Id
	 * 
	 * @param emplLeaveToBeUpdated the EmplLeave thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmplLeave(EmplLeave emplLeaveToBeUpdated) {

		UpdateEmplLeave com = new UpdateEmplLeave(emplLeaveToBeUpdated);

		int usedTicketId;

		synchronized (EmplLeaveController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplLeaveUpdated.class,
				event -> sendEmplLeaveChangedMessage(((EmplLeaveUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmplLeave from the database
	 * 
	 * @param emplLeaveId:
	 *            the id of the EmplLeave thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemplLeaveById(@RequestParam(value = "emplLeaveId") String emplLeaveId) {

		DeleteEmplLeave com = new DeleteEmplLeave(emplLeaveId);

		int usedTicketId;

		synchronized (EmplLeaveController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplLeaveDeleted.class,
				event -> sendEmplLeaveChangedMessage(((EmplLeaveDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmplLeaveChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emplLeave/\" plus one of the following: "
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
