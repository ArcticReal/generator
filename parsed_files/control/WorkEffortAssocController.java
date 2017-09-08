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
import com.skytala.eCommerce.command.AddWorkEffortAssoc;
import com.skytala.eCommerce.command.DeleteWorkEffortAssoc;
import com.skytala.eCommerce.command.UpdateWorkEffortAssoc;
import com.skytala.eCommerce.entity.WorkEffortAssoc;
import com.skytala.eCommerce.entity.WorkEffortAssocMapper;
import com.skytala.eCommerce.event.WorkEffortAssocAdded;
import com.skytala.eCommerce.event.WorkEffortAssocDeleted;
import com.skytala.eCommerce.event.WorkEffortAssocFound;
import com.skytala.eCommerce.event.WorkEffortAssocUpdated;
import com.skytala.eCommerce.query.FindWorkEffortAssocsBy;

@RestController
@RequestMapping("/api/workEffortAssoc")
public class WorkEffortAssocController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortAssoc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortAssocController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortAssoc
	 * @return a List with the WorkEffortAssocs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortAssoc> findWorkEffortAssocsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortAssocsBy query = new FindWorkEffortAssocsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortAssocController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocFound.class,
				event -> sendWorkEffortAssocsFoundMessage(((WorkEffortAssocFound) event).getWorkEffortAssocs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortAssocsFoundMessage(List<WorkEffortAssoc> workEffortAssocs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortAssocs);
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
	public boolean createWorkEffortAssoc(HttpServletRequest request) {

		WorkEffortAssoc workEffortAssocToBeAdded = new WorkEffortAssoc();
		try {
			workEffortAssocToBeAdded = WorkEffortAssocMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortAssoc(workEffortAssocToBeAdded);

	}

	/**
	 * creates a new WorkEffortAssoc entry in the ofbiz database
	 * 
	 * @param workEffortAssocToBeAdded
	 *            the WorkEffortAssoc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortAssoc(WorkEffortAssoc workEffortAssocToBeAdded) {

		AddWorkEffortAssoc com = new AddWorkEffortAssoc(workEffortAssocToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocAdded.class,
				event -> sendWorkEffortAssocChangedMessage(((WorkEffortAssocAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortAssoc(HttpServletRequest request) {

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

		WorkEffortAssoc workEffortAssocToBeUpdated = new WorkEffortAssoc();

		try {
			workEffortAssocToBeUpdated = WorkEffortAssocMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortAssoc(workEffortAssocToBeUpdated);

	}

	/**
	 * Updates the WorkEffortAssoc with the specific Id
	 * 
	 * @param workEffortAssocToBeUpdated the WorkEffortAssoc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortAssoc(WorkEffortAssoc workEffortAssocToBeUpdated) {

		UpdateWorkEffortAssoc com = new UpdateWorkEffortAssoc(workEffortAssocToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocUpdated.class,
				event -> sendWorkEffortAssocChangedMessage(((WorkEffortAssocUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortAssoc from the database
	 * 
	 * @param workEffortAssocId:
	 *            the id of the WorkEffortAssoc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortAssocById(@RequestParam(value = "workEffortAssocId") String workEffortAssocId) {

		DeleteWorkEffortAssoc com = new DeleteWorkEffortAssoc(workEffortAssocId);

		int usedTicketId;

		synchronized (WorkEffortAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortAssocDeleted.class,
				event -> sendWorkEffortAssocChangedMessage(((WorkEffortAssocDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortAssocChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortAssoc/\" plus one of the following: "
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
