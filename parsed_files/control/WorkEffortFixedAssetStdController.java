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
import com.skytala.eCommerce.command.AddWorkEffortFixedAssetStd;
import com.skytala.eCommerce.command.DeleteWorkEffortFixedAssetStd;
import com.skytala.eCommerce.command.UpdateWorkEffortFixedAssetStd;
import com.skytala.eCommerce.entity.WorkEffortFixedAssetStd;
import com.skytala.eCommerce.entity.WorkEffortFixedAssetStdMapper;
import com.skytala.eCommerce.event.WorkEffortFixedAssetStdAdded;
import com.skytala.eCommerce.event.WorkEffortFixedAssetStdDeleted;
import com.skytala.eCommerce.event.WorkEffortFixedAssetStdFound;
import com.skytala.eCommerce.event.WorkEffortFixedAssetStdUpdated;
import com.skytala.eCommerce.query.FindWorkEffortFixedAssetStdsBy;

@RestController
@RequestMapping("/api/workEffortFixedAssetStd")
public class WorkEffortFixedAssetStdController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortFixedAssetStd>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortFixedAssetStdController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortFixedAssetStd
	 * @return a List with the WorkEffortFixedAssetStds
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortFixedAssetStd> findWorkEffortFixedAssetStdsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortFixedAssetStdsBy query = new FindWorkEffortFixedAssetStdsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortFixedAssetStdController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortFixedAssetStdFound.class,
				event -> sendWorkEffortFixedAssetStdsFoundMessage(((WorkEffortFixedAssetStdFound) event).getWorkEffortFixedAssetStds(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortFixedAssetStdsFoundMessage(List<WorkEffortFixedAssetStd> workEffortFixedAssetStds, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortFixedAssetStds);
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
	public boolean createWorkEffortFixedAssetStd(HttpServletRequest request) {

		WorkEffortFixedAssetStd workEffortFixedAssetStdToBeAdded = new WorkEffortFixedAssetStd();
		try {
			workEffortFixedAssetStdToBeAdded = WorkEffortFixedAssetStdMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortFixedAssetStd(workEffortFixedAssetStdToBeAdded);

	}

	/**
	 * creates a new WorkEffortFixedAssetStd entry in the ofbiz database
	 * 
	 * @param workEffortFixedAssetStdToBeAdded
	 *            the WorkEffortFixedAssetStd thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortFixedAssetStd(WorkEffortFixedAssetStd workEffortFixedAssetStdToBeAdded) {

		AddWorkEffortFixedAssetStd com = new AddWorkEffortFixedAssetStd(workEffortFixedAssetStdToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortFixedAssetStdController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortFixedAssetStdAdded.class,
				event -> sendWorkEffortFixedAssetStdChangedMessage(((WorkEffortFixedAssetStdAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortFixedAssetStd(HttpServletRequest request) {

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

		WorkEffortFixedAssetStd workEffortFixedAssetStdToBeUpdated = new WorkEffortFixedAssetStd();

		try {
			workEffortFixedAssetStdToBeUpdated = WorkEffortFixedAssetStdMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortFixedAssetStd(workEffortFixedAssetStdToBeUpdated);

	}

	/**
	 * Updates the WorkEffortFixedAssetStd with the specific Id
	 * 
	 * @param workEffortFixedAssetStdToBeUpdated the WorkEffortFixedAssetStd thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortFixedAssetStd(WorkEffortFixedAssetStd workEffortFixedAssetStdToBeUpdated) {

		UpdateWorkEffortFixedAssetStd com = new UpdateWorkEffortFixedAssetStd(workEffortFixedAssetStdToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortFixedAssetStdController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortFixedAssetStdUpdated.class,
				event -> sendWorkEffortFixedAssetStdChangedMessage(((WorkEffortFixedAssetStdUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortFixedAssetStd from the database
	 * 
	 * @param workEffortFixedAssetStdId:
	 *            the id of the WorkEffortFixedAssetStd thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortFixedAssetStdById(@RequestParam(value = "workEffortFixedAssetStdId") String workEffortFixedAssetStdId) {

		DeleteWorkEffortFixedAssetStd com = new DeleteWorkEffortFixedAssetStd(workEffortFixedAssetStdId);

		int usedTicketId;

		synchronized (WorkEffortFixedAssetStdController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortFixedAssetStdDeleted.class,
				event -> sendWorkEffortFixedAssetStdChangedMessage(((WorkEffortFixedAssetStdDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortFixedAssetStdChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortFixedAssetStd/\" plus one of the following: "
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
