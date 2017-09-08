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
import com.skytala.eCommerce.command.AddContactList;
import com.skytala.eCommerce.command.DeleteContactList;
import com.skytala.eCommerce.command.UpdateContactList;
import com.skytala.eCommerce.entity.ContactList;
import com.skytala.eCommerce.entity.ContactListMapper;
import com.skytala.eCommerce.event.ContactListAdded;
import com.skytala.eCommerce.event.ContactListDeleted;
import com.skytala.eCommerce.event.ContactListFound;
import com.skytala.eCommerce.event.ContactListUpdated;
import com.skytala.eCommerce.query.FindContactListsBy;

@RestController
@RequestMapping("/api/contactList")
public class ContactListController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactList>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactListController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactList
	 * @return a List with the ContactLists
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactList> findContactListsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactListsBy query = new FindContactListsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactListController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListFound.class,
				event -> sendContactListsFoundMessage(((ContactListFound) event).getContactLists(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactListsFoundMessage(List<ContactList> contactLists, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactLists);
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
	public boolean createContactList(HttpServletRequest request) {

		ContactList contactListToBeAdded = new ContactList();
		try {
			contactListToBeAdded = ContactListMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactList(contactListToBeAdded);

	}

	/**
	 * creates a new ContactList entry in the ofbiz database
	 * 
	 * @param contactListToBeAdded
	 *            the ContactList thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactList(ContactList contactListToBeAdded) {

		AddContactList com = new AddContactList(contactListToBeAdded);
		int usedTicketId;

		synchronized (ContactListController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListAdded.class,
				event -> sendContactListChangedMessage(((ContactListAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactList(HttpServletRequest request) {

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

		ContactList contactListToBeUpdated = new ContactList();

		try {
			contactListToBeUpdated = ContactListMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactList(contactListToBeUpdated);

	}

	/**
	 * Updates the ContactList with the specific Id
	 * 
	 * @param contactListToBeUpdated the ContactList thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactList(ContactList contactListToBeUpdated) {

		UpdateContactList com = new UpdateContactList(contactListToBeUpdated);

		int usedTicketId;

		synchronized (ContactListController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListUpdated.class,
				event -> sendContactListChangedMessage(((ContactListUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactList from the database
	 * 
	 * @param contactListId:
	 *            the id of the ContactList thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactListById(@RequestParam(value = "contactListId") String contactListId) {

		DeleteContactList com = new DeleteContactList(contactListId);

		int usedTicketId;

		synchronized (ContactListController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListDeleted.class,
				event -> sendContactListChangedMessage(((ContactListDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactListChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactList/\" plus one of the following: "
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
