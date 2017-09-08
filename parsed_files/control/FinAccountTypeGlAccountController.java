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
import com.skytala.eCommerce.command.AddFinAccountTypeGlAccount;
import com.skytala.eCommerce.command.DeleteFinAccountTypeGlAccount;
import com.skytala.eCommerce.command.UpdateFinAccountTypeGlAccount;
import com.skytala.eCommerce.entity.FinAccountTypeGlAccount;
import com.skytala.eCommerce.entity.FinAccountTypeGlAccountMapper;
import com.skytala.eCommerce.event.FinAccountTypeGlAccountAdded;
import com.skytala.eCommerce.event.FinAccountTypeGlAccountDeleted;
import com.skytala.eCommerce.event.FinAccountTypeGlAccountFound;
import com.skytala.eCommerce.event.FinAccountTypeGlAccountUpdated;
import com.skytala.eCommerce.query.FindFinAccountTypeGlAccountsBy;

@RestController
@RequestMapping("/api/finAccountTypeGlAccount")
public class FinAccountTypeGlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccountTypeGlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountTypeGlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccountTypeGlAccount
	 * @return a List with the FinAccountTypeGlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccountTypeGlAccount> findFinAccountTypeGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountTypeGlAccountsBy query = new FindFinAccountTypeGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountTypeGlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeGlAccountFound.class,
				event -> sendFinAccountTypeGlAccountsFoundMessage(((FinAccountTypeGlAccountFound) event).getFinAccountTypeGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountTypeGlAccountsFoundMessage(List<FinAccountTypeGlAccount> finAccountTypeGlAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccountTypeGlAccounts);
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
	public boolean createFinAccountTypeGlAccount(HttpServletRequest request) {

		FinAccountTypeGlAccount finAccountTypeGlAccountToBeAdded = new FinAccountTypeGlAccount();
		try {
			finAccountTypeGlAccountToBeAdded = FinAccountTypeGlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccountTypeGlAccount(finAccountTypeGlAccountToBeAdded);

	}

	/**
	 * creates a new FinAccountTypeGlAccount entry in the ofbiz database
	 * 
	 * @param finAccountTypeGlAccountToBeAdded
	 *            the FinAccountTypeGlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccountTypeGlAccount(FinAccountTypeGlAccount finAccountTypeGlAccountToBeAdded) {

		AddFinAccountTypeGlAccount com = new AddFinAccountTypeGlAccount(finAccountTypeGlAccountToBeAdded);
		int usedTicketId;

		synchronized (FinAccountTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeGlAccountAdded.class,
				event -> sendFinAccountTypeGlAccountChangedMessage(((FinAccountTypeGlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccountTypeGlAccount(HttpServletRequest request) {

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

		FinAccountTypeGlAccount finAccountTypeGlAccountToBeUpdated = new FinAccountTypeGlAccount();

		try {
			finAccountTypeGlAccountToBeUpdated = FinAccountTypeGlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccountTypeGlAccount(finAccountTypeGlAccountToBeUpdated);

	}

	/**
	 * Updates the FinAccountTypeGlAccount with the specific Id
	 * 
	 * @param finAccountTypeGlAccountToBeUpdated the FinAccountTypeGlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccountTypeGlAccount(FinAccountTypeGlAccount finAccountTypeGlAccountToBeUpdated) {

		UpdateFinAccountTypeGlAccount com = new UpdateFinAccountTypeGlAccount(finAccountTypeGlAccountToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeGlAccountUpdated.class,
				event -> sendFinAccountTypeGlAccountChangedMessage(((FinAccountTypeGlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccountTypeGlAccount from the database
	 * 
	 * @param finAccountTypeGlAccountId:
	 *            the id of the FinAccountTypeGlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountTypeGlAccountById(@RequestParam(value = "finAccountTypeGlAccountId") String finAccountTypeGlAccountId) {

		DeleteFinAccountTypeGlAccount com = new DeleteFinAccountTypeGlAccount(finAccountTypeGlAccountId);

		int usedTicketId;

		synchronized (FinAccountTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeGlAccountDeleted.class,
				event -> sendFinAccountTypeGlAccountChangedMessage(((FinAccountTypeGlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountTypeGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccountTypeGlAccount/\" plus one of the following: "
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
