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
import com.skytala.eCommerce.command.AddPartyClassification;
import com.skytala.eCommerce.command.DeletePartyClassification;
import com.skytala.eCommerce.command.UpdatePartyClassification;
import com.skytala.eCommerce.entity.PartyClassification;
import com.skytala.eCommerce.entity.PartyClassificationMapper;
import com.skytala.eCommerce.event.PartyClassificationAdded;
import com.skytala.eCommerce.event.PartyClassificationDeleted;
import com.skytala.eCommerce.event.PartyClassificationFound;
import com.skytala.eCommerce.event.PartyClassificationUpdated;
import com.skytala.eCommerce.query.FindPartyClassificationsBy;

@RestController
@RequestMapping("/api/partyClassification")
public class PartyClassificationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyClassification>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyClassificationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyClassification
	 * @return a List with the PartyClassifications
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyClassification> findPartyClassificationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyClassificationsBy query = new FindPartyClassificationsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyClassificationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationFound.class,
				event -> sendPartyClassificationsFoundMessage(((PartyClassificationFound) event).getPartyClassifications(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyClassificationsFoundMessage(List<PartyClassification> partyClassifications, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyClassifications);
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
	public boolean createPartyClassification(HttpServletRequest request) {

		PartyClassification partyClassificationToBeAdded = new PartyClassification();
		try {
			partyClassificationToBeAdded = PartyClassificationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyClassification(partyClassificationToBeAdded);

	}

	/**
	 * creates a new PartyClassification entry in the ofbiz database
	 * 
	 * @param partyClassificationToBeAdded
	 *            the PartyClassification thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyClassification(PartyClassification partyClassificationToBeAdded) {

		AddPartyClassification com = new AddPartyClassification(partyClassificationToBeAdded);
		int usedTicketId;

		synchronized (PartyClassificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationAdded.class,
				event -> sendPartyClassificationChangedMessage(((PartyClassificationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyClassification(HttpServletRequest request) {

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

		PartyClassification partyClassificationToBeUpdated = new PartyClassification();

		try {
			partyClassificationToBeUpdated = PartyClassificationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyClassification(partyClassificationToBeUpdated);

	}

	/**
	 * Updates the PartyClassification with the specific Id
	 * 
	 * @param partyClassificationToBeUpdated the PartyClassification thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyClassification(PartyClassification partyClassificationToBeUpdated) {

		UpdatePartyClassification com = new UpdatePartyClassification(partyClassificationToBeUpdated);

		int usedTicketId;

		synchronized (PartyClassificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationUpdated.class,
				event -> sendPartyClassificationChangedMessage(((PartyClassificationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyClassification from the database
	 * 
	 * @param partyClassificationId:
	 *            the id of the PartyClassification thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyClassificationById(@RequestParam(value = "partyClassificationId") String partyClassificationId) {

		DeletePartyClassification com = new DeletePartyClassification(partyClassificationId);

		int usedTicketId;

		synchronized (PartyClassificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationDeleted.class,
				event -> sendPartyClassificationChangedMessage(((PartyClassificationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyClassificationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyClassification/\" plus one of the following: "
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
