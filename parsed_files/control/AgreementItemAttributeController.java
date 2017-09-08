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
import com.skytala.eCommerce.command.AddAgreementItemAttribute;
import com.skytala.eCommerce.command.DeleteAgreementItemAttribute;
import com.skytala.eCommerce.command.UpdateAgreementItemAttribute;
import com.skytala.eCommerce.entity.AgreementItemAttribute;
import com.skytala.eCommerce.entity.AgreementItemAttributeMapper;
import com.skytala.eCommerce.event.AgreementItemAttributeAdded;
import com.skytala.eCommerce.event.AgreementItemAttributeDeleted;
import com.skytala.eCommerce.event.AgreementItemAttributeFound;
import com.skytala.eCommerce.event.AgreementItemAttributeUpdated;
import com.skytala.eCommerce.query.FindAgreementItemAttributesBy;

@RestController
@RequestMapping("/api/agreementItemAttribute")
public class AgreementItemAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementItemAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementItemAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementItemAttribute
	 * @return a List with the AgreementItemAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementItemAttribute> findAgreementItemAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementItemAttributesBy query = new FindAgreementItemAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementItemAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemAttributeFound.class,
				event -> sendAgreementItemAttributesFoundMessage(((AgreementItemAttributeFound) event).getAgreementItemAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementItemAttributesFoundMessage(List<AgreementItemAttribute> agreementItemAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementItemAttributes);
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
	public boolean createAgreementItemAttribute(HttpServletRequest request) {

		AgreementItemAttribute agreementItemAttributeToBeAdded = new AgreementItemAttribute();
		try {
			agreementItemAttributeToBeAdded = AgreementItemAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementItemAttribute(agreementItemAttributeToBeAdded);

	}

	/**
	 * creates a new AgreementItemAttribute entry in the ofbiz database
	 * 
	 * @param agreementItemAttributeToBeAdded
	 *            the AgreementItemAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementItemAttribute(AgreementItemAttribute agreementItemAttributeToBeAdded) {

		AddAgreementItemAttribute com = new AddAgreementItemAttribute(agreementItemAttributeToBeAdded);
		int usedTicketId;

		synchronized (AgreementItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemAttributeAdded.class,
				event -> sendAgreementItemAttributeChangedMessage(((AgreementItemAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementItemAttribute(HttpServletRequest request) {

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

		AgreementItemAttribute agreementItemAttributeToBeUpdated = new AgreementItemAttribute();

		try {
			agreementItemAttributeToBeUpdated = AgreementItemAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementItemAttribute(agreementItemAttributeToBeUpdated);

	}

	/**
	 * Updates the AgreementItemAttribute with the specific Id
	 * 
	 * @param agreementItemAttributeToBeUpdated the AgreementItemAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementItemAttribute(AgreementItemAttribute agreementItemAttributeToBeUpdated) {

		UpdateAgreementItemAttribute com = new UpdateAgreementItemAttribute(agreementItemAttributeToBeUpdated);

		int usedTicketId;

		synchronized (AgreementItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemAttributeUpdated.class,
				event -> sendAgreementItemAttributeChangedMessage(((AgreementItemAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementItemAttribute from the database
	 * 
	 * @param agreementItemAttributeId:
	 *            the id of the AgreementItemAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementItemAttributeById(@RequestParam(value = "agreementItemAttributeId") String agreementItemAttributeId) {

		DeleteAgreementItemAttribute com = new DeleteAgreementItemAttribute(agreementItemAttributeId);

		int usedTicketId;

		synchronized (AgreementItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemAttributeDeleted.class,
				event -> sendAgreementItemAttributeChangedMessage(((AgreementItemAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementItemAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementItemAttribute/\" plus one of the following: "
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
