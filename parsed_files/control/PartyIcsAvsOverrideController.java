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
import com.skytala.eCommerce.command.AddPartyIcsAvsOverride;
import com.skytala.eCommerce.command.DeletePartyIcsAvsOverride;
import com.skytala.eCommerce.command.UpdatePartyIcsAvsOverride;
import com.skytala.eCommerce.entity.PartyIcsAvsOverride;
import com.skytala.eCommerce.entity.PartyIcsAvsOverrideMapper;
import com.skytala.eCommerce.event.PartyIcsAvsOverrideAdded;
import com.skytala.eCommerce.event.PartyIcsAvsOverrideDeleted;
import com.skytala.eCommerce.event.PartyIcsAvsOverrideFound;
import com.skytala.eCommerce.event.PartyIcsAvsOverrideUpdated;
import com.skytala.eCommerce.query.FindPartyIcsAvsOverridesBy;

@RestController
@RequestMapping("/api/partyIcsAvsOverride")
public class PartyIcsAvsOverrideController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyIcsAvsOverride>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyIcsAvsOverrideController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyIcsAvsOverride
	 * @return a List with the PartyIcsAvsOverrides
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyIcsAvsOverride> findPartyIcsAvsOverridesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyIcsAvsOverridesBy query = new FindPartyIcsAvsOverridesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyIcsAvsOverrideController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIcsAvsOverrideFound.class,
				event -> sendPartyIcsAvsOverridesFoundMessage(((PartyIcsAvsOverrideFound) event).getPartyIcsAvsOverrides(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyIcsAvsOverridesFoundMessage(List<PartyIcsAvsOverride> partyIcsAvsOverrides, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyIcsAvsOverrides);
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
	public boolean createPartyIcsAvsOverride(HttpServletRequest request) {

		PartyIcsAvsOverride partyIcsAvsOverrideToBeAdded = new PartyIcsAvsOverride();
		try {
			partyIcsAvsOverrideToBeAdded = PartyIcsAvsOverrideMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyIcsAvsOverride(partyIcsAvsOverrideToBeAdded);

	}

	/**
	 * creates a new PartyIcsAvsOverride entry in the ofbiz database
	 * 
	 * @param partyIcsAvsOverrideToBeAdded
	 *            the PartyIcsAvsOverride thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyIcsAvsOverride(PartyIcsAvsOverride partyIcsAvsOverrideToBeAdded) {

		AddPartyIcsAvsOverride com = new AddPartyIcsAvsOverride(partyIcsAvsOverrideToBeAdded);
		int usedTicketId;

		synchronized (PartyIcsAvsOverrideController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIcsAvsOverrideAdded.class,
				event -> sendPartyIcsAvsOverrideChangedMessage(((PartyIcsAvsOverrideAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyIcsAvsOverride(HttpServletRequest request) {

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

		PartyIcsAvsOverride partyIcsAvsOverrideToBeUpdated = new PartyIcsAvsOverride();

		try {
			partyIcsAvsOverrideToBeUpdated = PartyIcsAvsOverrideMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyIcsAvsOverride(partyIcsAvsOverrideToBeUpdated);

	}

	/**
	 * Updates the PartyIcsAvsOverride with the specific Id
	 * 
	 * @param partyIcsAvsOverrideToBeUpdated the PartyIcsAvsOverride thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyIcsAvsOverride(PartyIcsAvsOverride partyIcsAvsOverrideToBeUpdated) {

		UpdatePartyIcsAvsOverride com = new UpdatePartyIcsAvsOverride(partyIcsAvsOverrideToBeUpdated);

		int usedTicketId;

		synchronized (PartyIcsAvsOverrideController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIcsAvsOverrideUpdated.class,
				event -> sendPartyIcsAvsOverrideChangedMessage(((PartyIcsAvsOverrideUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyIcsAvsOverride from the database
	 * 
	 * @param partyIcsAvsOverrideId:
	 *            the id of the PartyIcsAvsOverride thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyIcsAvsOverrideById(@RequestParam(value = "partyIcsAvsOverrideId") String partyIcsAvsOverrideId) {

		DeletePartyIcsAvsOverride com = new DeletePartyIcsAvsOverride(partyIcsAvsOverrideId);

		int usedTicketId;

		synchronized (PartyIcsAvsOverrideController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyIcsAvsOverrideDeleted.class,
				event -> sendPartyIcsAvsOverrideChangedMessage(((PartyIcsAvsOverrideDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyIcsAvsOverrideChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyIcsAvsOverride/\" plus one of the following: "
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
