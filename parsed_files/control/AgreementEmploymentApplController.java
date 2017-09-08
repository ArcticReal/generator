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
import com.skytala.eCommerce.command.AddAgreementEmploymentAppl;
import com.skytala.eCommerce.command.DeleteAgreementEmploymentAppl;
import com.skytala.eCommerce.command.UpdateAgreementEmploymentAppl;
import com.skytala.eCommerce.entity.AgreementEmploymentAppl;
import com.skytala.eCommerce.entity.AgreementEmploymentApplMapper;
import com.skytala.eCommerce.event.AgreementEmploymentApplAdded;
import com.skytala.eCommerce.event.AgreementEmploymentApplDeleted;
import com.skytala.eCommerce.event.AgreementEmploymentApplFound;
import com.skytala.eCommerce.event.AgreementEmploymentApplUpdated;
import com.skytala.eCommerce.query.FindAgreementEmploymentApplsBy;

@RestController
@RequestMapping("/api/agreementEmploymentAppl")
public class AgreementEmploymentApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementEmploymentAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementEmploymentApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementEmploymentAppl
	 * @return a List with the AgreementEmploymentAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementEmploymentAppl> findAgreementEmploymentApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementEmploymentApplsBy query = new FindAgreementEmploymentApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementEmploymentApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementEmploymentApplFound.class,
				event -> sendAgreementEmploymentApplsFoundMessage(((AgreementEmploymentApplFound) event).getAgreementEmploymentAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementEmploymentApplsFoundMessage(List<AgreementEmploymentAppl> agreementEmploymentAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementEmploymentAppls);
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
	public boolean createAgreementEmploymentAppl(HttpServletRequest request) {

		AgreementEmploymentAppl agreementEmploymentApplToBeAdded = new AgreementEmploymentAppl();
		try {
			agreementEmploymentApplToBeAdded = AgreementEmploymentApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementEmploymentAppl(agreementEmploymentApplToBeAdded);

	}

	/**
	 * creates a new AgreementEmploymentAppl entry in the ofbiz database
	 * 
	 * @param agreementEmploymentApplToBeAdded
	 *            the AgreementEmploymentAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementEmploymentAppl(AgreementEmploymentAppl agreementEmploymentApplToBeAdded) {

		AddAgreementEmploymentAppl com = new AddAgreementEmploymentAppl(agreementEmploymentApplToBeAdded);
		int usedTicketId;

		synchronized (AgreementEmploymentApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementEmploymentApplAdded.class,
				event -> sendAgreementEmploymentApplChangedMessage(((AgreementEmploymentApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementEmploymentAppl(HttpServletRequest request) {

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

		AgreementEmploymentAppl agreementEmploymentApplToBeUpdated = new AgreementEmploymentAppl();

		try {
			agreementEmploymentApplToBeUpdated = AgreementEmploymentApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementEmploymentAppl(agreementEmploymentApplToBeUpdated);

	}

	/**
	 * Updates the AgreementEmploymentAppl with the specific Id
	 * 
	 * @param agreementEmploymentApplToBeUpdated the AgreementEmploymentAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementEmploymentAppl(AgreementEmploymentAppl agreementEmploymentApplToBeUpdated) {

		UpdateAgreementEmploymentAppl com = new UpdateAgreementEmploymentAppl(agreementEmploymentApplToBeUpdated);

		int usedTicketId;

		synchronized (AgreementEmploymentApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementEmploymentApplUpdated.class,
				event -> sendAgreementEmploymentApplChangedMessage(((AgreementEmploymentApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementEmploymentAppl from the database
	 * 
	 * @param agreementEmploymentApplId:
	 *            the id of the AgreementEmploymentAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementEmploymentApplById(@RequestParam(value = "agreementEmploymentApplId") String agreementEmploymentApplId) {

		DeleteAgreementEmploymentAppl com = new DeleteAgreementEmploymentAppl(agreementEmploymentApplId);

		int usedTicketId;

		synchronized (AgreementEmploymentApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementEmploymentApplDeleted.class,
				event -> sendAgreementEmploymentApplChangedMessage(((AgreementEmploymentApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementEmploymentApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementEmploymentAppl/\" plus one of the following: "
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
