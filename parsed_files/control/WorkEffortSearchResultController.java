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
import com.skytala.eCommerce.command.AddWorkEffortSearchResult;
import com.skytala.eCommerce.command.DeleteWorkEffortSearchResult;
import com.skytala.eCommerce.command.UpdateWorkEffortSearchResult;
import com.skytala.eCommerce.entity.WorkEffortSearchResult;
import com.skytala.eCommerce.entity.WorkEffortSearchResultMapper;
import com.skytala.eCommerce.event.WorkEffortSearchResultAdded;
import com.skytala.eCommerce.event.WorkEffortSearchResultDeleted;
import com.skytala.eCommerce.event.WorkEffortSearchResultFound;
import com.skytala.eCommerce.event.WorkEffortSearchResultUpdated;
import com.skytala.eCommerce.query.FindWorkEffortSearchResultsBy;

@RestController
@RequestMapping("/api/workEffortSearchResult")
public class WorkEffortSearchResultController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortSearchResult>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortSearchResultController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortSearchResult
	 * @return a List with the WorkEffortSearchResults
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortSearchResult> findWorkEffortSearchResultsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortSearchResultsBy query = new FindWorkEffortSearchResultsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortSearchResultController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSearchResultFound.class,
				event -> sendWorkEffortSearchResultsFoundMessage(((WorkEffortSearchResultFound) event).getWorkEffortSearchResults(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortSearchResultsFoundMessage(List<WorkEffortSearchResult> workEffortSearchResults, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortSearchResults);
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
	public boolean createWorkEffortSearchResult(HttpServletRequest request) {

		WorkEffortSearchResult workEffortSearchResultToBeAdded = new WorkEffortSearchResult();
		try {
			workEffortSearchResultToBeAdded = WorkEffortSearchResultMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortSearchResult(workEffortSearchResultToBeAdded);

	}

	/**
	 * creates a new WorkEffortSearchResult entry in the ofbiz database
	 * 
	 * @param workEffortSearchResultToBeAdded
	 *            the WorkEffortSearchResult thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortSearchResult(WorkEffortSearchResult workEffortSearchResultToBeAdded) {

		AddWorkEffortSearchResult com = new AddWorkEffortSearchResult(workEffortSearchResultToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortSearchResultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSearchResultAdded.class,
				event -> sendWorkEffortSearchResultChangedMessage(((WorkEffortSearchResultAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortSearchResult(HttpServletRequest request) {

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

		WorkEffortSearchResult workEffortSearchResultToBeUpdated = new WorkEffortSearchResult();

		try {
			workEffortSearchResultToBeUpdated = WorkEffortSearchResultMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortSearchResult(workEffortSearchResultToBeUpdated);

	}

	/**
	 * Updates the WorkEffortSearchResult with the specific Id
	 * 
	 * @param workEffortSearchResultToBeUpdated the WorkEffortSearchResult thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortSearchResult(WorkEffortSearchResult workEffortSearchResultToBeUpdated) {

		UpdateWorkEffortSearchResult com = new UpdateWorkEffortSearchResult(workEffortSearchResultToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortSearchResultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSearchResultUpdated.class,
				event -> sendWorkEffortSearchResultChangedMessage(((WorkEffortSearchResultUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortSearchResult from the database
	 * 
	 * @param workEffortSearchResultId:
	 *            the id of the WorkEffortSearchResult thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortSearchResultById(@RequestParam(value = "workEffortSearchResultId") String workEffortSearchResultId) {

		DeleteWorkEffortSearchResult com = new DeleteWorkEffortSearchResult(workEffortSearchResultId);

		int usedTicketId;

		synchronized (WorkEffortSearchResultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortSearchResultDeleted.class,
				event -> sendWorkEffortSearchResultChangedMessage(((WorkEffortSearchResultDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortSearchResultChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortSearchResult/\" plus one of the following: "
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
