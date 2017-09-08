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
import com.skytala.eCommerce.command.AddPartyNameHistory;
import com.skytala.eCommerce.command.DeletePartyNameHistory;
import com.skytala.eCommerce.command.UpdatePartyNameHistory;
import com.skytala.eCommerce.entity.PartyNameHistory;
import com.skytala.eCommerce.entity.PartyNameHistoryMapper;
import com.skytala.eCommerce.event.PartyNameHistoryAdded;
import com.skytala.eCommerce.event.PartyNameHistoryDeleted;
import com.skytala.eCommerce.event.PartyNameHistoryFound;
import com.skytala.eCommerce.event.PartyNameHistoryUpdated;
import com.skytala.eCommerce.query.FindPartyNameHistorysBy;

@RestController
@RequestMapping("/api/partyNameHistory")
public class PartyNameHistoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyNameHistory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyNameHistoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyNameHistory
	 * @return a List with the PartyNameHistorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyNameHistory> findPartyNameHistorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyNameHistorysBy query = new FindPartyNameHistorysBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyNameHistoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNameHistoryFound.class,
				event -> sendPartyNameHistorysFoundMessage(((PartyNameHistoryFound) event).getPartyNameHistorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyNameHistorysFoundMessage(List<PartyNameHistory> partyNameHistorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyNameHistorys);
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
	public boolean createPartyNameHistory(HttpServletRequest request) {

		PartyNameHistory partyNameHistoryToBeAdded = new PartyNameHistory();
		try {
			partyNameHistoryToBeAdded = PartyNameHistoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyNameHistory(partyNameHistoryToBeAdded);

	}

	/**
	 * creates a new PartyNameHistory entry in the ofbiz database
	 * 
	 * @param partyNameHistoryToBeAdded
	 *            the PartyNameHistory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyNameHistory(PartyNameHistory partyNameHistoryToBeAdded) {

		AddPartyNameHistory com = new AddPartyNameHistory(partyNameHistoryToBeAdded);
		int usedTicketId;

		synchronized (PartyNameHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNameHistoryAdded.class,
				event -> sendPartyNameHistoryChangedMessage(((PartyNameHistoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyNameHistory(HttpServletRequest request) {

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

		PartyNameHistory partyNameHistoryToBeUpdated = new PartyNameHistory();

		try {
			partyNameHistoryToBeUpdated = PartyNameHistoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyNameHistory(partyNameHistoryToBeUpdated);

	}

	/**
	 * Updates the PartyNameHistory with the specific Id
	 * 
	 * @param partyNameHistoryToBeUpdated the PartyNameHistory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyNameHistory(PartyNameHistory partyNameHistoryToBeUpdated) {

		UpdatePartyNameHistory com = new UpdatePartyNameHistory(partyNameHistoryToBeUpdated);

		int usedTicketId;

		synchronized (PartyNameHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNameHistoryUpdated.class,
				event -> sendPartyNameHistoryChangedMessage(((PartyNameHistoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyNameHistory from the database
	 * 
	 * @param partyNameHistoryId:
	 *            the id of the PartyNameHistory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyNameHistoryById(@RequestParam(value = "partyNameHistoryId") String partyNameHistoryId) {

		DeletePartyNameHistory com = new DeletePartyNameHistory(partyNameHistoryId);

		int usedTicketId;

		synchronized (PartyNameHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNameHistoryDeleted.class,
				event -> sendPartyNameHistoryChangedMessage(((PartyNameHistoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyNameHistoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyNameHistory/\" plus one of the following: "
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
