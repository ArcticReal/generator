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
import com.skytala.eCommerce.command.AddWorkEffortEventReminder;
import com.skytala.eCommerce.command.DeleteWorkEffortEventReminder;
import com.skytala.eCommerce.command.UpdateWorkEffortEventReminder;
import com.skytala.eCommerce.entity.WorkEffortEventReminder;
import com.skytala.eCommerce.entity.WorkEffortEventReminderMapper;
import com.skytala.eCommerce.event.WorkEffortEventReminderAdded;
import com.skytala.eCommerce.event.WorkEffortEventReminderDeleted;
import com.skytala.eCommerce.event.WorkEffortEventReminderFound;
import com.skytala.eCommerce.event.WorkEffortEventReminderUpdated;
import com.skytala.eCommerce.query.FindWorkEffortEventRemindersBy;

@RestController
@RequestMapping("/api/workEffortEventReminder")
public class WorkEffortEventReminderController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortEventReminder>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortEventReminderController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortEventReminder
	 * @return a List with the WorkEffortEventReminders
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortEventReminder> findWorkEffortEventRemindersBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortEventRemindersBy query = new FindWorkEffortEventRemindersBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortEventReminderController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortEventReminderFound.class,
				event -> sendWorkEffortEventRemindersFoundMessage(((WorkEffortEventReminderFound) event).getWorkEffortEventReminders(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortEventRemindersFoundMessage(List<WorkEffortEventReminder> workEffortEventReminders, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortEventReminders);
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
	public boolean createWorkEffortEventReminder(HttpServletRequest request) {

		WorkEffortEventReminder workEffortEventReminderToBeAdded = new WorkEffortEventReminder();
		try {
			workEffortEventReminderToBeAdded = WorkEffortEventReminderMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortEventReminder(workEffortEventReminderToBeAdded);

	}

	/**
	 * creates a new WorkEffortEventReminder entry in the ofbiz database
	 * 
	 * @param workEffortEventReminderToBeAdded
	 *            the WorkEffortEventReminder thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortEventReminder(WorkEffortEventReminder workEffortEventReminderToBeAdded) {

		AddWorkEffortEventReminder com = new AddWorkEffortEventReminder(workEffortEventReminderToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortEventReminderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortEventReminderAdded.class,
				event -> sendWorkEffortEventReminderChangedMessage(((WorkEffortEventReminderAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortEventReminder(HttpServletRequest request) {

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

		WorkEffortEventReminder workEffortEventReminderToBeUpdated = new WorkEffortEventReminder();

		try {
			workEffortEventReminderToBeUpdated = WorkEffortEventReminderMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortEventReminder(workEffortEventReminderToBeUpdated);

	}

	/**
	 * Updates the WorkEffortEventReminder with the specific Id
	 * 
	 * @param workEffortEventReminderToBeUpdated the WorkEffortEventReminder thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortEventReminder(WorkEffortEventReminder workEffortEventReminderToBeUpdated) {

		UpdateWorkEffortEventReminder com = new UpdateWorkEffortEventReminder(workEffortEventReminderToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortEventReminderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortEventReminderUpdated.class,
				event -> sendWorkEffortEventReminderChangedMessage(((WorkEffortEventReminderUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortEventReminder from the database
	 * 
	 * @param workEffortEventReminderId:
	 *            the id of the WorkEffortEventReminder thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortEventReminderById(@RequestParam(value = "workEffortEventReminderId") String workEffortEventReminderId) {

		DeleteWorkEffortEventReminder com = new DeleteWorkEffortEventReminder(workEffortEventReminderId);

		int usedTicketId;

		synchronized (WorkEffortEventReminderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortEventReminderDeleted.class,
				event -> sendWorkEffortEventReminderChangedMessage(((WorkEffortEventReminderDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortEventReminderChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortEventReminder/\" plus one of the following: "
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
