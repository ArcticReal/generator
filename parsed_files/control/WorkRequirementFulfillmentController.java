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
import com.skytala.eCommerce.command.AddWorkRequirementFulfillment;
import com.skytala.eCommerce.command.DeleteWorkRequirementFulfillment;
import com.skytala.eCommerce.command.UpdateWorkRequirementFulfillment;
import com.skytala.eCommerce.entity.WorkRequirementFulfillment;
import com.skytala.eCommerce.entity.WorkRequirementFulfillmentMapper;
import com.skytala.eCommerce.event.WorkRequirementFulfillmentAdded;
import com.skytala.eCommerce.event.WorkRequirementFulfillmentDeleted;
import com.skytala.eCommerce.event.WorkRequirementFulfillmentFound;
import com.skytala.eCommerce.event.WorkRequirementFulfillmentUpdated;
import com.skytala.eCommerce.query.FindWorkRequirementFulfillmentsBy;

@RestController
@RequestMapping("/api/workRequirementFulfillment")
public class WorkRequirementFulfillmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkRequirementFulfillment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkRequirementFulfillmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkRequirementFulfillment
	 * @return a List with the WorkRequirementFulfillments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkRequirementFulfillment> findWorkRequirementFulfillmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkRequirementFulfillmentsBy query = new FindWorkRequirementFulfillmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkRequirementFulfillmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkRequirementFulfillmentFound.class,
				event -> sendWorkRequirementFulfillmentsFoundMessage(((WorkRequirementFulfillmentFound) event).getWorkRequirementFulfillments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkRequirementFulfillmentsFoundMessage(List<WorkRequirementFulfillment> workRequirementFulfillments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workRequirementFulfillments);
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
	public boolean createWorkRequirementFulfillment(HttpServletRequest request) {

		WorkRequirementFulfillment workRequirementFulfillmentToBeAdded = new WorkRequirementFulfillment();
		try {
			workRequirementFulfillmentToBeAdded = WorkRequirementFulfillmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkRequirementFulfillment(workRequirementFulfillmentToBeAdded);

	}

	/**
	 * creates a new WorkRequirementFulfillment entry in the ofbiz database
	 * 
	 * @param workRequirementFulfillmentToBeAdded
	 *            the WorkRequirementFulfillment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkRequirementFulfillment(WorkRequirementFulfillment workRequirementFulfillmentToBeAdded) {

		AddWorkRequirementFulfillment com = new AddWorkRequirementFulfillment(workRequirementFulfillmentToBeAdded);
		int usedTicketId;

		synchronized (WorkRequirementFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkRequirementFulfillmentAdded.class,
				event -> sendWorkRequirementFulfillmentChangedMessage(((WorkRequirementFulfillmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkRequirementFulfillment(HttpServletRequest request) {

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

		WorkRequirementFulfillment workRequirementFulfillmentToBeUpdated = new WorkRequirementFulfillment();

		try {
			workRequirementFulfillmentToBeUpdated = WorkRequirementFulfillmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkRequirementFulfillment(workRequirementFulfillmentToBeUpdated);

	}

	/**
	 * Updates the WorkRequirementFulfillment with the specific Id
	 * 
	 * @param workRequirementFulfillmentToBeUpdated the WorkRequirementFulfillment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkRequirementFulfillment(WorkRequirementFulfillment workRequirementFulfillmentToBeUpdated) {

		UpdateWorkRequirementFulfillment com = new UpdateWorkRequirementFulfillment(workRequirementFulfillmentToBeUpdated);

		int usedTicketId;

		synchronized (WorkRequirementFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkRequirementFulfillmentUpdated.class,
				event -> sendWorkRequirementFulfillmentChangedMessage(((WorkRequirementFulfillmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkRequirementFulfillment from the database
	 * 
	 * @param workRequirementFulfillmentId:
	 *            the id of the WorkRequirementFulfillment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkRequirementFulfillmentById(@RequestParam(value = "workRequirementFulfillmentId") String workRequirementFulfillmentId) {

		DeleteWorkRequirementFulfillment com = new DeleteWorkRequirementFulfillment(workRequirementFulfillmentId);

		int usedTicketId;

		synchronized (WorkRequirementFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkRequirementFulfillmentDeleted.class,
				event -> sendWorkRequirementFulfillmentChangedMessage(((WorkRequirementFulfillmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkRequirementFulfillmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workRequirementFulfillment/\" plus one of the following: "
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
