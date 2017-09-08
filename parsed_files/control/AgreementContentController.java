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
import com.skytala.eCommerce.command.AddAgreementContent;
import com.skytala.eCommerce.command.DeleteAgreementContent;
import com.skytala.eCommerce.command.UpdateAgreementContent;
import com.skytala.eCommerce.entity.AgreementContent;
import com.skytala.eCommerce.entity.AgreementContentMapper;
import com.skytala.eCommerce.event.AgreementContentAdded;
import com.skytala.eCommerce.event.AgreementContentDeleted;
import com.skytala.eCommerce.event.AgreementContentFound;
import com.skytala.eCommerce.event.AgreementContentUpdated;
import com.skytala.eCommerce.query.FindAgreementContentsBy;

@RestController
@RequestMapping("/api/agreementContent")
public class AgreementContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementContent
	 * @return a List with the AgreementContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementContent> findAgreementContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementContentsBy query = new FindAgreementContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementContentFound.class,
				event -> sendAgreementContentsFoundMessage(((AgreementContentFound) event).getAgreementContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementContentsFoundMessage(List<AgreementContent> agreementContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementContents);
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
	public boolean createAgreementContent(HttpServletRequest request) {

		AgreementContent agreementContentToBeAdded = new AgreementContent();
		try {
			agreementContentToBeAdded = AgreementContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementContent(agreementContentToBeAdded);

	}

	/**
	 * creates a new AgreementContent entry in the ofbiz database
	 * 
	 * @param agreementContentToBeAdded
	 *            the AgreementContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementContent(AgreementContent agreementContentToBeAdded) {

		AddAgreementContent com = new AddAgreementContent(agreementContentToBeAdded);
		int usedTicketId;

		synchronized (AgreementContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementContentAdded.class,
				event -> sendAgreementContentChangedMessage(((AgreementContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementContent(HttpServletRequest request) {

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

		AgreementContent agreementContentToBeUpdated = new AgreementContent();

		try {
			agreementContentToBeUpdated = AgreementContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementContent(agreementContentToBeUpdated);

	}

	/**
	 * Updates the AgreementContent with the specific Id
	 * 
	 * @param agreementContentToBeUpdated the AgreementContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementContent(AgreementContent agreementContentToBeUpdated) {

		UpdateAgreementContent com = new UpdateAgreementContent(agreementContentToBeUpdated);

		int usedTicketId;

		synchronized (AgreementContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementContentUpdated.class,
				event -> sendAgreementContentChangedMessage(((AgreementContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementContent from the database
	 * 
	 * @param agreementContentId:
	 *            the id of the AgreementContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementContentById(@RequestParam(value = "agreementContentId") String agreementContentId) {

		DeleteAgreementContent com = new DeleteAgreementContent(agreementContentId);

		int usedTicketId;

		synchronized (AgreementContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementContentDeleted.class,
				event -> sendAgreementContentChangedMessage(((AgreementContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementContent/\" plus one of the following: "
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
