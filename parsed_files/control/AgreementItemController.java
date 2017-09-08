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
import com.skytala.eCommerce.command.AddAgreementItem;
import com.skytala.eCommerce.command.DeleteAgreementItem;
import com.skytala.eCommerce.command.UpdateAgreementItem;
import com.skytala.eCommerce.entity.AgreementItem;
import com.skytala.eCommerce.entity.AgreementItemMapper;
import com.skytala.eCommerce.event.AgreementItemAdded;
import com.skytala.eCommerce.event.AgreementItemDeleted;
import com.skytala.eCommerce.event.AgreementItemFound;
import com.skytala.eCommerce.event.AgreementItemUpdated;
import com.skytala.eCommerce.query.FindAgreementItemsBy;

@RestController
@RequestMapping("/api/agreementItem")
public class AgreementItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AgreementItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AgreementItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AgreementItem
	 * @return a List with the AgreementItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AgreementItem> findAgreementItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAgreementItemsBy query = new FindAgreementItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (AgreementItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemFound.class,
				event -> sendAgreementItemsFoundMessage(((AgreementItemFound) event).getAgreementItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAgreementItemsFoundMessage(List<AgreementItem> agreementItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, agreementItems);
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
	public boolean createAgreementItem(HttpServletRequest request) {

		AgreementItem agreementItemToBeAdded = new AgreementItem();
		try {
			agreementItemToBeAdded = AgreementItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAgreementItem(agreementItemToBeAdded);

	}

	/**
	 * creates a new AgreementItem entry in the ofbiz database
	 * 
	 * @param agreementItemToBeAdded
	 *            the AgreementItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAgreementItem(AgreementItem agreementItemToBeAdded) {

		AddAgreementItem com = new AddAgreementItem(agreementItemToBeAdded);
		int usedTicketId;

		synchronized (AgreementItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemAdded.class,
				event -> sendAgreementItemChangedMessage(((AgreementItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAgreementItem(HttpServletRequest request) {

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

		AgreementItem agreementItemToBeUpdated = new AgreementItem();

		try {
			agreementItemToBeUpdated = AgreementItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAgreementItem(agreementItemToBeUpdated);

	}

	/**
	 * Updates the AgreementItem with the specific Id
	 * 
	 * @param agreementItemToBeUpdated the AgreementItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAgreementItem(AgreementItem agreementItemToBeUpdated) {

		UpdateAgreementItem com = new UpdateAgreementItem(agreementItemToBeUpdated);

		int usedTicketId;

		synchronized (AgreementItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemUpdated.class,
				event -> sendAgreementItemChangedMessage(((AgreementItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AgreementItem from the database
	 * 
	 * @param agreementItemId:
	 *            the id of the AgreementItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteagreementItemById(@RequestParam(value = "agreementItemId") String agreementItemId) {

		DeleteAgreementItem com = new DeleteAgreementItem(agreementItemId);

		int usedTicketId;

		synchronized (AgreementItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AgreementItemDeleted.class,
				event -> sendAgreementItemChangedMessage(((AgreementItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAgreementItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/agreementItem/\" plus one of the following: "
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
