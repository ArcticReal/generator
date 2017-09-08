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
import com.skytala.eCommerce.command.AddCommunicationEventPurpose;
import com.skytala.eCommerce.command.DeleteCommunicationEventPurpose;
import com.skytala.eCommerce.command.UpdateCommunicationEventPurpose;
import com.skytala.eCommerce.entity.CommunicationEventPurpose;
import com.skytala.eCommerce.entity.CommunicationEventPurposeMapper;
import com.skytala.eCommerce.event.CommunicationEventPurposeAdded;
import com.skytala.eCommerce.event.CommunicationEventPurposeDeleted;
import com.skytala.eCommerce.event.CommunicationEventPurposeFound;
import com.skytala.eCommerce.event.CommunicationEventPurposeUpdated;
import com.skytala.eCommerce.query.FindCommunicationEventPurposesBy;

@RestController
@RequestMapping("/api/communicationEventPurpose")
public class CommunicationEventPurposeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommunicationEventPurpose>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommunicationEventPurposeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommunicationEventPurpose
	 * @return a List with the CommunicationEventPurposes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommunicationEventPurpose> findCommunicationEventPurposesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommunicationEventPurposesBy query = new FindCommunicationEventPurposesBy(allRequestParams);

		int usedTicketId;

		synchronized (CommunicationEventPurposeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventPurposeFound.class,
				event -> sendCommunicationEventPurposesFoundMessage(((CommunicationEventPurposeFound) event).getCommunicationEventPurposes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommunicationEventPurposesFoundMessage(List<CommunicationEventPurpose> communicationEventPurposes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, communicationEventPurposes);
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
	public boolean createCommunicationEventPurpose(HttpServletRequest request) {

		CommunicationEventPurpose communicationEventPurposeToBeAdded = new CommunicationEventPurpose();
		try {
			communicationEventPurposeToBeAdded = CommunicationEventPurposeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommunicationEventPurpose(communicationEventPurposeToBeAdded);

	}

	/**
	 * creates a new CommunicationEventPurpose entry in the ofbiz database
	 * 
	 * @param communicationEventPurposeToBeAdded
	 *            the CommunicationEventPurpose thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommunicationEventPurpose(CommunicationEventPurpose communicationEventPurposeToBeAdded) {

		AddCommunicationEventPurpose com = new AddCommunicationEventPurpose(communicationEventPurposeToBeAdded);
		int usedTicketId;

		synchronized (CommunicationEventPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventPurposeAdded.class,
				event -> sendCommunicationEventPurposeChangedMessage(((CommunicationEventPurposeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCommunicationEventPurpose(HttpServletRequest request) {

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

		CommunicationEventPurpose communicationEventPurposeToBeUpdated = new CommunicationEventPurpose();

		try {
			communicationEventPurposeToBeUpdated = CommunicationEventPurposeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommunicationEventPurpose(communicationEventPurposeToBeUpdated);

	}

	/**
	 * Updates the CommunicationEventPurpose with the specific Id
	 * 
	 * @param communicationEventPurposeToBeUpdated the CommunicationEventPurpose thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommunicationEventPurpose(CommunicationEventPurpose communicationEventPurposeToBeUpdated) {

		UpdateCommunicationEventPurpose com = new UpdateCommunicationEventPurpose(communicationEventPurposeToBeUpdated);

		int usedTicketId;

		synchronized (CommunicationEventPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventPurposeUpdated.class,
				event -> sendCommunicationEventPurposeChangedMessage(((CommunicationEventPurposeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CommunicationEventPurpose from the database
	 * 
	 * @param communicationEventPurposeId:
	 *            the id of the CommunicationEventPurpose thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommunicationEventPurposeById(@RequestParam(value = "communicationEventPurposeId") String communicationEventPurposeId) {

		DeleteCommunicationEventPurpose com = new DeleteCommunicationEventPurpose(communicationEventPurposeId);

		int usedTicketId;

		synchronized (CommunicationEventPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventPurposeDeleted.class,
				event -> sendCommunicationEventPurposeChangedMessage(((CommunicationEventPurposeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCommunicationEventPurposeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/communicationEventPurpose/\" plus one of the following: "
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
