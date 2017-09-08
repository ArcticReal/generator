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
import com.skytala.eCommerce.command.AddPartyInvitationRoleAssoc;
import com.skytala.eCommerce.command.DeletePartyInvitationRoleAssoc;
import com.skytala.eCommerce.command.UpdatePartyInvitationRoleAssoc;
import com.skytala.eCommerce.entity.PartyInvitationRoleAssoc;
import com.skytala.eCommerce.entity.PartyInvitationRoleAssocMapper;
import com.skytala.eCommerce.event.PartyInvitationRoleAssocAdded;
import com.skytala.eCommerce.event.PartyInvitationRoleAssocDeleted;
import com.skytala.eCommerce.event.PartyInvitationRoleAssocFound;
import com.skytala.eCommerce.event.PartyInvitationRoleAssocUpdated;
import com.skytala.eCommerce.query.FindPartyInvitationRoleAssocsBy;

@RestController
@RequestMapping("/api/partyInvitationRoleAssoc")
public class PartyInvitationRoleAssocController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyInvitationRoleAssoc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyInvitationRoleAssocController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyInvitationRoleAssoc
	 * @return a List with the PartyInvitationRoleAssocs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyInvitationRoleAssoc> findPartyInvitationRoleAssocsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyInvitationRoleAssocsBy query = new FindPartyInvitationRoleAssocsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyInvitationRoleAssocController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationRoleAssocFound.class,
				event -> sendPartyInvitationRoleAssocsFoundMessage(((PartyInvitationRoleAssocFound) event).getPartyInvitationRoleAssocs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyInvitationRoleAssocsFoundMessage(List<PartyInvitationRoleAssoc> partyInvitationRoleAssocs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyInvitationRoleAssocs);
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
	public boolean createPartyInvitationRoleAssoc(HttpServletRequest request) {

		PartyInvitationRoleAssoc partyInvitationRoleAssocToBeAdded = new PartyInvitationRoleAssoc();
		try {
			partyInvitationRoleAssocToBeAdded = PartyInvitationRoleAssocMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyInvitationRoleAssoc(partyInvitationRoleAssocToBeAdded);

	}

	/**
	 * creates a new PartyInvitationRoleAssoc entry in the ofbiz database
	 * 
	 * @param partyInvitationRoleAssocToBeAdded
	 *            the PartyInvitationRoleAssoc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyInvitationRoleAssoc(PartyInvitationRoleAssoc partyInvitationRoleAssocToBeAdded) {

		AddPartyInvitationRoleAssoc com = new AddPartyInvitationRoleAssoc(partyInvitationRoleAssocToBeAdded);
		int usedTicketId;

		synchronized (PartyInvitationRoleAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationRoleAssocAdded.class,
				event -> sendPartyInvitationRoleAssocChangedMessage(((PartyInvitationRoleAssocAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyInvitationRoleAssoc(HttpServletRequest request) {

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

		PartyInvitationRoleAssoc partyInvitationRoleAssocToBeUpdated = new PartyInvitationRoleAssoc();

		try {
			partyInvitationRoleAssocToBeUpdated = PartyInvitationRoleAssocMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyInvitationRoleAssoc(partyInvitationRoleAssocToBeUpdated);

	}

	/**
	 * Updates the PartyInvitationRoleAssoc with the specific Id
	 * 
	 * @param partyInvitationRoleAssocToBeUpdated the PartyInvitationRoleAssoc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyInvitationRoleAssoc(PartyInvitationRoleAssoc partyInvitationRoleAssocToBeUpdated) {

		UpdatePartyInvitationRoleAssoc com = new UpdatePartyInvitationRoleAssoc(partyInvitationRoleAssocToBeUpdated);

		int usedTicketId;

		synchronized (PartyInvitationRoleAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationRoleAssocUpdated.class,
				event -> sendPartyInvitationRoleAssocChangedMessage(((PartyInvitationRoleAssocUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyInvitationRoleAssoc from the database
	 * 
	 * @param partyInvitationRoleAssocId:
	 *            the id of the PartyInvitationRoleAssoc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyInvitationRoleAssocById(@RequestParam(value = "partyInvitationRoleAssocId") String partyInvitationRoleAssocId) {

		DeletePartyInvitationRoleAssoc com = new DeletePartyInvitationRoleAssoc(partyInvitationRoleAssocId);

		int usedTicketId;

		synchronized (PartyInvitationRoleAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyInvitationRoleAssocDeleted.class,
				event -> sendPartyInvitationRoleAssocChangedMessage(((PartyInvitationRoleAssocDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyInvitationRoleAssocChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyInvitationRoleAssoc/\" plus one of the following: "
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
