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
import com.skytala.eCommerce.command.AddPartyGeoPoint;
import com.skytala.eCommerce.command.DeletePartyGeoPoint;
import com.skytala.eCommerce.command.UpdatePartyGeoPoint;
import com.skytala.eCommerce.entity.PartyGeoPoint;
import com.skytala.eCommerce.entity.PartyGeoPointMapper;
import com.skytala.eCommerce.event.PartyGeoPointAdded;
import com.skytala.eCommerce.event.PartyGeoPointDeleted;
import com.skytala.eCommerce.event.PartyGeoPointFound;
import com.skytala.eCommerce.event.PartyGeoPointUpdated;
import com.skytala.eCommerce.query.FindPartyGeoPointsBy;

@RestController
@RequestMapping("/api/partyGeoPoint")
public class PartyGeoPointController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyGeoPoint>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyGeoPointController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyGeoPoint
	 * @return a List with the PartyGeoPoints
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyGeoPoint> findPartyGeoPointsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyGeoPointsBy query = new FindPartyGeoPointsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyGeoPointController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyGeoPointFound.class,
				event -> sendPartyGeoPointsFoundMessage(((PartyGeoPointFound) event).getPartyGeoPoints(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyGeoPointsFoundMessage(List<PartyGeoPoint> partyGeoPoints, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyGeoPoints);
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
	public boolean createPartyGeoPoint(HttpServletRequest request) {

		PartyGeoPoint partyGeoPointToBeAdded = new PartyGeoPoint();
		try {
			partyGeoPointToBeAdded = PartyGeoPointMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyGeoPoint(partyGeoPointToBeAdded);

	}

	/**
	 * creates a new PartyGeoPoint entry in the ofbiz database
	 * 
	 * @param partyGeoPointToBeAdded
	 *            the PartyGeoPoint thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyGeoPoint(PartyGeoPoint partyGeoPointToBeAdded) {

		AddPartyGeoPoint com = new AddPartyGeoPoint(partyGeoPointToBeAdded);
		int usedTicketId;

		synchronized (PartyGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyGeoPointAdded.class,
				event -> sendPartyGeoPointChangedMessage(((PartyGeoPointAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyGeoPoint(HttpServletRequest request) {

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

		PartyGeoPoint partyGeoPointToBeUpdated = new PartyGeoPoint();

		try {
			partyGeoPointToBeUpdated = PartyGeoPointMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyGeoPoint(partyGeoPointToBeUpdated);

	}

	/**
	 * Updates the PartyGeoPoint with the specific Id
	 * 
	 * @param partyGeoPointToBeUpdated the PartyGeoPoint thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyGeoPoint(PartyGeoPoint partyGeoPointToBeUpdated) {

		UpdatePartyGeoPoint com = new UpdatePartyGeoPoint(partyGeoPointToBeUpdated);

		int usedTicketId;

		synchronized (PartyGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyGeoPointUpdated.class,
				event -> sendPartyGeoPointChangedMessage(((PartyGeoPointUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyGeoPoint from the database
	 * 
	 * @param partyGeoPointId:
	 *            the id of the PartyGeoPoint thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyGeoPointById(@RequestParam(value = "partyGeoPointId") String partyGeoPointId) {

		DeletePartyGeoPoint com = new DeletePartyGeoPoint(partyGeoPointId);

		int usedTicketId;

		synchronized (PartyGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyGeoPointDeleted.class,
				event -> sendPartyGeoPointChangedMessage(((PartyGeoPointDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyGeoPointChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyGeoPoint/\" plus one of the following: "
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
