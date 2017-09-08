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
import com.skytala.eCommerce.command.AddCreditCardTypeGlAccount;
import com.skytala.eCommerce.command.DeleteCreditCardTypeGlAccount;
import com.skytala.eCommerce.command.UpdateCreditCardTypeGlAccount;
import com.skytala.eCommerce.entity.CreditCardTypeGlAccount;
import com.skytala.eCommerce.entity.CreditCardTypeGlAccountMapper;
import com.skytala.eCommerce.event.CreditCardTypeGlAccountAdded;
import com.skytala.eCommerce.event.CreditCardTypeGlAccountDeleted;
import com.skytala.eCommerce.event.CreditCardTypeGlAccountFound;
import com.skytala.eCommerce.event.CreditCardTypeGlAccountUpdated;
import com.skytala.eCommerce.query.FindCreditCardTypeGlAccountsBy;

@RestController
@RequestMapping("/api/creditCardTypeGlAccount")
public class CreditCardTypeGlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CreditCardTypeGlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CreditCardTypeGlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CreditCardTypeGlAccount
	 * @return a List with the CreditCardTypeGlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CreditCardTypeGlAccount> findCreditCardTypeGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCreditCardTypeGlAccountsBy query = new FindCreditCardTypeGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (CreditCardTypeGlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CreditCardTypeGlAccountFound.class,
				event -> sendCreditCardTypeGlAccountsFoundMessage(((CreditCardTypeGlAccountFound) event).getCreditCardTypeGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCreditCardTypeGlAccountsFoundMessage(List<CreditCardTypeGlAccount> creditCardTypeGlAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, creditCardTypeGlAccounts);
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
	public boolean createCreditCardTypeGlAccount(HttpServletRequest request) {

		CreditCardTypeGlAccount creditCardTypeGlAccountToBeAdded = new CreditCardTypeGlAccount();
		try {
			creditCardTypeGlAccountToBeAdded = CreditCardTypeGlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCreditCardTypeGlAccount(creditCardTypeGlAccountToBeAdded);

	}

	/**
	 * creates a new CreditCardTypeGlAccount entry in the ofbiz database
	 * 
	 * @param creditCardTypeGlAccountToBeAdded
	 *            the CreditCardTypeGlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCreditCardTypeGlAccount(CreditCardTypeGlAccount creditCardTypeGlAccountToBeAdded) {

		AddCreditCardTypeGlAccount com = new AddCreditCardTypeGlAccount(creditCardTypeGlAccountToBeAdded);
		int usedTicketId;

		synchronized (CreditCardTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CreditCardTypeGlAccountAdded.class,
				event -> sendCreditCardTypeGlAccountChangedMessage(((CreditCardTypeGlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCreditCardTypeGlAccount(HttpServletRequest request) {

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

		CreditCardTypeGlAccount creditCardTypeGlAccountToBeUpdated = new CreditCardTypeGlAccount();

		try {
			creditCardTypeGlAccountToBeUpdated = CreditCardTypeGlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCreditCardTypeGlAccount(creditCardTypeGlAccountToBeUpdated);

	}

	/**
	 * Updates the CreditCardTypeGlAccount with the specific Id
	 * 
	 * @param creditCardTypeGlAccountToBeUpdated the CreditCardTypeGlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCreditCardTypeGlAccount(CreditCardTypeGlAccount creditCardTypeGlAccountToBeUpdated) {

		UpdateCreditCardTypeGlAccount com = new UpdateCreditCardTypeGlAccount(creditCardTypeGlAccountToBeUpdated);

		int usedTicketId;

		synchronized (CreditCardTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CreditCardTypeGlAccountUpdated.class,
				event -> sendCreditCardTypeGlAccountChangedMessage(((CreditCardTypeGlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CreditCardTypeGlAccount from the database
	 * 
	 * @param creditCardTypeGlAccountId:
	 *            the id of the CreditCardTypeGlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecreditCardTypeGlAccountById(@RequestParam(value = "creditCardTypeGlAccountId") String creditCardTypeGlAccountId) {

		DeleteCreditCardTypeGlAccount com = new DeleteCreditCardTypeGlAccount(creditCardTypeGlAccountId);

		int usedTicketId;

		synchronized (CreditCardTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CreditCardTypeGlAccountDeleted.class,
				event -> sendCreditCardTypeGlAccountChangedMessage(((CreditCardTypeGlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCreditCardTypeGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/creditCardTypeGlAccount/\" plus one of the following: "
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
