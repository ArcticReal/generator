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
import com.skytala.eCommerce.command.AddWorkEffortFixedAssetAssign;
import com.skytala.eCommerce.command.DeleteWorkEffortFixedAssetAssign;
import com.skytala.eCommerce.command.UpdateWorkEffortFixedAssetAssign;
import com.skytala.eCommerce.entity.WorkEffortFixedAssetAssign;
import com.skytala.eCommerce.entity.WorkEffortFixedAssetAssignMapper;
import com.skytala.eCommerce.event.WorkEffortFixedAssetAssignAdded;
import com.skytala.eCommerce.event.WorkEffortFixedAssetAssignDeleted;
import com.skytala.eCommerce.event.WorkEffortFixedAssetAssignFound;
import com.skytala.eCommerce.event.WorkEffortFixedAssetAssignUpdated;
import com.skytala.eCommerce.query.FindWorkEffortFixedAssetAssignsBy;

@RestController
@RequestMapping("/api/workEffortFixedAssetAssign")
public class WorkEffortFixedAssetAssignController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortFixedAssetAssign>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortFixedAssetAssignController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortFixedAssetAssign
	 * @return a List with the WorkEffortFixedAssetAssigns
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortFixedAssetAssign> findWorkEffortFixedAssetAssignsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortFixedAssetAssignsBy query = new FindWorkEffortFixedAssetAssignsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortFixedAssetAssignController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortFixedAssetAssignFound.class,
				event -> sendWorkEffortFixedAssetAssignsFoundMessage(((WorkEffortFixedAssetAssignFound) event).getWorkEffortFixedAssetAssigns(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortFixedAssetAssignsFoundMessage(List<WorkEffortFixedAssetAssign> workEffortFixedAssetAssigns, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortFixedAssetAssigns);
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
	public boolean createWorkEffortFixedAssetAssign(HttpServletRequest request) {

		WorkEffortFixedAssetAssign workEffortFixedAssetAssignToBeAdded = new WorkEffortFixedAssetAssign();
		try {
			workEffortFixedAssetAssignToBeAdded = WorkEffortFixedAssetAssignMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortFixedAssetAssign(workEffortFixedAssetAssignToBeAdded);

	}

	/**
	 * creates a new WorkEffortFixedAssetAssign entry in the ofbiz database
	 * 
	 * @param workEffortFixedAssetAssignToBeAdded
	 *            the WorkEffortFixedAssetAssign thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortFixedAssetAssign(WorkEffortFixedAssetAssign workEffortFixedAssetAssignToBeAdded) {

		AddWorkEffortFixedAssetAssign com = new AddWorkEffortFixedAssetAssign(workEffortFixedAssetAssignToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortFixedAssetAssignController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortFixedAssetAssignAdded.class,
				event -> sendWorkEffortFixedAssetAssignChangedMessage(((WorkEffortFixedAssetAssignAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortFixedAssetAssign(HttpServletRequest request) {

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

		WorkEffortFixedAssetAssign workEffortFixedAssetAssignToBeUpdated = new WorkEffortFixedAssetAssign();

		try {
			workEffortFixedAssetAssignToBeUpdated = WorkEffortFixedAssetAssignMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortFixedAssetAssign(workEffortFixedAssetAssignToBeUpdated);

	}

	/**
	 * Updates the WorkEffortFixedAssetAssign with the specific Id
	 * 
	 * @param workEffortFixedAssetAssignToBeUpdated the WorkEffortFixedAssetAssign thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortFixedAssetAssign(WorkEffortFixedAssetAssign workEffortFixedAssetAssignToBeUpdated) {

		UpdateWorkEffortFixedAssetAssign com = new UpdateWorkEffortFixedAssetAssign(workEffortFixedAssetAssignToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortFixedAssetAssignController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortFixedAssetAssignUpdated.class,
				event -> sendWorkEffortFixedAssetAssignChangedMessage(((WorkEffortFixedAssetAssignUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortFixedAssetAssign from the database
	 * 
	 * @param workEffortFixedAssetAssignId:
	 *            the id of the WorkEffortFixedAssetAssign thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortFixedAssetAssignById(@RequestParam(value = "workEffortFixedAssetAssignId") String workEffortFixedAssetAssignId) {

		DeleteWorkEffortFixedAssetAssign com = new DeleteWorkEffortFixedAssetAssign(workEffortFixedAssetAssignId);

		int usedTicketId;

		synchronized (WorkEffortFixedAssetAssignController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortFixedAssetAssignDeleted.class,
				event -> sendWorkEffortFixedAssetAssignChangedMessage(((WorkEffortFixedAssetAssignDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortFixedAssetAssignChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortFixedAssetAssign/\" plus one of the following: "
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
