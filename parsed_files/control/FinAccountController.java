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
import com.skytala.eCommerce.command.AddFinAccount;
import com.skytala.eCommerce.command.DeleteFinAccount;
import com.skytala.eCommerce.command.UpdateFinAccount;
import com.skytala.eCommerce.entity.FinAccount;
import com.skytala.eCommerce.entity.FinAccountMapper;
import com.skytala.eCommerce.event.FinAccountAdded;
import com.skytala.eCommerce.event.FinAccountDeleted;
import com.skytala.eCommerce.event.FinAccountFound;
import com.skytala.eCommerce.event.FinAccountUpdated;
import com.skytala.eCommerce.query.FindFinAccountsBy;

@RestController
@RequestMapping("/api/finAccount")
public class FinAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccount
	 * @return a List with the FinAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccount> findFinAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountsBy query = new FindFinAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountFound.class,
				event -> sendFinAccountsFoundMessage(((FinAccountFound) event).getFinAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountsFoundMessage(List<FinAccount> finAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccounts);
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
	public boolean createFinAccount(HttpServletRequest request) {

		FinAccount finAccountToBeAdded = new FinAccount();
		try {
			finAccountToBeAdded = FinAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccount(finAccountToBeAdded);

	}

	/**
	 * creates a new FinAccount entry in the ofbiz database
	 * 
	 * @param finAccountToBeAdded
	 *            the FinAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccount(FinAccount finAccountToBeAdded) {

		AddFinAccount com = new AddFinAccount(finAccountToBeAdded);
		int usedTicketId;

		synchronized (FinAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountAdded.class,
				event -> sendFinAccountChangedMessage(((FinAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccount(HttpServletRequest request) {

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

		FinAccount finAccountToBeUpdated = new FinAccount();

		try {
			finAccountToBeUpdated = FinAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccount(finAccountToBeUpdated);

	}

	/**
	 * Updates the FinAccount with the specific Id
	 * 
	 * @param finAccountToBeUpdated the FinAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccount(FinAccount finAccountToBeUpdated) {

		UpdateFinAccount com = new UpdateFinAccount(finAccountToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountUpdated.class,
				event -> sendFinAccountChangedMessage(((FinAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccount from the database
	 * 
	 * @param finAccountId:
	 *            the id of the FinAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountById(@RequestParam(value = "finAccountId") String finAccountId) {

		DeleteFinAccount com = new DeleteFinAccount(finAccountId);

		int usedTicketId;

		synchronized (FinAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountDeleted.class,
				event -> sendFinAccountChangedMessage(((FinAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccount/\" plus one of the following: "
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
