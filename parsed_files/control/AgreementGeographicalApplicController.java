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
import com.skytala.eCommerce.command.AddAgreementGeographicalApplic;
import com.skytala.eCommerce.command.DeleteAgreementGeographicalApplic;
import com.skytala.eCommerce.command.UpdateAgreementGeographicalApplic;
import com.skytala.eCommerce.entity.AgreementGeographicalApplic;
import com.skytala.eCommerce.entity.AgreementGeographicalApplicMapper;
import com.skytala.eCommerce.event.AgreementGeographicalApplicAdded;
import com.skytala.eCommerce.event.AgreementGeographicalApplicDeleted;
import com.skytala.eCommerce.event.AgreementGeographicalApplicFound;
import com.skytala.eCommerce.event.AgreementGeographicalApplicUpdated;
import com.skytala.eCommerce.query.FindAgreementGeographicalApplicsBy;

@RestController
@RequestMapping("/api/agreementGeographicalApplic")
public class AgreementGeographicalApplicController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementGeographicalApplic>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementGeographicalApplicController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementGeographicalApplic
	 * @return a List with the AgreementGeographicalApplics
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementGeographicalApplic> findAgreementGeographicalApplicsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementGeographicalApplicsBy query = new FindAgreementGeographicalApplicsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementGeographicalApplicController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementGeographicalApplicFound.class,
				event -> sendAgreementGeographicalApplicsFoundMessage(((AgreementGeographicalApplicFound) event).getAgreementGeographicalApplics(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementGeographicalApplicsFoundMessage(List<AgreementGeographicalApplic> agreementGeographicalApplics, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementGeographicalApplics);
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
	public boolean createAgreementGeographicalApplic(HttpServletRequest request) {

		AgreementGeographicalApplic agreementGeographicalApplicToBeAdded = new AgreementGeographicalApplic();
		try {
			agreementGeographicalApplicToBeAdded = AgreementGeographicalApplicMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementGeographicalApplic(agreementGeographicalApplicToBeAdded);

	}

	/**
	 * creates a new AgreementGeographicalApplic entry in the ofbiz database
	 * 
	 * @param agreementGeographicalApplicToBeAdded
	 *            the AgreementGeographicalApplic thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementGeographicalApplic(AgreementGeographicalApplic agreementGeographicalApplicToBeAdded) {

		AddAgreementGeographicalApplic com = new AddAgreementGeographicalApplic(agreementGeographicalApplicToBeAdded);
		int usedTicketId;

		synchronized (AgreementGeographicalApplicController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementGeographicalApplicAdded.class,
				event -> sendAgreementGeographicalApplicChangedMessage(((AgreementGeographicalApplicAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementGeographicalApplic(HttpServletRequest request) {

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

		AgreementGeographicalApplic agreementGeographicalApplicToBeUpdated = new AgreementGeographicalApplic();

		try {
			agreementGeographicalApplicToBeUpdated = AgreementGeographicalApplicMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementGeographicalApplic(agreementGeographicalApplicToBeUpdated);

	}

	/**
	 * Updates the AgreementGeographicalApplic with the specific Id
	 * 
	 * @param agreementGeographicalApplicToBeUpdated the AgreementGeographicalApplic thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementGeographicalApplic(AgreementGeographicalApplic agreementGeographicalApplicToBeUpdated) {

		UpdateAgreementGeographicalApplic com = new UpdateAgreementGeographicalApplic(agreementGeographicalApplicToBeUpdated);

		int usedTicketId;

		synchronized (AgreementGeographicalApplicController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementGeographicalApplicUpdated.class,
				event -> sendAgreementGeographicalApplicChangedMessage(((AgreementGeographicalApplicUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementGeographicalApplic from the database
	 * 
	 * @param agreementGeographicalApplicId:
	 *            the id of the AgreementGeographicalApplic thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementGeographicalApplicById(@RequestParam(value = "agreementGeographicalApplicId") String agreementGeographicalApplicId) {

		DeleteAgreementGeographicalApplic com = new DeleteAgreementGeographicalApplic(agreementGeographicalApplicId);

		int usedTicketId;

		synchronized (AgreementGeographicalApplicController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementGeographicalApplicDeleted.class,
				event -> sendAgreementGeographicalApplicChangedMessage(((AgreementGeographicalApplicDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementGeographicalApplicChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementGeographicalApplic/\" plus one of the following: "
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
