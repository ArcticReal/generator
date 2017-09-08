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
import com.skytala.eCommerce.command.AddInvoiceItemTypeGlAccount;
import com.skytala.eCommerce.command.DeleteInvoiceItemTypeGlAccount;
import com.skytala.eCommerce.command.UpdateInvoiceItemTypeGlAccount;
import com.skytala.eCommerce.entity.InvoiceItemTypeGlAccount;
import com.skytala.eCommerce.entity.InvoiceItemTypeGlAccountMapper;
import com.skytala.eCommerce.event.InvoiceItemTypeGlAccountAdded;
import com.skytala.eCommerce.event.InvoiceItemTypeGlAccountDeleted;
import com.skytala.eCommerce.event.InvoiceItemTypeGlAccountFound;
import com.skytala.eCommerce.event.InvoiceItemTypeGlAccountUpdated;
import com.skytala.eCommerce.query.FindInvoiceItemTypeGlAccountsBy;

@RestController
@RequestMapping("/api/invoiceItemTypeGlAccount")
public class InvoiceItemTypeGlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceItemTypeGlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceItemTypeGlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceItemTypeGlAccount
	 * @return a List with the InvoiceItemTypeGlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceItemTypeGlAccount> findInvoiceItemTypeGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceItemTypeGlAccountsBy query = new FindInvoiceItemTypeGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceItemTypeGlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeGlAccountFound.class,
				event -> sendInvoiceItemTypeGlAccountsFoundMessage(((InvoiceItemTypeGlAccountFound) event).getInvoiceItemTypeGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceItemTypeGlAccountsFoundMessage(List<InvoiceItemTypeGlAccount> invoiceItemTypeGlAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceItemTypeGlAccounts);
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
	public boolean createInvoiceItemTypeGlAccount(HttpServletRequest request) {

		InvoiceItemTypeGlAccount invoiceItemTypeGlAccountToBeAdded = new InvoiceItemTypeGlAccount();
		try {
			invoiceItemTypeGlAccountToBeAdded = InvoiceItemTypeGlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceItemTypeGlAccount(invoiceItemTypeGlAccountToBeAdded);

	}

	/**
	 * creates a new InvoiceItemTypeGlAccount entry in the ofbiz database
	 * 
	 * @param invoiceItemTypeGlAccountToBeAdded
	 *            the InvoiceItemTypeGlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceItemTypeGlAccount(InvoiceItemTypeGlAccount invoiceItemTypeGlAccountToBeAdded) {

		AddInvoiceItemTypeGlAccount com = new AddInvoiceItemTypeGlAccount(invoiceItemTypeGlAccountToBeAdded);
		int usedTicketId;

		synchronized (InvoiceItemTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeGlAccountAdded.class,
				event -> sendInvoiceItemTypeGlAccountChangedMessage(((InvoiceItemTypeGlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceItemTypeGlAccount(HttpServletRequest request) {

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

		InvoiceItemTypeGlAccount invoiceItemTypeGlAccountToBeUpdated = new InvoiceItemTypeGlAccount();

		try {
			invoiceItemTypeGlAccountToBeUpdated = InvoiceItemTypeGlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceItemTypeGlAccount(invoiceItemTypeGlAccountToBeUpdated);

	}

	/**
	 * Updates the InvoiceItemTypeGlAccount with the specific Id
	 * 
	 * @param invoiceItemTypeGlAccountToBeUpdated the InvoiceItemTypeGlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceItemTypeGlAccount(InvoiceItemTypeGlAccount invoiceItemTypeGlAccountToBeUpdated) {

		UpdateInvoiceItemTypeGlAccount com = new UpdateInvoiceItemTypeGlAccount(invoiceItemTypeGlAccountToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceItemTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeGlAccountUpdated.class,
				event -> sendInvoiceItemTypeGlAccountChangedMessage(((InvoiceItemTypeGlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceItemTypeGlAccount from the database
	 * 
	 * @param invoiceItemTypeGlAccountId:
	 *            the id of the InvoiceItemTypeGlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceItemTypeGlAccountById(@RequestParam(value = "invoiceItemTypeGlAccountId") String invoiceItemTypeGlAccountId) {

		DeleteInvoiceItemTypeGlAccount com = new DeleteInvoiceItemTypeGlAccount(invoiceItemTypeGlAccountId);

		int usedTicketId;

		synchronized (InvoiceItemTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeGlAccountDeleted.class,
				event -> sendInvoiceItemTypeGlAccountChangedMessage(((InvoiceItemTypeGlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceItemTypeGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceItemTypeGlAccount/\" plus one of the following: "
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
