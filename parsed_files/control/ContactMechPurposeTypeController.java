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
import com.skytala.eCommerce.command.AddContactMechPurposeType;
import com.skytala.eCommerce.command.DeleteContactMechPurposeType;
import com.skytala.eCommerce.command.UpdateContactMechPurposeType;
import com.skytala.eCommerce.entity.ContactMechPurposeType;
import com.skytala.eCommerce.entity.ContactMechPurposeTypeMapper;
import com.skytala.eCommerce.event.ContactMechPurposeTypeAdded;
import com.skytala.eCommerce.event.ContactMechPurposeTypeDeleted;
import com.skytala.eCommerce.event.ContactMechPurposeTypeFound;
import com.skytala.eCommerce.event.ContactMechPurposeTypeUpdated;
import com.skytala.eCommerce.query.FindContactMechPurposeTypesBy;

@RestController
@RequestMapping("/api/contactMechPurposeType")
public class ContactMechPurposeTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactMechPurposeType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactMechPurposeTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactMechPurposeType
	 * @return a List with the ContactMechPurposeTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactMechPurposeType> findContactMechPurposeTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactMechPurposeTypesBy query = new FindContactMechPurposeTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactMechPurposeTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechPurposeTypeFound.class,
				event -> sendContactMechPurposeTypesFoundMessage(((ContactMechPurposeTypeFound) event).getContactMechPurposeTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactMechPurposeTypesFoundMessage(List<ContactMechPurposeType> contactMechPurposeTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactMechPurposeTypes);
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
	public boolean createContactMechPurposeType(HttpServletRequest request) {

		ContactMechPurposeType contactMechPurposeTypeToBeAdded = new ContactMechPurposeType();
		try {
			contactMechPurposeTypeToBeAdded = ContactMechPurposeTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactMechPurposeType(contactMechPurposeTypeToBeAdded);

	}

	/**
	 * creates a new ContactMechPurposeType entry in the ofbiz database
	 * 
	 * @param contactMechPurposeTypeToBeAdded
	 *            the ContactMechPurposeType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactMechPurposeType(ContactMechPurposeType contactMechPurposeTypeToBeAdded) {

		AddContactMechPurposeType com = new AddContactMechPurposeType(contactMechPurposeTypeToBeAdded);
		int usedTicketId;

		synchronized (ContactMechPurposeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechPurposeTypeAdded.class,
				event -> sendContactMechPurposeTypeChangedMessage(((ContactMechPurposeTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactMechPurposeType(HttpServletRequest request) {

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

		ContactMechPurposeType contactMechPurposeTypeToBeUpdated = new ContactMechPurposeType();

		try {
			contactMechPurposeTypeToBeUpdated = ContactMechPurposeTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactMechPurposeType(contactMechPurposeTypeToBeUpdated);

	}

	/**
	 * Updates the ContactMechPurposeType with the specific Id
	 * 
	 * @param contactMechPurposeTypeToBeUpdated the ContactMechPurposeType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactMechPurposeType(ContactMechPurposeType contactMechPurposeTypeToBeUpdated) {

		UpdateContactMechPurposeType com = new UpdateContactMechPurposeType(contactMechPurposeTypeToBeUpdated);

		int usedTicketId;

		synchronized (ContactMechPurposeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechPurposeTypeUpdated.class,
				event -> sendContactMechPurposeTypeChangedMessage(((ContactMechPurposeTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactMechPurposeType from the database
	 * 
	 * @param contactMechPurposeTypeId:
	 *            the id of the ContactMechPurposeType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactMechPurposeTypeById(@RequestParam(value = "contactMechPurposeTypeId") String contactMechPurposeTypeId) {

		DeleteContactMechPurposeType com = new DeleteContactMechPurposeType(contactMechPurposeTypeId);

		int usedTicketId;

		synchronized (ContactMechPurposeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechPurposeTypeDeleted.class,
				event -> sendContactMechPurposeTypeChangedMessage(((ContactMechPurposeTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactMechPurposeTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactMechPurposeType/\" plus one of the following: "
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
