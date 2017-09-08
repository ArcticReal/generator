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
import com.skytala.eCommerce.command.AddAgreementTermAttribute;
import com.skytala.eCommerce.command.DeleteAgreementTermAttribute;
import com.skytala.eCommerce.command.UpdateAgreementTermAttribute;
import com.skytala.eCommerce.entity.AgreementTermAttribute;
import com.skytala.eCommerce.entity.AgreementTermAttributeMapper;
import com.skytala.eCommerce.event.AgreementTermAttributeAdded;
import com.skytala.eCommerce.event.AgreementTermAttributeDeleted;
import com.skytala.eCommerce.event.AgreementTermAttributeFound;
import com.skytala.eCommerce.event.AgreementTermAttributeUpdated;
import com.skytala.eCommerce.query.FindAgreementTermAttributesBy;

@RestController
@RequestMapping("/api/agreementTermAttribute")
public class AgreementTermAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementTermAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementTermAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementTermAttribute
	 * @return a List with the AgreementTermAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementTermAttribute> findAgreementTermAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementTermAttributesBy query = new FindAgreementTermAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementTermAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTermAttributeFound.class,
				event -> sendAgreementTermAttributesFoundMessage(((AgreementTermAttributeFound) event).getAgreementTermAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementTermAttributesFoundMessage(List<AgreementTermAttribute> agreementTermAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementTermAttributes);
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
	public boolean createAgreementTermAttribute(HttpServletRequest request) {

		AgreementTermAttribute agreementTermAttributeToBeAdded = new AgreementTermAttribute();
		try {
			agreementTermAttributeToBeAdded = AgreementTermAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementTermAttribute(agreementTermAttributeToBeAdded);

	}

	/**
	 * creates a new AgreementTermAttribute entry in the ofbiz database
	 * 
	 * @param agreementTermAttributeToBeAdded
	 *            the AgreementTermAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementTermAttribute(AgreementTermAttribute agreementTermAttributeToBeAdded) {

		AddAgreementTermAttribute com = new AddAgreementTermAttribute(agreementTermAttributeToBeAdded);
		int usedTicketId;

		synchronized (AgreementTermAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTermAttributeAdded.class,
				event -> sendAgreementTermAttributeChangedMessage(((AgreementTermAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementTermAttribute(HttpServletRequest request) {

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

		AgreementTermAttribute agreementTermAttributeToBeUpdated = new AgreementTermAttribute();

		try {
			agreementTermAttributeToBeUpdated = AgreementTermAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementTermAttribute(agreementTermAttributeToBeUpdated);

	}

	/**
	 * Updates the AgreementTermAttribute with the specific Id
	 * 
	 * @param agreementTermAttributeToBeUpdated the AgreementTermAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementTermAttribute(AgreementTermAttribute agreementTermAttributeToBeUpdated) {

		UpdateAgreementTermAttribute com = new UpdateAgreementTermAttribute(agreementTermAttributeToBeUpdated);

		int usedTicketId;

		synchronized (AgreementTermAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTermAttributeUpdated.class,
				event -> sendAgreementTermAttributeChangedMessage(((AgreementTermAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementTermAttribute from the database
	 * 
	 * @param agreementTermAttributeId:
	 *            the id of the AgreementTermAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementTermAttributeById(@RequestParam(value = "agreementTermAttributeId") String agreementTermAttributeId) {

		DeleteAgreementTermAttribute com = new DeleteAgreementTermAttribute(agreementTermAttributeId);

		int usedTicketId;

		synchronized (AgreementTermAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTermAttributeDeleted.class,
				event -> sendAgreementTermAttributeChangedMessage(((AgreementTermAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementTermAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementTermAttribute/\" plus one of the following: "
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
