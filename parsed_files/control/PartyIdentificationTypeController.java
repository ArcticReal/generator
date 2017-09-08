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
import com.skytala.eCommerce.command.AddPartyIdentificationType;
import com.skytala.eCommerce.command.DeletePartyIdentificationType;
import com.skytala.eCommerce.command.UpdatePartyIdentificationType;
import com.skytala.eCommerce.entity.PartyIdentificationType;
import com.skytala.eCommerce.entity.PartyIdentificationTypeMapper;
import com.skytala.eCommerce.event.PartyIdentificationTypeAdded;
import com.skytala.eCommerce.event.PartyIdentificationTypeDeleted;
import com.skytala.eCommerce.event.PartyIdentificationTypeFound;
import com.skytala.eCommerce.event.PartyIdentificationTypeUpdated;
import com.skytala.eCommerce.query.FindPartyIdentificationTypesBy;

@RestController
@RequestMapping("/api/partyIdentificationType")
public class PartyIdentificationTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyIdentificationType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyIdentificationTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyIdentificationType
	 * @return a List with the PartyIdentificationTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyIdentificationType> findPartyIdentificationTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyIdentificationTypesBy query = new FindPartyIdentificationTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyIdentificationTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIdentificationTypeFound.class,
				event -> sendPartyIdentificationTypesFoundMessage(((PartyIdentificationTypeFound) event).getPartyIdentificationTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyIdentificationTypesFoundMessage(List<PartyIdentificationType> partyIdentificationTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyIdentificationTypes);
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
	public boolean createPartyIdentificationType(HttpServletRequest request) {

		PartyIdentificationType partyIdentificationTypeToBeAdded = new PartyIdentificationType();
		try {
			partyIdentificationTypeToBeAdded = PartyIdentificationTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyIdentificationType(partyIdentificationTypeToBeAdded);

	}

	/**
	 * creates a new PartyIdentificationType entry in the ofbiz database
	 * 
	 * @param partyIdentificationTypeToBeAdded
	 *            the PartyIdentificationType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyIdentificationType(PartyIdentificationType partyIdentificationTypeToBeAdded) {

		AddPartyIdentificationType com = new AddPartyIdentificationType(partyIdentificationTypeToBeAdded);
		int usedTicketId;

		synchronized (PartyIdentificationTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIdentificationTypeAdded.class,
				event -> sendPartyIdentificationTypeChangedMessage(((PartyIdentificationTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyIdentificationType(HttpServletRequest request) {

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

		PartyIdentificationType partyIdentificationTypeToBeUpdated = new PartyIdentificationType();

		try {
			partyIdentificationTypeToBeUpdated = PartyIdentificationTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyIdentificationType(partyIdentificationTypeToBeUpdated);

	}

	/**
	 * Updates the PartyIdentificationType with the specific Id
	 * 
	 * @param partyIdentificationTypeToBeUpdated the PartyIdentificationType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyIdentificationType(PartyIdentificationType partyIdentificationTypeToBeUpdated) {

		UpdatePartyIdentificationType com = new UpdatePartyIdentificationType(partyIdentificationTypeToBeUpdated);

		int usedTicketId;

		synchronized (PartyIdentificationTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIdentificationTypeUpdated.class,
				event -> sendPartyIdentificationTypeChangedMessage(((PartyIdentificationTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyIdentificationType from the database
	 * 
	 * @param partyIdentificationTypeId:
	 *            the id of the PartyIdentificationType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyIdentificationTypeById(@RequestParam(value = "partyIdentificationTypeId") String partyIdentificationTypeId) {

		DeletePartyIdentificationType com = new DeletePartyIdentificationType(partyIdentificationTypeId);

		int usedTicketId;

		synchronized (PartyIdentificationTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIdentificationTypeDeleted.class,
				event -> sendPartyIdentificationTypeChangedMessage(((PartyIdentificationTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyIdentificationTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyIdentificationType/\" plus one of the following: "
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
