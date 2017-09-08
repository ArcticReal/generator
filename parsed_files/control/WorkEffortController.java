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
import com.skytala.eCommerce.command.AddWorkEffort;
import com.skytala.eCommerce.command.DeleteWorkEffort;
import com.skytala.eCommerce.command.UpdateWorkEffort;
import com.skytala.eCommerce.entity.WorkEffort;
import com.skytala.eCommerce.entity.WorkEffortMapper;
import com.skytala.eCommerce.event.WorkEffortAdded;
import com.skytala.eCommerce.event.WorkEffortDeleted;
import com.skytala.eCommerce.event.WorkEffortFound;
import com.skytala.eCommerce.event.WorkEffortUpdated;
import com.skytala.eCommerce.query.FindWorkEffortsBy;

@RestController
@RequestMapping("/api/workEffort")
public class WorkEffortController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffort>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffort
	 * @return a List with the WorkEfforts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffort> findWorkEffortsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortsBy query = new FindWorkEffortsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortFound.class,
				event -> sendWorkEffortsFoundMessage(((WorkEffortFound) event).getWorkEfforts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortsFoundMessage(List<WorkEffort> workEfforts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEfforts);
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
	public boolean createWorkEffort(HttpServletRequest request) {

		WorkEffort workEffortToBeAdded = new WorkEffort();
		try {
			workEffortToBeAdded = WorkEffortMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffort(workEffortToBeAdded);

	}

	/**
	 * creates a new WorkEffort entry in the ofbiz database
	 * 
	 * @param workEffortToBeAdded
	 *            the WorkEffort thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffort(WorkEffort workEffortToBeAdded) {

		AddWorkEffort com = new AddWorkEffort(workEffortToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAdded.class,
				event -> sendWorkEffortChangedMessage(((WorkEffortAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffort(HttpServletRequest request) {

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

		WorkEffort workEffortToBeUpdated = new WorkEffort();

		try {
			workEffortToBeUpdated = WorkEffortMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffort(workEffortToBeUpdated);

	}

	/**
	 * Updates the WorkEffort with the specific Id
	 * 
	 * @param workEffortToBeUpdated the WorkEffort thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffort(WorkEffort workEffortToBeUpdated) {

		UpdateWorkEffort com = new UpdateWorkEffort(workEffortToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortUpdated.class,
				event -> sendWorkEffortChangedMessage(((WorkEffortUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffort from the database
	 * 
	 * @param workEffortId:
	 *            the id of the WorkEffort thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortById(@RequestParam(value = "workEffortId") String workEffortId) {

		DeleteWorkEffort com = new DeleteWorkEffort(workEffortId);

		int usedTicketId;

		synchronized (WorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortDeleted.class,
				event -> sendWorkEffortChangedMessage(((WorkEffortDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffort/\" plus one of the following: "
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
