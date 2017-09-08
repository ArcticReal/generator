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
import com.skytala.eCommerce.command.AddPartyQual;
import com.skytala.eCommerce.command.DeletePartyQual;
import com.skytala.eCommerce.command.UpdatePartyQual;
import com.skytala.eCommerce.entity.PartyQual;
import com.skytala.eCommerce.entity.PartyQualMapper;
import com.skytala.eCommerce.event.PartyQualAdded;
import com.skytala.eCommerce.event.PartyQualDeleted;
import com.skytala.eCommerce.event.PartyQualFound;
import com.skytala.eCommerce.event.PartyQualUpdated;
import com.skytala.eCommerce.query.FindPartyQualsBy;

@RestController
@RequestMapping("/api/partyQual")
public class PartyQualController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyQual>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyQualController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyQual
	 * @return a List with the PartyQuals
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyQual> findPartyQualsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyQualsBy query = new FindPartyQualsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyQualController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyQualFound.class,
				event -> sendPartyQualsFoundMessage(((PartyQualFound) event).getPartyQuals(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyQualsFoundMessage(List<PartyQual> partyQuals, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyQuals);
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
	public boolean createPartyQual(HttpServletRequest request) {

		PartyQual partyQualToBeAdded = new PartyQual();
		try {
			partyQualToBeAdded = PartyQualMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyQual(partyQualToBeAdded);

	}

	/**
	 * creates a new PartyQual entry in the ofbiz database
	 * 
	 * @param partyQualToBeAdded
	 *            the PartyQual thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyQual(PartyQual partyQualToBeAdded) {

		AddPartyQual com = new AddPartyQual(partyQualToBeAdded);
		int usedTicketId;

		synchronized (PartyQualController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyQualAdded.class,
				event -> sendPartyQualChangedMessage(((PartyQualAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyQual(HttpServletRequest request) {

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

		PartyQual partyQualToBeUpdated = new PartyQual();

		try {
			partyQualToBeUpdated = PartyQualMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyQual(partyQualToBeUpdated);

	}

	/**
	 * Updates the PartyQual with the specific Id
	 * 
	 * @param partyQualToBeUpdated the PartyQual thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyQual(PartyQual partyQualToBeUpdated) {

		UpdatePartyQual com = new UpdatePartyQual(partyQualToBeUpdated);

		int usedTicketId;

		synchronized (PartyQualController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyQualUpdated.class,
				event -> sendPartyQualChangedMessage(((PartyQualUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyQual from the database
	 * 
	 * @param partyQualId:
	 *            the id of the PartyQual thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyQualById(@RequestParam(value = "partyQualId") String partyQualId) {

		DeletePartyQual com = new DeletePartyQual(partyQualId);

		int usedTicketId;

		synchronized (PartyQualController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyQualDeleted.class,
				event -> sendPartyQualChangedMessage(((PartyQualDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyQualChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyQual/\" plus one of the following: "
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
