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
import com.skytala.eCommerce.command.AddWorkEffortGoodStandard;
import com.skytala.eCommerce.command.DeleteWorkEffortGoodStandard;
import com.skytala.eCommerce.command.UpdateWorkEffortGoodStandard;
import com.skytala.eCommerce.entity.WorkEffortGoodStandard;
import com.skytala.eCommerce.entity.WorkEffortGoodStandardMapper;
import com.skytala.eCommerce.event.WorkEffortGoodStandardAdded;
import com.skytala.eCommerce.event.WorkEffortGoodStandardDeleted;
import com.skytala.eCommerce.event.WorkEffortGoodStandardFound;
import com.skytala.eCommerce.event.WorkEffortGoodStandardUpdated;
import com.skytala.eCommerce.query.FindWorkEffortGoodStandardsBy;

@RestController
@RequestMapping("/api/workEffortGoodStandard")
public class WorkEffortGoodStandardController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortGoodStandard>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortGoodStandardController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortGoodStandard
	 * @return a List with the WorkEffortGoodStandards
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortGoodStandard> findWorkEffortGoodStandardsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortGoodStandardsBy query = new FindWorkEffortGoodStandardsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortGoodStandardController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortGoodStandardFound.class,
				event -> sendWorkEffortGoodStandardsFoundMessage(((WorkEffortGoodStandardFound) event).getWorkEffortGoodStandards(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortGoodStandardsFoundMessage(List<WorkEffortGoodStandard> workEffortGoodStandards, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortGoodStandards);
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
	public boolean createWorkEffortGoodStandard(HttpServletRequest request) {

		WorkEffortGoodStandard workEffortGoodStandardToBeAdded = new WorkEffortGoodStandard();
		try {
			workEffortGoodStandardToBeAdded = WorkEffortGoodStandardMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortGoodStandard(workEffortGoodStandardToBeAdded);

	}

	/**
	 * creates a new WorkEffortGoodStandard entry in the ofbiz database
	 * 
	 * @param workEffortGoodStandardToBeAdded
	 *            the WorkEffortGoodStandard thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortGoodStandard(WorkEffortGoodStandard workEffortGoodStandardToBeAdded) {

		AddWorkEffortGoodStandard com = new AddWorkEffortGoodStandard(workEffortGoodStandardToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortGoodStandardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortGoodStandardAdded.class,
				event -> sendWorkEffortGoodStandardChangedMessage(((WorkEffortGoodStandardAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortGoodStandard(HttpServletRequest request) {

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

		WorkEffortGoodStandard workEffortGoodStandardToBeUpdated = new WorkEffortGoodStandard();

		try {
			workEffortGoodStandardToBeUpdated = WorkEffortGoodStandardMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortGoodStandard(workEffortGoodStandardToBeUpdated);

	}

	/**
	 * Updates the WorkEffortGoodStandard with the specific Id
	 * 
	 * @param workEffortGoodStandardToBeUpdated the WorkEffortGoodStandard thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortGoodStandard(WorkEffortGoodStandard workEffortGoodStandardToBeUpdated) {

		UpdateWorkEffortGoodStandard com = new UpdateWorkEffortGoodStandard(workEffortGoodStandardToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortGoodStandardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortGoodStandardUpdated.class,
				event -> sendWorkEffortGoodStandardChangedMessage(((WorkEffortGoodStandardUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortGoodStandard from the database
	 * 
	 * @param workEffortGoodStandardId:
	 *            the id of the WorkEffortGoodStandard thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortGoodStandardById(@RequestParam(value = "workEffortGoodStandardId") String workEffortGoodStandardId) {

		DeleteWorkEffortGoodStandard com = new DeleteWorkEffortGoodStandard(workEffortGoodStandardId);

		int usedTicketId;

		synchronized (WorkEffortGoodStandardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortGoodStandardDeleted.class,
				event -> sendWorkEffortGoodStandardChangedMessage(((WorkEffortGoodStandardDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortGoodStandardChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortGoodStandard/\" plus one of the following: "
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
