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
import com.skytala.eCommerce.command.AddPartyContentType;
import com.skytala.eCommerce.command.DeletePartyContentType;
import com.skytala.eCommerce.command.UpdatePartyContentType;
import com.skytala.eCommerce.entity.PartyContentType;
import com.skytala.eCommerce.entity.PartyContentTypeMapper;
import com.skytala.eCommerce.event.PartyContentTypeAdded;
import com.skytala.eCommerce.event.PartyContentTypeDeleted;
import com.skytala.eCommerce.event.PartyContentTypeFound;
import com.skytala.eCommerce.event.PartyContentTypeUpdated;
import com.skytala.eCommerce.query.FindPartyContentTypesBy;

@RestController
@RequestMapping("/api/partyContentType")
public class PartyContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyContentType
	 * @return a List with the PartyContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyContentType> findPartyContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyContentTypesBy query = new FindPartyContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContentTypeFound.class,
				event -> sendPartyContentTypesFoundMessage(((PartyContentTypeFound) event).getPartyContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyContentTypesFoundMessage(List<PartyContentType> partyContentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyContentTypes);
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
	public boolean createPartyContentType(HttpServletRequest request) {

		PartyContentType partyContentTypeToBeAdded = new PartyContentType();
		try {
			partyContentTypeToBeAdded = PartyContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyContentType(partyContentTypeToBeAdded);

	}

	/**
	 * creates a new PartyContentType entry in the ofbiz database
	 * 
	 * @param partyContentTypeToBeAdded
	 *            the PartyContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyContentType(PartyContentType partyContentTypeToBeAdded) {

		AddPartyContentType com = new AddPartyContentType(partyContentTypeToBeAdded);
		int usedTicketId;

		synchronized (PartyContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContentTypeAdded.class,
				event -> sendPartyContentTypeChangedMessage(((PartyContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyContentType(HttpServletRequest request) {

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

		PartyContentType partyContentTypeToBeUpdated = new PartyContentType();

		try {
			partyContentTypeToBeUpdated = PartyContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyContentType(partyContentTypeToBeUpdated);

	}

	/**
	 * Updates the PartyContentType with the specific Id
	 * 
	 * @param partyContentTypeToBeUpdated the PartyContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyContentType(PartyContentType partyContentTypeToBeUpdated) {

		UpdatePartyContentType com = new UpdatePartyContentType(partyContentTypeToBeUpdated);

		int usedTicketId;

		synchronized (PartyContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContentTypeUpdated.class,
				event -> sendPartyContentTypeChangedMessage(((PartyContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyContentType from the database
	 * 
	 * @param partyContentTypeId:
	 *            the id of the PartyContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyContentTypeById(@RequestParam(value = "partyContentTypeId") String partyContentTypeId) {

		DeletePartyContentType com = new DeletePartyContentType(partyContentTypeId);

		int usedTicketId;

		synchronized (PartyContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContentTypeDeleted.class,
				event -> sendPartyContentTypeChangedMessage(((PartyContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyContentType/\" plus one of the following: "
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
