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
import com.skytala.eCommerce.command.AddContactMechAttribute;
import com.skytala.eCommerce.command.DeleteContactMechAttribute;
import com.skytala.eCommerce.command.UpdateContactMechAttribute;
import com.skytala.eCommerce.entity.ContactMechAttribute;
import com.skytala.eCommerce.entity.ContactMechAttributeMapper;
import com.skytala.eCommerce.event.ContactMechAttributeAdded;
import com.skytala.eCommerce.event.ContactMechAttributeDeleted;
import com.skytala.eCommerce.event.ContactMechAttributeFound;
import com.skytala.eCommerce.event.ContactMechAttributeUpdated;
import com.skytala.eCommerce.query.FindContactMechAttributesBy;

@RestController
@RequestMapping("/api/contactMechAttribute")
public class ContactMechAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactMechAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactMechAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactMechAttribute
	 * @return a List with the ContactMechAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactMechAttribute> findContactMechAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactMechAttributesBy query = new FindContactMechAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactMechAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechAttributeFound.class,
				event -> sendContactMechAttributesFoundMessage(((ContactMechAttributeFound) event).getContactMechAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactMechAttributesFoundMessage(List<ContactMechAttribute> contactMechAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactMechAttributes);
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
	public boolean createContactMechAttribute(HttpServletRequest request) {

		ContactMechAttribute contactMechAttributeToBeAdded = new ContactMechAttribute();
		try {
			contactMechAttributeToBeAdded = ContactMechAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactMechAttribute(contactMechAttributeToBeAdded);

	}

	/**
	 * creates a new ContactMechAttribute entry in the ofbiz database
	 * 
	 * @param contactMechAttributeToBeAdded
	 *            the ContactMechAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactMechAttribute(ContactMechAttribute contactMechAttributeToBeAdded) {

		AddContactMechAttribute com = new AddContactMechAttribute(contactMechAttributeToBeAdded);
		int usedTicketId;

		synchronized (ContactMechAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechAttributeAdded.class,
				event -> sendContactMechAttributeChangedMessage(((ContactMechAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactMechAttribute(HttpServletRequest request) {

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

		ContactMechAttribute contactMechAttributeToBeUpdated = new ContactMechAttribute();

		try {
			contactMechAttributeToBeUpdated = ContactMechAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactMechAttribute(contactMechAttributeToBeUpdated);

	}

	/**
	 * Updates the ContactMechAttribute with the specific Id
	 * 
	 * @param contactMechAttributeToBeUpdated the ContactMechAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactMechAttribute(ContactMechAttribute contactMechAttributeToBeUpdated) {

		UpdateContactMechAttribute com = new UpdateContactMechAttribute(contactMechAttributeToBeUpdated);

		int usedTicketId;

		synchronized (ContactMechAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechAttributeUpdated.class,
				event -> sendContactMechAttributeChangedMessage(((ContactMechAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactMechAttribute from the database
	 * 
	 * @param contactMechAttributeId:
	 *            the id of the ContactMechAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactMechAttributeById(@RequestParam(value = "contactMechAttributeId") String contactMechAttributeId) {

		DeleteContactMechAttribute com = new DeleteContactMechAttribute(contactMechAttributeId);

		int usedTicketId;

		synchronized (ContactMechAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechAttributeDeleted.class,
				event -> sendContactMechAttributeChangedMessage(((ContactMechAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactMechAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactMechAttribute/\" plus one of the following: "
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
