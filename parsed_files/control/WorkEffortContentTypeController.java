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
import com.skytala.eCommerce.command.AddWorkEffortContentType;
import com.skytala.eCommerce.command.DeleteWorkEffortContentType;
import com.skytala.eCommerce.command.UpdateWorkEffortContentType;
import com.skytala.eCommerce.entity.WorkEffortContentType;
import com.skytala.eCommerce.entity.WorkEffortContentTypeMapper;
import com.skytala.eCommerce.event.WorkEffortContentTypeAdded;
import com.skytala.eCommerce.event.WorkEffortContentTypeDeleted;
import com.skytala.eCommerce.event.WorkEffortContentTypeFound;
import com.skytala.eCommerce.event.WorkEffortContentTypeUpdated;
import com.skytala.eCommerce.query.FindWorkEffortContentTypesBy;

@RestController
@RequestMapping("/api/workEffortContentType")
public class WorkEffortContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortContentType
	 * @return a List with the WorkEffortContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortContentType> findWorkEffortContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortContentTypesBy query = new FindWorkEffortContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContentTypeFound.class,
				event -> sendWorkEffortContentTypesFoundMessage(((WorkEffortContentTypeFound) event).getWorkEffortContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortContentTypesFoundMessage(List<WorkEffortContentType> workEffortContentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortContentTypes);
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
	public boolean createWorkEffortContentType(HttpServletRequest request) {

		WorkEffortContentType workEffortContentTypeToBeAdded = new WorkEffortContentType();
		try {
			workEffortContentTypeToBeAdded = WorkEffortContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortContentType(workEffortContentTypeToBeAdded);

	}

	/**
	 * creates a new WorkEffortContentType entry in the ofbiz database
	 * 
	 * @param workEffortContentTypeToBeAdded
	 *            the WorkEffortContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortContentType(WorkEffortContentType workEffortContentTypeToBeAdded) {

		AddWorkEffortContentType com = new AddWorkEffortContentType(workEffortContentTypeToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContentTypeAdded.class,
				event -> sendWorkEffortContentTypeChangedMessage(((WorkEffortContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortContentType(HttpServletRequest request) {

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

		WorkEffortContentType workEffortContentTypeToBeUpdated = new WorkEffortContentType();

		try {
			workEffortContentTypeToBeUpdated = WorkEffortContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortContentType(workEffortContentTypeToBeUpdated);

	}

	/**
	 * Updates the WorkEffortContentType with the specific Id
	 * 
	 * @param workEffortContentTypeToBeUpdated the WorkEffortContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortContentType(WorkEffortContentType workEffortContentTypeToBeUpdated) {

		UpdateWorkEffortContentType com = new UpdateWorkEffortContentType(workEffortContentTypeToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContentTypeUpdated.class,
				event -> sendWorkEffortContentTypeChangedMessage(((WorkEffortContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortContentType from the database
	 * 
	 * @param workEffortContentTypeId:
	 *            the id of the WorkEffortContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortContentTypeById(@RequestParam(value = "workEffortContentTypeId") String workEffortContentTypeId) {

		DeleteWorkEffortContentType com = new DeleteWorkEffortContentType(workEffortContentTypeId);

		int usedTicketId;

		synchronized (WorkEffortContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContentTypeDeleted.class,
				event -> sendWorkEffortContentTypeChangedMessage(((WorkEffortContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortContentType/\" plus one of the following: "
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
