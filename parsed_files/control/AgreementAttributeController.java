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
import com.skytala.eCommerce.command.AddAgreementAttribute;
import com.skytala.eCommerce.command.DeleteAgreementAttribute;
import com.skytala.eCommerce.command.UpdateAgreementAttribute;
import com.skytala.eCommerce.entity.AgreementAttribute;
import com.skytala.eCommerce.entity.AgreementAttributeMapper;
import com.skytala.eCommerce.event.AgreementAttributeAdded;
import com.skytala.eCommerce.event.AgreementAttributeDeleted;
import com.skytala.eCommerce.event.AgreementAttributeFound;
import com.skytala.eCommerce.event.AgreementAttributeUpdated;
import com.skytala.eCommerce.query.FindAgreementAttributesBy;

@RestController
@RequestMapping("/api/agreementAttribute")
public class AgreementAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementAttribute
	 * @return a List with the AgreementAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementAttribute> findAgreementAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementAttributesBy query = new FindAgreementAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementAttributeFound.class,
				event -> sendAgreementAttributesFoundMessage(((AgreementAttributeFound) event).getAgreementAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementAttributesFoundMessage(List<AgreementAttribute> agreementAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementAttributes);
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
	public boolean createAgreementAttribute(HttpServletRequest request) {

		AgreementAttribute agreementAttributeToBeAdded = new AgreementAttribute();
		try {
			agreementAttributeToBeAdded = AgreementAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementAttribute(agreementAttributeToBeAdded);

	}

	/**
	 * creates a new AgreementAttribute entry in the ofbiz database
	 * 
	 * @param agreementAttributeToBeAdded
	 *            the AgreementAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementAttribute(AgreementAttribute agreementAttributeToBeAdded) {

		AddAgreementAttribute com = new AddAgreementAttribute(agreementAttributeToBeAdded);
		int usedTicketId;

		synchronized (AgreementAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementAttributeAdded.class,
				event -> sendAgreementAttributeChangedMessage(((AgreementAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementAttribute(HttpServletRequest request) {

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

		AgreementAttribute agreementAttributeToBeUpdated = new AgreementAttribute();

		try {
			agreementAttributeToBeUpdated = AgreementAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementAttribute(agreementAttributeToBeUpdated);

	}

	/**
	 * Updates the AgreementAttribute with the specific Id
	 * 
	 * @param agreementAttributeToBeUpdated the AgreementAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementAttribute(AgreementAttribute agreementAttributeToBeUpdated) {

		UpdateAgreementAttribute com = new UpdateAgreementAttribute(agreementAttributeToBeUpdated);

		int usedTicketId;

		synchronized (AgreementAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementAttributeUpdated.class,
				event -> sendAgreementAttributeChangedMessage(((AgreementAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementAttribute from the database
	 * 
	 * @param agreementAttributeId:
	 *            the id of the AgreementAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementAttributeById(@RequestParam(value = "agreementAttributeId") String agreementAttributeId) {

		DeleteAgreementAttribute com = new DeleteAgreementAttribute(agreementAttributeId);

		int usedTicketId;

		synchronized (AgreementAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementAttributeDeleted.class,
				event -> sendAgreementAttributeChangedMessage(((AgreementAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementAttribute/\" plus one of the following: "
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
