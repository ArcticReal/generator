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
import com.skytala.eCommerce.command.AddSalesOpportunityStage;
import com.skytala.eCommerce.command.DeleteSalesOpportunityStage;
import com.skytala.eCommerce.command.UpdateSalesOpportunityStage;
import com.skytala.eCommerce.entity.SalesOpportunityStage;
import com.skytala.eCommerce.entity.SalesOpportunityStageMapper;
import com.skytala.eCommerce.event.SalesOpportunityStageAdded;
import com.skytala.eCommerce.event.SalesOpportunityStageDeleted;
import com.skytala.eCommerce.event.SalesOpportunityStageFound;
import com.skytala.eCommerce.event.SalesOpportunityStageUpdated;
import com.skytala.eCommerce.query.FindSalesOpportunityStagesBy;

@RestController
@RequestMapping("/api/salesOpportunityStage")
public class SalesOpportunityStageController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesOpportunityStage>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesOpportunityStageController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesOpportunityStage
	 * @return a List with the SalesOpportunityStages
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesOpportunityStage> findSalesOpportunityStagesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesOpportunityStagesBy query = new FindSalesOpportunityStagesBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesOpportunityStageController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityStageFound.class,
				event -> sendSalesOpportunityStagesFoundMessage(((SalesOpportunityStageFound) event).getSalesOpportunityStages(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesOpportunityStagesFoundMessage(List<SalesOpportunityStage> salesOpportunityStages, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesOpportunityStages);
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
	public boolean createSalesOpportunityStage(HttpServletRequest request) {

		SalesOpportunityStage salesOpportunityStageToBeAdded = new SalesOpportunityStage();
		try {
			salesOpportunityStageToBeAdded = SalesOpportunityStageMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesOpportunityStage(salesOpportunityStageToBeAdded);

	}

	/**
	 * creates a new SalesOpportunityStage entry in the ofbiz database
	 * 
	 * @param salesOpportunityStageToBeAdded
	 *            the SalesOpportunityStage thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesOpportunityStage(SalesOpportunityStage salesOpportunityStageToBeAdded) {

		AddSalesOpportunityStage com = new AddSalesOpportunityStage(salesOpportunityStageToBeAdded);
		int usedTicketId;

		synchronized (SalesOpportunityStageController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityStageAdded.class,
				event -> sendSalesOpportunityStageChangedMessage(((SalesOpportunityStageAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesOpportunityStage(HttpServletRequest request) {

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

		SalesOpportunityStage salesOpportunityStageToBeUpdated = new SalesOpportunityStage();

		try {
			salesOpportunityStageToBeUpdated = SalesOpportunityStageMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesOpportunityStage(salesOpportunityStageToBeUpdated);

	}

	/**
	 * Updates the SalesOpportunityStage with the specific Id
	 * 
	 * @param salesOpportunityStageToBeUpdated the SalesOpportunityStage thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesOpportunityStage(SalesOpportunityStage salesOpportunityStageToBeUpdated) {

		UpdateSalesOpportunityStage com = new UpdateSalesOpportunityStage(salesOpportunityStageToBeUpdated);

		int usedTicketId;

		synchronized (SalesOpportunityStageController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityStageUpdated.class,
				event -> sendSalesOpportunityStageChangedMessage(((SalesOpportunityStageUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesOpportunityStage from the database
	 * 
	 * @param salesOpportunityStageId:
	 *            the id of the SalesOpportunityStage thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesOpportunityStageById(@RequestParam(value = "salesOpportunityStageId") String salesOpportunityStageId) {

		DeleteSalesOpportunityStage com = new DeleteSalesOpportunityStage(salesOpportunityStageId);

		int usedTicketId;

		synchronized (SalesOpportunityStageController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityStageDeleted.class,
				event -> sendSalesOpportunityStageChangedMessage(((SalesOpportunityStageDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesOpportunityStageChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesOpportunityStage/\" plus one of the following: "
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
