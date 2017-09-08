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
import com.skytala.eCommerce.command.AddContactListParty;
import com.skytala.eCommerce.command.DeleteContactListParty;
import com.skytala.eCommerce.command.UpdateContactListParty;
import com.skytala.eCommerce.entity.ContactListParty;
import com.skytala.eCommerce.entity.ContactListPartyMapper;
import com.skytala.eCommerce.event.ContactListPartyAdded;
import com.skytala.eCommerce.event.ContactListPartyDeleted;
import com.skytala.eCommerce.event.ContactListPartyFound;
import com.skytala.eCommerce.event.ContactListPartyUpdated;
import com.skytala.eCommerce.query.FindContactListPartysBy;

@RestController
@RequestMapping("/api/contactListParty")
public class ContactListPartyController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactListParty>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactListPartyController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactListParty
	 * @return a List with the ContactListPartys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactListParty> findContactListPartysBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactListPartysBy query = new FindContactListPartysBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactListPartyController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListPartyFound.class,
				event -> sendContactListPartysFoundMessage(((ContactListPartyFound) event).getContactListPartys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactListPartysFoundMessage(List<ContactListParty> contactListPartys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactListPartys);
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
	public boolean createContactListParty(HttpServletRequest request) {

		ContactListParty contactListPartyToBeAdded = new ContactListParty();
		try {
			contactListPartyToBeAdded = ContactListPartyMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactListParty(contactListPartyToBeAdded);

	}

	/**
	 * creates a new ContactListParty entry in the ofbiz database
	 * 
	 * @param contactListPartyToBeAdded
	 *            the ContactListParty thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactListParty(ContactListParty contactListPartyToBeAdded) {

		AddContactListParty com = new AddContactListParty(contactListPartyToBeAdded);
		int usedTicketId;

		synchronized (ContactListPartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListPartyAdded.class,
				event -> sendContactListPartyChangedMessage(((ContactListPartyAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactListParty(HttpServletRequest request) {

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

		ContactListParty contactListPartyToBeUpdated = new ContactListParty();

		try {
			contactListPartyToBeUpdated = ContactListPartyMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactListParty(contactListPartyToBeUpdated);

	}

	/**
	 * Updates the ContactListParty with the specific Id
	 * 
	 * @param contactListPartyToBeUpdated the ContactListParty thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactListParty(ContactListParty contactListPartyToBeUpdated) {

		UpdateContactListParty com = new UpdateContactListParty(contactListPartyToBeUpdated);

		int usedTicketId;

		synchronized (ContactListPartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListPartyUpdated.class,
				event -> sendContactListPartyChangedMessage(((ContactListPartyUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactListParty from the database
	 * 
	 * @param contactListPartyId:
	 *            the id of the ContactListParty thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactListPartyById(@RequestParam(value = "contactListPartyId") String contactListPartyId) {

		DeleteContactListParty com = new DeleteContactListParty(contactListPartyId);

		int usedTicketId;

		synchronized (ContactListPartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListPartyDeleted.class,
				event -> sendContactListPartyChangedMessage(((ContactListPartyDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactListPartyChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactListParty/\" plus one of the following: "
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
