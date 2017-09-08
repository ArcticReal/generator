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
import com.skytala.eCommerce.command.AddWorkEffortIcalData;
import com.skytala.eCommerce.command.DeleteWorkEffortIcalData;
import com.skytala.eCommerce.command.UpdateWorkEffortIcalData;
import com.skytala.eCommerce.entity.WorkEffortIcalData;
import com.skytala.eCommerce.entity.WorkEffortIcalDataMapper;
import com.skytala.eCommerce.event.WorkEffortIcalDataAdded;
import com.skytala.eCommerce.event.WorkEffortIcalDataDeleted;
import com.skytala.eCommerce.event.WorkEffortIcalDataFound;
import com.skytala.eCommerce.event.WorkEffortIcalDataUpdated;
import com.skytala.eCommerce.query.FindWorkEffortIcalDatasBy;

@RestController
@RequestMapping("/api/workEffortIcalData")
public class WorkEffortIcalDataController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortIcalData>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortIcalDataController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortIcalData
	 * @return a List with the WorkEffortIcalDatas
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortIcalData> findWorkEffortIcalDatasBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortIcalDatasBy query = new FindWorkEffortIcalDatasBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortIcalDataController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortIcalDataFound.class,
				event -> sendWorkEffortIcalDatasFoundMessage(((WorkEffortIcalDataFound) event).getWorkEffortIcalDatas(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortIcalDatasFoundMessage(List<WorkEffortIcalData> workEffortIcalDatas, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortIcalDatas);
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
	public boolean createWorkEffortIcalData(HttpServletRequest request) {

		WorkEffortIcalData workEffortIcalDataToBeAdded = new WorkEffortIcalData();
		try {
			workEffortIcalDataToBeAdded = WorkEffortIcalDataMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortIcalData(workEffortIcalDataToBeAdded);

	}

	/**
	 * creates a new WorkEffortIcalData entry in the ofbiz database
	 * 
	 * @param workEffortIcalDataToBeAdded
	 *            the WorkEffortIcalData thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortIcalData(WorkEffortIcalData workEffortIcalDataToBeAdded) {

		AddWorkEffortIcalData com = new AddWorkEffortIcalData(workEffortIcalDataToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortIcalDataController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortIcalDataAdded.class,
				event -> sendWorkEffortIcalDataChangedMessage(((WorkEffortIcalDataAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortIcalData(HttpServletRequest request) {

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

		WorkEffortIcalData workEffortIcalDataToBeUpdated = new WorkEffortIcalData();

		try {
			workEffortIcalDataToBeUpdated = WorkEffortIcalDataMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortIcalData(workEffortIcalDataToBeUpdated);

	}

	/**
	 * Updates the WorkEffortIcalData with the specific Id
	 * 
	 * @param workEffortIcalDataToBeUpdated the WorkEffortIcalData thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortIcalData(WorkEffortIcalData workEffortIcalDataToBeUpdated) {

		UpdateWorkEffortIcalData com = new UpdateWorkEffortIcalData(workEffortIcalDataToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortIcalDataController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortIcalDataUpdated.class,
				event -> sendWorkEffortIcalDataChangedMessage(((WorkEffortIcalDataUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortIcalData from the database
	 * 
	 * @param workEffortIcalDataId:
	 *            the id of the WorkEffortIcalData thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortIcalDataById(@RequestParam(value = "workEffortIcalDataId") String workEffortIcalDataId) {

		DeleteWorkEffortIcalData com = new DeleteWorkEffortIcalData(workEffortIcalDataId);

		int usedTicketId;

		synchronized (WorkEffortIcalDataController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortIcalDataDeleted.class,
				event -> sendWorkEffortIcalDataChangedMessage(((WorkEffortIcalDataDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortIcalDataChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortIcalData/\" plus one of the following: "
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
