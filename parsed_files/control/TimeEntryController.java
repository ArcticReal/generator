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
import com.skytala.eCommerce.command.AddTimeEntry;
import com.skytala.eCommerce.command.DeleteTimeEntry;
import com.skytala.eCommerce.command.UpdateTimeEntry;
import com.skytala.eCommerce.entity.TimeEntry;
import com.skytala.eCommerce.entity.TimeEntryMapper;
import com.skytala.eCommerce.event.TimeEntryAdded;
import com.skytala.eCommerce.event.TimeEntryDeleted;
import com.skytala.eCommerce.event.TimeEntryFound;
import com.skytala.eCommerce.event.TimeEntryUpdated;
import com.skytala.eCommerce.query.FindTimeEntrysBy;

@RestController
@RequestMapping("/api/timeEntry")
public class TimeEntryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TimeEntry>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TimeEntryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TimeEntry
	 * @return a List with the TimeEntrys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TimeEntry> findTimeEntrysBy(@RequestParam Map<String, String> allRequestParams) {

		FindTimeEntrysBy query = new FindTimeEntrysBy(allRequestParams);

		int usedTicketId;

		synchronized (TimeEntryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TimeEntryFound.class,
				event -> sendTimeEntrysFoundMessage(((TimeEntryFound) event).getTimeEntrys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTimeEntrysFoundMessage(List<TimeEntry> timeEntrys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, timeEntrys);
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
	public boolean createTimeEntry(HttpServletRequest request) {

		TimeEntry timeEntryToBeAdded = new TimeEntry();
		try {
			timeEntryToBeAdded = TimeEntryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTimeEntry(timeEntryToBeAdded);

	}

	/**
	 * creates a new TimeEntry entry in the ofbiz database
	 * 
	 * @param timeEntryToBeAdded
	 *            the TimeEntry thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTimeEntry(TimeEntry timeEntryToBeAdded) {

		AddTimeEntry com = new AddTimeEntry(timeEntryToBeAdded);
		int usedTicketId;

		synchronized (TimeEntryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TimeEntryAdded.class,
				event -> sendTimeEntryChangedMessage(((TimeEntryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTimeEntry(HttpServletRequest request) {

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

		TimeEntry timeEntryToBeUpdated = new TimeEntry();

		try {
			timeEntryToBeUpdated = TimeEntryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTimeEntry(timeEntryToBeUpdated);

	}

	/**
	 * Updates the TimeEntry with the specific Id
	 * 
	 * @param timeEntryToBeUpdated the TimeEntry thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTimeEntry(TimeEntry timeEntryToBeUpdated) {

		UpdateTimeEntry com = new UpdateTimeEntry(timeEntryToBeUpdated);

		int usedTicketId;

		synchronized (TimeEntryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TimeEntryUpdated.class,
				event -> sendTimeEntryChangedMessage(((TimeEntryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TimeEntry from the database
	 * 
	 * @param timeEntryId:
	 *            the id of the TimeEntry thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetimeEntryById(@RequestParam(value = "timeEntryId") String timeEntryId) {

		DeleteTimeEntry com = new DeleteTimeEntry(timeEntryId);

		int usedTicketId;

		synchronized (TimeEntryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TimeEntryDeleted.class,
				event -> sendTimeEntryChangedMessage(((TimeEntryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTimeEntryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/timeEntry/\" plus one of the following: "
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
