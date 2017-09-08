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
import com.skytala.eCommerce.command.AddPartyContactMech;
import com.skytala.eCommerce.command.DeletePartyContactMech;
import com.skytala.eCommerce.command.UpdatePartyContactMech;
import com.skytala.eCommerce.entity.PartyContactMech;
import com.skytala.eCommerce.entity.PartyContactMechMapper;
import com.skytala.eCommerce.event.PartyContactMechAdded;
import com.skytala.eCommerce.event.PartyContactMechDeleted;
import com.skytala.eCommerce.event.PartyContactMechFound;
import com.skytala.eCommerce.event.PartyContactMechUpdated;
import com.skytala.eCommerce.query.FindPartyContactMechsBy;

@RestController
@RequestMapping("/api/partyContactMech")
public class PartyContactMechController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyContactMech>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyContactMechController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyContactMech
	 * @return a List with the PartyContactMechs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyContactMech> findPartyContactMechsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyContactMechsBy query = new FindPartyContactMechsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyContactMechController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContactMechFound.class,
				event -> sendPartyContactMechsFoundMessage(((PartyContactMechFound) event).getPartyContactMechs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyContactMechsFoundMessage(List<PartyContactMech> partyContactMechs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyContactMechs);
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
	public boolean createPartyContactMech(HttpServletRequest request) {

		PartyContactMech partyContactMechToBeAdded = new PartyContactMech();
		try {
			partyContactMechToBeAdded = PartyContactMechMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyContactMech(partyContactMechToBeAdded);

	}

	/**
	 * creates a new PartyContactMech entry in the ofbiz database
	 * 
	 * @param partyContactMechToBeAdded
	 *            the PartyContactMech thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyContactMech(PartyContactMech partyContactMechToBeAdded) {

		AddPartyContactMech com = new AddPartyContactMech(partyContactMechToBeAdded);
		int usedTicketId;

		synchronized (PartyContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContactMechAdded.class,
				event -> sendPartyContactMechChangedMessage(((PartyContactMechAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyContactMech(HttpServletRequest request) {

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

		PartyContactMech partyContactMechToBeUpdated = new PartyContactMech();

		try {
			partyContactMechToBeUpdated = PartyContactMechMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyContactMech(partyContactMechToBeUpdated);

	}

	/**
	 * Updates the PartyContactMech with the specific Id
	 * 
	 * @param partyContactMechToBeUpdated the PartyContactMech thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyContactMech(PartyContactMech partyContactMechToBeUpdated) {

		UpdatePartyContactMech com = new UpdatePartyContactMech(partyContactMechToBeUpdated);

		int usedTicketId;

		synchronized (PartyContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContactMechUpdated.class,
				event -> sendPartyContactMechChangedMessage(((PartyContactMechUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyContactMech from the database
	 * 
	 * @param partyContactMechId:
	 *            the id of the PartyContactMech thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyContactMechById(@RequestParam(value = "partyContactMechId") String partyContactMechId) {

		DeletePartyContactMech com = new DeletePartyContactMech(partyContactMechId);

		int usedTicketId;

		synchronized (PartyContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyContactMechDeleted.class,
				event -> sendPartyContactMechChangedMessage(((PartyContactMechDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyContactMechChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyContactMech/\" plus one of the following: "
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
