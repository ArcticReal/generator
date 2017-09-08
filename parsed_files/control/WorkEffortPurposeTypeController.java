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
import com.skytala.eCommerce.command.AddWorkEffortPurposeType;
import com.skytala.eCommerce.command.DeleteWorkEffortPurposeType;
import com.skytala.eCommerce.command.UpdateWorkEffortPurposeType;
import com.skytala.eCommerce.entity.WorkEffortPurposeType;
import com.skytala.eCommerce.entity.WorkEffortPurposeTypeMapper;
import com.skytala.eCommerce.event.WorkEffortPurposeTypeAdded;
import com.skytala.eCommerce.event.WorkEffortPurposeTypeDeleted;
import com.skytala.eCommerce.event.WorkEffortPurposeTypeFound;
import com.skytala.eCommerce.event.WorkEffortPurposeTypeUpdated;
import com.skytala.eCommerce.query.FindWorkEffortPurposeTypesBy;

@RestController
@RequestMapping("/api/workEffortPurposeType")
public class WorkEffortPurposeTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortPurposeType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortPurposeTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortPurposeType
	 * @return a List with the WorkEffortPurposeTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortPurposeType> findWorkEffortPurposeTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortPurposeTypesBy query = new FindWorkEffortPurposeTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortPurposeTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortPurposeTypeFound.class,
				event -> sendWorkEffortPurposeTypesFoundMessage(((WorkEffortPurposeTypeFound) event).getWorkEffortPurposeTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortPurposeTypesFoundMessage(List<WorkEffortPurposeType> workEffortPurposeTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortPurposeTypes);
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
	public boolean createWorkEffortPurposeType(HttpServletRequest request) {

		WorkEffortPurposeType workEffortPurposeTypeToBeAdded = new WorkEffortPurposeType();
		try {
			workEffortPurposeTypeToBeAdded = WorkEffortPurposeTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortPurposeType(workEffortPurposeTypeToBeAdded);

	}

	/**
	 * creates a new WorkEffortPurposeType entry in the ofbiz database
	 * 
	 * @param workEffortPurposeTypeToBeAdded
	 *            the WorkEffortPurposeType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortPurposeType(WorkEffortPurposeType workEffortPurposeTypeToBeAdded) {

		AddWorkEffortPurposeType com = new AddWorkEffortPurposeType(workEffortPurposeTypeToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortPurposeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortPurposeTypeAdded.class,
				event -> sendWorkEffortPurposeTypeChangedMessage(((WorkEffortPurposeTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortPurposeType(HttpServletRequest request) {

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

		WorkEffortPurposeType workEffortPurposeTypeToBeUpdated = new WorkEffortPurposeType();

		try {
			workEffortPurposeTypeToBeUpdated = WorkEffortPurposeTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortPurposeType(workEffortPurposeTypeToBeUpdated);

	}

	/**
	 * Updates the WorkEffortPurposeType with the specific Id
	 * 
	 * @param workEffortPurposeTypeToBeUpdated the WorkEffortPurposeType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortPurposeType(WorkEffortPurposeType workEffortPurposeTypeToBeUpdated) {

		UpdateWorkEffortPurposeType com = new UpdateWorkEffortPurposeType(workEffortPurposeTypeToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortPurposeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortPurposeTypeUpdated.class,
				event -> sendWorkEffortPurposeTypeChangedMessage(((WorkEffortPurposeTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortPurposeType from the database
	 * 
	 * @param workEffortPurposeTypeId:
	 *            the id of the WorkEffortPurposeType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortPurposeTypeById(@RequestParam(value = "workEffortPurposeTypeId") String workEffortPurposeTypeId) {

		DeleteWorkEffortPurposeType com = new DeleteWorkEffortPurposeType(workEffortPurposeTypeId);

		int usedTicketId;

		synchronized (WorkEffortPurposeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortPurposeTypeDeleted.class,
				event -> sendWorkEffortPurposeTypeChangedMessage(((WorkEffortPurposeTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortPurposeTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortPurposeType/\" plus one of the following: "
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
