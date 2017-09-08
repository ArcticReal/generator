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
import com.skytala.eCommerce.command.AddWorkEffortContent;
import com.skytala.eCommerce.command.DeleteWorkEffortContent;
import com.skytala.eCommerce.command.UpdateWorkEffortContent;
import com.skytala.eCommerce.entity.WorkEffortContent;
import com.skytala.eCommerce.entity.WorkEffortContentMapper;
import com.skytala.eCommerce.event.WorkEffortContentAdded;
import com.skytala.eCommerce.event.WorkEffortContentDeleted;
import com.skytala.eCommerce.event.WorkEffortContentFound;
import com.skytala.eCommerce.event.WorkEffortContentUpdated;
import com.skytala.eCommerce.query.FindWorkEffortContentsBy;

@RestController
@RequestMapping("/api/workEffortContent")
public class WorkEffortContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortContent
	 * @return a List with the WorkEffortContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortContent> findWorkEffortContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortContentsBy query = new FindWorkEffortContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContentFound.class,
				event -> sendWorkEffortContentsFoundMessage(((WorkEffortContentFound) event).getWorkEffortContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortContentsFoundMessage(List<WorkEffortContent> workEffortContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortContents);
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
	public boolean createWorkEffortContent(HttpServletRequest request) {

		WorkEffortContent workEffortContentToBeAdded = new WorkEffortContent();
		try {
			workEffortContentToBeAdded = WorkEffortContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortContent(workEffortContentToBeAdded);

	}

	/**
	 * creates a new WorkEffortContent entry in the ofbiz database
	 * 
	 * @param workEffortContentToBeAdded
	 *            the WorkEffortContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortContent(WorkEffortContent workEffortContentToBeAdded) {

		AddWorkEffortContent com = new AddWorkEffortContent(workEffortContentToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContentAdded.class,
				event -> sendWorkEffortContentChangedMessage(((WorkEffortContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortContent(HttpServletRequest request) {

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

		WorkEffortContent workEffortContentToBeUpdated = new WorkEffortContent();

		try {
			workEffortContentToBeUpdated = WorkEffortContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortContent(workEffortContentToBeUpdated);

	}

	/**
	 * Updates the WorkEffortContent with the specific Id
	 * 
	 * @param workEffortContentToBeUpdated the WorkEffortContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortContent(WorkEffortContent workEffortContentToBeUpdated) {

		UpdateWorkEffortContent com = new UpdateWorkEffortContent(workEffortContentToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContentUpdated.class,
				event -> sendWorkEffortContentChangedMessage(((WorkEffortContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortContent from the database
	 * 
	 * @param workEffortContentId:
	 *            the id of the WorkEffortContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortContentById(@RequestParam(value = "workEffortContentId") String workEffortContentId) {

		DeleteWorkEffortContent com = new DeleteWorkEffortContent(workEffortContentId);

		int usedTicketId;

		synchronized (WorkEffortContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortContentDeleted.class,
				event -> sendWorkEffortContentChangedMessage(((WorkEffortContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortContent/\" plus one of the following: "
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
