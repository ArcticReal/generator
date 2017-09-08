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
import com.skytala.eCommerce.command.AddCustRequestWorkEffort;
import com.skytala.eCommerce.command.DeleteCustRequestWorkEffort;
import com.skytala.eCommerce.command.UpdateCustRequestWorkEffort;
import com.skytala.eCommerce.entity.CustRequestWorkEffort;
import com.skytala.eCommerce.entity.CustRequestWorkEffortMapper;
import com.skytala.eCommerce.event.CustRequestWorkEffortAdded;
import com.skytala.eCommerce.event.CustRequestWorkEffortDeleted;
import com.skytala.eCommerce.event.CustRequestWorkEffortFound;
import com.skytala.eCommerce.event.CustRequestWorkEffortUpdated;
import com.skytala.eCommerce.query.FindCustRequestWorkEffortsBy;

@RestController
@RequestMapping("/api/custRequestWorkEffort")
public class CustRequestWorkEffortController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestWorkEffort>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestWorkEffortController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestWorkEffort
	 * @return a List with the CustRequestWorkEfforts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestWorkEffort> findCustRequestWorkEffortsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestWorkEffortsBy query = new FindCustRequestWorkEffortsBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestWorkEffortController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestWorkEffortFound.class,
				event -> sendCustRequestWorkEffortsFoundMessage(((CustRequestWorkEffortFound) event).getCustRequestWorkEfforts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestWorkEffortsFoundMessage(List<CustRequestWorkEffort> custRequestWorkEfforts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestWorkEfforts);
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
	public boolean createCustRequestWorkEffort(HttpServletRequest request) {

		CustRequestWorkEffort custRequestWorkEffortToBeAdded = new CustRequestWorkEffort();
		try {
			custRequestWorkEffortToBeAdded = CustRequestWorkEffortMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestWorkEffort(custRequestWorkEffortToBeAdded);

	}

	/**
	 * creates a new CustRequestWorkEffort entry in the ofbiz database
	 * 
	 * @param custRequestWorkEffortToBeAdded
	 *            the CustRequestWorkEffort thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestWorkEffort(CustRequestWorkEffort custRequestWorkEffortToBeAdded) {

		AddCustRequestWorkEffort com = new AddCustRequestWorkEffort(custRequestWorkEffortToBeAdded);
		int usedTicketId;

		synchronized (CustRequestWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestWorkEffortAdded.class,
				event -> sendCustRequestWorkEffortChangedMessage(((CustRequestWorkEffortAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestWorkEffort(HttpServletRequest request) {

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

		CustRequestWorkEffort custRequestWorkEffortToBeUpdated = new CustRequestWorkEffort();

		try {
			custRequestWorkEffortToBeUpdated = CustRequestWorkEffortMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestWorkEffort(custRequestWorkEffortToBeUpdated);

	}

	/**
	 * Updates the CustRequestWorkEffort with the specific Id
	 * 
	 * @param custRequestWorkEffortToBeUpdated the CustRequestWorkEffort thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestWorkEffort(CustRequestWorkEffort custRequestWorkEffortToBeUpdated) {

		UpdateCustRequestWorkEffort com = new UpdateCustRequestWorkEffort(custRequestWorkEffortToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestWorkEffortUpdated.class,
				event -> sendCustRequestWorkEffortChangedMessage(((CustRequestWorkEffortUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestWorkEffort from the database
	 * 
	 * @param custRequestWorkEffortId:
	 *            the id of the CustRequestWorkEffort thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestWorkEffortById(@RequestParam(value = "custRequestWorkEffortId") String custRequestWorkEffortId) {

		DeleteCustRequestWorkEffort com = new DeleteCustRequestWorkEffort(custRequestWorkEffortId);

		int usedTicketId;

		synchronized (CustRequestWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestWorkEffortDeleted.class,
				event -> sendCustRequestWorkEffortChangedMessage(((CustRequestWorkEffortDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestWorkEffortChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestWorkEffort/\" plus one of the following: "
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
