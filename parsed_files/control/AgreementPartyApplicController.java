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
import com.skytala.eCommerce.command.AddAgreementPartyApplic;
import com.skytala.eCommerce.command.DeleteAgreementPartyApplic;
import com.skytala.eCommerce.command.UpdateAgreementPartyApplic;
import com.skytala.eCommerce.entity.AgreementPartyApplic;
import com.skytala.eCommerce.entity.AgreementPartyApplicMapper;
import com.skytala.eCommerce.event.AgreementPartyApplicAdded;
import com.skytala.eCommerce.event.AgreementPartyApplicDeleted;
import com.skytala.eCommerce.event.AgreementPartyApplicFound;
import com.skytala.eCommerce.event.AgreementPartyApplicUpdated;
import com.skytala.eCommerce.query.FindAgreementPartyApplicsBy;

@RestController
@RequestMapping("/api/agreementPartyApplic")
public class AgreementPartyApplicController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementPartyApplic>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementPartyApplicController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementPartyApplic
	 * @return a List with the AgreementPartyApplics
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementPartyApplic> findAgreementPartyApplicsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementPartyApplicsBy query = new FindAgreementPartyApplicsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementPartyApplicController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementPartyApplicFound.class,
				event -> sendAgreementPartyApplicsFoundMessage(((AgreementPartyApplicFound) event).getAgreementPartyApplics(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementPartyApplicsFoundMessage(List<AgreementPartyApplic> agreementPartyApplics, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementPartyApplics);
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
	public boolean createAgreementPartyApplic(HttpServletRequest request) {

		AgreementPartyApplic agreementPartyApplicToBeAdded = new AgreementPartyApplic();
		try {
			agreementPartyApplicToBeAdded = AgreementPartyApplicMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementPartyApplic(agreementPartyApplicToBeAdded);

	}

	/**
	 * creates a new AgreementPartyApplic entry in the ofbiz database
	 * 
	 * @param agreementPartyApplicToBeAdded
	 *            the AgreementPartyApplic thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementPartyApplic(AgreementPartyApplic agreementPartyApplicToBeAdded) {

		AddAgreementPartyApplic com = new AddAgreementPartyApplic(agreementPartyApplicToBeAdded);
		int usedTicketId;

		synchronized (AgreementPartyApplicController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementPartyApplicAdded.class,
				event -> sendAgreementPartyApplicChangedMessage(((AgreementPartyApplicAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementPartyApplic(HttpServletRequest request) {

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

		AgreementPartyApplic agreementPartyApplicToBeUpdated = new AgreementPartyApplic();

		try {
			agreementPartyApplicToBeUpdated = AgreementPartyApplicMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementPartyApplic(agreementPartyApplicToBeUpdated);

	}

	/**
	 * Updates the AgreementPartyApplic with the specific Id
	 * 
	 * @param agreementPartyApplicToBeUpdated the AgreementPartyApplic thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementPartyApplic(AgreementPartyApplic agreementPartyApplicToBeUpdated) {

		UpdateAgreementPartyApplic com = new UpdateAgreementPartyApplic(agreementPartyApplicToBeUpdated);

		int usedTicketId;

		synchronized (AgreementPartyApplicController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementPartyApplicUpdated.class,
				event -> sendAgreementPartyApplicChangedMessage(((AgreementPartyApplicUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementPartyApplic from the database
	 * 
	 * @param agreementPartyApplicId:
	 *            the id of the AgreementPartyApplic thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementPartyApplicById(@RequestParam(value = "agreementPartyApplicId") String agreementPartyApplicId) {

		DeleteAgreementPartyApplic com = new DeleteAgreementPartyApplic(agreementPartyApplicId);

		int usedTicketId;

		synchronized (AgreementPartyApplicController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementPartyApplicDeleted.class,
				event -> sendAgreementPartyApplicChangedMessage(((AgreementPartyApplicDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementPartyApplicChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementPartyApplic/\" plus one of the following: "
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
