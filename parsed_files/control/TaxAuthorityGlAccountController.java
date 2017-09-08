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
import com.skytala.eCommerce.command.AddTaxAuthorityGlAccount;
import com.skytala.eCommerce.command.DeleteTaxAuthorityGlAccount;
import com.skytala.eCommerce.command.UpdateTaxAuthorityGlAccount;
import com.skytala.eCommerce.entity.TaxAuthorityGlAccount;
import com.skytala.eCommerce.entity.TaxAuthorityGlAccountMapper;
import com.skytala.eCommerce.event.TaxAuthorityGlAccountAdded;
import com.skytala.eCommerce.event.TaxAuthorityGlAccountDeleted;
import com.skytala.eCommerce.event.TaxAuthorityGlAccountFound;
import com.skytala.eCommerce.event.TaxAuthorityGlAccountUpdated;
import com.skytala.eCommerce.query.FindTaxAuthorityGlAccountsBy;

@RestController
@RequestMapping("/api/taxAuthorityGlAccount")
public class TaxAuthorityGlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TaxAuthorityGlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TaxAuthorityGlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TaxAuthorityGlAccount
	 * @return a List with the TaxAuthorityGlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TaxAuthorityGlAccount> findTaxAuthorityGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTaxAuthorityGlAccountsBy query = new FindTaxAuthorityGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (TaxAuthorityGlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityGlAccountFound.class,
				event -> sendTaxAuthorityGlAccountsFoundMessage(((TaxAuthorityGlAccountFound) event).getTaxAuthorityGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTaxAuthorityGlAccountsFoundMessage(List<TaxAuthorityGlAccount> taxAuthorityGlAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, taxAuthorityGlAccounts);
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
	public boolean createTaxAuthorityGlAccount(HttpServletRequest request) {

		TaxAuthorityGlAccount taxAuthorityGlAccountToBeAdded = new TaxAuthorityGlAccount();
		try {
			taxAuthorityGlAccountToBeAdded = TaxAuthorityGlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTaxAuthorityGlAccount(taxAuthorityGlAccountToBeAdded);

	}

	/**
	 * creates a new TaxAuthorityGlAccount entry in the ofbiz database
	 * 
	 * @param taxAuthorityGlAccountToBeAdded
	 *            the TaxAuthorityGlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTaxAuthorityGlAccount(TaxAuthorityGlAccount taxAuthorityGlAccountToBeAdded) {

		AddTaxAuthorityGlAccount com = new AddTaxAuthorityGlAccount(taxAuthorityGlAccountToBeAdded);
		int usedTicketId;

		synchronized (TaxAuthorityGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityGlAccountAdded.class,
				event -> sendTaxAuthorityGlAccountChangedMessage(((TaxAuthorityGlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTaxAuthorityGlAccount(HttpServletRequest request) {

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

		TaxAuthorityGlAccount taxAuthorityGlAccountToBeUpdated = new TaxAuthorityGlAccount();

		try {
			taxAuthorityGlAccountToBeUpdated = TaxAuthorityGlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTaxAuthorityGlAccount(taxAuthorityGlAccountToBeUpdated);

	}

	/**
	 * Updates the TaxAuthorityGlAccount with the specific Id
	 * 
	 * @param taxAuthorityGlAccountToBeUpdated the TaxAuthorityGlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTaxAuthorityGlAccount(TaxAuthorityGlAccount taxAuthorityGlAccountToBeUpdated) {

		UpdateTaxAuthorityGlAccount com = new UpdateTaxAuthorityGlAccount(taxAuthorityGlAccountToBeUpdated);

		int usedTicketId;

		synchronized (TaxAuthorityGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityGlAccountUpdated.class,
				event -> sendTaxAuthorityGlAccountChangedMessage(((TaxAuthorityGlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TaxAuthorityGlAccount from the database
	 * 
	 * @param taxAuthorityGlAccountId:
	 *            the id of the TaxAuthorityGlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetaxAuthorityGlAccountById(@RequestParam(value = "taxAuthorityGlAccountId") String taxAuthorityGlAccountId) {

		DeleteTaxAuthorityGlAccount com = new DeleteTaxAuthorityGlAccount(taxAuthorityGlAccountId);

		int usedTicketId;

		synchronized (TaxAuthorityGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityGlAccountDeleted.class,
				event -> sendTaxAuthorityGlAccountChangedMessage(((TaxAuthorityGlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTaxAuthorityGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/taxAuthorityGlAccount/\" plus one of the following: "
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
