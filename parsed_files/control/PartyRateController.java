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
import com.skytala.eCommerce.command.AddPartyRate;
import com.skytala.eCommerce.command.DeletePartyRate;
import com.skytala.eCommerce.command.UpdatePartyRate;
import com.skytala.eCommerce.entity.PartyRate;
import com.skytala.eCommerce.entity.PartyRateMapper;
import com.skytala.eCommerce.event.PartyRateAdded;
import com.skytala.eCommerce.event.PartyRateDeleted;
import com.skytala.eCommerce.event.PartyRateFound;
import com.skytala.eCommerce.event.PartyRateUpdated;
import com.skytala.eCommerce.query.FindPartyRatesBy;

@RestController
@RequestMapping("/api/partyRate")
public class PartyRateController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyRate>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyRateController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyRate
	 * @return a List with the PartyRates
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyRate> findPartyRatesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyRatesBy query = new FindPartyRatesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyRateController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRateFound.class,
				event -> sendPartyRatesFoundMessage(((PartyRateFound) event).getPartyRates(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyRatesFoundMessage(List<PartyRate> partyRates, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyRates);
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
	public boolean createPartyRate(HttpServletRequest request) {

		PartyRate partyRateToBeAdded = new PartyRate();
		try {
			partyRateToBeAdded = PartyRateMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyRate(partyRateToBeAdded);

	}

	/**
	 * creates a new PartyRate entry in the ofbiz database
	 * 
	 * @param partyRateToBeAdded
	 *            the PartyRate thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyRate(PartyRate partyRateToBeAdded) {

		AddPartyRate com = new AddPartyRate(partyRateToBeAdded);
		int usedTicketId;

		synchronized (PartyRateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRateAdded.class,
				event -> sendPartyRateChangedMessage(((PartyRateAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyRate(HttpServletRequest request) {

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

		PartyRate partyRateToBeUpdated = new PartyRate();

		try {
			partyRateToBeUpdated = PartyRateMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyRate(partyRateToBeUpdated);

	}

	/**
	 * Updates the PartyRate with the specific Id
	 * 
	 * @param partyRateToBeUpdated the PartyRate thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyRate(PartyRate partyRateToBeUpdated) {

		UpdatePartyRate com = new UpdatePartyRate(partyRateToBeUpdated);

		int usedTicketId;

		synchronized (PartyRateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRateUpdated.class,
				event -> sendPartyRateChangedMessage(((PartyRateUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyRate from the database
	 * 
	 * @param partyRateId:
	 *            the id of the PartyRate thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyRateById(@RequestParam(value = "partyRateId") String partyRateId) {

		DeletePartyRate com = new DeletePartyRate(partyRateId);

		int usedTicketId;

		synchronized (PartyRateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRateDeleted.class,
				event -> sendPartyRateChangedMessage(((PartyRateDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyRateChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyRate/\" plus one of the following: "
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
