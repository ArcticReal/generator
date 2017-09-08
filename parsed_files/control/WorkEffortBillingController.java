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
import com.skytala.eCommerce.command.AddWorkEffortBilling;
import com.skytala.eCommerce.command.DeleteWorkEffortBilling;
import com.skytala.eCommerce.command.UpdateWorkEffortBilling;
import com.skytala.eCommerce.entity.WorkEffortBilling;
import com.skytala.eCommerce.entity.WorkEffortBillingMapper;
import com.skytala.eCommerce.event.WorkEffortBillingAdded;
import com.skytala.eCommerce.event.WorkEffortBillingDeleted;
import com.skytala.eCommerce.event.WorkEffortBillingFound;
import com.skytala.eCommerce.event.WorkEffortBillingUpdated;
import com.skytala.eCommerce.query.FindWorkEffortBillingsBy;

@RestController
@RequestMapping("/api/workEffortBilling")
public class WorkEffortBillingController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortBilling>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortBillingController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortBilling
	 * @return a List with the WorkEffortBillings
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortBilling> findWorkEffortBillingsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortBillingsBy query = new FindWorkEffortBillingsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortBillingController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortBillingFound.class,
				event -> sendWorkEffortBillingsFoundMessage(((WorkEffortBillingFound) event).getWorkEffortBillings(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortBillingsFoundMessage(List<WorkEffortBilling> workEffortBillings, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortBillings);
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
	public boolean createWorkEffortBilling(HttpServletRequest request) {

		WorkEffortBilling workEffortBillingToBeAdded = new WorkEffortBilling();
		try {
			workEffortBillingToBeAdded = WorkEffortBillingMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortBilling(workEffortBillingToBeAdded);

	}

	/**
	 * creates a new WorkEffortBilling entry in the ofbiz database
	 * 
	 * @param workEffortBillingToBeAdded
	 *            the WorkEffortBilling thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortBilling(WorkEffortBilling workEffortBillingToBeAdded) {

		AddWorkEffortBilling com = new AddWorkEffortBilling(workEffortBillingToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortBillingAdded.class,
				event -> sendWorkEffortBillingChangedMessage(((WorkEffortBillingAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortBilling(HttpServletRequest request) {

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

		WorkEffortBilling workEffortBillingToBeUpdated = new WorkEffortBilling();

		try {
			workEffortBillingToBeUpdated = WorkEffortBillingMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortBilling(workEffortBillingToBeUpdated);

	}

	/**
	 * Updates the WorkEffortBilling with the specific Id
	 * 
	 * @param workEffortBillingToBeUpdated the WorkEffortBilling thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortBilling(WorkEffortBilling workEffortBillingToBeUpdated) {

		UpdateWorkEffortBilling com = new UpdateWorkEffortBilling(workEffortBillingToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortBillingUpdated.class,
				event -> sendWorkEffortBillingChangedMessage(((WorkEffortBillingUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortBilling from the database
	 * 
	 * @param workEffortBillingId:
	 *            the id of the WorkEffortBilling thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortBillingById(@RequestParam(value = "workEffortBillingId") String workEffortBillingId) {

		DeleteWorkEffortBilling com = new DeleteWorkEffortBilling(workEffortBillingId);

		int usedTicketId;

		synchronized (WorkEffortBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortBillingDeleted.class,
				event -> sendWorkEffortBillingChangedMessage(((WorkEffortBillingDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortBillingChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortBilling/\" plus one of the following: "
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
