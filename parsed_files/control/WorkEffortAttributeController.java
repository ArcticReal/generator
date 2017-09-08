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
import com.skytala.eCommerce.command.AddWorkEffortAttribute;
import com.skytala.eCommerce.command.DeleteWorkEffortAttribute;
import com.skytala.eCommerce.command.UpdateWorkEffortAttribute;
import com.skytala.eCommerce.entity.WorkEffortAttribute;
import com.skytala.eCommerce.entity.WorkEffortAttributeMapper;
import com.skytala.eCommerce.event.WorkEffortAttributeAdded;
import com.skytala.eCommerce.event.WorkEffortAttributeDeleted;
import com.skytala.eCommerce.event.WorkEffortAttributeFound;
import com.skytala.eCommerce.event.WorkEffortAttributeUpdated;
import com.skytala.eCommerce.query.FindWorkEffortAttributesBy;

@RestController
@RequestMapping("/api/workEffortAttribute")
public class WorkEffortAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortAttribute
	 * @return a List with the WorkEffortAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortAttribute> findWorkEffortAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortAttributesBy query = new FindWorkEffortAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAttributeFound.class,
				event -> sendWorkEffortAttributesFoundMessage(((WorkEffortAttributeFound) event).getWorkEffortAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortAttributesFoundMessage(List<WorkEffortAttribute> workEffortAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortAttributes);
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
	public boolean createWorkEffortAttribute(HttpServletRequest request) {

		WorkEffortAttribute workEffortAttributeToBeAdded = new WorkEffortAttribute();
		try {
			workEffortAttributeToBeAdded = WorkEffortAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortAttribute(workEffortAttributeToBeAdded);

	}

	/**
	 * creates a new WorkEffortAttribute entry in the ofbiz database
	 * 
	 * @param workEffortAttributeToBeAdded
	 *            the WorkEffortAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortAttribute(WorkEffortAttribute workEffortAttributeToBeAdded) {

		AddWorkEffortAttribute com = new AddWorkEffortAttribute(workEffortAttributeToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAttributeAdded.class,
				event -> sendWorkEffortAttributeChangedMessage(((WorkEffortAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortAttribute(HttpServletRequest request) {

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

		WorkEffortAttribute workEffortAttributeToBeUpdated = new WorkEffortAttribute();

		try {
			workEffortAttributeToBeUpdated = WorkEffortAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortAttribute(workEffortAttributeToBeUpdated);

	}

	/**
	 * Updates the WorkEffortAttribute with the specific Id
	 * 
	 * @param workEffortAttributeToBeUpdated the WorkEffortAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortAttribute(WorkEffortAttribute workEffortAttributeToBeUpdated) {

		UpdateWorkEffortAttribute com = new UpdateWorkEffortAttribute(workEffortAttributeToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAttributeUpdated.class,
				event -> sendWorkEffortAttributeChangedMessage(((WorkEffortAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortAttribute from the database
	 * 
	 * @param workEffortAttributeId:
	 *            the id of the WorkEffortAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortAttributeById(@RequestParam(value = "workEffortAttributeId") String workEffortAttributeId) {

		DeleteWorkEffortAttribute com = new DeleteWorkEffortAttribute(workEffortAttributeId);

		int usedTicketId;

		synchronized (WorkEffortAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAttributeDeleted.class,
				event -> sendWorkEffortAttributeChangedMessage(((WorkEffortAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortAttribute/\" plus one of the following: "
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
