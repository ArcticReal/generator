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
import com.skytala.eCommerce.command.AddContactMech;
import com.skytala.eCommerce.command.DeleteContactMech;
import com.skytala.eCommerce.command.UpdateContactMech;
import com.skytala.eCommerce.entity.ContactMech;
import com.skytala.eCommerce.entity.ContactMechMapper;
import com.skytala.eCommerce.event.ContactMechAdded;
import com.skytala.eCommerce.event.ContactMechDeleted;
import com.skytala.eCommerce.event.ContactMechFound;
import com.skytala.eCommerce.event.ContactMechUpdated;
import com.skytala.eCommerce.query.FindContactMechsBy;

@RestController
@RequestMapping("/api/contactMech")
public class ContactMechController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactMech>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactMechController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactMech
	 * @return a List with the ContactMechs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactMech> findContactMechsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactMechsBy query = new FindContactMechsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactMechController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechFound.class,
				event -> sendContactMechsFoundMessage(((ContactMechFound) event).getContactMechs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactMechsFoundMessage(List<ContactMech> contactMechs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactMechs);
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
	public boolean createContactMech(HttpServletRequest request) {

		ContactMech contactMechToBeAdded = new ContactMech();
		try {
			contactMechToBeAdded = ContactMechMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactMech(contactMechToBeAdded);

	}

	/**
	 * creates a new ContactMech entry in the ofbiz database
	 * 
	 * @param contactMechToBeAdded
	 *            the ContactMech thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactMech(ContactMech contactMechToBeAdded) {

		AddContactMech com = new AddContactMech(contactMechToBeAdded);
		int usedTicketId;

		synchronized (ContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechAdded.class,
				event -> sendContactMechChangedMessage(((ContactMechAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactMech(HttpServletRequest request) {

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

		ContactMech contactMechToBeUpdated = new ContactMech();

		try {
			contactMechToBeUpdated = ContactMechMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactMech(contactMechToBeUpdated);

	}

	/**
	 * Updates the ContactMech with the specific Id
	 * 
	 * @param contactMechToBeUpdated the ContactMech thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactMech(ContactMech contactMechToBeUpdated) {

		UpdateContactMech com = new UpdateContactMech(contactMechToBeUpdated);

		int usedTicketId;

		synchronized (ContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechUpdated.class,
				event -> sendContactMechChangedMessage(((ContactMechUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactMech from the database
	 * 
	 * @param contactMechId:
	 *            the id of the ContactMech thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactMechById(@RequestParam(value = "contactMechId") String contactMechId) {

		DeleteContactMech com = new DeleteContactMech(contactMechId);

		int usedTicketId;

		synchronized (ContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactMechDeleted.class,
				event -> sendContactMechChangedMessage(((ContactMechDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactMechChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactMech/\" plus one of the following: "
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
