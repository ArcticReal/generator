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
import com.skytala.eCommerce.command.AddPaymentMethodTypeGlAccount;
import com.skytala.eCommerce.command.DeletePaymentMethodTypeGlAccount;
import com.skytala.eCommerce.command.UpdatePaymentMethodTypeGlAccount;
import com.skytala.eCommerce.entity.PaymentMethodTypeGlAccount;
import com.skytala.eCommerce.entity.PaymentMethodTypeGlAccountMapper;
import com.skytala.eCommerce.event.PaymentMethodTypeGlAccountAdded;
import com.skytala.eCommerce.event.PaymentMethodTypeGlAccountDeleted;
import com.skytala.eCommerce.event.PaymentMethodTypeGlAccountFound;
import com.skytala.eCommerce.event.PaymentMethodTypeGlAccountUpdated;
import com.skytala.eCommerce.query.FindPaymentMethodTypeGlAccountsBy;

@RestController
@RequestMapping("/api/paymentMethodTypeGlAccount")
public class PaymentMethodTypeGlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentMethodTypeGlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentMethodTypeGlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentMethodTypeGlAccount
	 * @return a List with the PaymentMethodTypeGlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentMethodTypeGlAccount> findPaymentMethodTypeGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentMethodTypeGlAccountsBy query = new FindPaymentMethodTypeGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentMethodTypeGlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodTypeGlAccountFound.class,
				event -> sendPaymentMethodTypeGlAccountsFoundMessage(((PaymentMethodTypeGlAccountFound) event).getPaymentMethodTypeGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentMethodTypeGlAccountsFoundMessage(List<PaymentMethodTypeGlAccount> paymentMethodTypeGlAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentMethodTypeGlAccounts);
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
	public boolean createPaymentMethodTypeGlAccount(HttpServletRequest request) {

		PaymentMethodTypeGlAccount paymentMethodTypeGlAccountToBeAdded = new PaymentMethodTypeGlAccount();
		try {
			paymentMethodTypeGlAccountToBeAdded = PaymentMethodTypeGlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentMethodTypeGlAccount(paymentMethodTypeGlAccountToBeAdded);

	}

	/**
	 * creates a new PaymentMethodTypeGlAccount entry in the ofbiz database
	 * 
	 * @param paymentMethodTypeGlAccountToBeAdded
	 *            the PaymentMethodTypeGlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentMethodTypeGlAccount(PaymentMethodTypeGlAccount paymentMethodTypeGlAccountToBeAdded) {

		AddPaymentMethodTypeGlAccount com = new AddPaymentMethodTypeGlAccount(paymentMethodTypeGlAccountToBeAdded);
		int usedTicketId;

		synchronized (PaymentMethodTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodTypeGlAccountAdded.class,
				event -> sendPaymentMethodTypeGlAccountChangedMessage(((PaymentMethodTypeGlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentMethodTypeGlAccount(HttpServletRequest request) {

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

		PaymentMethodTypeGlAccount paymentMethodTypeGlAccountToBeUpdated = new PaymentMethodTypeGlAccount();

		try {
			paymentMethodTypeGlAccountToBeUpdated = PaymentMethodTypeGlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentMethodTypeGlAccount(paymentMethodTypeGlAccountToBeUpdated);

	}

	/**
	 * Updates the PaymentMethodTypeGlAccount with the specific Id
	 * 
	 * @param paymentMethodTypeGlAccountToBeUpdated the PaymentMethodTypeGlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentMethodTypeGlAccount(PaymentMethodTypeGlAccount paymentMethodTypeGlAccountToBeUpdated) {

		UpdatePaymentMethodTypeGlAccount com = new UpdatePaymentMethodTypeGlAccount(paymentMethodTypeGlAccountToBeUpdated);

		int usedTicketId;

		synchronized (PaymentMethodTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodTypeGlAccountUpdated.class,
				event -> sendPaymentMethodTypeGlAccountChangedMessage(((PaymentMethodTypeGlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentMethodTypeGlAccount from the database
	 * 
	 * @param paymentMethodTypeGlAccountId:
	 *            the id of the PaymentMethodTypeGlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentMethodTypeGlAccountById(@RequestParam(value = "paymentMethodTypeGlAccountId") String paymentMethodTypeGlAccountId) {

		DeletePaymentMethodTypeGlAccount com = new DeletePaymentMethodTypeGlAccount(paymentMethodTypeGlAccountId);

		int usedTicketId;

		synchronized (PaymentMethodTypeGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodTypeGlAccountDeleted.class,
				event -> sendPaymentMethodTypeGlAccountChangedMessage(((PaymentMethodTypeGlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentMethodTypeGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentMethodTypeGlAccount/\" plus one of the following: "
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
