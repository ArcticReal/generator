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
import com.skytala.eCommerce.command.AddPartyContactMechPurpose;
import com.skytala.eCommerce.command.DeletePartyContactMechPurpose;
import com.skytala.eCommerce.command.UpdatePartyContactMechPurpose;
import com.skytala.eCommerce.entity.PartyContactMechPurpose;
import com.skytala.eCommerce.entity.PartyContactMechPurposeMapper;
import com.skytala.eCommerce.event.PartyContactMechPurposeAdded;
import com.skytala.eCommerce.event.PartyContactMechPurposeDeleted;
import com.skytala.eCommerce.event.PartyContactMechPurposeFound;
import com.skytala.eCommerce.event.PartyContactMechPurposeUpdated;
import com.skytala.eCommerce.query.FindPartyContactMechPurposesBy;

@RestController
@RequestMapping("/api/partyContactMechPurpose")
public class PartyContactMechPurposeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyContactMechPurpose>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyContactMechPurposeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyContactMechPurpose
	 * @return a List with the PartyContactMechPurposes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyContactMechPurpose> findPartyContactMechPurposesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyContactMechPurposesBy query = new FindPartyContactMechPurposesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyContactMechPurposeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContactMechPurposeFound.class,
				event -> sendPartyContactMechPurposesFoundMessage(((PartyContactMechPurposeFound) event).getPartyContactMechPurposes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyContactMechPurposesFoundMessage(List<PartyContactMechPurpose> partyContactMechPurposes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyContactMechPurposes);
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
	public boolean createPartyContactMechPurpose(HttpServletRequest request) {

		PartyContactMechPurpose partyContactMechPurposeToBeAdded = new PartyContactMechPurpose();
		try {
			partyContactMechPurposeToBeAdded = PartyContactMechPurposeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyContactMechPurpose(partyContactMechPurposeToBeAdded);

	}

	/**
	 * creates a new PartyContactMechPurpose entry in the ofbiz database
	 * 
	 * @param partyContactMechPurposeToBeAdded
	 *            the PartyContactMechPurpose thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyContactMechPurpose(PartyContactMechPurpose partyContactMechPurposeToBeAdded) {

		AddPartyContactMechPurpose com = new AddPartyContactMechPurpose(partyContactMechPurposeToBeAdded);
		int usedTicketId;

		synchronized (PartyContactMechPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContactMechPurposeAdded.class,
				event -> sendPartyContactMechPurposeChangedMessage(((PartyContactMechPurposeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyContactMechPurpose(HttpServletRequest request) {

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

		PartyContactMechPurpose partyContactMechPurposeToBeUpdated = new PartyContactMechPurpose();

		try {
			partyContactMechPurposeToBeUpdated = PartyContactMechPurposeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyContactMechPurpose(partyContactMechPurposeToBeUpdated);

	}

	/**
	 * Updates the PartyContactMechPurpose with the specific Id
	 * 
	 * @param partyContactMechPurposeToBeUpdated the PartyContactMechPurpose thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyContactMechPurpose(PartyContactMechPurpose partyContactMechPurposeToBeUpdated) {

		UpdatePartyContactMechPurpose com = new UpdatePartyContactMechPurpose(partyContactMechPurposeToBeUpdated);

		int usedTicketId;

		synchronized (PartyContactMechPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContactMechPurposeUpdated.class,
				event -> sendPartyContactMechPurposeChangedMessage(((PartyContactMechPurposeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyContactMechPurpose from the database
	 * 
	 * @param partyContactMechPurposeId:
	 *            the id of the PartyContactMechPurpose thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyContactMechPurposeById(@RequestParam(value = "partyContactMechPurposeId") String partyContactMechPurposeId) {

		DeletePartyContactMechPurpose com = new DeletePartyContactMechPurpose(partyContactMechPurposeId);

		int usedTicketId;

		synchronized (PartyContactMechPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContactMechPurposeDeleted.class,
				event -> sendPartyContactMechPurposeChangedMessage(((PartyContactMechPurposeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyContactMechPurposeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyContactMechPurpose/\" plus one of the following: "
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
