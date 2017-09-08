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
import com.skytala.eCommerce.command.AddPartyStatus;
import com.skytala.eCommerce.command.DeletePartyStatus;
import com.skytala.eCommerce.command.UpdatePartyStatus;
import com.skytala.eCommerce.entity.PartyStatus;
import com.skytala.eCommerce.entity.PartyStatusMapper;
import com.skytala.eCommerce.event.PartyStatusAdded;
import com.skytala.eCommerce.event.PartyStatusDeleted;
import com.skytala.eCommerce.event.PartyStatusFound;
import com.skytala.eCommerce.event.PartyStatusUpdated;
import com.skytala.eCommerce.query.FindPartyStatussBy;

@RestController
@RequestMapping("/api/partyStatus")
public class PartyStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyStatus
	 * @return a List with the PartyStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyStatus> findPartyStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyStatussBy query = new FindPartyStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyStatusFound.class,
				event -> sendPartyStatussFoundMessage(((PartyStatusFound) event).getPartyStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyStatussFoundMessage(List<PartyStatus> partyStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyStatuss);
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
	public boolean createPartyStatus(HttpServletRequest request) {

		PartyStatus partyStatusToBeAdded = new PartyStatus();
		try {
			partyStatusToBeAdded = PartyStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyStatus(partyStatusToBeAdded);

	}

	/**
	 * creates a new PartyStatus entry in the ofbiz database
	 * 
	 * @param partyStatusToBeAdded
	 *            the PartyStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyStatus(PartyStatus partyStatusToBeAdded) {

		AddPartyStatus com = new AddPartyStatus(partyStatusToBeAdded);
		int usedTicketId;

		synchronized (PartyStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyStatusAdded.class,
				event -> sendPartyStatusChangedMessage(((PartyStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyStatus(HttpServletRequest request) {

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

		PartyStatus partyStatusToBeUpdated = new PartyStatus();

		try {
			partyStatusToBeUpdated = PartyStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyStatus(partyStatusToBeUpdated);

	}

	/**
	 * Updates the PartyStatus with the specific Id
	 * 
	 * @param partyStatusToBeUpdated the PartyStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyStatus(PartyStatus partyStatusToBeUpdated) {

		UpdatePartyStatus com = new UpdatePartyStatus(partyStatusToBeUpdated);

		int usedTicketId;

		synchronized (PartyStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyStatusUpdated.class,
				event -> sendPartyStatusChangedMessage(((PartyStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyStatus from the database
	 * 
	 * @param partyStatusId:
	 *            the id of the PartyStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyStatusById(@RequestParam(value = "partyStatusId") String partyStatusId) {

		DeletePartyStatus com = new DeletePartyStatus(partyStatusId);

		int usedTicketId;

		synchronized (PartyStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyStatusDeleted.class,
				event -> sendPartyStatusChangedMessage(((PartyStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyStatus/\" plus one of the following: "
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
