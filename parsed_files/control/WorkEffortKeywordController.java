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
import com.skytala.eCommerce.command.AddWorkEffortKeyword;
import com.skytala.eCommerce.command.DeleteWorkEffortKeyword;
import com.skytala.eCommerce.command.UpdateWorkEffortKeyword;
import com.skytala.eCommerce.entity.WorkEffortKeyword;
import com.skytala.eCommerce.entity.WorkEffortKeywordMapper;
import com.skytala.eCommerce.event.WorkEffortKeywordAdded;
import com.skytala.eCommerce.event.WorkEffortKeywordDeleted;
import com.skytala.eCommerce.event.WorkEffortKeywordFound;
import com.skytala.eCommerce.event.WorkEffortKeywordUpdated;
import com.skytala.eCommerce.query.FindWorkEffortKeywordsBy;

@RestController
@RequestMapping("/api/workEffortKeyword")
public class WorkEffortKeywordController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortKeyword>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortKeywordController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortKeyword
	 * @return a List with the WorkEffortKeywords
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortKeyword> findWorkEffortKeywordsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortKeywordsBy query = new FindWorkEffortKeywordsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortKeywordController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortKeywordFound.class,
				event -> sendWorkEffortKeywordsFoundMessage(((WorkEffortKeywordFound) event).getWorkEffortKeywords(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortKeywordsFoundMessage(List<WorkEffortKeyword> workEffortKeywords, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortKeywords);
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
	public boolean createWorkEffortKeyword(HttpServletRequest request) {

		WorkEffortKeyword workEffortKeywordToBeAdded = new WorkEffortKeyword();
		try {
			workEffortKeywordToBeAdded = WorkEffortKeywordMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortKeyword(workEffortKeywordToBeAdded);

	}

	/**
	 * creates a new WorkEffortKeyword entry in the ofbiz database
	 * 
	 * @param workEffortKeywordToBeAdded
	 *            the WorkEffortKeyword thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortKeyword(WorkEffortKeyword workEffortKeywordToBeAdded) {

		AddWorkEffortKeyword com = new AddWorkEffortKeyword(workEffortKeywordToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortKeywordController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortKeywordAdded.class,
				event -> sendWorkEffortKeywordChangedMessage(((WorkEffortKeywordAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortKeyword(HttpServletRequest request) {

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

		WorkEffortKeyword workEffortKeywordToBeUpdated = new WorkEffortKeyword();

		try {
			workEffortKeywordToBeUpdated = WorkEffortKeywordMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortKeyword(workEffortKeywordToBeUpdated);

	}

	/**
	 * Updates the WorkEffortKeyword with the specific Id
	 * 
	 * @param workEffortKeywordToBeUpdated the WorkEffortKeyword thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortKeyword(WorkEffortKeyword workEffortKeywordToBeUpdated) {

		UpdateWorkEffortKeyword com = new UpdateWorkEffortKeyword(workEffortKeywordToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortKeywordController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortKeywordUpdated.class,
				event -> sendWorkEffortKeywordChangedMessage(((WorkEffortKeywordUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortKeyword from the database
	 * 
	 * @param workEffortKeywordId:
	 *            the id of the WorkEffortKeyword thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortKeywordById(@RequestParam(value = "workEffortKeywordId") String workEffortKeywordId) {

		DeleteWorkEffortKeyword com = new DeleteWorkEffortKeyword(workEffortKeywordId);

		int usedTicketId;

		synchronized (WorkEffortKeywordController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortKeywordDeleted.class,
				event -> sendWorkEffortKeywordChangedMessage(((WorkEffortKeywordDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortKeywordChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortKeyword/\" plus one of the following: "
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
