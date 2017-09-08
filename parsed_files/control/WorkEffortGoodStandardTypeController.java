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
import com.skytala.eCommerce.command.AddWorkEffortGoodStandardType;
import com.skytala.eCommerce.command.DeleteWorkEffortGoodStandardType;
import com.skytala.eCommerce.command.UpdateWorkEffortGoodStandardType;
import com.skytala.eCommerce.entity.WorkEffortGoodStandardType;
import com.skytala.eCommerce.entity.WorkEffortGoodStandardTypeMapper;
import com.skytala.eCommerce.event.WorkEffortGoodStandardTypeAdded;
import com.skytala.eCommerce.event.WorkEffortGoodStandardTypeDeleted;
import com.skytala.eCommerce.event.WorkEffortGoodStandardTypeFound;
import com.skytala.eCommerce.event.WorkEffortGoodStandardTypeUpdated;
import com.skytala.eCommerce.query.FindWorkEffortGoodStandardTypesBy;

@RestController
@RequestMapping("/api/workEffortGoodStandardType")
public class WorkEffortGoodStandardTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortGoodStandardType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortGoodStandardTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortGoodStandardType
	 * @return a List with the WorkEffortGoodStandardTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortGoodStandardType> findWorkEffortGoodStandardTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortGoodStandardTypesBy query = new FindWorkEffortGoodStandardTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortGoodStandardTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortGoodStandardTypeFound.class,
				event -> sendWorkEffortGoodStandardTypesFoundMessage(((WorkEffortGoodStandardTypeFound) event).getWorkEffortGoodStandardTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortGoodStandardTypesFoundMessage(List<WorkEffortGoodStandardType> workEffortGoodStandardTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortGoodStandardTypes);
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
	public boolean createWorkEffortGoodStandardType(HttpServletRequest request) {

		WorkEffortGoodStandardType workEffortGoodStandardTypeToBeAdded = new WorkEffortGoodStandardType();
		try {
			workEffortGoodStandardTypeToBeAdded = WorkEffortGoodStandardTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortGoodStandardType(workEffortGoodStandardTypeToBeAdded);

	}

	/**
	 * creates a new WorkEffortGoodStandardType entry in the ofbiz database
	 * 
	 * @param workEffortGoodStandardTypeToBeAdded
	 *            the WorkEffortGoodStandardType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortGoodStandardType(WorkEffortGoodStandardType workEffortGoodStandardTypeToBeAdded) {

		AddWorkEffortGoodStandardType com = new AddWorkEffortGoodStandardType(workEffortGoodStandardTypeToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortGoodStandardTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortGoodStandardTypeAdded.class,
				event -> sendWorkEffortGoodStandardTypeChangedMessage(((WorkEffortGoodStandardTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortGoodStandardType(HttpServletRequest request) {

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

		WorkEffortGoodStandardType workEffortGoodStandardTypeToBeUpdated = new WorkEffortGoodStandardType();

		try {
			workEffortGoodStandardTypeToBeUpdated = WorkEffortGoodStandardTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortGoodStandardType(workEffortGoodStandardTypeToBeUpdated);

	}

	/**
	 * Updates the WorkEffortGoodStandardType with the specific Id
	 * 
	 * @param workEffortGoodStandardTypeToBeUpdated the WorkEffortGoodStandardType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortGoodStandardType(WorkEffortGoodStandardType workEffortGoodStandardTypeToBeUpdated) {

		UpdateWorkEffortGoodStandardType com = new UpdateWorkEffortGoodStandardType(workEffortGoodStandardTypeToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortGoodStandardTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortGoodStandardTypeUpdated.class,
				event -> sendWorkEffortGoodStandardTypeChangedMessage(((WorkEffortGoodStandardTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortGoodStandardType from the database
	 * 
	 * @param workEffortGoodStandardTypeId:
	 *            the id of the WorkEffortGoodStandardType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortGoodStandardTypeById(@RequestParam(value = "workEffortGoodStandardTypeId") String workEffortGoodStandardTypeId) {

		DeleteWorkEffortGoodStandardType com = new DeleteWorkEffortGoodStandardType(workEffortGoodStandardTypeId);

		int usedTicketId;

		synchronized (WorkEffortGoodStandardTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortGoodStandardTypeDeleted.class,
				event -> sendWorkEffortGoodStandardTypeChangedMessage(((WorkEffortGoodStandardTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortGoodStandardTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortGoodStandardType/\" plus one of the following: "
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
