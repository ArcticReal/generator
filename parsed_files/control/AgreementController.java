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
import com.skytala.eCommerce.command.AddAgreement;
import com.skytala.eCommerce.command.DeleteAgreement;
import com.skytala.eCommerce.command.UpdateAgreement;
import com.skytala.eCommerce.entity.Agreement;
import com.skytala.eCommerce.entity.AgreementMapper;
import com.skytala.eCommerce.event.AgreementAdded;
import com.skytala.eCommerce.event.AgreementDeleted;
import com.skytala.eCommerce.event.AgreementFound;
import com.skytala.eCommerce.event.AgreementUpdated;
import com.skytala.eCommerce.query.FindAgreementsBy;

@RestController
@RequestMapping("/api/agreement")
public class AgreementController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Agreement>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Agreement
	 * @return a List with the Agreements
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Agreement> findAgreementsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementsBy query = new FindAgreementsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementFound.class,
				event -> sendAgreementsFoundMessage(((AgreementFound) event).getAgreements(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementsFoundMessage(List<Agreement> agreements, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreements);
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
	public boolean createAgreement(HttpServletRequest request) {

		Agreement agreementToBeAdded = new Agreement();
		try {
			agreementToBeAdded = AgreementMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreement(agreementToBeAdded);

	}

	/**
	 * creates a new Agreement entry in the ofbiz database
	 * 
	 * @param agreementToBeAdded
	 *            the Agreement thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreement(Agreement agreementToBeAdded) {

		AddAgreement com = new AddAgreement(agreementToBeAdded);
		int usedTicketId;

		synchronized (AgreementController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementAdded.class,
				event -> sendAgreementChangedMessage(((AgreementAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreement(HttpServletRequest request) {

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

		Agreement agreementToBeUpdated = new Agreement();

		try {
			agreementToBeUpdated = AgreementMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreement(agreementToBeUpdated);

	}

	/**
	 * Updates the Agreement with the specific Id
	 * 
	 * @param agreementToBeUpdated the Agreement thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreement(Agreement agreementToBeUpdated) {

		UpdateAgreement com = new UpdateAgreement(agreementToBeUpdated);

		int usedTicketId;

		synchronized (AgreementController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementUpdated.class,
				event -> sendAgreementChangedMessage(((AgreementUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Agreement from the database
	 * 
	 * @param agreementId:
	 *            the id of the Agreement thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementById(@RequestParam(value = "agreementId") String agreementId) {

		DeleteAgreement com = new DeleteAgreement(agreementId);

		int usedTicketId;

		synchronized (AgreementController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementDeleted.class,
				event -> sendAgreementChangedMessage(((AgreementDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreement/\" plus one of the following: "
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
