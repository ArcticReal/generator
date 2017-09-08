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
import com.skytala.eCommerce.command.AddWorkEffortContactMech;
import com.skytala.eCommerce.command.DeleteWorkEffortContactMech;
import com.skytala.eCommerce.command.UpdateWorkEffortContactMech;
import com.skytala.eCommerce.entity.WorkEffortContactMech;
import com.skytala.eCommerce.entity.WorkEffortContactMechMapper;
import com.skytala.eCommerce.event.WorkEffortContactMechAdded;
import com.skytala.eCommerce.event.WorkEffortContactMechDeleted;
import com.skytala.eCommerce.event.WorkEffortContactMechFound;
import com.skytala.eCommerce.event.WorkEffortContactMechUpdated;
import com.skytala.eCommerce.query.FindWorkEffortContactMechsBy;

@RestController
@RequestMapping("/api/workEffortContactMech")
public class WorkEffortContactMechController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortContactMech>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortContactMechController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortContactMech
	 * @return a List with the WorkEffortContactMechs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortContactMech> findWorkEffortContactMechsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortContactMechsBy query = new FindWorkEffortContactMechsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortContactMechController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContactMechFound.class,
				event -> sendWorkEffortContactMechsFoundMessage(((WorkEffortContactMechFound) event).getWorkEffortContactMechs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortContactMechsFoundMessage(List<WorkEffortContactMech> workEffortContactMechs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortContactMechs);
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
	public boolean createWorkEffortContactMech(HttpServletRequest request) {

		WorkEffortContactMech workEffortContactMechToBeAdded = new WorkEffortContactMech();
		try {
			workEffortContactMechToBeAdded = WorkEffortContactMechMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortContactMech(workEffortContactMechToBeAdded);

	}

	/**
	 * creates a new WorkEffortContactMech entry in the ofbiz database
	 * 
	 * @param workEffortContactMechToBeAdded
	 *            the WorkEffortContactMech thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortContactMech(WorkEffortContactMech workEffortContactMechToBeAdded) {

		AddWorkEffortContactMech com = new AddWorkEffortContactMech(workEffortContactMechToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContactMechAdded.class,
				event -> sendWorkEffortContactMechChangedMessage(((WorkEffortContactMechAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortContactMech(HttpServletRequest request) {

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

		WorkEffortContactMech workEffortContactMechToBeUpdated = new WorkEffortContactMech();

		try {
			workEffortContactMechToBeUpdated = WorkEffortContactMechMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortContactMech(workEffortContactMechToBeUpdated);

	}

	/**
	 * Updates the WorkEffortContactMech with the specific Id
	 * 
	 * @param workEffortContactMechToBeUpdated the WorkEffortContactMech thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortContactMech(WorkEffortContactMech workEffortContactMechToBeUpdated) {

		UpdateWorkEffortContactMech com = new UpdateWorkEffortContactMech(workEffortContactMechToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContactMechUpdated.class,
				event -> sendWorkEffortContactMechChangedMessage(((WorkEffortContactMechUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortContactMech from the database
	 * 
	 * @param workEffortContactMechId:
	 *            the id of the WorkEffortContactMech thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortContactMechById(@RequestParam(value = "workEffortContactMechId") String workEffortContactMechId) {

		DeleteWorkEffortContactMech com = new DeleteWorkEffortContactMech(workEffortContactMechId);

		int usedTicketId;

		synchronized (WorkEffortContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContactMechDeleted.class,
				event -> sendWorkEffortContactMechChangedMessage(((WorkEffortContactMechDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortContactMechChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortContactMech/\" plus one of the following: "
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
