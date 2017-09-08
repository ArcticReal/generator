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
import com.skytala.eCommerce.command.AddCheckAccount;
import com.skytala.eCommerce.command.DeleteCheckAccount;
import com.skytala.eCommerce.command.UpdateCheckAccount;
import com.skytala.eCommerce.entity.CheckAccount;
import com.skytala.eCommerce.entity.CheckAccountMapper;
import com.skytala.eCommerce.event.CheckAccountAdded;
import com.skytala.eCommerce.event.CheckAccountDeleted;
import com.skytala.eCommerce.event.CheckAccountFound;
import com.skytala.eCommerce.event.CheckAccountUpdated;
import com.skytala.eCommerce.query.FindCheckAccountsBy;

@RestController
@RequestMapping("/api/checkAccount")
public class CheckAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CheckAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CheckAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CheckAccount
	 * @return a List with the CheckAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CheckAccount> findCheckAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCheckAccountsBy query = new FindCheckAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (CheckAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CheckAccountFound.class,
				event -> sendCheckAccountsFoundMessage(((CheckAccountFound) event).getCheckAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCheckAccountsFoundMessage(List<CheckAccount> checkAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, checkAccounts);
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
	public boolean createCheckAccount(HttpServletRequest request) {

		CheckAccount checkAccountToBeAdded = new CheckAccount();
		try {
			checkAccountToBeAdded = CheckAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCheckAccount(checkAccountToBeAdded);

	}

	/**
	 * creates a new CheckAccount entry in the ofbiz database
	 * 
	 * @param checkAccountToBeAdded
	 *            the CheckAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCheckAccount(CheckAccount checkAccountToBeAdded) {

		AddCheckAccount com = new AddCheckAccount(checkAccountToBeAdded);
		int usedTicketId;

		synchronized (CheckAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CheckAccountAdded.class,
				event -> sendCheckAccountChangedMessage(((CheckAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCheckAccount(HttpServletRequest request) {

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

		CheckAccount checkAccountToBeUpdated = new CheckAccount();

		try {
			checkAccountToBeUpdated = CheckAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCheckAccount(checkAccountToBeUpdated);

	}

	/**
	 * Updates the CheckAccount with the specific Id
	 * 
	 * @param checkAccountToBeUpdated the CheckAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCheckAccount(CheckAccount checkAccountToBeUpdated) {

		UpdateCheckAccount com = new UpdateCheckAccount(checkAccountToBeUpdated);

		int usedTicketId;

		synchronized (CheckAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CheckAccountUpdated.class,
				event -> sendCheckAccountChangedMessage(((CheckAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CheckAccount from the database
	 * 
	 * @param checkAccountId:
	 *            the id of the CheckAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecheckAccountById(@RequestParam(value = "checkAccountId") String checkAccountId) {

		DeleteCheckAccount com = new DeleteCheckAccount(checkAccountId);

		int usedTicketId;

		synchronized (CheckAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CheckAccountDeleted.class,
				event -> sendCheckAccountChangedMessage(((CheckAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCheckAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/checkAccount/\" plus one of the following: "
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
