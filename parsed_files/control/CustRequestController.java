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
import com.skytala.eCommerce.command.AddCustRequest;
import com.skytala.eCommerce.command.DeleteCustRequest;
import com.skytala.eCommerce.command.UpdateCustRequest;
import com.skytala.eCommerce.entity.CustRequest;
import com.skytala.eCommerce.entity.CustRequestMapper;
import com.skytala.eCommerce.event.CustRequestAdded;
import com.skytala.eCommerce.event.CustRequestDeleted;
import com.skytala.eCommerce.event.CustRequestFound;
import com.skytala.eCommerce.event.CustRequestUpdated;
import com.skytala.eCommerce.query.FindCustRequestsBy;

@RestController
@RequestMapping("/api/custRequest")
public class CustRequestController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequest>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequest
	 * @return a List with the CustRequests
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequest> findCustRequestsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestsBy query = new FindCustRequestsBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestFound.class,
				event -> sendCustRequestsFoundMessage(((CustRequestFound) event).getCustRequests(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestsFoundMessage(List<CustRequest> custRequests, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequests);
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
	public boolean createCustRequest(HttpServletRequest request) {

		CustRequest custRequestToBeAdded = new CustRequest();
		try {
			custRequestToBeAdded = CustRequestMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequest(custRequestToBeAdded);

	}

	/**
	 * creates a new CustRequest entry in the ofbiz database
	 * 
	 * @param custRequestToBeAdded
	 *            the CustRequest thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequest(CustRequest custRequestToBeAdded) {

		AddCustRequest com = new AddCustRequest(custRequestToBeAdded);
		int usedTicketId;

		synchronized (CustRequestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestAdded.class,
				event -> sendCustRequestChangedMessage(((CustRequestAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequest(HttpServletRequest request) {

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

		CustRequest custRequestToBeUpdated = new CustRequest();

		try {
			custRequestToBeUpdated = CustRequestMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequest(custRequestToBeUpdated);

	}

	/**
	 * Updates the CustRequest with the specific Id
	 * 
	 * @param custRequestToBeUpdated the CustRequest thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequest(CustRequest custRequestToBeUpdated) {

		UpdateCustRequest com = new UpdateCustRequest(custRequestToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestUpdated.class,
				event -> sendCustRequestChangedMessage(((CustRequestUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequest from the database
	 * 
	 * @param custRequestId:
	 *            the id of the CustRequest thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestById(@RequestParam(value = "custRequestId") String custRequestId) {

		DeleteCustRequest com = new DeleteCustRequest(custRequestId);

		int usedTicketId;

		synchronized (CustRequestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestDeleted.class,
				event -> sendCustRequestChangedMessage(((CustRequestDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequest/\" plus one of the following: "
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
