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
import com.skytala.eCommerce.command.AddPartyAttribute;
import com.skytala.eCommerce.command.DeletePartyAttribute;
import com.skytala.eCommerce.command.UpdatePartyAttribute;
import com.skytala.eCommerce.entity.PartyAttribute;
import com.skytala.eCommerce.entity.PartyAttributeMapper;
import com.skytala.eCommerce.event.PartyAttributeAdded;
import com.skytala.eCommerce.event.PartyAttributeDeleted;
import com.skytala.eCommerce.event.PartyAttributeFound;
import com.skytala.eCommerce.event.PartyAttributeUpdated;
import com.skytala.eCommerce.query.FindPartyAttributesBy;

@RestController
@RequestMapping("/api/partyAttribute")
public class PartyAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyAttribute
	 * @return a List with the PartyAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyAttribute> findPartyAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyAttributesBy query = new FindPartyAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyAttributeFound.class,
				event -> sendPartyAttributesFoundMessage(((PartyAttributeFound) event).getPartyAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyAttributesFoundMessage(List<PartyAttribute> partyAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyAttributes);
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
	public boolean createPartyAttribute(HttpServletRequest request) {

		PartyAttribute partyAttributeToBeAdded = new PartyAttribute();
		try {
			partyAttributeToBeAdded = PartyAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyAttribute(partyAttributeToBeAdded);

	}

	/**
	 * creates a new PartyAttribute entry in the ofbiz database
	 * 
	 * @param partyAttributeToBeAdded
	 *            the PartyAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyAttribute(PartyAttribute partyAttributeToBeAdded) {

		AddPartyAttribute com = new AddPartyAttribute(partyAttributeToBeAdded);
		int usedTicketId;

		synchronized (PartyAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyAttributeAdded.class,
				event -> sendPartyAttributeChangedMessage(((PartyAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyAttribute(HttpServletRequest request) {

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

		PartyAttribute partyAttributeToBeUpdated = new PartyAttribute();

		try {
			partyAttributeToBeUpdated = PartyAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyAttribute(partyAttributeToBeUpdated);

	}

	/**
	 * Updates the PartyAttribute with the specific Id
	 * 
	 * @param partyAttributeToBeUpdated the PartyAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyAttribute(PartyAttribute partyAttributeToBeUpdated) {

		UpdatePartyAttribute com = new UpdatePartyAttribute(partyAttributeToBeUpdated);

		int usedTicketId;

		synchronized (PartyAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyAttributeUpdated.class,
				event -> sendPartyAttributeChangedMessage(((PartyAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyAttribute from the database
	 * 
	 * @param partyAttributeId:
	 *            the id of the PartyAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyAttributeById(@RequestParam(value = "partyAttributeId") String partyAttributeId) {

		DeletePartyAttribute com = new DeletePartyAttribute(partyAttributeId);

		int usedTicketId;

		synchronized (PartyAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyAttributeDeleted.class,
				event -> sendPartyAttributeChangedMessage(((PartyAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyAttribute/\" plus one of the following: "
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
