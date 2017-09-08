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
import com.skytala.eCommerce.command.AddWorkEffortSkillStandard;
import com.skytala.eCommerce.command.DeleteWorkEffortSkillStandard;
import com.skytala.eCommerce.command.UpdateWorkEffortSkillStandard;
import com.skytala.eCommerce.entity.WorkEffortSkillStandard;
import com.skytala.eCommerce.entity.WorkEffortSkillStandardMapper;
import com.skytala.eCommerce.event.WorkEffortSkillStandardAdded;
import com.skytala.eCommerce.event.WorkEffortSkillStandardDeleted;
import com.skytala.eCommerce.event.WorkEffortSkillStandardFound;
import com.skytala.eCommerce.event.WorkEffortSkillStandardUpdated;
import com.skytala.eCommerce.query.FindWorkEffortSkillStandardsBy;

@RestController
@RequestMapping("/api/workEffortSkillStandard")
public class WorkEffortSkillStandardController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortSkillStandard>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortSkillStandardController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortSkillStandard
	 * @return a List with the WorkEffortSkillStandards
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortSkillStandard> findWorkEffortSkillStandardsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortSkillStandardsBy query = new FindWorkEffortSkillStandardsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortSkillStandardController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSkillStandardFound.class,
				event -> sendWorkEffortSkillStandardsFoundMessage(((WorkEffortSkillStandardFound) event).getWorkEffortSkillStandards(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortSkillStandardsFoundMessage(List<WorkEffortSkillStandard> workEffortSkillStandards, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortSkillStandards);
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
	public boolean createWorkEffortSkillStandard(HttpServletRequest request) {

		WorkEffortSkillStandard workEffortSkillStandardToBeAdded = new WorkEffortSkillStandard();
		try {
			workEffortSkillStandardToBeAdded = WorkEffortSkillStandardMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortSkillStandard(workEffortSkillStandardToBeAdded);

	}

	/**
	 * creates a new WorkEffortSkillStandard entry in the ofbiz database
	 * 
	 * @param workEffortSkillStandardToBeAdded
	 *            the WorkEffortSkillStandard thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortSkillStandard(WorkEffortSkillStandard workEffortSkillStandardToBeAdded) {

		AddWorkEffortSkillStandard com = new AddWorkEffortSkillStandard(workEffortSkillStandardToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortSkillStandardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSkillStandardAdded.class,
				event -> sendWorkEffortSkillStandardChangedMessage(((WorkEffortSkillStandardAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortSkillStandard(HttpServletRequest request) {

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

		WorkEffortSkillStandard workEffortSkillStandardToBeUpdated = new WorkEffortSkillStandard();

		try {
			workEffortSkillStandardToBeUpdated = WorkEffortSkillStandardMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortSkillStandard(workEffortSkillStandardToBeUpdated);

	}

	/**
	 * Updates the WorkEffortSkillStandard with the specific Id
	 * 
	 * @param workEffortSkillStandardToBeUpdated the WorkEffortSkillStandard thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortSkillStandard(WorkEffortSkillStandard workEffortSkillStandardToBeUpdated) {

		UpdateWorkEffortSkillStandard com = new UpdateWorkEffortSkillStandard(workEffortSkillStandardToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortSkillStandardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSkillStandardUpdated.class,
				event -> sendWorkEffortSkillStandardChangedMessage(((WorkEffortSkillStandardUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortSkillStandard from the database
	 * 
	 * @param workEffortSkillStandardId:
	 *            the id of the WorkEffortSkillStandard thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortSkillStandardById(@RequestParam(value = "workEffortSkillStandardId") String workEffortSkillStandardId) {

		DeleteWorkEffortSkillStandard com = new DeleteWorkEffortSkillStandard(workEffortSkillStandardId);

		int usedTicketId;

		synchronized (WorkEffortSkillStandardController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSkillStandardDeleted.class,
				event -> sendWorkEffortSkillStandardChangedMessage(((WorkEffortSkillStandardDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortSkillStandardChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortSkillStandard/\" plus one of the following: "
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
