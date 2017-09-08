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
import com.skytala.eCommerce.command.AddPartyNote;
import com.skytala.eCommerce.command.DeletePartyNote;
import com.skytala.eCommerce.command.UpdatePartyNote;
import com.skytala.eCommerce.entity.PartyNote;
import com.skytala.eCommerce.entity.PartyNoteMapper;
import com.skytala.eCommerce.event.PartyNoteAdded;
import com.skytala.eCommerce.event.PartyNoteDeleted;
import com.skytala.eCommerce.event.PartyNoteFound;
import com.skytala.eCommerce.event.PartyNoteUpdated;
import com.skytala.eCommerce.query.FindPartyNotesBy;

@RestController
@RequestMapping("/api/partyNote")
public class PartyNoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyNote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyNoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyNote
	 * @return a List with the PartyNotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyNote> findPartyNotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyNotesBy query = new FindPartyNotesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyNoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNoteFound.class,
				event -> sendPartyNotesFoundMessage(((PartyNoteFound) event).getPartyNotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyNotesFoundMessage(List<PartyNote> partyNotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyNotes);
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
	public boolean createPartyNote(HttpServletRequest request) {

		PartyNote partyNoteToBeAdded = new PartyNote();
		try {
			partyNoteToBeAdded = PartyNoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyNote(partyNoteToBeAdded);

	}

	/**
	 * creates a new PartyNote entry in the ofbiz database
	 * 
	 * @param partyNoteToBeAdded
	 *            the PartyNote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyNote(PartyNote partyNoteToBeAdded) {

		AddPartyNote com = new AddPartyNote(partyNoteToBeAdded);
		int usedTicketId;

		synchronized (PartyNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNoteAdded.class,
				event -> sendPartyNoteChangedMessage(((PartyNoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyNote(HttpServletRequest request) {

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

		PartyNote partyNoteToBeUpdated = new PartyNote();

		try {
			partyNoteToBeUpdated = PartyNoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyNote(partyNoteToBeUpdated);

	}

	/**
	 * Updates the PartyNote with the specific Id
	 * 
	 * @param partyNoteToBeUpdated the PartyNote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyNote(PartyNote partyNoteToBeUpdated) {

		UpdatePartyNote com = new UpdatePartyNote(partyNoteToBeUpdated);

		int usedTicketId;

		synchronized (PartyNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNoteUpdated.class,
				event -> sendPartyNoteChangedMessage(((PartyNoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyNote from the database
	 * 
	 * @param partyNoteId:
	 *            the id of the PartyNote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyNoteById(@RequestParam(value = "partyNoteId") String partyNoteId) {

		DeletePartyNote com = new DeletePartyNote(partyNoteId);

		int usedTicketId;

		synchronized (PartyNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyNoteDeleted.class,
				event -> sendPartyNoteChangedMessage(((PartyNoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyNoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyNote/\" plus one of the following: "
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
