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
import com.skytala.eCommerce.command.AddContactMechTypePurpose;
import com.skytala.eCommerce.command.DeleteContactMechTypePurpose;
import com.skytala.eCommerce.command.UpdateContactMechTypePurpose;
import com.skytala.eCommerce.entity.ContactMechTypePurpose;
import com.skytala.eCommerce.entity.ContactMechTypePurposeMapper;
import com.skytala.eCommerce.event.ContactMechTypePurposeAdded;
import com.skytala.eCommerce.event.ContactMechTypePurposeDeleted;
import com.skytala.eCommerce.event.ContactMechTypePurposeFound;
import com.skytala.eCommerce.event.ContactMechTypePurposeUpdated;
import com.skytala.eCommerce.query.FindContactMechTypePurposesBy;

@RestController
@RequestMapping("/api/contactMechTypePurpose")
public class ContactMechTypePurposeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactMechTypePurpose>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactMechTypePurposeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactMechTypePurpose
	 * @return a List with the ContactMechTypePurposes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactMechTypePurpose> findContactMechTypePurposesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactMechTypePurposesBy query = new FindContactMechTypePurposesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactMechTypePurposeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypePurposeFound.class,
				event -> sendContactMechTypePurposesFoundMessage(((ContactMechTypePurposeFound) event).getContactMechTypePurposes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactMechTypePurposesFoundMessage(List<ContactMechTypePurpose> contactMechTypePurposes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactMechTypePurposes);
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
	public boolean createContactMechTypePurpose(HttpServletRequest request) {

		ContactMechTypePurpose contactMechTypePurposeToBeAdded = new ContactMechTypePurpose();
		try {
			contactMechTypePurposeToBeAdded = ContactMechTypePurposeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactMechTypePurpose(contactMechTypePurposeToBeAdded);

	}

	/**
	 * creates a new ContactMechTypePurpose entry in the ofbiz database
	 * 
	 * @param contactMechTypePurposeToBeAdded
	 *            the ContactMechTypePurpose thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactMechTypePurpose(ContactMechTypePurpose contactMechTypePurposeToBeAdded) {

		AddContactMechTypePurpose com = new AddContactMechTypePurpose(contactMechTypePurposeToBeAdded);
		int usedTicketId;

		synchronized (ContactMechTypePurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypePurposeAdded.class,
				event -> sendContactMechTypePurposeChangedMessage(((ContactMechTypePurposeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactMechTypePurpose(HttpServletRequest request) {

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

		ContactMechTypePurpose contactMechTypePurposeToBeUpdated = new ContactMechTypePurpose();

		try {
			contactMechTypePurposeToBeUpdated = ContactMechTypePurposeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactMechTypePurpose(contactMechTypePurposeToBeUpdated);

	}

	/**
	 * Updates the ContactMechTypePurpose with the specific Id
	 * 
	 * @param contactMechTypePurposeToBeUpdated the ContactMechTypePurpose thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactMechTypePurpose(ContactMechTypePurpose contactMechTypePurposeToBeUpdated) {

		UpdateContactMechTypePurpose com = new UpdateContactMechTypePurpose(contactMechTypePurposeToBeUpdated);

		int usedTicketId;

		synchronized (ContactMechTypePurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypePurposeUpdated.class,
				event -> sendContactMechTypePurposeChangedMessage(((ContactMechTypePurposeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactMechTypePurpose from the database
	 * 
	 * @param contactMechTypePurposeId:
	 *            the id of the ContactMechTypePurpose thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactMechTypePurposeById(@RequestParam(value = "contactMechTypePurposeId") String contactMechTypePurposeId) {

		DeleteContactMechTypePurpose com = new DeleteContactMechTypePurpose(contactMechTypePurposeId);

		int usedTicketId;

		synchronized (ContactMechTypePurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypePurposeDeleted.class,
				event -> sendContactMechTypePurposeChangedMessage(((ContactMechTypePurposeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactMechTypePurposeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactMechTypePurpose/\" plus one of the following: "
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
