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
import com.skytala.eCommerce.command.AddPartyBenefit;
import com.skytala.eCommerce.command.DeletePartyBenefit;
import com.skytala.eCommerce.command.UpdatePartyBenefit;
import com.skytala.eCommerce.entity.PartyBenefit;
import com.skytala.eCommerce.entity.PartyBenefitMapper;
import com.skytala.eCommerce.event.PartyBenefitAdded;
import com.skytala.eCommerce.event.PartyBenefitDeleted;
import com.skytala.eCommerce.event.PartyBenefitFound;
import com.skytala.eCommerce.event.PartyBenefitUpdated;
import com.skytala.eCommerce.query.FindPartyBenefitsBy;

@RestController
@RequestMapping("/api/partyBenefit")
public class PartyBenefitController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyBenefit>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyBenefitController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyBenefit
	 * @return a List with the PartyBenefits
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyBenefit> findPartyBenefitsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyBenefitsBy query = new FindPartyBenefitsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyBenefitController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyBenefitFound.class,
				event -> sendPartyBenefitsFoundMessage(((PartyBenefitFound) event).getPartyBenefits(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyBenefitsFoundMessage(List<PartyBenefit> partyBenefits, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyBenefits);
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
	public boolean createPartyBenefit(HttpServletRequest request) {

		PartyBenefit partyBenefitToBeAdded = new PartyBenefit();
		try {
			partyBenefitToBeAdded = PartyBenefitMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyBenefit(partyBenefitToBeAdded);

	}

	/**
	 * creates a new PartyBenefit entry in the ofbiz database
	 * 
	 * @param partyBenefitToBeAdded
	 *            the PartyBenefit thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyBenefit(PartyBenefit partyBenefitToBeAdded) {

		AddPartyBenefit com = new AddPartyBenefit(partyBenefitToBeAdded);
		int usedTicketId;

		synchronized (PartyBenefitController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyBenefitAdded.class,
				event -> sendPartyBenefitChangedMessage(((PartyBenefitAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyBenefit(HttpServletRequest request) {

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

		PartyBenefit partyBenefitToBeUpdated = new PartyBenefit();

		try {
			partyBenefitToBeUpdated = PartyBenefitMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyBenefit(partyBenefitToBeUpdated);

	}

	/**
	 * Updates the PartyBenefit with the specific Id
	 * 
	 * @param partyBenefitToBeUpdated the PartyBenefit thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyBenefit(PartyBenefit partyBenefitToBeUpdated) {

		UpdatePartyBenefit com = new UpdatePartyBenefit(partyBenefitToBeUpdated);

		int usedTicketId;

		synchronized (PartyBenefitController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyBenefitUpdated.class,
				event -> sendPartyBenefitChangedMessage(((PartyBenefitUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyBenefit from the database
	 * 
	 * @param partyBenefitId:
	 *            the id of the PartyBenefit thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyBenefitById(@RequestParam(value = "partyBenefitId") String partyBenefitId) {

		DeletePartyBenefit com = new DeletePartyBenefit(partyBenefitId);

		int usedTicketId;

		synchronized (PartyBenefitController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyBenefitDeleted.class,
				event -> sendPartyBenefitChangedMessage(((PartyBenefitDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyBenefitChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyBenefit/\" plus one of the following: "
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
