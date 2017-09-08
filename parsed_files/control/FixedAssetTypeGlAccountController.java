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
import com.skytala.eCommerce.command.AddFixedAssetTypeGlAccount;
import com.skytala.eCommerce.command.DeleteFixedAssetTypeGlAccount;
import com.skytala.eCommerce.command.UpdateFixedAssetTypeGlAccount;
import com.skytala.eCommerce.entity.FixedAssetTypeGlAccount;
import com.skytala.eCommerce.entity.FixedAssetTypeGlAccountMapper;
import com.skytala.eCommerce.event.FixedAssetTypeGlAccountAdded;
import com.skytala.eCommerce.event.FixedAssetTypeGlAccountDeleted;
import com.skytala.eCommerce.event.FixedAssetTypeGlAccountFound;
import com.skytala.eCommerce.event.FixedAssetTypeGlAccountUpdated;
import com.skytala.eCommerce.query.FindFixedAssetTypeGlAccountsBy;

@RestController
@RequestMapping("/api/fixedAssetTypeGlAccount")
public class FixedAssetTypeGlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetTypeGlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetTypeGlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetTypeGlAccount
	 * @return a List with the FixedAssetTypeGlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetTypeGlAccount> findFixedAssetTypeGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetTypeGlAccountsBy query = new FindFixedAssetTypeGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetTypeGlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetTypeGlAccountFound.class,
				event -> sendFixedAssetTypeGlAccountsFoundMessage(((FixedAssetTypeGlAccountFound) event).getFixedAssetTypeGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetTypeGlAccountsFoundMessage(List<FixedAssetTypeGlAccount> fixedAssetTypeGlAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetTypeGlAccounts);
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
	public boolean createFixedAssetTypeGlAccount(HttpServletRequest request) {

		FixedAssetTypeGlAccount fixedAssetTypeGlAccountToBeAdded = new FixedAssetTypeGlAccount();
		try {
			fixedAssetTypeGlAccountToBeAdded = FixedAssetTypeGlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetTypeGlAccount(fixedAssetTypeGlAccountToBeAdded);

	}

	/**
	 * creates a new FixedAssetTypeGlAccount entry in the ofbiz database
	 * 
	 * @param fixedAssetTypeGlAccountToBeAdded
	 *            the FixedAssetTypeGlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetTypeGlAccount(FixedAssetTypeGlAccount fixedAssetTypeGlAccountToBeAdded) {

		AddFixedAssetTypeGlAccount com = new AddFixedAssetTypeGlAccount(fixedAssetTypeGlAccountToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetTypeGlAccountAdded.class,
				event -> sendFixedAssetTypeGlAccountChangedMessage(((FixedAssetTypeGlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetTypeGlAccount(HttpServletRequest request) {

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

		FixedAssetTypeGlAccount fixedAssetTypeGlAccountToBeUpdated = new FixedAssetTypeGlAccount();

		try {
			fixedAssetTypeGlAccountToBeUpdated = FixedAssetTypeGlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetTypeGlAccount(fixedAssetTypeGlAccountToBeUpdated);

	}

	/**
	 * Updates the FixedAssetTypeGlAccount with the specific Id
	 * 
	 * @param fixedAssetTypeGlAccountToBeUpdated the FixedAssetTypeGlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetTypeGlAccount(FixedAssetTypeGlAccount fixedAssetTypeGlAccountToBeUpdated) {

		UpdateFixedAssetTypeGlAccount com = new UpdateFixedAssetTypeGlAccount(fixedAssetTypeGlAccountToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetTypeGlAccountUpdated.class,
				event -> sendFixedAssetTypeGlAccountChangedMessage(((FixedAssetTypeGlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetTypeGlAccount from the database
	 * 
	 * @param fixedAssetTypeGlAccountId:
	 *            the id of the FixedAssetTypeGlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetTypeGlAccountById(@RequestParam(value = "fixedAssetTypeGlAccountId") String fixedAssetTypeGlAccountId) {

		DeleteFixedAssetTypeGlAccount com = new DeleteFixedAssetTypeGlAccount(fixedAssetTypeGlAccountId);

		int usedTicketId;

		synchronized (FixedAssetTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetTypeGlAccountDeleted.class,
				event -> sendFixedAssetTypeGlAccountChangedMessage(((FixedAssetTypeGlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetTypeGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetTypeGlAccount/\" plus one of the following: "
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
