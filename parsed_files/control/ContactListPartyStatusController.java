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
import com.skytala.eCommerce.command.AddContactListPartyStatus;
import com.skytala.eCommerce.command.DeleteContactListPartyStatus;
import com.skytala.eCommerce.command.UpdateContactListPartyStatus;
import com.skytala.eCommerce.entity.ContactListPartyStatus;
import com.skytala.eCommerce.entity.ContactListPartyStatusMapper;
import com.skytala.eCommerce.event.ContactListPartyStatusAdded;
import com.skytala.eCommerce.event.ContactListPartyStatusDeleted;
import com.skytala.eCommerce.event.ContactListPartyStatusFound;
import com.skytala.eCommerce.event.ContactListPartyStatusUpdated;
import com.skytala.eCommerce.query.FindContactListPartyStatussBy;

@RestController
@RequestMapping("/api/contactListPartyStatus")
public class ContactListPartyStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactListPartyStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactListPartyStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactListPartyStatus
	 * @return a List with the ContactListPartyStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactListPartyStatus> findContactListPartyStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactListPartyStatussBy query = new FindContactListPartyStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactListPartyStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListPartyStatusFound.class,
				event -> sendContactListPartyStatussFoundMessage(((ContactListPartyStatusFound) event).getContactListPartyStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactListPartyStatussFoundMessage(List<ContactListPartyStatus> contactListPartyStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactListPartyStatuss);
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
	public boolean createContactListPartyStatus(HttpServletRequest request) {

		ContactListPartyStatus contactListPartyStatusToBeAdded = new ContactListPartyStatus();
		try {
			contactListPartyStatusToBeAdded = ContactListPartyStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactListPartyStatus(contactListPartyStatusToBeAdded);

	}

	/**
	 * creates a new ContactListPartyStatus entry in the ofbiz database
	 * 
	 * @param contactListPartyStatusToBeAdded
	 *            the ContactListPartyStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactListPartyStatus(ContactListPartyStatus contactListPartyStatusToBeAdded) {

		AddContactListPartyStatus com = new AddContactListPartyStatus(contactListPartyStatusToBeAdded);
		int usedTicketId;

		synchronized (ContactListPartyStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListPartyStatusAdded.class,
				event -> sendContactListPartyStatusChangedMessage(((ContactListPartyStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactListPartyStatus(HttpServletRequest request) {

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

		ContactListPartyStatus contactListPartyStatusToBeUpdated = new ContactListPartyStatus();

		try {
			contactListPartyStatusToBeUpdated = ContactListPartyStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactListPartyStatus(contactListPartyStatusToBeUpdated);

	}

	/**
	 * Updates the ContactListPartyStatus with the specific Id
	 * 
	 * @param contactListPartyStatusToBeUpdated the ContactListPartyStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactListPartyStatus(ContactListPartyStatus contactListPartyStatusToBeUpdated) {

		UpdateContactListPartyStatus com = new UpdateContactListPartyStatus(contactListPartyStatusToBeUpdated);

		int usedTicketId;

		synchronized (ContactListPartyStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListPartyStatusUpdated.class,
				event -> sendContactListPartyStatusChangedMessage(((ContactListPartyStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactListPartyStatus from the database
	 * 
	 * @param contactListPartyStatusId:
	 *            the id of the ContactListPartyStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactListPartyStatusById(@RequestParam(value = "contactListPartyStatusId") String contactListPartyStatusId) {

		DeleteContactListPartyStatus com = new DeleteContactListPartyStatus(contactListPartyStatusId);

		int usedTicketId;

		synchronized (ContactListPartyStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListPartyStatusDeleted.class,
				event -> sendContactListPartyStatusChangedMessage(((ContactListPartyStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactListPartyStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactListPartyStatus/\" plus one of the following: "
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
