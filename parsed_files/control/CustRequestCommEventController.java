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
import com.skytala.eCommerce.command.AddCustRequestCommEvent;
import com.skytala.eCommerce.command.DeleteCustRequestCommEvent;
import com.skytala.eCommerce.command.UpdateCustRequestCommEvent;
import com.skytala.eCommerce.entity.CustRequestCommEvent;
import com.skytala.eCommerce.entity.CustRequestCommEventMapper;
import com.skytala.eCommerce.event.CustRequestCommEventAdded;
import com.skytala.eCommerce.event.CustRequestCommEventDeleted;
import com.skytala.eCommerce.event.CustRequestCommEventFound;
import com.skytala.eCommerce.event.CustRequestCommEventUpdated;
import com.skytala.eCommerce.query.FindCustRequestCommEventsBy;

@RestController
@RequestMapping("/api/custRequestCommEvent")
public class CustRequestCommEventController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestCommEvent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestCommEventController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestCommEvent
	 * @return a List with the CustRequestCommEvents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestCommEvent> findCustRequestCommEventsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestCommEventsBy query = new FindCustRequestCommEventsBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestCommEventController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestCommEventFound.class,
				event -> sendCustRequestCommEventsFoundMessage(((CustRequestCommEventFound) event).getCustRequestCommEvents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestCommEventsFoundMessage(List<CustRequestCommEvent> custRequestCommEvents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestCommEvents);
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
	public boolean createCustRequestCommEvent(HttpServletRequest request) {

		CustRequestCommEvent custRequestCommEventToBeAdded = new CustRequestCommEvent();
		try {
			custRequestCommEventToBeAdded = CustRequestCommEventMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestCommEvent(custRequestCommEventToBeAdded);

	}

	/**
	 * creates a new CustRequestCommEvent entry in the ofbiz database
	 * 
	 * @param custRequestCommEventToBeAdded
	 *            the CustRequestCommEvent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestCommEvent(CustRequestCommEvent custRequestCommEventToBeAdded) {

		AddCustRequestCommEvent com = new AddCustRequestCommEvent(custRequestCommEventToBeAdded);
		int usedTicketId;

		synchronized (CustRequestCommEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestCommEventAdded.class,
				event -> sendCustRequestCommEventChangedMessage(((CustRequestCommEventAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestCommEvent(HttpServletRequest request) {

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

		CustRequestCommEvent custRequestCommEventToBeUpdated = new CustRequestCommEvent();

		try {
			custRequestCommEventToBeUpdated = CustRequestCommEventMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestCommEvent(custRequestCommEventToBeUpdated);

	}

	/**
	 * Updates the CustRequestCommEvent with the specific Id
	 * 
	 * @param custRequestCommEventToBeUpdated the CustRequestCommEvent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestCommEvent(CustRequestCommEvent custRequestCommEventToBeUpdated) {

		UpdateCustRequestCommEvent com = new UpdateCustRequestCommEvent(custRequestCommEventToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestCommEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestCommEventUpdated.class,
				event -> sendCustRequestCommEventChangedMessage(((CustRequestCommEventUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestCommEvent from the database
	 * 
	 * @param custRequestCommEventId:
	 *            the id of the CustRequestCommEvent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestCommEventById(@RequestParam(value = "custRequestCommEventId") String custRequestCommEventId) {

		DeleteCustRequestCommEvent com = new DeleteCustRequestCommEvent(custRequestCommEventId);

		int usedTicketId;

		synchronized (CustRequestCommEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestCommEventDeleted.class,
				event -> sendCustRequestCommEventChangedMessage(((CustRequestCommEventDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestCommEventChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestCommEvent/\" plus one of the following: "
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
