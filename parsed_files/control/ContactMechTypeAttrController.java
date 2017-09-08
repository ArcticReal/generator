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
import com.skytala.eCommerce.command.AddContactMechTypeAttr;
import com.skytala.eCommerce.command.DeleteContactMechTypeAttr;
import com.skytala.eCommerce.command.UpdateContactMechTypeAttr;
import com.skytala.eCommerce.entity.ContactMechTypeAttr;
import com.skytala.eCommerce.entity.ContactMechTypeAttrMapper;
import com.skytala.eCommerce.event.ContactMechTypeAttrAdded;
import com.skytala.eCommerce.event.ContactMechTypeAttrDeleted;
import com.skytala.eCommerce.event.ContactMechTypeAttrFound;
import com.skytala.eCommerce.event.ContactMechTypeAttrUpdated;
import com.skytala.eCommerce.query.FindContactMechTypeAttrsBy;

@RestController
@RequestMapping("/api/contactMechTypeAttr")
public class ContactMechTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactMechTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactMechTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactMechTypeAttr
	 * @return a List with the ContactMechTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactMechTypeAttr> findContactMechTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactMechTypeAttrsBy query = new FindContactMechTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactMechTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypeAttrFound.class,
				event -> sendContactMechTypeAttrsFoundMessage(((ContactMechTypeAttrFound) event).getContactMechTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactMechTypeAttrsFoundMessage(List<ContactMechTypeAttr> contactMechTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactMechTypeAttrs);
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
	public boolean createContactMechTypeAttr(HttpServletRequest request) {

		ContactMechTypeAttr contactMechTypeAttrToBeAdded = new ContactMechTypeAttr();
		try {
			contactMechTypeAttrToBeAdded = ContactMechTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactMechTypeAttr(contactMechTypeAttrToBeAdded);

	}

	/**
	 * creates a new ContactMechTypeAttr entry in the ofbiz database
	 * 
	 * @param contactMechTypeAttrToBeAdded
	 *            the ContactMechTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactMechTypeAttr(ContactMechTypeAttr contactMechTypeAttrToBeAdded) {

		AddContactMechTypeAttr com = new AddContactMechTypeAttr(contactMechTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (ContactMechTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypeAttrAdded.class,
				event -> sendContactMechTypeAttrChangedMessage(((ContactMechTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactMechTypeAttr(HttpServletRequest request) {

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

		ContactMechTypeAttr contactMechTypeAttrToBeUpdated = new ContactMechTypeAttr();

		try {
			contactMechTypeAttrToBeUpdated = ContactMechTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactMechTypeAttr(contactMechTypeAttrToBeUpdated);

	}

	/**
	 * Updates the ContactMechTypeAttr with the specific Id
	 * 
	 * @param contactMechTypeAttrToBeUpdated the ContactMechTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactMechTypeAttr(ContactMechTypeAttr contactMechTypeAttrToBeUpdated) {

		UpdateContactMechTypeAttr com = new UpdateContactMechTypeAttr(contactMechTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (ContactMechTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypeAttrUpdated.class,
				event -> sendContactMechTypeAttrChangedMessage(((ContactMechTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactMechTypeAttr from the database
	 * 
	 * @param contactMechTypeAttrId:
	 *            the id of the ContactMechTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactMechTypeAttrById(@RequestParam(value = "contactMechTypeAttrId") String contactMechTypeAttrId) {

		DeleteContactMechTypeAttr com = new DeleteContactMechTypeAttr(contactMechTypeAttrId);

		int usedTicketId;

		synchronized (ContactMechTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechTypeAttrDeleted.class,
				event -> sendContactMechTypeAttrChangedMessage(((ContactMechTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactMechTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactMechTypeAttr/\" plus one of the following: "
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
