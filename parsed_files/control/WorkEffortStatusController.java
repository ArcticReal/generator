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
import com.skytala.eCommerce.command.AddWorkEffortStatus;
import com.skytala.eCommerce.command.DeleteWorkEffortStatus;
import com.skytala.eCommerce.command.UpdateWorkEffortStatus;
import com.skytala.eCommerce.entity.WorkEffortStatus;
import com.skytala.eCommerce.entity.WorkEffortStatusMapper;
import com.skytala.eCommerce.event.WorkEffortStatusAdded;
import com.skytala.eCommerce.event.WorkEffortStatusDeleted;
import com.skytala.eCommerce.event.WorkEffortStatusFound;
import com.skytala.eCommerce.event.WorkEffortStatusUpdated;
import com.skytala.eCommerce.query.FindWorkEffortStatussBy;

@RestController
@RequestMapping("/api/workEffortStatus")
public class WorkEffortStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortStatus
	 * @return a List with the WorkEffortStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortStatus> findWorkEffortStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortStatussBy query = new FindWorkEffortStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortStatusFound.class,
				event -> sendWorkEffortStatussFoundMessage(((WorkEffortStatusFound) event).getWorkEffortStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortStatussFoundMessage(List<WorkEffortStatus> workEffortStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortStatuss);
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
	public boolean createWorkEffortStatus(HttpServletRequest request) {

		WorkEffortStatus workEffortStatusToBeAdded = new WorkEffortStatus();
		try {
			workEffortStatusToBeAdded = WorkEffortStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortStatus(workEffortStatusToBeAdded);

	}

	/**
	 * creates a new WorkEffortStatus entry in the ofbiz database
	 * 
	 * @param workEffortStatusToBeAdded
	 *            the WorkEffortStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortStatus(WorkEffortStatus workEffortStatusToBeAdded) {

		AddWorkEffortStatus com = new AddWorkEffortStatus(workEffortStatusToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortStatusAdded.class,
				event -> sendWorkEffortStatusChangedMessage(((WorkEffortStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortStatus(HttpServletRequest request) {

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

		WorkEffortStatus workEffortStatusToBeUpdated = new WorkEffortStatus();

		try {
			workEffortStatusToBeUpdated = WorkEffortStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortStatus(workEffortStatusToBeUpdated);

	}

	/**
	 * Updates the WorkEffortStatus with the specific Id
	 * 
	 * @param workEffortStatusToBeUpdated the WorkEffortStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortStatus(WorkEffortStatus workEffortStatusToBeUpdated) {

		UpdateWorkEffortStatus com = new UpdateWorkEffortStatus(workEffortStatusToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortStatusUpdated.class,
				event -> sendWorkEffortStatusChangedMessage(((WorkEffortStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortStatus from the database
	 * 
	 * @param workEffortStatusId:
	 *            the id of the WorkEffortStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortStatusById(@RequestParam(value = "workEffortStatusId") String workEffortStatusId) {

		DeleteWorkEffortStatus com = new DeleteWorkEffortStatus(workEffortStatusId);

		int usedTicketId;

		synchronized (WorkEffortStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortStatusDeleted.class,
				event -> sendWorkEffortStatusChangedMessage(((WorkEffortStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortStatus/\" plus one of the following: "
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
