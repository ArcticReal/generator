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
import com.skytala.eCommerce.command.AddWorkEffortDeliverableProd;
import com.skytala.eCommerce.command.DeleteWorkEffortDeliverableProd;
import com.skytala.eCommerce.command.UpdateWorkEffortDeliverableProd;
import com.skytala.eCommerce.entity.WorkEffortDeliverableProd;
import com.skytala.eCommerce.entity.WorkEffortDeliverableProdMapper;
import com.skytala.eCommerce.event.WorkEffortDeliverableProdAdded;
import com.skytala.eCommerce.event.WorkEffortDeliverableProdDeleted;
import com.skytala.eCommerce.event.WorkEffortDeliverableProdFound;
import com.skytala.eCommerce.event.WorkEffortDeliverableProdUpdated;
import com.skytala.eCommerce.query.FindWorkEffortDeliverableProdsBy;

@RestController
@RequestMapping("/api/workEffortDeliverableProd")
public class WorkEffortDeliverableProdController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortDeliverableProd>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortDeliverableProdController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortDeliverableProd
	 * @return a List with the WorkEffortDeliverableProds
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortDeliverableProd> findWorkEffortDeliverableProdsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortDeliverableProdsBy query = new FindWorkEffortDeliverableProdsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortDeliverableProdController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortDeliverableProdFound.class,
				event -> sendWorkEffortDeliverableProdsFoundMessage(((WorkEffortDeliverableProdFound) event).getWorkEffortDeliverableProds(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortDeliverableProdsFoundMessage(List<WorkEffortDeliverableProd> workEffortDeliverableProds, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortDeliverableProds);
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
	public boolean createWorkEffortDeliverableProd(HttpServletRequest request) {

		WorkEffortDeliverableProd workEffortDeliverableProdToBeAdded = new WorkEffortDeliverableProd();
		try {
			workEffortDeliverableProdToBeAdded = WorkEffortDeliverableProdMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortDeliverableProd(workEffortDeliverableProdToBeAdded);

	}

	/**
	 * creates a new WorkEffortDeliverableProd entry in the ofbiz database
	 * 
	 * @param workEffortDeliverableProdToBeAdded
	 *            the WorkEffortDeliverableProd thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortDeliverableProd(WorkEffortDeliverableProd workEffortDeliverableProdToBeAdded) {

		AddWorkEffortDeliverableProd com = new AddWorkEffortDeliverableProd(workEffortDeliverableProdToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortDeliverableProdController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortDeliverableProdAdded.class,
				event -> sendWorkEffortDeliverableProdChangedMessage(((WorkEffortDeliverableProdAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortDeliverableProd(HttpServletRequest request) {

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

		WorkEffortDeliverableProd workEffortDeliverableProdToBeUpdated = new WorkEffortDeliverableProd();

		try {
			workEffortDeliverableProdToBeUpdated = WorkEffortDeliverableProdMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortDeliverableProd(workEffortDeliverableProdToBeUpdated);

	}

	/**
	 * Updates the WorkEffortDeliverableProd with the specific Id
	 * 
	 * @param workEffortDeliverableProdToBeUpdated the WorkEffortDeliverableProd thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortDeliverableProd(WorkEffortDeliverableProd workEffortDeliverableProdToBeUpdated) {

		UpdateWorkEffortDeliverableProd com = new UpdateWorkEffortDeliverableProd(workEffortDeliverableProdToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortDeliverableProdController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortDeliverableProdUpdated.class,
				event -> sendWorkEffortDeliverableProdChangedMessage(((WorkEffortDeliverableProdUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortDeliverableProd from the database
	 * 
	 * @param workEffortDeliverableProdId:
	 *            the id of the WorkEffortDeliverableProd thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortDeliverableProdById(@RequestParam(value = "workEffortDeliverableProdId") String workEffortDeliverableProdId) {

		DeleteWorkEffortDeliverableProd com = new DeleteWorkEffortDeliverableProd(workEffortDeliverableProdId);

		int usedTicketId;

		synchronized (WorkEffortDeliverableProdController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortDeliverableProdDeleted.class,
				event -> sendWorkEffortDeliverableProdChangedMessage(((WorkEffortDeliverableProdDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortDeliverableProdChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortDeliverableProd/\" plus one of the following: "
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
