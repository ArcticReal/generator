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
import com.skytala.eCommerce.command.AddContactListType;
import com.skytala.eCommerce.command.DeleteContactListType;
import com.skytala.eCommerce.command.UpdateContactListType;
import com.skytala.eCommerce.entity.ContactListType;
import com.skytala.eCommerce.entity.ContactListTypeMapper;
import com.skytala.eCommerce.event.ContactListTypeAdded;
import com.skytala.eCommerce.event.ContactListTypeDeleted;
import com.skytala.eCommerce.event.ContactListTypeFound;
import com.skytala.eCommerce.event.ContactListTypeUpdated;
import com.skytala.eCommerce.query.FindContactListTypesBy;

@RestController
@RequestMapping("/api/contactListType")
public class ContactListTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactListType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactListTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactListType
	 * @return a List with the ContactListTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactListType> findContactListTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactListTypesBy query = new FindContactListTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactListTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListTypeFound.class,
				event -> sendContactListTypesFoundMessage(((ContactListTypeFound) event).getContactListTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactListTypesFoundMessage(List<ContactListType> contactListTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactListTypes);
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
	public boolean createContactListType(HttpServletRequest request) {

		ContactListType contactListTypeToBeAdded = new ContactListType();
		try {
			contactListTypeToBeAdded = ContactListTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactListType(contactListTypeToBeAdded);

	}

	/**
	 * creates a new ContactListType entry in the ofbiz database
	 * 
	 * @param contactListTypeToBeAdded
	 *            the ContactListType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactListType(ContactListType contactListTypeToBeAdded) {

		AddContactListType com = new AddContactListType(contactListTypeToBeAdded);
		int usedTicketId;

		synchronized (ContactListTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListTypeAdded.class,
				event -> sendContactListTypeChangedMessage(((ContactListTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactListType(HttpServletRequest request) {

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

		ContactListType contactListTypeToBeUpdated = new ContactListType();

		try {
			contactListTypeToBeUpdated = ContactListTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactListType(contactListTypeToBeUpdated);

	}

	/**
	 * Updates the ContactListType with the specific Id
	 * 
	 * @param contactListTypeToBeUpdated the ContactListType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactListType(ContactListType contactListTypeToBeUpdated) {

		UpdateContactListType com = new UpdateContactListType(contactListTypeToBeUpdated);

		int usedTicketId;

		synchronized (ContactListTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListTypeUpdated.class,
				event -> sendContactListTypeChangedMessage(((ContactListTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactListType from the database
	 * 
	 * @param contactListTypeId:
	 *            the id of the ContactListType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactListTypeById(@RequestParam(value = "contactListTypeId") String contactListTypeId) {

		DeleteContactListType com = new DeleteContactListType(contactListTypeId);

		int usedTicketId;

		synchronized (ContactListTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListTypeDeleted.class,
				event -> sendContactListTypeChangedMessage(((ContactListTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactListTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactListType/\" plus one of the following: "
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
