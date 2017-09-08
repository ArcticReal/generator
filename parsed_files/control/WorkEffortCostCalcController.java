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
import com.skytala.eCommerce.command.AddWorkEffortCostCalc;
import com.skytala.eCommerce.command.DeleteWorkEffortCostCalc;
import com.skytala.eCommerce.command.UpdateWorkEffortCostCalc;
import com.skytala.eCommerce.entity.WorkEffortCostCalc;
import com.skytala.eCommerce.entity.WorkEffortCostCalcMapper;
import com.skytala.eCommerce.event.WorkEffortCostCalcAdded;
import com.skytala.eCommerce.event.WorkEffortCostCalcDeleted;
import com.skytala.eCommerce.event.WorkEffortCostCalcFound;
import com.skytala.eCommerce.event.WorkEffortCostCalcUpdated;
import com.skytala.eCommerce.query.FindWorkEffortCostCalcsBy;

@RestController
@RequestMapping("/api/workEffortCostCalc")
public class WorkEffortCostCalcController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortCostCalc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortCostCalcController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortCostCalc
	 * @return a List with the WorkEffortCostCalcs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortCostCalc> findWorkEffortCostCalcsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortCostCalcsBy query = new FindWorkEffortCostCalcsBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortCostCalcController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortCostCalcFound.class,
				event -> sendWorkEffortCostCalcsFoundMessage(((WorkEffortCostCalcFound) event).getWorkEffortCostCalcs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortCostCalcsFoundMessage(List<WorkEffortCostCalc> workEffortCostCalcs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortCostCalcs);
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
	public boolean createWorkEffortCostCalc(HttpServletRequest request) {

		WorkEffortCostCalc workEffortCostCalcToBeAdded = new WorkEffortCostCalc();
		try {
			workEffortCostCalcToBeAdded = WorkEffortCostCalcMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortCostCalc(workEffortCostCalcToBeAdded);

	}

	/**
	 * creates a new WorkEffortCostCalc entry in the ofbiz database
	 * 
	 * @param workEffortCostCalcToBeAdded
	 *            the WorkEffortCostCalc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortCostCalc(WorkEffortCostCalc workEffortCostCalcToBeAdded) {

		AddWorkEffortCostCalc com = new AddWorkEffortCostCalc(workEffortCostCalcToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortCostCalcController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortCostCalcAdded.class,
				event -> sendWorkEffortCostCalcChangedMessage(((WorkEffortCostCalcAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortCostCalc(HttpServletRequest request) {

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

		WorkEffortCostCalc workEffortCostCalcToBeUpdated = new WorkEffortCostCalc();

		try {
			workEffortCostCalcToBeUpdated = WorkEffortCostCalcMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortCostCalc(workEffortCostCalcToBeUpdated);

	}

	/**
	 * Updates the WorkEffortCostCalc with the specific Id
	 * 
	 * @param workEffortCostCalcToBeUpdated the WorkEffortCostCalc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortCostCalc(WorkEffortCostCalc workEffortCostCalcToBeUpdated) {

		UpdateWorkEffortCostCalc com = new UpdateWorkEffortCostCalc(workEffortCostCalcToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortCostCalcController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortCostCalcUpdated.class,
				event -> sendWorkEffortCostCalcChangedMessage(((WorkEffortCostCalcUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortCostCalc from the database
	 * 
	 * @param workEffortCostCalcId:
	 *            the id of the WorkEffortCostCalc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortCostCalcById(@RequestParam(value = "workEffortCostCalcId") String workEffortCostCalcId) {

		DeleteWorkEffortCostCalc com = new DeleteWorkEffortCostCalc(workEffortCostCalcId);

		int usedTicketId;

		synchronized (WorkEffortCostCalcController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortCostCalcDeleted.class,
				event -> sendWorkEffortCostCalcChangedMessage(((WorkEffortCostCalcDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortCostCalcChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortCostCalc/\" plus one of the following: "
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
