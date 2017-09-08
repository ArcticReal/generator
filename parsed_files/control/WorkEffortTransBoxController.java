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
import com.skytala.eCommerce.command.AddWorkEffortTransBox;
import com.skytala.eCommerce.command.DeleteWorkEffortTransBox;
import com.skytala.eCommerce.command.UpdateWorkEffortTransBox;
import com.skytala.eCommerce.entity.WorkEffortTransBox;
import com.skytala.eCommerce.entity.WorkEffortTransBoxMapper;
import com.skytala.eCommerce.event.WorkEffortTransBoxAdded;
import com.skytala.eCommerce.event.WorkEffortTransBoxDeleted;
import com.skytala.eCommerce.event.WorkEffortTransBoxFound;
import com.skytala.eCommerce.event.WorkEffortTransBoxUpdated;
import com.skytala.eCommerce.query.FindWorkEffortTransBoxsBy;

@RestController
@RequestMapping("/api/workEffortTransBox")
public class WorkEffortTransBoxController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortTransBox>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortTransBoxController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortTransBox
	 * @return a List with the WorkEffortTransBoxs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortTransBox> findWorkEffortTransBoxsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortTransBoxsBy query = new FindWorkEffortTransBoxsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortTransBoxController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTransBoxFound.class,
				event -> sendWorkEffortTransBoxsFoundMessage(((WorkEffortTransBoxFound) event).getWorkEffortTransBoxs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortTransBoxsFoundMessage(List<WorkEffortTransBox> workEffortTransBoxs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortTransBoxs);
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
	public boolean createWorkEffortTransBox(HttpServletRequest request) {

		WorkEffortTransBox workEffortTransBoxToBeAdded = new WorkEffortTransBox();
		try {
			workEffortTransBoxToBeAdded = WorkEffortTransBoxMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortTransBox(workEffortTransBoxToBeAdded);

	}

	/**
	 * creates a new WorkEffortTransBox entry in the ofbiz database
	 * 
	 * @param workEffortTransBoxToBeAdded
	 *            the WorkEffortTransBox thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortTransBox(WorkEffortTransBox workEffortTransBoxToBeAdded) {

		AddWorkEffortTransBox com = new AddWorkEffortTransBox(workEffortTransBoxToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortTransBoxController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTransBoxAdded.class,
				event -> sendWorkEffortTransBoxChangedMessage(((WorkEffortTransBoxAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortTransBox(HttpServletRequest request) {

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

		WorkEffortTransBox workEffortTransBoxToBeUpdated = new WorkEffortTransBox();

		try {
			workEffortTransBoxToBeUpdated = WorkEffortTransBoxMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortTransBox(workEffortTransBoxToBeUpdated);

	}

	/**
	 * Updates the WorkEffortTransBox with the specific Id
	 * 
	 * @param workEffortTransBoxToBeUpdated the WorkEffortTransBox thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortTransBox(WorkEffortTransBox workEffortTransBoxToBeUpdated) {

		UpdateWorkEffortTransBox com = new UpdateWorkEffortTransBox(workEffortTransBoxToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortTransBoxController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTransBoxUpdated.class,
				event -> sendWorkEffortTransBoxChangedMessage(((WorkEffortTransBoxUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortTransBox from the database
	 * 
	 * @param workEffortTransBoxId:
	 *            the id of the WorkEffortTransBox thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortTransBoxById(@RequestParam(value = "workEffortTransBoxId") String workEffortTransBoxId) {

		DeleteWorkEffortTransBox com = new DeleteWorkEffortTransBox(workEffortTransBoxId);

		int usedTicketId;

		synchronized (WorkEffortTransBoxController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTransBoxDeleted.class,
				event -> sendWorkEffortTransBoxChangedMessage(((WorkEffortTransBoxDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortTransBoxChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortTransBox/\" plus one of the following: "
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
