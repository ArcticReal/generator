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
import com.skytala.eCommerce.command.AddContactListCommStatus;
import com.skytala.eCommerce.command.DeleteContactListCommStatus;
import com.skytala.eCommerce.command.UpdateContactListCommStatus;
import com.skytala.eCommerce.entity.ContactListCommStatus;
import com.skytala.eCommerce.entity.ContactListCommStatusMapper;
import com.skytala.eCommerce.event.ContactListCommStatusAdded;
import com.skytala.eCommerce.event.ContactListCommStatusDeleted;
import com.skytala.eCommerce.event.ContactListCommStatusFound;
import com.skytala.eCommerce.event.ContactListCommStatusUpdated;
import com.skytala.eCommerce.query.FindContactListCommStatussBy;

@RestController
@RequestMapping("/api/contactListCommStatus")
public class ContactListCommStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContactListCommStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContactListCommStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContactListCommStatus
	 * @return a List with the ContactListCommStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContactListCommStatus> findContactListCommStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindContactListCommStatussBy query = new FindContactListCommStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (ContactListCommStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListCommStatusFound.class,
				event -> sendContactListCommStatussFoundMessage(((ContactListCommStatusFound) event).getContactListCommStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContactListCommStatussFoundMessage(List<ContactListCommStatus> contactListCommStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contactListCommStatuss);
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
	public boolean createContactListCommStatus(HttpServletRequest request) {

		ContactListCommStatus contactListCommStatusToBeAdded = new ContactListCommStatus();
		try {
			contactListCommStatusToBeAdded = ContactListCommStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContactListCommStatus(contactListCommStatusToBeAdded);

	}

	/**
	 * creates a new ContactListCommStatus entry in the ofbiz database
	 * 
	 * @param contactListCommStatusToBeAdded
	 *            the ContactListCommStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContactListCommStatus(ContactListCommStatus contactListCommStatusToBeAdded) {

		AddContactListCommStatus com = new AddContactListCommStatus(contactListCommStatusToBeAdded);
		int usedTicketId;

		synchronized (ContactListCommStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListCommStatusAdded.class,
				event -> sendContactListCommStatusChangedMessage(((ContactListCommStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContactListCommStatus(HttpServletRequest request) {

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

		ContactListCommStatus contactListCommStatusToBeUpdated = new ContactListCommStatus();

		try {
			contactListCommStatusToBeUpdated = ContactListCommStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContactListCommStatus(contactListCommStatusToBeUpdated);

	}

	/**
	 * Updates the ContactListCommStatus with the specific Id
	 * 
	 * @param contactListCommStatusToBeUpdated the ContactListCommStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContactListCommStatus(ContactListCommStatus contactListCommStatusToBeUpdated) {

		UpdateContactListCommStatus com = new UpdateContactListCommStatus(contactListCommStatusToBeUpdated);

		int usedTicketId;

		synchronized (ContactListCommStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListCommStatusUpdated.class,
				event -> sendContactListCommStatusChangedMessage(((ContactListCommStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContactListCommStatus from the database
	 * 
	 * @param contactListCommStatusId:
	 *            the id of the ContactListCommStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontactListCommStatusById(@RequestParam(value = "contactListCommStatusId") String contactListCommStatusId) {

		DeleteContactListCommStatus com = new DeleteContactListCommStatus(contactListCommStatusId);

		int usedTicketId;

		synchronized (ContactListCommStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContactListCommStatusDeleted.class,
				event -> sendContactListCommStatusChangedMessage(((ContactListCommStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContactListCommStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contactListCommStatus/\" plus one of the following: "
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
