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
import com.skytala.eCommerce.command.AddPartyProfileDefault;
import com.skytala.eCommerce.command.DeletePartyProfileDefault;
import com.skytala.eCommerce.command.UpdatePartyProfileDefault;
import com.skytala.eCommerce.entity.PartyProfileDefault;
import com.skytala.eCommerce.entity.PartyProfileDefaultMapper;
import com.skytala.eCommerce.event.PartyProfileDefaultAdded;
import com.skytala.eCommerce.event.PartyProfileDefaultDeleted;
import com.skytala.eCommerce.event.PartyProfileDefaultFound;
import com.skytala.eCommerce.event.PartyProfileDefaultUpdated;
import com.skytala.eCommerce.query.FindPartyProfileDefaultsBy;

@RestController
@RequestMapping("/api/partyProfileDefault")
public class PartyProfileDefaultController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyProfileDefault>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyProfileDefaultController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyProfileDefault
	 * @return a List with the PartyProfileDefaults
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyProfileDefault> findPartyProfileDefaultsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyProfileDefaultsBy query = new FindPartyProfileDefaultsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyProfileDefaultController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyProfileDefaultFound.class,
				event -> sendPartyProfileDefaultsFoundMessage(((PartyProfileDefaultFound) event).getPartyProfileDefaults(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyProfileDefaultsFoundMessage(List<PartyProfileDefault> partyProfileDefaults, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyProfileDefaults);
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
	public boolean createPartyProfileDefault(HttpServletRequest request) {

		PartyProfileDefault partyProfileDefaultToBeAdded = new PartyProfileDefault();
		try {
			partyProfileDefaultToBeAdded = PartyProfileDefaultMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyProfileDefault(partyProfileDefaultToBeAdded);

	}

	/**
	 * creates a new PartyProfileDefault entry in the ofbiz database
	 * 
	 * @param partyProfileDefaultToBeAdded
	 *            the PartyProfileDefault thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyProfileDefault(PartyProfileDefault partyProfileDefaultToBeAdded) {

		AddPartyProfileDefault com = new AddPartyProfileDefault(partyProfileDefaultToBeAdded);
		int usedTicketId;

		synchronized (PartyProfileDefaultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyProfileDefaultAdded.class,
				event -> sendPartyProfileDefaultChangedMessage(((PartyProfileDefaultAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyProfileDefault(HttpServletRequest request) {

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

		PartyProfileDefault partyProfileDefaultToBeUpdated = new PartyProfileDefault();

		try {
			partyProfileDefaultToBeUpdated = PartyProfileDefaultMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyProfileDefault(partyProfileDefaultToBeUpdated);

	}

	/**
	 * Updates the PartyProfileDefault with the specific Id
	 * 
	 * @param partyProfileDefaultToBeUpdated the PartyProfileDefault thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyProfileDefault(PartyProfileDefault partyProfileDefaultToBeUpdated) {

		UpdatePartyProfileDefault com = new UpdatePartyProfileDefault(partyProfileDefaultToBeUpdated);

		int usedTicketId;

		synchronized (PartyProfileDefaultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyProfileDefaultUpdated.class,
				event -> sendPartyProfileDefaultChangedMessage(((PartyProfileDefaultUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyProfileDefault from the database
	 * 
	 * @param partyProfileDefaultId:
	 *            the id of the PartyProfileDefault thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyProfileDefaultById(@RequestParam(value = "partyProfileDefaultId") String partyProfileDefaultId) {

		DeletePartyProfileDefault com = new DeletePartyProfileDefault(partyProfileDefaultId);

		int usedTicketId;

		synchronized (PartyProfileDefaultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyProfileDefaultDeleted.class,
				event -> sendPartyProfileDefaultChangedMessage(((PartyProfileDefaultDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyProfileDefaultChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyProfileDefault/\" plus one of the following: "
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
