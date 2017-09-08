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
import com.skytala.eCommerce.command.AddPartyNeed;
import com.skytala.eCommerce.command.DeletePartyNeed;
import com.skytala.eCommerce.command.UpdatePartyNeed;
import com.skytala.eCommerce.entity.PartyNeed;
import com.skytala.eCommerce.entity.PartyNeedMapper;
import com.skytala.eCommerce.event.PartyNeedAdded;
import com.skytala.eCommerce.event.PartyNeedDeleted;
import com.skytala.eCommerce.event.PartyNeedFound;
import com.skytala.eCommerce.event.PartyNeedUpdated;
import com.skytala.eCommerce.query.FindPartyNeedsBy;

@RestController
@RequestMapping("/api/partyNeed")
public class PartyNeedController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyNeed>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyNeedController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyNeed
	 * @return a List with the PartyNeeds
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyNeed> findPartyNeedsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyNeedsBy query = new FindPartyNeedsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyNeedController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNeedFound.class,
				event -> sendPartyNeedsFoundMessage(((PartyNeedFound) event).getPartyNeeds(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyNeedsFoundMessage(List<PartyNeed> partyNeeds, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyNeeds);
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
	public boolean createPartyNeed(HttpServletRequest request) {

		PartyNeed partyNeedToBeAdded = new PartyNeed();
		try {
			partyNeedToBeAdded = PartyNeedMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyNeed(partyNeedToBeAdded);

	}

	/**
	 * creates a new PartyNeed entry in the ofbiz database
	 * 
	 * @param partyNeedToBeAdded
	 *            the PartyNeed thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyNeed(PartyNeed partyNeedToBeAdded) {

		AddPartyNeed com = new AddPartyNeed(partyNeedToBeAdded);
		int usedTicketId;

		synchronized (PartyNeedController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNeedAdded.class,
				event -> sendPartyNeedChangedMessage(((PartyNeedAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyNeed(HttpServletRequest request) {

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

		PartyNeed partyNeedToBeUpdated = new PartyNeed();

		try {
			partyNeedToBeUpdated = PartyNeedMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyNeed(partyNeedToBeUpdated);

	}

	/**
	 * Updates the PartyNeed with the specific Id
	 * 
	 * @param partyNeedToBeUpdated the PartyNeed thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyNeed(PartyNeed partyNeedToBeUpdated) {

		UpdatePartyNeed com = new UpdatePartyNeed(partyNeedToBeUpdated);

		int usedTicketId;

		synchronized (PartyNeedController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNeedUpdated.class,
				event -> sendPartyNeedChangedMessage(((PartyNeedUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyNeed from the database
	 * 
	 * @param partyNeedId:
	 *            the id of the PartyNeed thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyNeedById(@RequestParam(value = "partyNeedId") String partyNeedId) {

		DeletePartyNeed com = new DeletePartyNeed(partyNeedId);

		int usedTicketId;

		synchronized (PartyNeedController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNeedDeleted.class,
				event -> sendPartyNeedChangedMessage(((PartyNeedDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyNeedChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyNeed/\" plus one of the following: "
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
