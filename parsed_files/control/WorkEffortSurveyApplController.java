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
import com.skytala.eCommerce.command.AddWorkEffortSurveyAppl;
import com.skytala.eCommerce.command.DeleteWorkEffortSurveyAppl;
import com.skytala.eCommerce.command.UpdateWorkEffortSurveyAppl;
import com.skytala.eCommerce.entity.WorkEffortSurveyAppl;
import com.skytala.eCommerce.entity.WorkEffortSurveyApplMapper;
import com.skytala.eCommerce.event.WorkEffortSurveyApplAdded;
import com.skytala.eCommerce.event.WorkEffortSurveyApplDeleted;
import com.skytala.eCommerce.event.WorkEffortSurveyApplFound;
import com.skytala.eCommerce.event.WorkEffortSurveyApplUpdated;
import com.skytala.eCommerce.query.FindWorkEffortSurveyApplsBy;

@RestController
@RequestMapping("/api/workEffortSurveyAppl")
public class WorkEffortSurveyApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortSurveyAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortSurveyApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortSurveyAppl
	 * @return a List with the WorkEffortSurveyAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortSurveyAppl> findWorkEffortSurveyApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortSurveyApplsBy query = new FindWorkEffortSurveyApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortSurveyApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSurveyApplFound.class,
				event -> sendWorkEffortSurveyApplsFoundMessage(((WorkEffortSurveyApplFound) event).getWorkEffortSurveyAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortSurveyApplsFoundMessage(List<WorkEffortSurveyAppl> workEffortSurveyAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortSurveyAppls);
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
	public boolean createWorkEffortSurveyAppl(HttpServletRequest request) {

		WorkEffortSurveyAppl workEffortSurveyApplToBeAdded = new WorkEffortSurveyAppl();
		try {
			workEffortSurveyApplToBeAdded = WorkEffortSurveyApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortSurveyAppl(workEffortSurveyApplToBeAdded);

	}

	/**
	 * creates a new WorkEffortSurveyAppl entry in the ofbiz database
	 * 
	 * @param workEffortSurveyApplToBeAdded
	 *            the WorkEffortSurveyAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortSurveyAppl(WorkEffortSurveyAppl workEffortSurveyApplToBeAdded) {

		AddWorkEffortSurveyAppl com = new AddWorkEffortSurveyAppl(workEffortSurveyApplToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortSurveyApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSurveyApplAdded.class,
				event -> sendWorkEffortSurveyApplChangedMessage(((WorkEffortSurveyApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortSurveyAppl(HttpServletRequest request) {

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

		WorkEffortSurveyAppl workEffortSurveyApplToBeUpdated = new WorkEffortSurveyAppl();

		try {
			workEffortSurveyApplToBeUpdated = WorkEffortSurveyApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortSurveyAppl(workEffortSurveyApplToBeUpdated);

	}

	/**
	 * Updates the WorkEffortSurveyAppl with the specific Id
	 * 
	 * @param workEffortSurveyApplToBeUpdated the WorkEffortSurveyAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortSurveyAppl(WorkEffortSurveyAppl workEffortSurveyApplToBeUpdated) {

		UpdateWorkEffortSurveyAppl com = new UpdateWorkEffortSurveyAppl(workEffortSurveyApplToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortSurveyApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSurveyApplUpdated.class,
				event -> sendWorkEffortSurveyApplChangedMessage(((WorkEffortSurveyApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortSurveyAppl from the database
	 * 
	 * @param workEffortSurveyApplId:
	 *            the id of the WorkEffortSurveyAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortSurveyApplById(@RequestParam(value = "workEffortSurveyApplId") String workEffortSurveyApplId) {

		DeleteWorkEffortSurveyAppl com = new DeleteWorkEffortSurveyAppl(workEffortSurveyApplId);

		int usedTicketId;

		synchronized (WorkEffortSurveyApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSurveyApplDeleted.class,
				event -> sendWorkEffortSurveyApplChangedMessage(((WorkEffortSurveyApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortSurveyApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortSurveyAppl/\" plus one of the following: "
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