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
import com.skytala.eCommerce.command.AddPartyFixedAssetAssignment;
import com.skytala.eCommerce.command.DeletePartyFixedAssetAssignment;
import com.skytala.eCommerce.command.UpdatePartyFixedAssetAssignment;
import com.skytala.eCommerce.entity.PartyFixedAssetAssignment;
import com.skytala.eCommerce.entity.PartyFixedAssetAssignmentMapper;
import com.skytala.eCommerce.event.PartyFixedAssetAssignmentAdded;
import com.skytala.eCommerce.event.PartyFixedAssetAssignmentDeleted;
import com.skytala.eCommerce.event.PartyFixedAssetAssignmentFound;
import com.skytala.eCommerce.event.PartyFixedAssetAssignmentUpdated;
import com.skytala.eCommerce.query.FindPartyFixedAssetAssignmentsBy;

@RestController
@RequestMapping("/api/partyFixedAssetAssignment")
public class PartyFixedAssetAssignmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyFixedAssetAssignment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyFixedAssetAssignmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyFixedAssetAssignment
	 * @return a List with the PartyFixedAssetAssignments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyFixedAssetAssignment> findPartyFixedAssetAssignmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyFixedAssetAssignmentsBy query = new FindPartyFixedAssetAssignmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyFixedAssetAssignmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyFixedAssetAssignmentFound.class,
				event -> sendPartyFixedAssetAssignmentsFoundMessage(((PartyFixedAssetAssignmentFound) event).getPartyFixedAssetAssignments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyFixedAssetAssignmentsFoundMessage(List<PartyFixedAssetAssignment> partyFixedAssetAssignments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyFixedAssetAssignments);
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
	public boolean createPartyFixedAssetAssignment(HttpServletRequest request) {

		PartyFixedAssetAssignment partyFixedAssetAssignmentToBeAdded = new PartyFixedAssetAssignment();
		try {
			partyFixedAssetAssignmentToBeAdded = PartyFixedAssetAssignmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyFixedAssetAssignment(partyFixedAssetAssignmentToBeAdded);

	}

	/**
	 * creates a new PartyFixedAssetAssignment entry in the ofbiz database
	 * 
	 * @param partyFixedAssetAssignmentToBeAdded
	 *            the PartyFixedAssetAssignment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyFixedAssetAssignment(PartyFixedAssetAssignment partyFixedAssetAssignmentToBeAdded) {

		AddPartyFixedAssetAssignment com = new AddPartyFixedAssetAssignment(partyFixedAssetAssignmentToBeAdded);
		int usedTicketId;

		synchronized (PartyFixedAssetAssignmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyFixedAssetAssignmentAdded.class,
				event -> sendPartyFixedAssetAssignmentChangedMessage(((PartyFixedAssetAssignmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyFixedAssetAssignment(HttpServletRequest request) {

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

		PartyFixedAssetAssignment partyFixedAssetAssignmentToBeUpdated = new PartyFixedAssetAssignment();

		try {
			partyFixedAssetAssignmentToBeUpdated = PartyFixedAssetAssignmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyFixedAssetAssignment(partyFixedAssetAssignmentToBeUpdated);

	}

	/**
	 * Updates the PartyFixedAssetAssignment with the specific Id
	 * 
	 * @param partyFixedAssetAssignmentToBeUpdated the PartyFixedAssetAssignment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyFixedAssetAssignment(PartyFixedAssetAssignment partyFixedAssetAssignmentToBeUpdated) {

		UpdatePartyFixedAssetAssignment com = new UpdatePartyFixedAssetAssignment(partyFixedAssetAssignmentToBeUpdated);

		int usedTicketId;

		synchronized (PartyFixedAssetAssignmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyFixedAssetAssignmentUpdated.class,
				event -> sendPartyFixedAssetAssignmentChangedMessage(((PartyFixedAssetAssignmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyFixedAssetAssignment from the database
	 * 
	 * @param partyFixedAssetAssignmentId:
	 *            the id of the PartyFixedAssetAssignment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyFixedAssetAssignmentById(@RequestParam(value = "partyFixedAssetAssignmentId") String partyFixedAssetAssignmentId) {

		DeletePartyFixedAssetAssignment com = new DeletePartyFixedAssetAssignment(partyFixedAssetAssignmentId);

		int usedTicketId;

		synchronized (PartyFixedAssetAssignmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyFixedAssetAssignmentDeleted.class,
				event -> sendPartyFixedAssetAssignmentChangedMessage(((PartyFixedAssetAssignmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyFixedAssetAssignmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyFixedAssetAssignment/\" plus one of the following: "
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
