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
import com.skytala.eCommerce.command.AddWorkEffortInventoryAssign;
import com.skytala.eCommerce.command.DeleteWorkEffortInventoryAssign;
import com.skytala.eCommerce.command.UpdateWorkEffortInventoryAssign;
import com.skytala.eCommerce.entity.WorkEffortInventoryAssign;
import com.skytala.eCommerce.entity.WorkEffortInventoryAssignMapper;
import com.skytala.eCommerce.event.WorkEffortInventoryAssignAdded;
import com.skytala.eCommerce.event.WorkEffortInventoryAssignDeleted;
import com.skytala.eCommerce.event.WorkEffortInventoryAssignFound;
import com.skytala.eCommerce.event.WorkEffortInventoryAssignUpdated;
import com.skytala.eCommerce.query.FindWorkEffortInventoryAssignsBy;

@RestController
@RequestMapping("/api/workEffortInventoryAssign")
public class WorkEffortInventoryAssignController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortInventoryAssign>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortInventoryAssignController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortInventoryAssign
	 * @return a List with the WorkEffortInventoryAssigns
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortInventoryAssign> findWorkEffortInventoryAssignsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortInventoryAssignsBy query = new FindWorkEffortInventoryAssignsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortInventoryAssignController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortInventoryAssignFound.class,
				event -> sendWorkEffortInventoryAssignsFoundMessage(((WorkEffortInventoryAssignFound) event).getWorkEffortInventoryAssigns(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortInventoryAssignsFoundMessage(List<WorkEffortInventoryAssign> workEffortInventoryAssigns, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortInventoryAssigns);
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
	public boolean createWorkEffortInventoryAssign(HttpServletRequest request) {

		WorkEffortInventoryAssign workEffortInventoryAssignToBeAdded = new WorkEffortInventoryAssign();
		try {
			workEffortInventoryAssignToBeAdded = WorkEffortInventoryAssignMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortInventoryAssign(workEffortInventoryAssignToBeAdded);

	}

	/**
	 * creates a new WorkEffortInventoryAssign entry in the ofbiz database
	 * 
	 * @param workEffortInventoryAssignToBeAdded
	 *            the WorkEffortInventoryAssign thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortInventoryAssign(WorkEffortInventoryAssign workEffortInventoryAssignToBeAdded) {

		AddWorkEffortInventoryAssign com = new AddWorkEffortInventoryAssign(workEffortInventoryAssignToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortInventoryAssignController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortInventoryAssignAdded.class,
				event -> sendWorkEffortInventoryAssignChangedMessage(((WorkEffortInventoryAssignAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortInventoryAssign(HttpServletRequest request) {

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

		WorkEffortInventoryAssign workEffortInventoryAssignToBeUpdated = new WorkEffortInventoryAssign();

		try {
			workEffortInventoryAssignToBeUpdated = WorkEffortInventoryAssignMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortInventoryAssign(workEffortInventoryAssignToBeUpdated);

	}

	/**
	 * Updates the WorkEffortInventoryAssign with the specific Id
	 * 
	 * @param workEffortInventoryAssignToBeUpdated the WorkEffortInventoryAssign thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortInventoryAssign(WorkEffortInventoryAssign workEffortInventoryAssignToBeUpdated) {

		UpdateWorkEffortInventoryAssign com = new UpdateWorkEffortInventoryAssign(workEffortInventoryAssignToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortInventoryAssignController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortInventoryAssignUpdated.class,
				event -> sendWorkEffortInventoryAssignChangedMessage(((WorkEffortInventoryAssignUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortInventoryAssign from the database
	 * 
	 * @param workEffortInventoryAssignId:
	 *            the id of the WorkEffortInventoryAssign thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortInventoryAssignById(@RequestParam(value = "workEffortInventoryAssignId") String workEffortInventoryAssignId) {

		DeleteWorkEffortInventoryAssign com = new DeleteWorkEffortInventoryAssign(workEffortInventoryAssignId);

		int usedTicketId;

		synchronized (WorkEffortInventoryAssignController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortInventoryAssignDeleted.class,
				event -> sendWorkEffortInventoryAssignChangedMessage(((WorkEffortInventoryAssignDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortInventoryAssignChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortInventoryAssign/\" plus one of the following: "
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
