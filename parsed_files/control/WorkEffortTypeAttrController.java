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
import com.skytala.eCommerce.command.AddWorkEffortTypeAttr;
import com.skytala.eCommerce.command.DeleteWorkEffortTypeAttr;
import com.skytala.eCommerce.command.UpdateWorkEffortTypeAttr;
import com.skytala.eCommerce.entity.WorkEffortTypeAttr;
import com.skytala.eCommerce.entity.WorkEffortTypeAttrMapper;
import com.skytala.eCommerce.event.WorkEffortTypeAttrAdded;
import com.skytala.eCommerce.event.WorkEffortTypeAttrDeleted;
import com.skytala.eCommerce.event.WorkEffortTypeAttrFound;
import com.skytala.eCommerce.event.WorkEffortTypeAttrUpdated;
import com.skytala.eCommerce.query.FindWorkEffortTypeAttrsBy;

@RestController
@RequestMapping("/api/workEffortTypeAttr")
public class WorkEffortTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortTypeAttr
	 * @return a List with the WorkEffortTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortTypeAttr> findWorkEffortTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortTypeAttrsBy query = new FindWorkEffortTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTypeAttrFound.class,
				event -> sendWorkEffortTypeAttrsFoundMessage(((WorkEffortTypeAttrFound) event).getWorkEffortTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortTypeAttrsFoundMessage(List<WorkEffortTypeAttr> workEffortTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortTypeAttrs);
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
	public boolean createWorkEffortTypeAttr(HttpServletRequest request) {

		WorkEffortTypeAttr workEffortTypeAttrToBeAdded = new WorkEffortTypeAttr();
		try {
			workEffortTypeAttrToBeAdded = WorkEffortTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortTypeAttr(workEffortTypeAttrToBeAdded);

	}

	/**
	 * creates a new WorkEffortTypeAttr entry in the ofbiz database
	 * 
	 * @param workEffortTypeAttrToBeAdded
	 *            the WorkEffortTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortTypeAttr(WorkEffortTypeAttr workEffortTypeAttrToBeAdded) {

		AddWorkEffortTypeAttr com = new AddWorkEffortTypeAttr(workEffortTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTypeAttrAdded.class,
				event -> sendWorkEffortTypeAttrChangedMessage(((WorkEffortTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortTypeAttr(HttpServletRequest request) {

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

		WorkEffortTypeAttr workEffortTypeAttrToBeUpdated = new WorkEffortTypeAttr();

		try {
			workEffortTypeAttrToBeUpdated = WorkEffortTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortTypeAttr(workEffortTypeAttrToBeUpdated);

	}

	/**
	 * Updates the WorkEffortTypeAttr with the specific Id
	 * 
	 * @param workEffortTypeAttrToBeUpdated the WorkEffortTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortTypeAttr(WorkEffortTypeAttr workEffortTypeAttrToBeUpdated) {

		UpdateWorkEffortTypeAttr com = new UpdateWorkEffortTypeAttr(workEffortTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTypeAttrUpdated.class,
				event -> sendWorkEffortTypeAttrChangedMessage(((WorkEffortTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortTypeAttr from the database
	 * 
	 * @param workEffortTypeAttrId:
	 *            the id of the WorkEffortTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortTypeAttrById(@RequestParam(value = "workEffortTypeAttrId") String workEffortTypeAttrId) {

		DeleteWorkEffortTypeAttr com = new DeleteWorkEffortTypeAttr(workEffortTypeAttrId);

		int usedTicketId;

		synchronized (WorkEffortTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortTypeAttrDeleted.class,
				event -> sendWorkEffortTypeAttrChangedMessage(((WorkEffortTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortTypeAttr/\" plus one of the following: "
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
