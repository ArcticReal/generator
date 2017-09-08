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
import com.skytala.eCommerce.command.AddPartyCarrierAccount;
import com.skytala.eCommerce.command.DeletePartyCarrierAccount;
import com.skytala.eCommerce.command.UpdatePartyCarrierAccount;
import com.skytala.eCommerce.entity.PartyCarrierAccount;
import com.skytala.eCommerce.entity.PartyCarrierAccountMapper;
import com.skytala.eCommerce.event.PartyCarrierAccountAdded;
import com.skytala.eCommerce.event.PartyCarrierAccountDeleted;
import com.skytala.eCommerce.event.PartyCarrierAccountFound;
import com.skytala.eCommerce.event.PartyCarrierAccountUpdated;
import com.skytala.eCommerce.query.FindPartyCarrierAccountsBy;

@RestController
@RequestMapping("/api/partyCarrierAccount")
public class PartyCarrierAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyCarrierAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyCarrierAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyCarrierAccount
	 * @return a List with the PartyCarrierAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyCarrierAccount> findPartyCarrierAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyCarrierAccountsBy query = new FindPartyCarrierAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyCarrierAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyCarrierAccountFound.class,
				event -> sendPartyCarrierAccountsFoundMessage(((PartyCarrierAccountFound) event).getPartyCarrierAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyCarrierAccountsFoundMessage(List<PartyCarrierAccount> partyCarrierAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyCarrierAccounts);
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
	public boolean createPartyCarrierAccount(HttpServletRequest request) {

		PartyCarrierAccount partyCarrierAccountToBeAdded = new PartyCarrierAccount();
		try {
			partyCarrierAccountToBeAdded = PartyCarrierAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyCarrierAccount(partyCarrierAccountToBeAdded);

	}

	/**
	 * creates a new PartyCarrierAccount entry in the ofbiz database
	 * 
	 * @param partyCarrierAccountToBeAdded
	 *            the PartyCarrierAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyCarrierAccount(PartyCarrierAccount partyCarrierAccountToBeAdded) {

		AddPartyCarrierAccount com = new AddPartyCarrierAccount(partyCarrierAccountToBeAdded);
		int usedTicketId;

		synchronized (PartyCarrierAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyCarrierAccountAdded.class,
				event -> sendPartyCarrierAccountChangedMessage(((PartyCarrierAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyCarrierAccount(HttpServletRequest request) {

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

		PartyCarrierAccount partyCarrierAccountToBeUpdated = new PartyCarrierAccount();

		try {
			partyCarrierAccountToBeUpdated = PartyCarrierAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyCarrierAccount(partyCarrierAccountToBeUpdated);

	}

	/**
	 * Updates the PartyCarrierAccount with the specific Id
	 * 
	 * @param partyCarrierAccountToBeUpdated the PartyCarrierAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyCarrierAccount(PartyCarrierAccount partyCarrierAccountToBeUpdated) {

		UpdatePartyCarrierAccount com = new UpdatePartyCarrierAccount(partyCarrierAccountToBeUpdated);

		int usedTicketId;

		synchronized (PartyCarrierAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyCarrierAccountUpdated.class,
				event -> sendPartyCarrierAccountChangedMessage(((PartyCarrierAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyCarrierAccount from the database
	 * 
	 * @param partyCarrierAccountId:
	 *            the id of the PartyCarrierAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyCarrierAccountById(@RequestParam(value = "partyCarrierAccountId") String partyCarrierAccountId) {

		DeletePartyCarrierAccount com = new DeletePartyCarrierAccount(partyCarrierAccountId);

		int usedTicketId;

		synchronized (PartyCarrierAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyCarrierAccountDeleted.class,
				event -> sendPartyCarrierAccountChangedMessage(((PartyCarrierAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyCarrierAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyCarrierAccount/\" plus one of the following: "
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
