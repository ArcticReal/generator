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
import com.skytala.eCommerce.command.AddTimesheetRole;
import com.skytala.eCommerce.command.DeleteTimesheetRole;
import com.skytala.eCommerce.command.UpdateTimesheetRole;
import com.skytala.eCommerce.entity.TimesheetRole;
import com.skytala.eCommerce.entity.TimesheetRoleMapper;
import com.skytala.eCommerce.event.TimesheetRoleAdded;
import com.skytala.eCommerce.event.TimesheetRoleDeleted;
import com.skytala.eCommerce.event.TimesheetRoleFound;
import com.skytala.eCommerce.event.TimesheetRoleUpdated;
import com.skytala.eCommerce.query.FindTimesheetRolesBy;

@RestController
@RequestMapping("/api/timesheetRole")
public class TimesheetRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TimesheetRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TimesheetRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TimesheetRole
	 * @return a List with the TimesheetRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TimesheetRole> findTimesheetRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindTimesheetRolesBy query = new FindTimesheetRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (TimesheetRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TimesheetRoleFound.class,
				event -> sendTimesheetRolesFoundMessage(((TimesheetRoleFound) event).getTimesheetRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTimesheetRolesFoundMessage(List<TimesheetRole> timesheetRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, timesheetRoles);
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
	public boolean createTimesheetRole(HttpServletRequest request) {

		TimesheetRole timesheetRoleToBeAdded = new TimesheetRole();
		try {
			timesheetRoleToBeAdded = TimesheetRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTimesheetRole(timesheetRoleToBeAdded);

	}

	/**
	 * creates a new TimesheetRole entry in the ofbiz database
	 * 
	 * @param timesheetRoleToBeAdded
	 *            the TimesheetRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTimesheetRole(TimesheetRole timesheetRoleToBeAdded) {

		AddTimesheetRole com = new AddTimesheetRole(timesheetRoleToBeAdded);
		int usedTicketId;

		synchronized (TimesheetRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TimesheetRoleAdded.class,
				event -> sendTimesheetRoleChangedMessage(((TimesheetRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTimesheetRole(HttpServletRequest request) {

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

		TimesheetRole timesheetRoleToBeUpdated = new TimesheetRole();

		try {
			timesheetRoleToBeUpdated = TimesheetRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTimesheetRole(timesheetRoleToBeUpdated);

	}

	/**
	 * Updates the TimesheetRole with the specific Id
	 * 
	 * @param timesheetRoleToBeUpdated the TimesheetRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTimesheetRole(TimesheetRole timesheetRoleToBeUpdated) {

		UpdateTimesheetRole com = new UpdateTimesheetRole(timesheetRoleToBeUpdated);

		int usedTicketId;

		synchronized (TimesheetRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TimesheetRoleUpdated.class,
				event -> sendTimesheetRoleChangedMessage(((TimesheetRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TimesheetRole from the database
	 * 
	 * @param timesheetRoleId:
	 *            the id of the TimesheetRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetimesheetRoleById(@RequestParam(value = "timesheetRoleId") String timesheetRoleId) {

		DeleteTimesheetRole com = new DeleteTimesheetRole(timesheetRoleId);

		int usedTicketId;

		synchronized (TimesheetRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TimesheetRoleDeleted.class,
				event -> sendTimesheetRoleChangedMessage(((TimesheetRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTimesheetRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/timesheetRole/\" plus one of the following: "
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
