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
import com.skytala.eCommerce.command.AddRespondingParty;
import com.skytala.eCommerce.command.DeleteRespondingParty;
import com.skytala.eCommerce.command.UpdateRespondingParty;
import com.skytala.eCommerce.entity.RespondingParty;
import com.skytala.eCommerce.entity.RespondingPartyMapper;
import com.skytala.eCommerce.event.RespondingPartyAdded;
import com.skytala.eCommerce.event.RespondingPartyDeleted;
import com.skytala.eCommerce.event.RespondingPartyFound;
import com.skytala.eCommerce.event.RespondingPartyUpdated;
import com.skytala.eCommerce.query.FindRespondingPartysBy;

@RestController
@RequestMapping("/api/respondingParty")
public class RespondingPartyController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RespondingParty>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RespondingPartyController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RespondingParty
	 * @return a List with the RespondingPartys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RespondingParty> findRespondingPartysBy(@RequestParam Map<String, String> allRequestParams) {

		FindRespondingPartysBy query = new FindRespondingPartysBy(allRequestParams);

		int usedTicketId;

		synchronized (RespondingPartyController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RespondingPartyFound.class,
				event -> sendRespondingPartysFoundMessage(((RespondingPartyFound) event).getRespondingPartys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRespondingPartysFoundMessage(List<RespondingParty> respondingPartys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, respondingPartys);
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
	public boolean createRespondingParty(HttpServletRequest request) {

		RespondingParty respondingPartyToBeAdded = new RespondingParty();
		try {
			respondingPartyToBeAdded = RespondingPartyMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRespondingParty(respondingPartyToBeAdded);

	}

	/**
	 * creates a new RespondingParty entry in the ofbiz database
	 * 
	 * @param respondingPartyToBeAdded
	 *            the RespondingParty thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRespondingParty(RespondingParty respondingPartyToBeAdded) {

		AddRespondingParty com = new AddRespondingParty(respondingPartyToBeAdded);
		int usedTicketId;

		synchronized (RespondingPartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RespondingPartyAdded.class,
				event -> sendRespondingPartyChangedMessage(((RespondingPartyAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRespondingParty(HttpServletRequest request) {

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

		RespondingParty respondingPartyToBeUpdated = new RespondingParty();

		try {
			respondingPartyToBeUpdated = RespondingPartyMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRespondingParty(respondingPartyToBeUpdated);

	}

	/**
	 * Updates the RespondingParty with the specific Id
	 * 
	 * @param respondingPartyToBeUpdated the RespondingParty thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRespondingParty(RespondingParty respondingPartyToBeUpdated) {

		UpdateRespondingParty com = new UpdateRespondingParty(respondingPartyToBeUpdated);

		int usedTicketId;

		synchronized (RespondingPartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RespondingPartyUpdated.class,
				event -> sendRespondingPartyChangedMessage(((RespondingPartyUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RespondingParty from the database
	 * 
	 * @param respondingPartyId:
	 *            the id of the RespondingParty thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterespondingPartyById(@RequestParam(value = "respondingPartyId") String respondingPartyId) {

		DeleteRespondingParty com = new DeleteRespondingParty(respondingPartyId);

		int usedTicketId;

		synchronized (RespondingPartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RespondingPartyDeleted.class,
				event -> sendRespondingPartyChangedMessage(((RespondingPartyDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRespondingPartyChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/respondingParty/\" plus one of the following: "
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
