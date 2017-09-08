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
import com.skytala.eCommerce.command.AddPartyAcctgPreference;
import com.skytala.eCommerce.command.DeletePartyAcctgPreference;
import com.skytala.eCommerce.command.UpdatePartyAcctgPreference;
import com.skytala.eCommerce.entity.PartyAcctgPreference;
import com.skytala.eCommerce.entity.PartyAcctgPreferenceMapper;
import com.skytala.eCommerce.event.PartyAcctgPreferenceAdded;
import com.skytala.eCommerce.event.PartyAcctgPreferenceDeleted;
import com.skytala.eCommerce.event.PartyAcctgPreferenceFound;
import com.skytala.eCommerce.event.PartyAcctgPreferenceUpdated;
import com.skytala.eCommerce.query.FindPartyAcctgPreferencesBy;

@RestController
@RequestMapping("/api/partyAcctgPreference")
public class PartyAcctgPreferenceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyAcctgPreference>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyAcctgPreferenceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyAcctgPreference
	 * @return a List with the PartyAcctgPreferences
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyAcctgPreference> findPartyAcctgPreferencesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyAcctgPreferencesBy query = new FindPartyAcctgPreferencesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyAcctgPreferenceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyAcctgPreferenceFound.class,
				event -> sendPartyAcctgPreferencesFoundMessage(((PartyAcctgPreferenceFound) event).getPartyAcctgPreferences(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyAcctgPreferencesFoundMessage(List<PartyAcctgPreference> partyAcctgPreferences, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyAcctgPreferences);
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
	public boolean createPartyAcctgPreference(HttpServletRequest request) {

		PartyAcctgPreference partyAcctgPreferenceToBeAdded = new PartyAcctgPreference();
		try {
			partyAcctgPreferenceToBeAdded = PartyAcctgPreferenceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyAcctgPreference(partyAcctgPreferenceToBeAdded);

	}

	/**
	 * creates a new PartyAcctgPreference entry in the ofbiz database
	 * 
	 * @param partyAcctgPreferenceToBeAdded
	 *            the PartyAcctgPreference thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyAcctgPreference(PartyAcctgPreference partyAcctgPreferenceToBeAdded) {

		AddPartyAcctgPreference com = new AddPartyAcctgPreference(partyAcctgPreferenceToBeAdded);
		int usedTicketId;

		synchronized (PartyAcctgPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyAcctgPreferenceAdded.class,
				event -> sendPartyAcctgPreferenceChangedMessage(((PartyAcctgPreferenceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyAcctgPreference(HttpServletRequest request) {

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

		PartyAcctgPreference partyAcctgPreferenceToBeUpdated = new PartyAcctgPreference();

		try {
			partyAcctgPreferenceToBeUpdated = PartyAcctgPreferenceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyAcctgPreference(partyAcctgPreferenceToBeUpdated);

	}

	/**
	 * Updates the PartyAcctgPreference with the specific Id
	 * 
	 * @param partyAcctgPreferenceToBeUpdated the PartyAcctgPreference thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyAcctgPreference(PartyAcctgPreference partyAcctgPreferenceToBeUpdated) {

		UpdatePartyAcctgPreference com = new UpdatePartyAcctgPreference(partyAcctgPreferenceToBeUpdated);

		int usedTicketId;

		synchronized (PartyAcctgPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyAcctgPreferenceUpdated.class,
				event -> sendPartyAcctgPreferenceChangedMessage(((PartyAcctgPreferenceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyAcctgPreference from the database
	 * 
	 * @param partyAcctgPreferenceId:
	 *            the id of the PartyAcctgPreference thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyAcctgPreferenceById(@RequestParam(value = "partyAcctgPreferenceId") String partyAcctgPreferenceId) {

		DeletePartyAcctgPreference com = new DeletePartyAcctgPreference(partyAcctgPreferenceId);

		int usedTicketId;

		synchronized (PartyAcctgPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyAcctgPreferenceDeleted.class,
				event -> sendPartyAcctgPreferenceChangedMessage(((PartyAcctgPreferenceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyAcctgPreferenceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyAcctgPreference/\" plus one of the following: "
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
