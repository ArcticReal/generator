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
import com.skytala.eCommerce.command.AddAgreementItemTypeAttr;
import com.skytala.eCommerce.command.DeleteAgreementItemTypeAttr;
import com.skytala.eCommerce.command.UpdateAgreementItemTypeAttr;
import com.skytala.eCommerce.entity.AgreementItemTypeAttr;
import com.skytala.eCommerce.entity.AgreementItemTypeAttrMapper;
import com.skytala.eCommerce.event.AgreementItemTypeAttrAdded;
import com.skytala.eCommerce.event.AgreementItemTypeAttrDeleted;
import com.skytala.eCommerce.event.AgreementItemTypeAttrFound;
import com.skytala.eCommerce.event.AgreementItemTypeAttrUpdated;
import com.skytala.eCommerce.query.FindAgreementItemTypeAttrsBy;

@RestController
@RequestMapping("/api/agreementItemTypeAttr")
public class AgreementItemTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementItemTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementItemTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementItemTypeAttr
	 * @return a List with the AgreementItemTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementItemTypeAttr> findAgreementItemTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementItemTypeAttrsBy query = new FindAgreementItemTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementItemTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemTypeAttrFound.class,
				event -> sendAgreementItemTypeAttrsFoundMessage(((AgreementItemTypeAttrFound) event).getAgreementItemTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementItemTypeAttrsFoundMessage(List<AgreementItemTypeAttr> agreementItemTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementItemTypeAttrs);
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
	public boolean createAgreementItemTypeAttr(HttpServletRequest request) {

		AgreementItemTypeAttr agreementItemTypeAttrToBeAdded = new AgreementItemTypeAttr();
		try {
			agreementItemTypeAttrToBeAdded = AgreementItemTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementItemTypeAttr(agreementItemTypeAttrToBeAdded);

	}

	/**
	 * creates a new AgreementItemTypeAttr entry in the ofbiz database
	 * 
	 * @param agreementItemTypeAttrToBeAdded
	 *            the AgreementItemTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementItemTypeAttr(AgreementItemTypeAttr agreementItemTypeAttrToBeAdded) {

		AddAgreementItemTypeAttr com = new AddAgreementItemTypeAttr(agreementItemTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (AgreementItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemTypeAttrAdded.class,
				event -> sendAgreementItemTypeAttrChangedMessage(((AgreementItemTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementItemTypeAttr(HttpServletRequest request) {

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

		AgreementItemTypeAttr agreementItemTypeAttrToBeUpdated = new AgreementItemTypeAttr();

		try {
			agreementItemTypeAttrToBeUpdated = AgreementItemTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementItemTypeAttr(agreementItemTypeAttrToBeUpdated);

	}

	/**
	 * Updates the AgreementItemTypeAttr with the specific Id
	 * 
	 * @param agreementItemTypeAttrToBeUpdated the AgreementItemTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementItemTypeAttr(AgreementItemTypeAttr agreementItemTypeAttrToBeUpdated) {

		UpdateAgreementItemTypeAttr com = new UpdateAgreementItemTypeAttr(agreementItemTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (AgreementItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemTypeAttrUpdated.class,
				event -> sendAgreementItemTypeAttrChangedMessage(((AgreementItemTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementItemTypeAttr from the database
	 * 
	 * @param agreementItemTypeAttrId:
	 *            the id of the AgreementItemTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementItemTypeAttrById(@RequestParam(value = "agreementItemTypeAttrId") String agreementItemTypeAttrId) {

		DeleteAgreementItemTypeAttr com = new DeleteAgreementItemTypeAttr(agreementItemTypeAttrId);

		int usedTicketId;

		synchronized (AgreementItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemTypeAttrDeleted.class,
				event -> sendAgreementItemTypeAttrChangedMessage(((AgreementItemTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementItemTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementItemTypeAttr/\" plus one of the following: "
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
