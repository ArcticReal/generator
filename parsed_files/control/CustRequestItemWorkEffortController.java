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
import com.skytala.eCommerce.command.AddCustRequestItemWorkEffort;
import com.skytala.eCommerce.command.DeleteCustRequestItemWorkEffort;
import com.skytala.eCommerce.command.UpdateCustRequestItemWorkEffort;
import com.skytala.eCommerce.entity.CustRequestItemWorkEffort;
import com.skytala.eCommerce.entity.CustRequestItemWorkEffortMapper;
import com.skytala.eCommerce.event.CustRequestItemWorkEffortAdded;
import com.skytala.eCommerce.event.CustRequestItemWorkEffortDeleted;
import com.skytala.eCommerce.event.CustRequestItemWorkEffortFound;
import com.skytala.eCommerce.event.CustRequestItemWorkEffortUpdated;
import com.skytala.eCommerce.query.FindCustRequestItemWorkEffortsBy;

@RestController
@RequestMapping("/api/custRequestItemWorkEffort")
public class CustRequestItemWorkEffortController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestItemWorkEffort>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestItemWorkEffortController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestItemWorkEffort
	 * @return a List with the CustRequestItemWorkEfforts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestItemWorkEffort> findCustRequestItemWorkEffortsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestItemWorkEffortsBy query = new FindCustRequestItemWorkEffortsBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestItemWorkEffortController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemWorkEffortFound.class,
				event -> sendCustRequestItemWorkEffortsFoundMessage(((CustRequestItemWorkEffortFound) event).getCustRequestItemWorkEfforts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestItemWorkEffortsFoundMessage(List<CustRequestItemWorkEffort> custRequestItemWorkEfforts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestItemWorkEfforts);
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
	public boolean createCustRequestItemWorkEffort(HttpServletRequest request) {

		CustRequestItemWorkEffort custRequestItemWorkEffortToBeAdded = new CustRequestItemWorkEffort();
		try {
			custRequestItemWorkEffortToBeAdded = CustRequestItemWorkEffortMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestItemWorkEffort(custRequestItemWorkEffortToBeAdded);

	}

	/**
	 * creates a new CustRequestItemWorkEffort entry in the ofbiz database
	 * 
	 * @param custRequestItemWorkEffortToBeAdded
	 *            the CustRequestItemWorkEffort thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestItemWorkEffort(CustRequestItemWorkEffort custRequestItemWorkEffortToBeAdded) {

		AddCustRequestItemWorkEffort com = new AddCustRequestItemWorkEffort(custRequestItemWorkEffortToBeAdded);
		int usedTicketId;

		synchronized (CustRequestItemWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemWorkEffortAdded.class,
				event -> sendCustRequestItemWorkEffortChangedMessage(((CustRequestItemWorkEffortAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestItemWorkEffort(HttpServletRequest request) {

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

		CustRequestItemWorkEffort custRequestItemWorkEffortToBeUpdated = new CustRequestItemWorkEffort();

		try {
			custRequestItemWorkEffortToBeUpdated = CustRequestItemWorkEffortMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestItemWorkEffort(custRequestItemWorkEffortToBeUpdated);

	}

	/**
	 * Updates the CustRequestItemWorkEffort with the specific Id
	 * 
	 * @param custRequestItemWorkEffortToBeUpdated the CustRequestItemWorkEffort thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestItemWorkEffort(CustRequestItemWorkEffort custRequestItemWorkEffortToBeUpdated) {

		UpdateCustRequestItemWorkEffort com = new UpdateCustRequestItemWorkEffort(custRequestItemWorkEffortToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestItemWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemWorkEffortUpdated.class,
				event -> sendCustRequestItemWorkEffortChangedMessage(((CustRequestItemWorkEffortUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestItemWorkEffort from the database
	 * 
	 * @param custRequestItemWorkEffortId:
	 *            the id of the CustRequestItemWorkEffort thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestItemWorkEffortById(@RequestParam(value = "custRequestItemWorkEffortId") String custRequestItemWorkEffortId) {

		DeleteCustRequestItemWorkEffort com = new DeleteCustRequestItemWorkEffort(custRequestItemWorkEffortId);

		int usedTicketId;

		synchronized (CustRequestItemWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemWorkEffortDeleted.class,
				event -> sendCustRequestItemWorkEffortChangedMessage(((CustRequestItemWorkEffortDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestItemWorkEffortChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestItemWorkEffort/\" plus one of the following: "
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
