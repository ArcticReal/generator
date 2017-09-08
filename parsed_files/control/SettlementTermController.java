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
import com.skytala.eCommerce.command.AddSettlementTerm;
import com.skytala.eCommerce.command.DeleteSettlementTerm;
import com.skytala.eCommerce.command.UpdateSettlementTerm;
import com.skytala.eCommerce.entity.SettlementTerm;
import com.skytala.eCommerce.entity.SettlementTermMapper;
import com.skytala.eCommerce.event.SettlementTermAdded;
import com.skytala.eCommerce.event.SettlementTermDeleted;
import com.skytala.eCommerce.event.SettlementTermFound;
import com.skytala.eCommerce.event.SettlementTermUpdated;
import com.skytala.eCommerce.query.FindSettlementTermsBy;

@RestController
@RequestMapping("/api/settlementTerm")
public class SettlementTermController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SettlementTerm>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SettlementTermController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SettlementTerm
	 * @return a List with the SettlementTerms
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SettlementTerm> findSettlementTermsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSettlementTermsBy query = new FindSettlementTermsBy(allRequestParams);

		int usedTicketId;

		synchronized (SettlementTermController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SettlementTermFound.class,
				event -> sendSettlementTermsFoundMessage(((SettlementTermFound) event).getSettlementTerms(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSettlementTermsFoundMessage(List<SettlementTerm> settlementTerms, int usedTicketId) {
		queryReturnVal.put(usedTicketId, settlementTerms);
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
	public boolean createSettlementTerm(HttpServletRequest request) {

		SettlementTerm settlementTermToBeAdded = new SettlementTerm();
		try {
			settlementTermToBeAdded = SettlementTermMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSettlementTerm(settlementTermToBeAdded);

	}

	/**
	 * creates a new SettlementTerm entry in the ofbiz database
	 * 
	 * @param settlementTermToBeAdded
	 *            the SettlementTerm thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSettlementTerm(SettlementTerm settlementTermToBeAdded) {

		AddSettlementTerm com = new AddSettlementTerm(settlementTermToBeAdded);
		int usedTicketId;

		synchronized (SettlementTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SettlementTermAdded.class,
				event -> sendSettlementTermChangedMessage(((SettlementTermAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSettlementTerm(HttpServletRequest request) {

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

		SettlementTerm settlementTermToBeUpdated = new SettlementTerm();

		try {
			settlementTermToBeUpdated = SettlementTermMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSettlementTerm(settlementTermToBeUpdated);

	}

	/**
	 * Updates the SettlementTerm with the specific Id
	 * 
	 * @param settlementTermToBeUpdated the SettlementTerm thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSettlementTerm(SettlementTerm settlementTermToBeUpdated) {

		UpdateSettlementTerm com = new UpdateSettlementTerm(settlementTermToBeUpdated);

		int usedTicketId;

		synchronized (SettlementTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SettlementTermUpdated.class,
				event -> sendSettlementTermChangedMessage(((SettlementTermUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SettlementTerm from the database
	 * 
	 * @param settlementTermId:
	 *            the id of the SettlementTerm thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesettlementTermById(@RequestParam(value = "settlementTermId") String settlementTermId) {

		DeleteSettlementTerm com = new DeleteSettlementTerm(settlementTermId);

		int usedTicketId;

		synchronized (SettlementTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SettlementTermDeleted.class,
				event -> sendSettlementTermChangedMessage(((SettlementTermDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSettlementTermChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/settlementTerm/\" plus one of the following: "
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
