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
import com.skytala.eCommerce.command.AddPartyQualType;
import com.skytala.eCommerce.command.DeletePartyQualType;
import com.skytala.eCommerce.command.UpdatePartyQualType;
import com.skytala.eCommerce.entity.PartyQualType;
import com.skytala.eCommerce.entity.PartyQualTypeMapper;
import com.skytala.eCommerce.event.PartyQualTypeAdded;
import com.skytala.eCommerce.event.PartyQualTypeDeleted;
import com.skytala.eCommerce.event.PartyQualTypeFound;
import com.skytala.eCommerce.event.PartyQualTypeUpdated;
import com.skytala.eCommerce.query.FindPartyQualTypesBy;

@RestController
@RequestMapping("/api/partyQualType")
public class PartyQualTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyQualType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyQualTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyQualType
	 * @return a List with the PartyQualTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyQualType> findPartyQualTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyQualTypesBy query = new FindPartyQualTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyQualTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyQualTypeFound.class,
				event -> sendPartyQualTypesFoundMessage(((PartyQualTypeFound) event).getPartyQualTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyQualTypesFoundMessage(List<PartyQualType> partyQualTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyQualTypes);
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
	public boolean createPartyQualType(HttpServletRequest request) {

		PartyQualType partyQualTypeToBeAdded = new PartyQualType();
		try {
			partyQualTypeToBeAdded = PartyQualTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyQualType(partyQualTypeToBeAdded);

	}

	/**
	 * creates a new PartyQualType entry in the ofbiz database
	 * 
	 * @param partyQualTypeToBeAdded
	 *            the PartyQualType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyQualType(PartyQualType partyQualTypeToBeAdded) {

		AddPartyQualType com = new AddPartyQualType(partyQualTypeToBeAdded);
		int usedTicketId;

		synchronized (PartyQualTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyQualTypeAdded.class,
				event -> sendPartyQualTypeChangedMessage(((PartyQualTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyQualType(HttpServletRequest request) {

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

		PartyQualType partyQualTypeToBeUpdated = new PartyQualType();

		try {
			partyQualTypeToBeUpdated = PartyQualTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyQualType(partyQualTypeToBeUpdated);

	}

	/**
	 * Updates the PartyQualType with the specific Id
	 * 
	 * @param partyQualTypeToBeUpdated the PartyQualType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyQualType(PartyQualType partyQualTypeToBeUpdated) {

		UpdatePartyQualType com = new UpdatePartyQualType(partyQualTypeToBeUpdated);

		int usedTicketId;

		synchronized (PartyQualTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyQualTypeUpdated.class,
				event -> sendPartyQualTypeChangedMessage(((PartyQualTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyQualType from the database
	 * 
	 * @param partyQualTypeId:
	 *            the id of the PartyQualType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyQualTypeById(@RequestParam(value = "partyQualTypeId") String partyQualTypeId) {

		DeletePartyQualType com = new DeletePartyQualType(partyQualTypeId);

		int usedTicketId;

		synchronized (PartyQualTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyQualTypeDeleted.class,
				event -> sendPartyQualTypeChangedMessage(((PartyQualTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyQualTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyQualType/\" plus one of the following: "
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
