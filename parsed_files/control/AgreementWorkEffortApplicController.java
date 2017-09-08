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
import com.skytala.eCommerce.command.AddAgreementWorkEffortApplic;
import com.skytala.eCommerce.command.DeleteAgreementWorkEffortApplic;
import com.skytala.eCommerce.command.UpdateAgreementWorkEffortApplic;
import com.skytala.eCommerce.entity.AgreementWorkEffortApplic;
import com.skytala.eCommerce.entity.AgreementWorkEffortApplicMapper;
import com.skytala.eCommerce.event.AgreementWorkEffortApplicAdded;
import com.skytala.eCommerce.event.AgreementWorkEffortApplicDeleted;
import com.skytala.eCommerce.event.AgreementWorkEffortApplicFound;
import com.skytala.eCommerce.event.AgreementWorkEffortApplicUpdated;
import com.skytala.eCommerce.query.FindAgreementWorkEffortApplicsBy;

@RestController
@RequestMapping("/api/agreementWorkEffortApplic")
public class AgreementWorkEffortApplicController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementWorkEffortApplic>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementWorkEffortApplicController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementWorkEffortApplic
	 * @return a List with the AgreementWorkEffortApplics
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementWorkEffortApplic> findAgreementWorkEffortApplicsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementWorkEffortApplicsBy query = new FindAgreementWorkEffortApplicsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementWorkEffortApplicController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementWorkEffortApplicFound.class,
				event -> sendAgreementWorkEffortApplicsFoundMessage(((AgreementWorkEffortApplicFound) event).getAgreementWorkEffortApplics(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementWorkEffortApplicsFoundMessage(List<AgreementWorkEffortApplic> agreementWorkEffortApplics, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementWorkEffortApplics);
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
	public boolean createAgreementWorkEffortApplic(HttpServletRequest request) {

		AgreementWorkEffortApplic agreementWorkEffortApplicToBeAdded = new AgreementWorkEffortApplic();
		try {
			agreementWorkEffortApplicToBeAdded = AgreementWorkEffortApplicMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementWorkEffortApplic(agreementWorkEffortApplicToBeAdded);

	}

	/**
	 * creates a new AgreementWorkEffortApplic entry in the ofbiz database
	 * 
	 * @param agreementWorkEffortApplicToBeAdded
	 *            the AgreementWorkEffortApplic thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementWorkEffortApplic(AgreementWorkEffortApplic agreementWorkEffortApplicToBeAdded) {

		AddAgreementWorkEffortApplic com = new AddAgreementWorkEffortApplic(agreementWorkEffortApplicToBeAdded);
		int usedTicketId;

		synchronized (AgreementWorkEffortApplicController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementWorkEffortApplicAdded.class,
				event -> sendAgreementWorkEffortApplicChangedMessage(((AgreementWorkEffortApplicAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementWorkEffortApplic(HttpServletRequest request) {

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

		AgreementWorkEffortApplic agreementWorkEffortApplicToBeUpdated = new AgreementWorkEffortApplic();

		try {
			agreementWorkEffortApplicToBeUpdated = AgreementWorkEffortApplicMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementWorkEffortApplic(agreementWorkEffortApplicToBeUpdated);

	}

	/**
	 * Updates the AgreementWorkEffortApplic with the specific Id
	 * 
	 * @param agreementWorkEffortApplicToBeUpdated the AgreementWorkEffortApplic thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementWorkEffortApplic(AgreementWorkEffortApplic agreementWorkEffortApplicToBeUpdated) {

		UpdateAgreementWorkEffortApplic com = new UpdateAgreementWorkEffortApplic(agreementWorkEffortApplicToBeUpdated);

		int usedTicketId;

		synchronized (AgreementWorkEffortApplicController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementWorkEffortApplicUpdated.class,
				event -> sendAgreementWorkEffortApplicChangedMessage(((AgreementWorkEffortApplicUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementWorkEffortApplic from the database
	 * 
	 * @param agreementWorkEffortApplicId:
	 *            the id of the AgreementWorkEffortApplic thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementWorkEffortApplicById(@RequestParam(value = "agreementWorkEffortApplicId") String agreementWorkEffortApplicId) {

		DeleteAgreementWorkEffortApplic com = new DeleteAgreementWorkEffortApplic(agreementWorkEffortApplicId);

		int usedTicketId;

		synchronized (AgreementWorkEffortApplicController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementWorkEffortApplicDeleted.class,
				event -> sendAgreementWorkEffortApplicChangedMessage(((AgreementWorkEffortApplicDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementWorkEffortApplicChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementWorkEffortApplic/\" plus one of the following: "
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
