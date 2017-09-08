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
import com.skytala.eCommerce.command.AddSalesOpportunityTrckCode;
import com.skytala.eCommerce.command.DeleteSalesOpportunityTrckCode;
import com.skytala.eCommerce.command.UpdateSalesOpportunityTrckCode;
import com.skytala.eCommerce.entity.SalesOpportunityTrckCode;
import com.skytala.eCommerce.entity.SalesOpportunityTrckCodeMapper;
import com.skytala.eCommerce.event.SalesOpportunityTrckCodeAdded;
import com.skytala.eCommerce.event.SalesOpportunityTrckCodeDeleted;
import com.skytala.eCommerce.event.SalesOpportunityTrckCodeFound;
import com.skytala.eCommerce.event.SalesOpportunityTrckCodeUpdated;
import com.skytala.eCommerce.query.FindSalesOpportunityTrckCodesBy;

@RestController
@RequestMapping("/api/salesOpportunityTrckCode")
public class SalesOpportunityTrckCodeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesOpportunityTrckCode>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesOpportunityTrckCodeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesOpportunityTrckCode
	 * @return a List with the SalesOpportunityTrckCodes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesOpportunityTrckCode> findSalesOpportunityTrckCodesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesOpportunityTrckCodesBy query = new FindSalesOpportunityTrckCodesBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesOpportunityTrckCodeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityTrckCodeFound.class,
				event -> sendSalesOpportunityTrckCodesFoundMessage(((SalesOpportunityTrckCodeFound) event).getSalesOpportunityTrckCodes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesOpportunityTrckCodesFoundMessage(List<SalesOpportunityTrckCode> salesOpportunityTrckCodes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesOpportunityTrckCodes);
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
	public boolean createSalesOpportunityTrckCode(HttpServletRequest request) {

		SalesOpportunityTrckCode salesOpportunityTrckCodeToBeAdded = new SalesOpportunityTrckCode();
		try {
			salesOpportunityTrckCodeToBeAdded = SalesOpportunityTrckCodeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesOpportunityTrckCode(salesOpportunityTrckCodeToBeAdded);

	}

	/**
	 * creates a new SalesOpportunityTrckCode entry in the ofbiz database
	 * 
	 * @param salesOpportunityTrckCodeToBeAdded
	 *            the SalesOpportunityTrckCode thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesOpportunityTrckCode(SalesOpportunityTrckCode salesOpportunityTrckCodeToBeAdded) {

		AddSalesOpportunityTrckCode com = new AddSalesOpportunityTrckCode(salesOpportunityTrckCodeToBeAdded);
		int usedTicketId;

		synchronized (SalesOpportunityTrckCodeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityTrckCodeAdded.class,
				event -> sendSalesOpportunityTrckCodeChangedMessage(((SalesOpportunityTrckCodeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesOpportunityTrckCode(HttpServletRequest request) {

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

		SalesOpportunityTrckCode salesOpportunityTrckCodeToBeUpdated = new SalesOpportunityTrckCode();

		try {
			salesOpportunityTrckCodeToBeUpdated = SalesOpportunityTrckCodeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesOpportunityTrckCode(salesOpportunityTrckCodeToBeUpdated);

	}

	/**
	 * Updates the SalesOpportunityTrckCode with the specific Id
	 * 
	 * @param salesOpportunityTrckCodeToBeUpdated the SalesOpportunityTrckCode thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesOpportunityTrckCode(SalesOpportunityTrckCode salesOpportunityTrckCodeToBeUpdated) {

		UpdateSalesOpportunityTrckCode com = new UpdateSalesOpportunityTrckCode(salesOpportunityTrckCodeToBeUpdated);

		int usedTicketId;

		synchronized (SalesOpportunityTrckCodeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityTrckCodeUpdated.class,
				event -> sendSalesOpportunityTrckCodeChangedMessage(((SalesOpportunityTrckCodeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesOpportunityTrckCode from the database
	 * 
	 * @param salesOpportunityTrckCodeId:
	 *            the id of the SalesOpportunityTrckCode thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesOpportunityTrckCodeById(@RequestParam(value = "salesOpportunityTrckCodeId") String salesOpportunityTrckCodeId) {

		DeleteSalesOpportunityTrckCode com = new DeleteSalesOpportunityTrckCode(salesOpportunityTrckCodeId);

		int usedTicketId;

		synchronized (SalesOpportunityTrckCodeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityTrckCodeDeleted.class,
				event -> sendSalesOpportunityTrckCodeChangedMessage(((SalesOpportunityTrckCodeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesOpportunityTrckCodeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesOpportunityTrckCode/\" plus one of the following: "
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
