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
import com.skytala.eCommerce.command.AddWorkEffortAssocTypeAttr;
import com.skytala.eCommerce.command.DeleteWorkEffortAssocTypeAttr;
import com.skytala.eCommerce.command.UpdateWorkEffortAssocTypeAttr;
import com.skytala.eCommerce.entity.WorkEffortAssocTypeAttr;
import com.skytala.eCommerce.entity.WorkEffortAssocTypeAttrMapper;
import com.skytala.eCommerce.event.WorkEffortAssocTypeAttrAdded;
import com.skytala.eCommerce.event.WorkEffortAssocTypeAttrDeleted;
import com.skytala.eCommerce.event.WorkEffortAssocTypeAttrFound;
import com.skytala.eCommerce.event.WorkEffortAssocTypeAttrUpdated;
import com.skytala.eCommerce.query.FindWorkEffortAssocTypeAttrsBy;

@RestController
@RequestMapping("/api/workEffortAssocTypeAttr")
public class WorkEffortAssocTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortAssocTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortAssocTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortAssocTypeAttr
	 * @return a List with the WorkEffortAssocTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortAssocTypeAttr> findWorkEffortAssocTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortAssocTypeAttrsBy query = new FindWorkEffortAssocTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortAssocTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocTypeAttrFound.class,
				event -> sendWorkEffortAssocTypeAttrsFoundMessage(((WorkEffortAssocTypeAttrFound) event).getWorkEffortAssocTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortAssocTypeAttrsFoundMessage(List<WorkEffortAssocTypeAttr> workEffortAssocTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortAssocTypeAttrs);
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
	public boolean createWorkEffortAssocTypeAttr(HttpServletRequest request) {

		WorkEffortAssocTypeAttr workEffortAssocTypeAttrToBeAdded = new WorkEffortAssocTypeAttr();
		try {
			workEffortAssocTypeAttrToBeAdded = WorkEffortAssocTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortAssocTypeAttr(workEffortAssocTypeAttrToBeAdded);

	}

	/**
	 * creates a new WorkEffortAssocTypeAttr entry in the ofbiz database
	 * 
	 * @param workEffortAssocTypeAttrToBeAdded
	 *            the WorkEffortAssocTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortAssocTypeAttr(WorkEffortAssocTypeAttr workEffortAssocTypeAttrToBeAdded) {

		AddWorkEffortAssocTypeAttr com = new AddWorkEffortAssocTypeAttr(workEffortAssocTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortAssocTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocTypeAttrAdded.class,
				event -> sendWorkEffortAssocTypeAttrChangedMessage(((WorkEffortAssocTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortAssocTypeAttr(HttpServletRequest request) {

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

		WorkEffortAssocTypeAttr workEffortAssocTypeAttrToBeUpdated = new WorkEffortAssocTypeAttr();

		try {
			workEffortAssocTypeAttrToBeUpdated = WorkEffortAssocTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortAssocTypeAttr(workEffortAssocTypeAttrToBeUpdated);

	}

	/**
	 * Updates the WorkEffortAssocTypeAttr with the specific Id
	 * 
	 * @param workEffortAssocTypeAttrToBeUpdated the WorkEffortAssocTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortAssocTypeAttr(WorkEffortAssocTypeAttr workEffortAssocTypeAttrToBeUpdated) {

		UpdateWorkEffortAssocTypeAttr com = new UpdateWorkEffortAssocTypeAttr(workEffortAssocTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortAssocTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocTypeAttrUpdated.class,
				event -> sendWorkEffortAssocTypeAttrChangedMessage(((WorkEffortAssocTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortAssocTypeAttr from the database
	 * 
	 * @param workEffortAssocTypeAttrId:
	 *            the id of the WorkEffortAssocTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortAssocTypeAttrById(@RequestParam(value = "workEffortAssocTypeAttrId") String workEffortAssocTypeAttrId) {

		DeleteWorkEffortAssocTypeAttr com = new DeleteWorkEffortAssocTypeAttr(workEffortAssocTypeAttrId);

		int usedTicketId;

		synchronized (WorkEffortAssocTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocTypeAttrDeleted.class,
				event -> sendWorkEffortAssocTypeAttrChangedMessage(((WorkEffortAssocTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortAssocTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortAssocTypeAttr/\" plus one of the following: "
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
