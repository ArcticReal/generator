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
import com.skytala.eCommerce.command.AddWorkEffortSearchConstraint;
import com.skytala.eCommerce.command.DeleteWorkEffortSearchConstraint;
import com.skytala.eCommerce.command.UpdateWorkEffortSearchConstraint;
import com.skytala.eCommerce.entity.WorkEffortSearchConstraint;
import com.skytala.eCommerce.entity.WorkEffortSearchConstraintMapper;
import com.skytala.eCommerce.event.WorkEffortSearchConstraintAdded;
import com.skytala.eCommerce.event.WorkEffortSearchConstraintDeleted;
import com.skytala.eCommerce.event.WorkEffortSearchConstraintFound;
import com.skytala.eCommerce.event.WorkEffortSearchConstraintUpdated;
import com.skytala.eCommerce.query.FindWorkEffortSearchConstraintsBy;

@RestController
@RequestMapping("/api/workEffortSearchConstraint")
public class WorkEffortSearchConstraintController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortSearchConstraint>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortSearchConstraintController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortSearchConstraint
	 * @return a List with the WorkEffortSearchConstraints
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortSearchConstraint> findWorkEffortSearchConstraintsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortSearchConstraintsBy query = new FindWorkEffortSearchConstraintsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortSearchConstraintController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSearchConstraintFound.class,
				event -> sendWorkEffortSearchConstraintsFoundMessage(((WorkEffortSearchConstraintFound) event).getWorkEffortSearchConstraints(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortSearchConstraintsFoundMessage(List<WorkEffortSearchConstraint> workEffortSearchConstraints, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortSearchConstraints);
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
	public boolean createWorkEffortSearchConstraint(HttpServletRequest request) {

		WorkEffortSearchConstraint workEffortSearchConstraintToBeAdded = new WorkEffortSearchConstraint();
		try {
			workEffortSearchConstraintToBeAdded = WorkEffortSearchConstraintMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortSearchConstraint(workEffortSearchConstraintToBeAdded);

	}

	/**
	 * creates a new WorkEffortSearchConstraint entry in the ofbiz database
	 * 
	 * @param workEffortSearchConstraintToBeAdded
	 *            the WorkEffortSearchConstraint thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortSearchConstraint(WorkEffortSearchConstraint workEffortSearchConstraintToBeAdded) {

		AddWorkEffortSearchConstraint com = new AddWorkEffortSearchConstraint(workEffortSearchConstraintToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortSearchConstraintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSearchConstraintAdded.class,
				event -> sendWorkEffortSearchConstraintChangedMessage(((WorkEffortSearchConstraintAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortSearchConstraint(HttpServletRequest request) {

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

		WorkEffortSearchConstraint workEffortSearchConstraintToBeUpdated = new WorkEffortSearchConstraint();

		try {
			workEffortSearchConstraintToBeUpdated = WorkEffortSearchConstraintMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortSearchConstraint(workEffortSearchConstraintToBeUpdated);

	}

	/**
	 * Updates the WorkEffortSearchConstraint with the specific Id
	 * 
	 * @param workEffortSearchConstraintToBeUpdated the WorkEffortSearchConstraint thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortSearchConstraint(WorkEffortSearchConstraint workEffortSearchConstraintToBeUpdated) {

		UpdateWorkEffortSearchConstraint com = new UpdateWorkEffortSearchConstraint(workEffortSearchConstraintToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortSearchConstraintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSearchConstraintUpdated.class,
				event -> sendWorkEffortSearchConstraintChangedMessage(((WorkEffortSearchConstraintUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortSearchConstraint from the database
	 * 
	 * @param workEffortSearchConstraintId:
	 *            the id of the WorkEffortSearchConstraint thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortSearchConstraintById(@RequestParam(value = "workEffortSearchConstraintId") String workEffortSearchConstraintId) {

		DeleteWorkEffortSearchConstraint com = new DeleteWorkEffortSearchConstraint(workEffortSearchConstraintId);

		int usedTicketId;

		synchronized (WorkEffortSearchConstraintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSearchConstraintDeleted.class,
				event -> sendWorkEffortSearchConstraintChangedMessage(((WorkEffortSearchConstraintDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortSearchConstraintChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortSearchConstraint/\" plus one of the following: "
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
