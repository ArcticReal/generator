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
import com.skytala.eCommerce.command.AddWorkEffortPartyAssignment;
import com.skytala.eCommerce.command.DeleteWorkEffortPartyAssignment;
import com.skytala.eCommerce.command.UpdateWorkEffortPartyAssignment;
import com.skytala.eCommerce.entity.WorkEffortPartyAssignment;
import com.skytala.eCommerce.entity.WorkEffortPartyAssignmentMapper;
import com.skytala.eCommerce.event.WorkEffortPartyAssignmentAdded;
import com.skytala.eCommerce.event.WorkEffortPartyAssignmentDeleted;
import com.skytala.eCommerce.event.WorkEffortPartyAssignmentFound;
import com.skytala.eCommerce.event.WorkEffortPartyAssignmentUpdated;
import com.skytala.eCommerce.query.FindWorkEffortPartyAssignmentsBy;

@RestController
@RequestMapping("/api/workEffortPartyAssignment")
public class WorkEffortPartyAssignmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortPartyAssignment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortPartyAssignmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortPartyAssignment
	 * @return a List with the WorkEffortPartyAssignments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortPartyAssignment> findWorkEffortPartyAssignmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortPartyAssignmentsBy query = new FindWorkEffortPartyAssignmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortPartyAssignmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortPartyAssignmentFound.class,
				event -> sendWorkEffortPartyAssignmentsFoundMessage(((WorkEffortPartyAssignmentFound) event).getWorkEffortPartyAssignments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortPartyAssignmentsFoundMessage(List<WorkEffortPartyAssignment> workEffortPartyAssignments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortPartyAssignments);
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
	public boolean createWorkEffortPartyAssignment(HttpServletRequest request) {

		WorkEffortPartyAssignment workEffortPartyAssignmentToBeAdded = new WorkEffortPartyAssignment();
		try {
			workEffortPartyAssignmentToBeAdded = WorkEffortPartyAssignmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortPartyAssignment(workEffortPartyAssignmentToBeAdded);

	}

	/**
	 * creates a new WorkEffortPartyAssignment entry in the ofbiz database
	 * 
	 * @param workEffortPartyAssignmentToBeAdded
	 *            the WorkEffortPartyAssignment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortPartyAssignment(WorkEffortPartyAssignment workEffortPartyAssignmentToBeAdded) {

		AddWorkEffortPartyAssignment com = new AddWorkEffortPartyAssignment(workEffortPartyAssignmentToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortPartyAssignmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortPartyAssignmentAdded.class,
				event -> sendWorkEffortPartyAssignmentChangedMessage(((WorkEffortPartyAssignmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortPartyAssignment(HttpServletRequest request) {

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

		WorkEffortPartyAssignment workEffortPartyAssignmentToBeUpdated = new WorkEffortPartyAssignment();

		try {
			workEffortPartyAssignmentToBeUpdated = WorkEffortPartyAssignmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortPartyAssignment(workEffortPartyAssignmentToBeUpdated);

	}

	/**
	 * Updates the WorkEffortPartyAssignment with the specific Id
	 * 
	 * @param workEffortPartyAssignmentToBeUpdated the WorkEffortPartyAssignment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortPartyAssignment(WorkEffortPartyAssignment workEffortPartyAssignmentToBeUpdated) {

		UpdateWorkEffortPartyAssignment com = new UpdateWorkEffortPartyAssignment(workEffortPartyAssignmentToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortPartyAssignmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortPartyAssignmentUpdated.class,
				event -> sendWorkEffortPartyAssignmentChangedMessage(((WorkEffortPartyAssignmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortPartyAssignment from the database
	 * 
	 * @param workEffortPartyAssignmentId:
	 *            the id of the WorkEffortPartyAssignment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortPartyAssignmentById(@RequestParam(value = "workEffortPartyAssignmentId") String workEffortPartyAssignmentId) {

		DeleteWorkEffortPartyAssignment com = new DeleteWorkEffortPartyAssignment(workEffortPartyAssignmentId);

		int usedTicketId;

		synchronized (WorkEffortPartyAssignmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortPartyAssignmentDeleted.class,
				event -> sendWorkEffortPartyAssignmentChangedMessage(((WorkEffortPartyAssignmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortPartyAssignmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortPartyAssignment/\" plus one of the following: "
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
