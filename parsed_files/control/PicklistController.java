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
import com.skytala.eCommerce.command.AddPicklist;
import com.skytala.eCommerce.command.DeletePicklist;
import com.skytala.eCommerce.command.UpdatePicklist;
import com.skytala.eCommerce.entity.Picklist;
import com.skytala.eCommerce.entity.PicklistMapper;
import com.skytala.eCommerce.event.PicklistAdded;
import com.skytala.eCommerce.event.PicklistDeleted;
import com.skytala.eCommerce.event.PicklistFound;
import com.skytala.eCommerce.event.PicklistUpdated;
import com.skytala.eCommerce.query.FindPicklistsBy;

@RestController
@RequestMapping("/api/picklist")
public class PicklistController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Picklist>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PicklistController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Picklist
	 * @return a List with the Picklists
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Picklist> findPicklistsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPicklistsBy query = new FindPicklistsBy(allRequestParams);

		int usedTicketId;

		synchronized (PicklistController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistFound.class,
				event -> sendPicklistsFoundMessage(((PicklistFound) event).getPicklists(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPicklistsFoundMessage(List<Picklist> picklists, int usedTicketId) {
		queryReturnVal.put(usedTicketId, picklists);
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
	public boolean createPicklist(HttpServletRequest request) {

		Picklist picklistToBeAdded = new Picklist();
		try {
			picklistToBeAdded = PicklistMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPicklist(picklistToBeAdded);

	}

	/**
	 * creates a new Picklist entry in the ofbiz database
	 * 
	 * @param picklistToBeAdded
	 *            the Picklist thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPicklist(Picklist picklistToBeAdded) {

		AddPicklist com = new AddPicklist(picklistToBeAdded);
		int usedTicketId;

		synchronized (PicklistController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistAdded.class,
				event -> sendPicklistChangedMessage(((PicklistAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePicklist(HttpServletRequest request) {

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

		Picklist picklistToBeUpdated = new Picklist();

		try {
			picklistToBeUpdated = PicklistMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePicklist(picklistToBeUpdated);

	}

	/**
	 * Updates the Picklist with the specific Id
	 * 
	 * @param picklistToBeUpdated the Picklist thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePicklist(Picklist picklistToBeUpdated) {

		UpdatePicklist com = new UpdatePicklist(picklistToBeUpdated);

		int usedTicketId;

		synchronized (PicklistController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistUpdated.class,
				event -> sendPicklistChangedMessage(((PicklistUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Picklist from the database
	 * 
	 * @param picklistId:
	 *            the id of the Picklist thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepicklistById(@RequestParam(value = "picklistId") String picklistId) {

		DeletePicklist com = new DeletePicklist(picklistId);

		int usedTicketId;

		synchronized (PicklistController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistDeleted.class,
				event -> sendPicklistChangedMessage(((PicklistDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPicklistChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/picklist/\" plus one of the following: "
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
