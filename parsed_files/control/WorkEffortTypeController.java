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
import com.skytala.eCommerce.command.AddWorkEffortType;
import com.skytala.eCommerce.command.DeleteWorkEffortType;
import com.skytala.eCommerce.command.UpdateWorkEffortType;
import com.skytala.eCommerce.entity.WorkEffortType;
import com.skytala.eCommerce.entity.WorkEffortTypeMapper;
import com.skytala.eCommerce.event.WorkEffortTypeAdded;
import com.skytala.eCommerce.event.WorkEffortTypeDeleted;
import com.skytala.eCommerce.event.WorkEffortTypeFound;
import com.skytala.eCommerce.event.WorkEffortTypeUpdated;
import com.skytala.eCommerce.query.FindWorkEffortTypesBy;

@RestController
@RequestMapping("/api/workEffortType")
public class WorkEffortTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortType
	 * @return a List with the WorkEffortTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortType> findWorkEffortTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortTypesBy query = new FindWorkEffortTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTypeFound.class,
				event -> sendWorkEffortTypesFoundMessage(((WorkEffortTypeFound) event).getWorkEffortTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortTypesFoundMessage(List<WorkEffortType> workEffortTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortTypes);
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
	public boolean createWorkEffortType(HttpServletRequest request) {

		WorkEffortType workEffortTypeToBeAdded = new WorkEffortType();
		try {
			workEffortTypeToBeAdded = WorkEffortTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortType(workEffortTypeToBeAdded);

	}

	/**
	 * creates a new WorkEffortType entry in the ofbiz database
	 * 
	 * @param workEffortTypeToBeAdded
	 *            the WorkEffortType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortType(WorkEffortType workEffortTypeToBeAdded) {

		AddWorkEffortType com = new AddWorkEffortType(workEffortTypeToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTypeAdded.class,
				event -> sendWorkEffortTypeChangedMessage(((WorkEffortTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortType(HttpServletRequest request) {

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

		WorkEffortType workEffortTypeToBeUpdated = new WorkEffortType();

		try {
			workEffortTypeToBeUpdated = WorkEffortTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortType(workEffortTypeToBeUpdated);

	}

	/**
	 * Updates the WorkEffortType with the specific Id
	 * 
	 * @param workEffortTypeToBeUpdated the WorkEffortType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortType(WorkEffortType workEffortTypeToBeUpdated) {

		UpdateWorkEffortType com = new UpdateWorkEffortType(workEffortTypeToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTypeUpdated.class,
				event -> sendWorkEffortTypeChangedMessage(((WorkEffortTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortType from the database
	 * 
	 * @param workEffortTypeId:
	 *            the id of the WorkEffortType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortTypeById(@RequestParam(value = "workEffortTypeId") String workEffortTypeId) {

		DeleteWorkEffortType com = new DeleteWorkEffortType(workEffortTypeId);

		int usedTicketId;

		synchronized (WorkEffortTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTypeDeleted.class,
				event -> sendWorkEffortTypeChangedMessage(((WorkEffortTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortType/\" plus one of the following: "
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
