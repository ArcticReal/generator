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
import com.skytala.eCommerce.command.AddPartyClassificationType;
import com.skytala.eCommerce.command.DeletePartyClassificationType;
import com.skytala.eCommerce.command.UpdatePartyClassificationType;
import com.skytala.eCommerce.entity.PartyClassificationType;
import com.skytala.eCommerce.entity.PartyClassificationTypeMapper;
import com.skytala.eCommerce.event.PartyClassificationTypeAdded;
import com.skytala.eCommerce.event.PartyClassificationTypeDeleted;
import com.skytala.eCommerce.event.PartyClassificationTypeFound;
import com.skytala.eCommerce.event.PartyClassificationTypeUpdated;
import com.skytala.eCommerce.query.FindPartyClassificationTypesBy;

@RestController
@RequestMapping("/api/partyClassificationType")
public class PartyClassificationTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyClassificationType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyClassificationTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyClassificationType
	 * @return a List with the PartyClassificationTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyClassificationType> findPartyClassificationTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyClassificationTypesBy query = new FindPartyClassificationTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyClassificationTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationTypeFound.class,
				event -> sendPartyClassificationTypesFoundMessage(((PartyClassificationTypeFound) event).getPartyClassificationTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyClassificationTypesFoundMessage(List<PartyClassificationType> partyClassificationTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyClassificationTypes);
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
	public boolean createPartyClassificationType(HttpServletRequest request) {

		PartyClassificationType partyClassificationTypeToBeAdded = new PartyClassificationType();
		try {
			partyClassificationTypeToBeAdded = PartyClassificationTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyClassificationType(partyClassificationTypeToBeAdded);

	}

	/**
	 * creates a new PartyClassificationType entry in the ofbiz database
	 * 
	 * @param partyClassificationTypeToBeAdded
	 *            the PartyClassificationType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyClassificationType(PartyClassificationType partyClassificationTypeToBeAdded) {

		AddPartyClassificationType com = new AddPartyClassificationType(partyClassificationTypeToBeAdded);
		int usedTicketId;

		synchronized (PartyClassificationTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationTypeAdded.class,
				event -> sendPartyClassificationTypeChangedMessage(((PartyClassificationTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyClassificationType(HttpServletRequest request) {

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

		PartyClassificationType partyClassificationTypeToBeUpdated = new PartyClassificationType();

		try {
			partyClassificationTypeToBeUpdated = PartyClassificationTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyClassificationType(partyClassificationTypeToBeUpdated);

	}

	/**
	 * Updates the PartyClassificationType with the specific Id
	 * 
	 * @param partyClassificationTypeToBeUpdated the PartyClassificationType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyClassificationType(PartyClassificationType partyClassificationTypeToBeUpdated) {

		UpdatePartyClassificationType com = new UpdatePartyClassificationType(partyClassificationTypeToBeUpdated);

		int usedTicketId;

		synchronized (PartyClassificationTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationTypeUpdated.class,
				event -> sendPartyClassificationTypeChangedMessage(((PartyClassificationTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyClassificationType from the database
	 * 
	 * @param partyClassificationTypeId:
	 *            the id of the PartyClassificationType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyClassificationTypeById(@RequestParam(value = "partyClassificationTypeId") String partyClassificationTypeId) {

		DeletePartyClassificationType com = new DeletePartyClassificationType(partyClassificationTypeId);

		int usedTicketId;

		synchronized (PartyClassificationTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationTypeDeleted.class,
				event -> sendPartyClassificationTypeChangedMessage(((PartyClassificationTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyClassificationTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyClassificationType/\" plus one of the following: "
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
