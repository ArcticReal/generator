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
import com.skytala.eCommerce.command.AddAgreementProductAppl;
import com.skytala.eCommerce.command.DeleteAgreementProductAppl;
import com.skytala.eCommerce.command.UpdateAgreementProductAppl;
import com.skytala.eCommerce.entity.AgreementProductAppl;
import com.skytala.eCommerce.entity.AgreementProductApplMapper;
import com.skytala.eCommerce.event.AgreementProductApplAdded;
import com.skytala.eCommerce.event.AgreementProductApplDeleted;
import com.skytala.eCommerce.event.AgreementProductApplFound;
import com.skytala.eCommerce.event.AgreementProductApplUpdated;
import com.skytala.eCommerce.query.FindAgreementProductApplsBy;

@RestController
@RequestMapping("/api/agreementProductAppl")
public class AgreementProductApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementProductAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementProductApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementProductAppl
	 * @return a List with the AgreementProductAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementProductAppl> findAgreementProductApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementProductApplsBy query = new FindAgreementProductApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementProductApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementProductApplFound.class,
				event -> sendAgreementProductApplsFoundMessage(((AgreementProductApplFound) event).getAgreementProductAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementProductApplsFoundMessage(List<AgreementProductAppl> agreementProductAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementProductAppls);
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
	public boolean createAgreementProductAppl(HttpServletRequest request) {

		AgreementProductAppl agreementProductApplToBeAdded = new AgreementProductAppl();
		try {
			agreementProductApplToBeAdded = AgreementProductApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementProductAppl(agreementProductApplToBeAdded);

	}

	/**
	 * creates a new AgreementProductAppl entry in the ofbiz database
	 * 
	 * @param agreementProductApplToBeAdded
	 *            the AgreementProductAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementProductAppl(AgreementProductAppl agreementProductApplToBeAdded) {

		AddAgreementProductAppl com = new AddAgreementProductAppl(agreementProductApplToBeAdded);
		int usedTicketId;

		synchronized (AgreementProductApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementProductApplAdded.class,
				event -> sendAgreementProductApplChangedMessage(((AgreementProductApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementProductAppl(HttpServletRequest request) {

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

		AgreementProductAppl agreementProductApplToBeUpdated = new AgreementProductAppl();

		try {
			agreementProductApplToBeUpdated = AgreementProductApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementProductAppl(agreementProductApplToBeUpdated);

	}

	/**
	 * Updates the AgreementProductAppl with the specific Id
	 * 
	 * @param agreementProductApplToBeUpdated the AgreementProductAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementProductAppl(AgreementProductAppl agreementProductApplToBeUpdated) {

		UpdateAgreementProductAppl com = new UpdateAgreementProductAppl(agreementProductApplToBeUpdated);

		int usedTicketId;

		synchronized (AgreementProductApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementProductApplUpdated.class,
				event -> sendAgreementProductApplChangedMessage(((AgreementProductApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementProductAppl from the database
	 * 
	 * @param agreementProductApplId:
	 *            the id of the AgreementProductAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementProductApplById(@RequestParam(value = "agreementProductApplId") String agreementProductApplId) {

		DeleteAgreementProductAppl com = new DeleteAgreementProductAppl(agreementProductApplId);

		int usedTicketId;

		synchronized (AgreementProductApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementProductApplDeleted.class,
				event -> sendAgreementProductApplChangedMessage(((AgreementProductApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementProductApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementProductAppl/\" plus one of the following: "
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
