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
import com.skytala.eCommerce.command.AddAgreementItemType;
import com.skytala.eCommerce.command.DeleteAgreementItemType;
import com.skytala.eCommerce.command.UpdateAgreementItemType;
import com.skytala.eCommerce.entity.AgreementItemType;
import com.skytala.eCommerce.entity.AgreementItemTypeMapper;
import com.skytala.eCommerce.event.AgreementItemTypeAdded;
import com.skytala.eCommerce.event.AgreementItemTypeDeleted;
import com.skytala.eCommerce.event.AgreementItemTypeFound;
import com.skytala.eCommerce.event.AgreementItemTypeUpdated;
import com.skytala.eCommerce.query.FindAgreementItemTypesBy;

@RestController
@RequestMapping("/api/agreementItemType")
public class AgreementItemTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementItemType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementItemTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementItemType
	 * @return a List with the AgreementItemTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementItemType> findAgreementItemTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementItemTypesBy query = new FindAgreementItemTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementItemTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemTypeFound.class,
				event -> sendAgreementItemTypesFoundMessage(((AgreementItemTypeFound) event).getAgreementItemTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementItemTypesFoundMessage(List<AgreementItemType> agreementItemTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementItemTypes);
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
	public boolean createAgreementItemType(HttpServletRequest request) {

		AgreementItemType agreementItemTypeToBeAdded = new AgreementItemType();
		try {
			agreementItemTypeToBeAdded = AgreementItemTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementItemType(agreementItemTypeToBeAdded);

	}

	/**
	 * creates a new AgreementItemType entry in the ofbiz database
	 * 
	 * @param agreementItemTypeToBeAdded
	 *            the AgreementItemType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementItemType(AgreementItemType agreementItemTypeToBeAdded) {

		AddAgreementItemType com = new AddAgreementItemType(agreementItemTypeToBeAdded);
		int usedTicketId;

		synchronized (AgreementItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemTypeAdded.class,
				event -> sendAgreementItemTypeChangedMessage(((AgreementItemTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementItemType(HttpServletRequest request) {

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

		AgreementItemType agreementItemTypeToBeUpdated = new AgreementItemType();

		try {
			agreementItemTypeToBeUpdated = AgreementItemTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementItemType(agreementItemTypeToBeUpdated);

	}

	/**
	 * Updates the AgreementItemType with the specific Id
	 * 
	 * @param agreementItemTypeToBeUpdated the AgreementItemType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementItemType(AgreementItemType agreementItemTypeToBeUpdated) {

		UpdateAgreementItemType com = new UpdateAgreementItemType(agreementItemTypeToBeUpdated);

		int usedTicketId;

		synchronized (AgreementItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemTypeUpdated.class,
				event -> sendAgreementItemTypeChangedMessage(((AgreementItemTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementItemType from the database
	 * 
	 * @param agreementItemTypeId:
	 *            the id of the AgreementItemType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementItemTypeById(@RequestParam(value = "agreementItemTypeId") String agreementItemTypeId) {

		DeleteAgreementItemType com = new DeleteAgreementItemType(agreementItemTypeId);

		int usedTicketId;

		synchronized (AgreementItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemTypeDeleted.class,
				event -> sendAgreementItemTypeChangedMessage(((AgreementItemTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementItemTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementItemType/\" plus one of the following: "
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
