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
import com.skytala.eCommerce.command.AddAgreementTypeAttr;
import com.skytala.eCommerce.command.DeleteAgreementTypeAttr;
import com.skytala.eCommerce.command.UpdateAgreementTypeAttr;
import com.skytala.eCommerce.entity.AgreementTypeAttr;
import com.skytala.eCommerce.entity.AgreementTypeAttrMapper;
import com.skytala.eCommerce.event.AgreementTypeAttrAdded;
import com.skytala.eCommerce.event.AgreementTypeAttrDeleted;
import com.skytala.eCommerce.event.AgreementTypeAttrFound;
import com.skytala.eCommerce.event.AgreementTypeAttrUpdated;
import com.skytala.eCommerce.query.FindAgreementTypeAttrsBy;

@RestController
@RequestMapping("/api/agreementTypeAttr")
public class AgreementTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementTypeAttr
	 * @return a List with the AgreementTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementTypeAttr> findAgreementTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementTypeAttrsBy query = new FindAgreementTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTypeAttrFound.class,
				event -> sendAgreementTypeAttrsFoundMessage(((AgreementTypeAttrFound) event).getAgreementTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementTypeAttrsFoundMessage(List<AgreementTypeAttr> agreementTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementTypeAttrs);
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
	public boolean createAgreementTypeAttr(HttpServletRequest request) {

		AgreementTypeAttr agreementTypeAttrToBeAdded = new AgreementTypeAttr();
		try {
			agreementTypeAttrToBeAdded = AgreementTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementTypeAttr(agreementTypeAttrToBeAdded);

	}

	/**
	 * creates a new AgreementTypeAttr entry in the ofbiz database
	 * 
	 * @param agreementTypeAttrToBeAdded
	 *            the AgreementTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementTypeAttr(AgreementTypeAttr agreementTypeAttrToBeAdded) {

		AddAgreementTypeAttr com = new AddAgreementTypeAttr(agreementTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (AgreementTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTypeAttrAdded.class,
				event -> sendAgreementTypeAttrChangedMessage(((AgreementTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementTypeAttr(HttpServletRequest request) {

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

		AgreementTypeAttr agreementTypeAttrToBeUpdated = new AgreementTypeAttr();

		try {
			agreementTypeAttrToBeUpdated = AgreementTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementTypeAttr(agreementTypeAttrToBeUpdated);

	}

	/**
	 * Updates the AgreementTypeAttr with the specific Id
	 * 
	 * @param agreementTypeAttrToBeUpdated the AgreementTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementTypeAttr(AgreementTypeAttr agreementTypeAttrToBeUpdated) {

		UpdateAgreementTypeAttr com = new UpdateAgreementTypeAttr(agreementTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (AgreementTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTypeAttrUpdated.class,
				event -> sendAgreementTypeAttrChangedMessage(((AgreementTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementTypeAttr from the database
	 * 
	 * @param agreementTypeAttrId:
	 *            the id of the AgreementTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementTypeAttrById(@RequestParam(value = "agreementTypeAttrId") String agreementTypeAttrId) {

		DeleteAgreementTypeAttr com = new DeleteAgreementTypeAttr(agreementTypeAttrId);

		int usedTicketId;

		synchronized (AgreementTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementTypeAttrDeleted.class,
				event -> sendAgreementTypeAttrChangedMessage(((AgreementTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementTypeAttr/\" plus one of the following: "
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
