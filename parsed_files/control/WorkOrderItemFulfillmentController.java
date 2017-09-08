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
import com.skytala.eCommerce.command.AddWorkOrderItemFulfillment;
import com.skytala.eCommerce.command.DeleteWorkOrderItemFulfillment;
import com.skytala.eCommerce.command.UpdateWorkOrderItemFulfillment;
import com.skytala.eCommerce.entity.WorkOrderItemFulfillment;
import com.skytala.eCommerce.entity.WorkOrderItemFulfillmentMapper;
import com.skytala.eCommerce.event.WorkOrderItemFulfillmentAdded;
import com.skytala.eCommerce.event.WorkOrderItemFulfillmentDeleted;
import com.skytala.eCommerce.event.WorkOrderItemFulfillmentFound;
import com.skytala.eCommerce.event.WorkOrderItemFulfillmentUpdated;
import com.skytala.eCommerce.query.FindWorkOrderItemFulfillmentsBy;

@RestController
@RequestMapping("/api/workOrderItemFulfillment")
public class WorkOrderItemFulfillmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkOrderItemFulfillment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkOrderItemFulfillmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkOrderItemFulfillment
	 * @return a List with the WorkOrderItemFulfillments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkOrderItemFulfillment> findWorkOrderItemFulfillmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkOrderItemFulfillmentsBy query = new FindWorkOrderItemFulfillmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkOrderItemFulfillmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkOrderItemFulfillmentFound.class,
				event -> sendWorkOrderItemFulfillmentsFoundMessage(((WorkOrderItemFulfillmentFound) event).getWorkOrderItemFulfillments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkOrderItemFulfillmentsFoundMessage(List<WorkOrderItemFulfillment> workOrderItemFulfillments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workOrderItemFulfillments);
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
	public boolean createWorkOrderItemFulfillment(HttpServletRequest request) {

		WorkOrderItemFulfillment workOrderItemFulfillmentToBeAdded = new WorkOrderItemFulfillment();
		try {
			workOrderItemFulfillmentToBeAdded = WorkOrderItemFulfillmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkOrderItemFulfillment(workOrderItemFulfillmentToBeAdded);

	}

	/**
	 * creates a new WorkOrderItemFulfillment entry in the ofbiz database
	 * 
	 * @param workOrderItemFulfillmentToBeAdded
	 *            the WorkOrderItemFulfillment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkOrderItemFulfillment(WorkOrderItemFulfillment workOrderItemFulfillmentToBeAdded) {

		AddWorkOrderItemFulfillment com = new AddWorkOrderItemFulfillment(workOrderItemFulfillmentToBeAdded);
		int usedTicketId;

		synchronized (WorkOrderItemFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkOrderItemFulfillmentAdded.class,
				event -> sendWorkOrderItemFulfillmentChangedMessage(((WorkOrderItemFulfillmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkOrderItemFulfillment(HttpServletRequest request) {

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

		WorkOrderItemFulfillment workOrderItemFulfillmentToBeUpdated = new WorkOrderItemFulfillment();

		try {
			workOrderItemFulfillmentToBeUpdated = WorkOrderItemFulfillmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkOrderItemFulfillment(workOrderItemFulfillmentToBeUpdated);

	}

	/**
	 * Updates the WorkOrderItemFulfillment with the specific Id
	 * 
	 * @param workOrderItemFulfillmentToBeUpdated the WorkOrderItemFulfillment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkOrderItemFulfillment(WorkOrderItemFulfillment workOrderItemFulfillmentToBeUpdated) {

		UpdateWorkOrderItemFulfillment com = new UpdateWorkOrderItemFulfillment(workOrderItemFulfillmentToBeUpdated);

		int usedTicketId;

		synchronized (WorkOrderItemFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkOrderItemFulfillmentUpdated.class,
				event -> sendWorkOrderItemFulfillmentChangedMessage(((WorkOrderItemFulfillmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkOrderItemFulfillment from the database
	 * 
	 * @param workOrderItemFulfillmentId:
	 *            the id of the WorkOrderItemFulfillment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkOrderItemFulfillmentById(@RequestParam(value = "workOrderItemFulfillmentId") String workOrderItemFulfillmentId) {

		DeleteWorkOrderItemFulfillment com = new DeleteWorkOrderItemFulfillment(workOrderItemFulfillmentId);

		int usedTicketId;

		synchronized (WorkOrderItemFulfillmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkOrderItemFulfillmentDeleted.class,
				event -> sendWorkOrderItemFulfillmentChangedMessage(((WorkOrderItemFulfillmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkOrderItemFulfillmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workOrderItemFulfillment/\" plus one of the following: "
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
