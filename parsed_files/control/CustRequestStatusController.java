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
import com.skytala.eCommerce.command.AddCustRequestStatus;
import com.skytala.eCommerce.command.DeleteCustRequestStatus;
import com.skytala.eCommerce.command.UpdateCustRequestStatus;
import com.skytala.eCommerce.entity.CustRequestStatus;
import com.skytala.eCommerce.entity.CustRequestStatusMapper;
import com.skytala.eCommerce.event.CustRequestStatusAdded;
import com.skytala.eCommerce.event.CustRequestStatusDeleted;
import com.skytala.eCommerce.event.CustRequestStatusFound;
import com.skytala.eCommerce.event.CustRequestStatusUpdated;
import com.skytala.eCommerce.query.FindCustRequestStatussBy;

@RestController
@RequestMapping("/api/custRequestStatus")
public class CustRequestStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestStatus
	 * @return a List with the CustRequestStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestStatus> findCustRequestStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestStatussBy query = new FindCustRequestStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestStatusFound.class,
				event -> sendCustRequestStatussFoundMessage(((CustRequestStatusFound) event).getCustRequestStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestStatussFoundMessage(List<CustRequestStatus> custRequestStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestStatuss);
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
	public boolean createCustRequestStatus(HttpServletRequest request) {

		CustRequestStatus custRequestStatusToBeAdded = new CustRequestStatus();
		try {
			custRequestStatusToBeAdded = CustRequestStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestStatus(custRequestStatusToBeAdded);

	}

	/**
	 * creates a new CustRequestStatus entry in the ofbiz database
	 * 
	 * @param custRequestStatusToBeAdded
	 *            the CustRequestStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestStatus(CustRequestStatus custRequestStatusToBeAdded) {

		AddCustRequestStatus com = new AddCustRequestStatus(custRequestStatusToBeAdded);
		int usedTicketId;

		synchronized (CustRequestStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestStatusAdded.class,
				event -> sendCustRequestStatusChangedMessage(((CustRequestStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestStatus(HttpServletRequest request) {

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

		CustRequestStatus custRequestStatusToBeUpdated = new CustRequestStatus();

		try {
			custRequestStatusToBeUpdated = CustRequestStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestStatus(custRequestStatusToBeUpdated);

	}

	/**
	 * Updates the CustRequestStatus with the specific Id
	 * 
	 * @param custRequestStatusToBeUpdated the CustRequestStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestStatus(CustRequestStatus custRequestStatusToBeUpdated) {

		UpdateCustRequestStatus com = new UpdateCustRequestStatus(custRequestStatusToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestStatusUpdated.class,
				event -> sendCustRequestStatusChangedMessage(((CustRequestStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestStatus from the database
	 * 
	 * @param custRequestStatusId:
	 *            the id of the CustRequestStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestStatusById(@RequestParam(value = "custRequestStatusId") String custRequestStatusId) {

		DeleteCustRequestStatus com = new DeleteCustRequestStatus(custRequestStatusId);

		int usedTicketId;

		synchronized (CustRequestStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestStatusDeleted.class,
				event -> sendCustRequestStatusChangedMessage(((CustRequestStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestStatus/\" plus one of the following: "
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
