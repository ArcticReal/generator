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
import com.skytala.eCommerce.command.AddAgreementContentType;
import com.skytala.eCommerce.command.DeleteAgreementContentType;
import com.skytala.eCommerce.command.UpdateAgreementContentType;
import com.skytala.eCommerce.entity.AgreementContentType;
import com.skytala.eCommerce.entity.AgreementContentTypeMapper;
import com.skytala.eCommerce.event.AgreementContentTypeAdded;
import com.skytala.eCommerce.event.AgreementContentTypeDeleted;
import com.skytala.eCommerce.event.AgreementContentTypeFound;
import com.skytala.eCommerce.event.AgreementContentTypeUpdated;
import com.skytala.eCommerce.query.FindAgreementContentTypesBy;

@RestController
@RequestMapping("/api/agreementContentType")
public class AgreementContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementContentType
	 * @return a List with the AgreementContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementContentType> findAgreementContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementContentTypesBy query = new FindAgreementContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementContentTypeFound.class,
				event -> sendAgreementContentTypesFoundMessage(((AgreementContentTypeFound) event).getAgreementContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementContentTypesFoundMessage(List<AgreementContentType> agreementContentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementContentTypes);
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
	public boolean createAgreementContentType(HttpServletRequest request) {

		AgreementContentType agreementContentTypeToBeAdded = new AgreementContentType();
		try {
			agreementContentTypeToBeAdded = AgreementContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementContentType(agreementContentTypeToBeAdded);

	}

	/**
	 * creates a new AgreementContentType entry in the ofbiz database
	 * 
	 * @param agreementContentTypeToBeAdded
	 *            the AgreementContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementContentType(AgreementContentType agreementContentTypeToBeAdded) {

		AddAgreementContentType com = new AddAgreementContentType(agreementContentTypeToBeAdded);
		int usedTicketId;

		synchronized (AgreementContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementContentTypeAdded.class,
				event -> sendAgreementContentTypeChangedMessage(((AgreementContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementContentType(HttpServletRequest request) {

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

		AgreementContentType agreementContentTypeToBeUpdated = new AgreementContentType();

		try {
			agreementContentTypeToBeUpdated = AgreementContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementContentType(agreementContentTypeToBeUpdated);

	}

	/**
	 * Updates the AgreementContentType with the specific Id
	 * 
	 * @param agreementContentTypeToBeUpdated the AgreementContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementContentType(AgreementContentType agreementContentTypeToBeUpdated) {

		UpdateAgreementContentType com = new UpdateAgreementContentType(agreementContentTypeToBeUpdated);

		int usedTicketId;

		synchronized (AgreementContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementContentTypeUpdated.class,
				event -> sendAgreementContentTypeChangedMessage(((AgreementContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementContentType from the database
	 * 
	 * @param agreementContentTypeId:
	 *            the id of the AgreementContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementContentTypeById(@RequestParam(value = "agreementContentTypeId") String agreementContentTypeId) {

		DeleteAgreementContentType com = new DeleteAgreementContentType(agreementContentTypeId);

		int usedTicketId;

		synchronized (AgreementContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementContentTypeDeleted.class,
				event -> sendAgreementContentTypeChangedMessage(((AgreementContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementContentType/\" plus one of the following: "
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
