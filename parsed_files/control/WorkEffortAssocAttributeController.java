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
import com.skytala.eCommerce.command.AddWorkEffortAssocAttribute;
import com.skytala.eCommerce.command.DeleteWorkEffortAssocAttribute;
import com.skytala.eCommerce.command.UpdateWorkEffortAssocAttribute;
import com.skytala.eCommerce.entity.WorkEffortAssocAttribute;
import com.skytala.eCommerce.entity.WorkEffortAssocAttributeMapper;
import com.skytala.eCommerce.event.WorkEffortAssocAttributeAdded;
import com.skytala.eCommerce.event.WorkEffortAssocAttributeDeleted;
import com.skytala.eCommerce.event.WorkEffortAssocAttributeFound;
import com.skytala.eCommerce.event.WorkEffortAssocAttributeUpdated;
import com.skytala.eCommerce.query.FindWorkEffortAssocAttributesBy;

@RestController
@RequestMapping("/api/workEffortAssocAttribute")
public class WorkEffortAssocAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortAssocAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortAssocAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortAssocAttribute
	 * @return a List with the WorkEffortAssocAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortAssocAttribute> findWorkEffortAssocAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortAssocAttributesBy query = new FindWorkEffortAssocAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortAssocAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocAttributeFound.class,
				event -> sendWorkEffortAssocAttributesFoundMessage(((WorkEffortAssocAttributeFound) event).getWorkEffortAssocAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortAssocAttributesFoundMessage(List<WorkEffortAssocAttribute> workEffortAssocAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortAssocAttributes);
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
	public boolean createWorkEffortAssocAttribute(HttpServletRequest request) {

		WorkEffortAssocAttribute workEffortAssocAttributeToBeAdded = new WorkEffortAssocAttribute();
		try {
			workEffortAssocAttributeToBeAdded = WorkEffortAssocAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortAssocAttribute(workEffortAssocAttributeToBeAdded);

	}

	/**
	 * creates a new WorkEffortAssocAttribute entry in the ofbiz database
	 * 
	 * @param workEffortAssocAttributeToBeAdded
	 *            the WorkEffortAssocAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortAssocAttribute(WorkEffortAssocAttribute workEffortAssocAttributeToBeAdded) {

		AddWorkEffortAssocAttribute com = new AddWorkEffortAssocAttribute(workEffortAssocAttributeToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortAssocAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocAttributeAdded.class,
				event -> sendWorkEffortAssocAttributeChangedMessage(((WorkEffortAssocAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortAssocAttribute(HttpServletRequest request) {

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

		WorkEffortAssocAttribute workEffortAssocAttributeToBeUpdated = new WorkEffortAssocAttribute();

		try {
			workEffortAssocAttributeToBeUpdated = WorkEffortAssocAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortAssocAttribute(workEffortAssocAttributeToBeUpdated);

	}

	/**
	 * Updates the WorkEffortAssocAttribute with the specific Id
	 * 
	 * @param workEffortAssocAttributeToBeUpdated the WorkEffortAssocAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortAssocAttribute(WorkEffortAssocAttribute workEffortAssocAttributeToBeUpdated) {

		UpdateWorkEffortAssocAttribute com = new UpdateWorkEffortAssocAttribute(workEffortAssocAttributeToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortAssocAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocAttributeUpdated.class,
				event -> sendWorkEffortAssocAttributeChangedMessage(((WorkEffortAssocAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortAssocAttribute from the database
	 * 
	 * @param workEffortAssocAttributeId:
	 *            the id of the WorkEffortAssocAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortAssocAttributeById(@RequestParam(value = "workEffortAssocAttributeId") String workEffortAssocAttributeId) {

		DeleteWorkEffortAssocAttribute com = new DeleteWorkEffortAssocAttribute(workEffortAssocAttributeId);

		int usedTicketId;

		synchronized (WorkEffortAssocAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocAttributeDeleted.class,
				event -> sendWorkEffortAssocAttributeChangedMessage(((WorkEffortAssocAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortAssocAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortAssocAttribute/\" plus one of the following: "
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
