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
import com.skytala.eCommerce.command.AddBillingAccount;
import com.skytala.eCommerce.command.DeleteBillingAccount;
import com.skytala.eCommerce.command.UpdateBillingAccount;
import com.skytala.eCommerce.entity.BillingAccount;
import com.skytala.eCommerce.entity.BillingAccountMapper;
import com.skytala.eCommerce.event.BillingAccountAdded;
import com.skytala.eCommerce.event.BillingAccountDeleted;
import com.skytala.eCommerce.event.BillingAccountFound;
import com.skytala.eCommerce.event.BillingAccountUpdated;
import com.skytala.eCommerce.query.FindBillingAccountsBy;

@RestController
@RequestMapping("/api/billingAccount")
public class BillingAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BillingAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BillingAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BillingAccount
	 * @return a List with the BillingAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BillingAccount> findBillingAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBillingAccountsBy query = new FindBillingAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (BillingAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountFound.class,
				event -> sendBillingAccountsFoundMessage(((BillingAccountFound) event).getBillingAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBillingAccountsFoundMessage(List<BillingAccount> billingAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, billingAccounts);
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
	public boolean createBillingAccount(HttpServletRequest request) {

		BillingAccount billingAccountToBeAdded = new BillingAccount();
		try {
			billingAccountToBeAdded = BillingAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBillingAccount(billingAccountToBeAdded);

	}

	/**
	 * creates a new BillingAccount entry in the ofbiz database
	 * 
	 * @param billingAccountToBeAdded
	 *            the BillingAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBillingAccount(BillingAccount billingAccountToBeAdded) {

		AddBillingAccount com = new AddBillingAccount(billingAccountToBeAdded);
		int usedTicketId;

		synchronized (BillingAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountAdded.class,
				event -> sendBillingAccountChangedMessage(((BillingAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBillingAccount(HttpServletRequest request) {

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

		BillingAccount billingAccountToBeUpdated = new BillingAccount();

		try {
			billingAccountToBeUpdated = BillingAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBillingAccount(billingAccountToBeUpdated);

	}

	/**
	 * Updates the BillingAccount with the specific Id
	 * 
	 * @param billingAccountToBeUpdated the BillingAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBillingAccount(BillingAccount billingAccountToBeUpdated) {

		UpdateBillingAccount com = new UpdateBillingAccount(billingAccountToBeUpdated);

		int usedTicketId;

		synchronized (BillingAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountUpdated.class,
				event -> sendBillingAccountChangedMessage(((BillingAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BillingAccount from the database
	 * 
	 * @param billingAccountId:
	 *            the id of the BillingAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebillingAccountById(@RequestParam(value = "billingAccountId") String billingAccountId) {

		DeleteBillingAccount com = new DeleteBillingAccount(billingAccountId);

		int usedTicketId;

		synchronized (BillingAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountDeleted.class,
				event -> sendBillingAccountChangedMessage(((BillingAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBillingAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/billingAccount/\" plus one of the following: "
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
