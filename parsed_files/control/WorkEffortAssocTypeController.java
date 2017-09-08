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
import com.skytala.eCommerce.command.AddWorkEffortAssocType;
import com.skytala.eCommerce.command.DeleteWorkEffortAssocType;
import com.skytala.eCommerce.command.UpdateWorkEffortAssocType;
import com.skytala.eCommerce.entity.WorkEffortAssocType;
import com.skytala.eCommerce.entity.WorkEffortAssocTypeMapper;
import com.skytala.eCommerce.event.WorkEffortAssocTypeAdded;
import com.skytala.eCommerce.event.WorkEffortAssocTypeDeleted;
import com.skytala.eCommerce.event.WorkEffortAssocTypeFound;
import com.skytala.eCommerce.event.WorkEffortAssocTypeUpdated;
import com.skytala.eCommerce.query.FindWorkEffortAssocTypesBy;

@RestController
@RequestMapping("/api/workEffortAssocType")
public class WorkEffortAssocTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortAssocType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortAssocTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortAssocType
	 * @return a List with the WorkEffortAssocTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortAssocType> findWorkEffortAssocTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortAssocTypesBy query = new FindWorkEffortAssocTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortAssocTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocTypeFound.class,
				event -> sendWorkEffortAssocTypesFoundMessage(((WorkEffortAssocTypeFound) event).getWorkEffortAssocTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortAssocTypesFoundMessage(List<WorkEffortAssocType> workEffortAssocTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortAssocTypes);
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
	public boolean createWorkEffortAssocType(HttpServletRequest request) {

		WorkEffortAssocType workEffortAssocTypeToBeAdded = new WorkEffortAssocType();
		try {
			workEffortAssocTypeToBeAdded = WorkEffortAssocTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortAssocType(workEffortAssocTypeToBeAdded);

	}

	/**
	 * creates a new WorkEffortAssocType entry in the ofbiz database
	 * 
	 * @param workEffortAssocTypeToBeAdded
	 *            the WorkEffortAssocType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortAssocType(WorkEffortAssocType workEffortAssocTypeToBeAdded) {

		AddWorkEffortAssocType com = new AddWorkEffortAssocType(workEffortAssocTypeToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocTypeAdded.class,
				event -> sendWorkEffortAssocTypeChangedMessage(((WorkEffortAssocTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortAssocType(HttpServletRequest request) {

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

		WorkEffortAssocType workEffortAssocTypeToBeUpdated = new WorkEffortAssocType();

		try {
			workEffortAssocTypeToBeUpdated = WorkEffortAssocTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortAssocType(workEffortAssocTypeToBeUpdated);

	}

	/**
	 * Updates the WorkEffortAssocType with the specific Id
	 * 
	 * @param workEffortAssocTypeToBeUpdated the WorkEffortAssocType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortAssocType(WorkEffortAssocType workEffortAssocTypeToBeUpdated) {

		UpdateWorkEffortAssocType com = new UpdateWorkEffortAssocType(workEffortAssocTypeToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocTypeUpdated.class,
				event -> sendWorkEffortAssocTypeChangedMessage(((WorkEffortAssocTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortAssocType from the database
	 * 
	 * @param workEffortAssocTypeId:
	 *            the id of the WorkEffortAssocType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortAssocTypeById(@RequestParam(value = "workEffortAssocTypeId") String workEffortAssocTypeId) {

		DeleteWorkEffortAssocType com = new DeleteWorkEffortAssocType(workEffortAssocTypeId);

		int usedTicketId;

		synchronized (WorkEffortAssocTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocTypeDeleted.class,
				event -> sendWorkEffortAssocTypeChangedMessage(((WorkEffortAssocTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortAssocTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortAssocType/\" plus one of the following: "
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
