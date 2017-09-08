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
import com.skytala.eCommerce.command.AddAgreementType;
import com.skytala.eCommerce.command.DeleteAgreementType;
import com.skytala.eCommerce.command.UpdateAgreementType;
import com.skytala.eCommerce.entity.AgreementType;
import com.skytala.eCommerce.entity.AgreementTypeMapper;
import com.skytala.eCommerce.event.AgreementTypeAdded;
import com.skytala.eCommerce.event.AgreementTypeDeleted;
import com.skytala.eCommerce.event.AgreementTypeFound;
import com.skytala.eCommerce.event.AgreementTypeUpdated;
import com.skytala.eCommerce.query.FindAgreementTypesBy;

@RestController
@RequestMapping("/api/agreementType")
public class AgreementTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementType
	 * @return a List with the AgreementTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementType> findAgreementTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementTypesBy query = new FindAgreementTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTypeFound.class,
				event -> sendAgreementTypesFoundMessage(((AgreementTypeFound) event).getAgreementTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementTypesFoundMessage(List<AgreementType> agreementTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementTypes);
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
	public boolean createAgreementType(HttpServletRequest request) {

		AgreementType agreementTypeToBeAdded = new AgreementType();
		try {
			agreementTypeToBeAdded = AgreementTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementType(agreementTypeToBeAdded);

	}

	/**
	 * creates a new AgreementType entry in the ofbiz database
	 * 
	 * @param agreementTypeToBeAdded
	 *            the AgreementType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementType(AgreementType agreementTypeToBeAdded) {

		AddAgreementType com = new AddAgreementType(agreementTypeToBeAdded);
		int usedTicketId;

		synchronized (AgreementTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTypeAdded.class,
				event -> sendAgreementTypeChangedMessage(((AgreementTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementType(HttpServletRequest request) {

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

		AgreementType agreementTypeToBeUpdated = new AgreementType();

		try {
			agreementTypeToBeUpdated = AgreementTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementType(agreementTypeToBeUpdated);

	}

	/**
	 * Updates the AgreementType with the specific Id
	 * 
	 * @param agreementTypeToBeUpdated the AgreementType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementType(AgreementType agreementTypeToBeUpdated) {

		UpdateAgreementType com = new UpdateAgreementType(agreementTypeToBeUpdated);

		int usedTicketId;

		synchronized (AgreementTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTypeUpdated.class,
				event -> sendAgreementTypeChangedMessage(((AgreementTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementType from the database
	 * 
	 * @param agreementTypeId:
	 *            the id of the AgreementType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementTypeById(@RequestParam(value = "agreementTypeId") String agreementTypeId) {

		DeleteAgreementType com = new DeleteAgreementType(agreementTypeId);

		int usedTicketId;

		synchronized (AgreementTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTypeDeleted.class,
				event -> sendAgreementTypeChangedMessage(((AgreementTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementType/\" plus one of the following: "
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
