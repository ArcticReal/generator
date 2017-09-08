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
import com.skytala.eCommerce.command.AddAgreementPromoAppl;
import com.skytala.eCommerce.command.DeleteAgreementPromoAppl;
import com.skytala.eCommerce.command.UpdateAgreementPromoAppl;
import com.skytala.eCommerce.entity.AgreementPromoAppl;
import com.skytala.eCommerce.entity.AgreementPromoApplMapper;
import com.skytala.eCommerce.event.AgreementPromoApplAdded;
import com.skytala.eCommerce.event.AgreementPromoApplDeleted;
import com.skytala.eCommerce.event.AgreementPromoApplFound;
import com.skytala.eCommerce.event.AgreementPromoApplUpdated;
import com.skytala.eCommerce.query.FindAgreementPromoApplsBy;

@RestController
@RequestMapping("/api/agreementPromoAppl")
public class AgreementPromoApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementPromoAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementPromoApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementPromoAppl
	 * @return a List with the AgreementPromoAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementPromoAppl> findAgreementPromoApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementPromoApplsBy query = new FindAgreementPromoApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementPromoApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementPromoApplFound.class,
				event -> sendAgreementPromoApplsFoundMessage(((AgreementPromoApplFound) event).getAgreementPromoAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementPromoApplsFoundMessage(List<AgreementPromoAppl> agreementPromoAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementPromoAppls);
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
	public boolean createAgreementPromoAppl(HttpServletRequest request) {

		AgreementPromoAppl agreementPromoApplToBeAdded = new AgreementPromoAppl();
		try {
			agreementPromoApplToBeAdded = AgreementPromoApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementPromoAppl(agreementPromoApplToBeAdded);

	}

	/**
	 * creates a new AgreementPromoAppl entry in the ofbiz database
	 * 
	 * @param agreementPromoApplToBeAdded
	 *            the AgreementPromoAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementPromoAppl(AgreementPromoAppl agreementPromoApplToBeAdded) {

		AddAgreementPromoAppl com = new AddAgreementPromoAppl(agreementPromoApplToBeAdded);
		int usedTicketId;

		synchronized (AgreementPromoApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementPromoApplAdded.class,
				event -> sendAgreementPromoApplChangedMessage(((AgreementPromoApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementPromoAppl(HttpServletRequest request) {

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

		AgreementPromoAppl agreementPromoApplToBeUpdated = new AgreementPromoAppl();

		try {
			agreementPromoApplToBeUpdated = AgreementPromoApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementPromoAppl(agreementPromoApplToBeUpdated);

	}

	/**
	 * Updates the AgreementPromoAppl with the specific Id
	 * 
	 * @param agreementPromoApplToBeUpdated the AgreementPromoAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementPromoAppl(AgreementPromoAppl agreementPromoApplToBeUpdated) {

		UpdateAgreementPromoAppl com = new UpdateAgreementPromoAppl(agreementPromoApplToBeUpdated);

		int usedTicketId;

		synchronized (AgreementPromoApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementPromoApplUpdated.class,
				event -> sendAgreementPromoApplChangedMessage(((AgreementPromoApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementPromoAppl from the database
	 * 
	 * @param agreementPromoApplId:
	 *            the id of the AgreementPromoAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementPromoApplById(@RequestParam(value = "agreementPromoApplId") String agreementPromoApplId) {

		DeleteAgreementPromoAppl com = new DeleteAgreementPromoAppl(agreementPromoApplId);

		int usedTicketId;

		synchronized (AgreementPromoApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementPromoApplDeleted.class,
				event -> sendAgreementPromoApplChangedMessage(((AgreementPromoApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementPromoApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementPromoAppl/\" plus one of the following: "
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
