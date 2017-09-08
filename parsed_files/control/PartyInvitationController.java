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
import com.skytala.eCommerce.command.AddPartyInvitation;
import com.skytala.eCommerce.command.DeletePartyInvitation;
import com.skytala.eCommerce.command.UpdatePartyInvitation;
import com.skytala.eCommerce.entity.PartyInvitation;
import com.skytala.eCommerce.entity.PartyInvitationMapper;
import com.skytala.eCommerce.event.PartyInvitationAdded;
import com.skytala.eCommerce.event.PartyInvitationDeleted;
import com.skytala.eCommerce.event.PartyInvitationFound;
import com.skytala.eCommerce.event.PartyInvitationUpdated;
import com.skytala.eCommerce.query.FindPartyInvitationsBy;

@RestController
@RequestMapping("/api/partyInvitation")
public class PartyInvitationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyInvitation>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyInvitationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyInvitation
	 * @return a List with the PartyInvitations
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyInvitation> findPartyInvitationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyInvitationsBy query = new FindPartyInvitationsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyInvitationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationFound.class,
				event -> sendPartyInvitationsFoundMessage(((PartyInvitationFound) event).getPartyInvitations(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyInvitationsFoundMessage(List<PartyInvitation> partyInvitations, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyInvitations);
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
	public boolean createPartyInvitation(HttpServletRequest request) {

		PartyInvitation partyInvitationToBeAdded = new PartyInvitation();
		try {
			partyInvitationToBeAdded = PartyInvitationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyInvitation(partyInvitationToBeAdded);

	}

	/**
	 * creates a new PartyInvitation entry in the ofbiz database
	 * 
	 * @param partyInvitationToBeAdded
	 *            the PartyInvitation thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyInvitation(PartyInvitation partyInvitationToBeAdded) {

		AddPartyInvitation com = new AddPartyInvitation(partyInvitationToBeAdded);
		int usedTicketId;

		synchronized (PartyInvitationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationAdded.class,
				event -> sendPartyInvitationChangedMessage(((PartyInvitationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyInvitation(HttpServletRequest request) {

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

		PartyInvitation partyInvitationToBeUpdated = new PartyInvitation();

		try {
			partyInvitationToBeUpdated = PartyInvitationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyInvitation(partyInvitationToBeUpdated);

	}

	/**
	 * Updates the PartyInvitation with the specific Id
	 * 
	 * @param partyInvitationToBeUpdated the PartyInvitation thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyInvitation(PartyInvitation partyInvitationToBeUpdated) {

		UpdatePartyInvitation com = new UpdatePartyInvitation(partyInvitationToBeUpdated);

		int usedTicketId;

		synchronized (PartyInvitationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationUpdated.class,
				event -> sendPartyInvitationChangedMessage(((PartyInvitationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyInvitation from the database
	 * 
	 * @param partyInvitationId:
	 *            the id of the PartyInvitation thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyInvitationById(@RequestParam(value = "partyInvitationId") String partyInvitationId) {

		DeletePartyInvitation com = new DeletePartyInvitation(partyInvitationId);

		int usedTicketId;

		synchronized (PartyInvitationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationDeleted.class,
				event -> sendPartyInvitationChangedMessage(((PartyInvitationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyInvitationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyInvitation/\" plus one of the following: "
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
