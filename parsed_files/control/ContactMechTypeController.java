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
import com.skytala.eCommerce.command.AddContactMechType;
import com.skytala.eCommerce.command.DeleteContactMechType;
import com.skytala.eCommerce.command.UpdateContactMechType;
import com.skytala.eCommerce.entity.ContactMechType;
import com.skytala.eCommerce.entity.ContactMechTypeMapper;
import com.skytala.eCommerce.event.ContactMechTypeAdded;
import com.skytala.eCommerce.event.ContactMechTypeDeleted;
import com.skytala.eCommerce.event.ContactMechTypeFound;
import com.skytala.eCommerce.event.ContactMechTypeUpdated;
import com.skytala.eCommerce.query.FindContactMechTypesBy;

@RestController
@RequestMapping("/api/contactMechType")
public class ContactMechTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactMechType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactMechTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactMechType
	 * @return a List with the ContactMechTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactMechType> findContactMechTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactMechTypesBy query = new FindContactMechTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactMechTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypeFound.class,
				event -> sendContactMechTypesFoundMessage(((ContactMechTypeFound) event).getContactMechTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactMechTypesFoundMessage(List<ContactMechType> contactMechTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactMechTypes);
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
	public boolean createContactMechType(HttpServletRequest request) {

		ContactMechType contactMechTypeToBeAdded = new ContactMechType();
		try {
			contactMechTypeToBeAdded = ContactMechTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactMechType(contactMechTypeToBeAdded);

	}

	/**
	 * creates a new ContactMechType entry in the ofbiz database
	 * 
	 * @param contactMechTypeToBeAdded
	 *            the ContactMechType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactMechType(ContactMechType contactMechTypeToBeAdded) {

		AddContactMechType com = new AddContactMechType(contactMechTypeToBeAdded);
		int usedTicketId;

		synchronized (ContactMechTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypeAdded.class,
				event -> sendContactMechTypeChangedMessage(((ContactMechTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactMechType(HttpServletRequest request) {

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

		ContactMechType contactMechTypeToBeUpdated = new ContactMechType();

		try {
			contactMechTypeToBeUpdated = ContactMechTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactMechType(contactMechTypeToBeUpdated);

	}

	/**
	 * Updates the ContactMechType with the specific Id
	 * 
	 * @param contactMechTypeToBeUpdated the ContactMechType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactMechType(ContactMechType contactMechTypeToBeUpdated) {

		UpdateContactMechType com = new UpdateContactMechType(contactMechTypeToBeUpdated);

		int usedTicketId;

		synchronized (ContactMechTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypeUpdated.class,
				event -> sendContactMechTypeChangedMessage(((ContactMechTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactMechType from the database
	 * 
	 * @param contactMechTypeId:
	 *            the id of the ContactMechType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactMechTypeById(@RequestParam(value = "contactMechTypeId") String contactMechTypeId) {

		DeleteContactMechType com = new DeleteContactMechType(contactMechTypeId);

		int usedTicketId;

		synchronized (ContactMechTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypeDeleted.class,
				event -> sendContactMechTypeChangedMessage(((ContactMechTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactMechTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactMechType/\" plus one of the following: "
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
