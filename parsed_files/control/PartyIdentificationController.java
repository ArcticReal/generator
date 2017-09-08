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
import com.skytala.eCommerce.command.AddPartyIdentification;
import com.skytala.eCommerce.command.DeletePartyIdentification;
import com.skytala.eCommerce.command.UpdatePartyIdentification;
import com.skytala.eCommerce.entity.PartyIdentification;
import com.skytala.eCommerce.entity.PartyIdentificationMapper;
import com.skytala.eCommerce.event.PartyIdentificationAdded;
import com.skytala.eCommerce.event.PartyIdentificationDeleted;
import com.skytala.eCommerce.event.PartyIdentificationFound;
import com.skytala.eCommerce.event.PartyIdentificationUpdated;
import com.skytala.eCommerce.query.FindPartyIdentificationsBy;

@RestController
@RequestMapping("/api/partyIdentification")
public class PartyIdentificationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyIdentification>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyIdentificationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyIdentification
	 * @return a List with the PartyIdentifications
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyIdentification> findPartyIdentificationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyIdentificationsBy query = new FindPartyIdentificationsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyIdentificationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIdentificationFound.class,
				event -> sendPartyIdentificationsFoundMessage(((PartyIdentificationFound) event).getPartyIdentifications(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyIdentificationsFoundMessage(List<PartyIdentification> partyIdentifications, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyIdentifications);
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
	public boolean createPartyIdentification(HttpServletRequest request) {

		PartyIdentification partyIdentificationToBeAdded = new PartyIdentification();
		try {
			partyIdentificationToBeAdded = PartyIdentificationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyIdentification(partyIdentificationToBeAdded);

	}

	/**
	 * creates a new PartyIdentification entry in the ofbiz database
	 * 
	 * @param partyIdentificationToBeAdded
	 *            the PartyIdentification thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyIdentification(PartyIdentification partyIdentificationToBeAdded) {

		AddPartyIdentification com = new AddPartyIdentification(partyIdentificationToBeAdded);
		int usedTicketId;

		synchronized (PartyIdentificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIdentificationAdded.class,
				event -> sendPartyIdentificationChangedMessage(((PartyIdentificationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyIdentification(HttpServletRequest request) {

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

		PartyIdentification partyIdentificationToBeUpdated = new PartyIdentification();

		try {
			partyIdentificationToBeUpdated = PartyIdentificationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyIdentification(partyIdentificationToBeUpdated);

	}

	/**
	 * Updates the PartyIdentification with the specific Id
	 * 
	 * @param partyIdentificationToBeUpdated the PartyIdentification thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyIdentification(PartyIdentification partyIdentificationToBeUpdated) {

		UpdatePartyIdentification com = new UpdatePartyIdentification(partyIdentificationToBeUpdated);

		int usedTicketId;

		synchronized (PartyIdentificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIdentificationUpdated.class,
				event -> sendPartyIdentificationChangedMessage(((PartyIdentificationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyIdentification from the database
	 * 
	 * @param partyIdentificationId:
	 *            the id of the PartyIdentification thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyIdentificationById(@RequestParam(value = "partyIdentificationId") String partyIdentificationId) {

		DeletePartyIdentification com = new DeletePartyIdentification(partyIdentificationId);

		int usedTicketId;

		synchronized (PartyIdentificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIdentificationDeleted.class,
				event -> sendPartyIdentificationChangedMessage(((PartyIdentificationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyIdentificationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyIdentification/\" plus one of the following: "
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
