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
import com.skytala.eCommerce.command.AddPartyInvitationGroupAssoc;
import com.skytala.eCommerce.command.DeletePartyInvitationGroupAssoc;
import com.skytala.eCommerce.command.UpdatePartyInvitationGroupAssoc;
import com.skytala.eCommerce.entity.PartyInvitationGroupAssoc;
import com.skytala.eCommerce.entity.PartyInvitationGroupAssocMapper;
import com.skytala.eCommerce.event.PartyInvitationGroupAssocAdded;
import com.skytala.eCommerce.event.PartyInvitationGroupAssocDeleted;
import com.skytala.eCommerce.event.PartyInvitationGroupAssocFound;
import com.skytala.eCommerce.event.PartyInvitationGroupAssocUpdated;
import com.skytala.eCommerce.query.FindPartyInvitationGroupAssocsBy;

@RestController
@RequestMapping("/api/partyInvitationGroupAssoc")
public class PartyInvitationGroupAssocController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyInvitationGroupAssoc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyInvitationGroupAssocController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyInvitationGroupAssoc
	 * @return a List with the PartyInvitationGroupAssocs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyInvitationGroupAssoc> findPartyInvitationGroupAssocsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyInvitationGroupAssocsBy query = new FindPartyInvitationGroupAssocsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyInvitationGroupAssocController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationGroupAssocFound.class,
				event -> sendPartyInvitationGroupAssocsFoundMessage(((PartyInvitationGroupAssocFound) event).getPartyInvitationGroupAssocs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyInvitationGroupAssocsFoundMessage(List<PartyInvitationGroupAssoc> partyInvitationGroupAssocs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyInvitationGroupAssocs);
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
	public boolean createPartyInvitationGroupAssoc(HttpServletRequest request) {

		PartyInvitationGroupAssoc partyInvitationGroupAssocToBeAdded = new PartyInvitationGroupAssoc();
		try {
			partyInvitationGroupAssocToBeAdded = PartyInvitationGroupAssocMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyInvitationGroupAssoc(partyInvitationGroupAssocToBeAdded);

	}

	/**
	 * creates a new PartyInvitationGroupAssoc entry in the ofbiz database
	 * 
	 * @param partyInvitationGroupAssocToBeAdded
	 *            the PartyInvitationGroupAssoc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyInvitationGroupAssoc(PartyInvitationGroupAssoc partyInvitationGroupAssocToBeAdded) {

		AddPartyInvitationGroupAssoc com = new AddPartyInvitationGroupAssoc(partyInvitationGroupAssocToBeAdded);
		int usedTicketId;

		synchronized (PartyInvitationGroupAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationGroupAssocAdded.class,
				event -> sendPartyInvitationGroupAssocChangedMessage(((PartyInvitationGroupAssocAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyInvitationGroupAssoc(HttpServletRequest request) {

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

		PartyInvitationGroupAssoc partyInvitationGroupAssocToBeUpdated = new PartyInvitationGroupAssoc();

		try {
			partyInvitationGroupAssocToBeUpdated = PartyInvitationGroupAssocMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyInvitationGroupAssoc(partyInvitationGroupAssocToBeUpdated);

	}

	/**
	 * Updates the PartyInvitationGroupAssoc with the specific Id
	 * 
	 * @param partyInvitationGroupAssocToBeUpdated the PartyInvitationGroupAssoc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyInvitationGroupAssoc(PartyInvitationGroupAssoc partyInvitationGroupAssocToBeUpdated) {

		UpdatePartyInvitationGroupAssoc com = new UpdatePartyInvitationGroupAssoc(partyInvitationGroupAssocToBeUpdated);

		int usedTicketId;

		synchronized (PartyInvitationGroupAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationGroupAssocUpdated.class,
				event -> sendPartyInvitationGroupAssocChangedMessage(((PartyInvitationGroupAssocUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyInvitationGroupAssoc from the database
	 * 
	 * @param partyInvitationGroupAssocId:
	 *            the id of the PartyInvitationGroupAssoc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyInvitationGroupAssocById(@RequestParam(value = "partyInvitationGroupAssocId") String partyInvitationGroupAssocId) {

		DeletePartyInvitationGroupAssoc com = new DeletePartyInvitationGroupAssoc(partyInvitationGroupAssocId);

		int usedTicketId;

		synchronized (PartyInvitationGroupAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationGroupAssocDeleted.class,
				event -> sendPartyInvitationGroupAssocChangedMessage(((PartyInvitationGroupAssocDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyInvitationGroupAssocChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyInvitationGroupAssoc/\" plus one of the following: "
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
