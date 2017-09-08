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
import com.skytala.eCommerce.command.AddPaymentGatewaySagePay;
import com.skytala.eCommerce.command.DeletePaymentGatewaySagePay;
import com.skytala.eCommerce.command.UpdatePaymentGatewaySagePay;
import com.skytala.eCommerce.entity.PaymentGatewaySagePay;
import com.skytala.eCommerce.entity.PaymentGatewaySagePayMapper;
import com.skytala.eCommerce.event.PaymentGatewaySagePayAdded;
import com.skytala.eCommerce.event.PaymentGatewaySagePayDeleted;
import com.skytala.eCommerce.event.PaymentGatewaySagePayFound;
import com.skytala.eCommerce.event.PaymentGatewaySagePayUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewaySagePaysBy;

@RestController
@RequestMapping("/api/paymentGatewaySagePay")
public class PaymentGatewaySagePayController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewaySagePay>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewaySagePayController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewaySagePay
	 * @return a List with the PaymentGatewaySagePays
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewaySagePay> findPaymentGatewaySagePaysBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewaySagePaysBy query = new FindPaymentGatewaySagePaysBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewaySagePayController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewaySagePayFound.class,
				event -> sendPaymentGatewaySagePaysFoundMessage(((PaymentGatewaySagePayFound) event).getPaymentGatewaySagePays(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewaySagePaysFoundMessage(List<PaymentGatewaySagePay> paymentGatewaySagePays, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewaySagePays);
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
	public boolean createPaymentGatewaySagePay(HttpServletRequest request) {

		PaymentGatewaySagePay paymentGatewaySagePayToBeAdded = new PaymentGatewaySagePay();
		try {
			paymentGatewaySagePayToBeAdded = PaymentGatewaySagePayMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewaySagePay(paymentGatewaySagePayToBeAdded);

	}

	/**
	 * creates a new PaymentGatewaySagePay entry in the ofbiz database
	 * 
	 * @param paymentGatewaySagePayToBeAdded
	 *            the PaymentGatewaySagePay thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewaySagePay(PaymentGatewaySagePay paymentGatewaySagePayToBeAdded) {

		AddPaymentGatewaySagePay com = new AddPaymentGatewaySagePay(paymentGatewaySagePayToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewaySagePayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewaySagePayAdded.class,
				event -> sendPaymentGatewaySagePayChangedMessage(((PaymentGatewaySagePayAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewaySagePay(HttpServletRequest request) {

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

		PaymentGatewaySagePay paymentGatewaySagePayToBeUpdated = new PaymentGatewaySagePay();

		try {
			paymentGatewaySagePayToBeUpdated = PaymentGatewaySagePayMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewaySagePay(paymentGatewaySagePayToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewaySagePay with the specific Id
	 * 
	 * @param paymentGatewaySagePayToBeUpdated the PaymentGatewaySagePay thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewaySagePay(PaymentGatewaySagePay paymentGatewaySagePayToBeUpdated) {

		UpdatePaymentGatewaySagePay com = new UpdatePaymentGatewaySagePay(paymentGatewaySagePayToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewaySagePayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewaySagePayUpdated.class,
				event -> sendPaymentGatewaySagePayChangedMessage(((PaymentGatewaySagePayUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewaySagePay from the database
	 * 
	 * @param paymentGatewaySagePayId:
	 *            the id of the PaymentGatewaySagePay thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewaySagePayById(@RequestParam(value = "paymentGatewaySagePayId") String paymentGatewaySagePayId) {

		DeletePaymentGatewaySagePay com = new DeletePaymentGatewaySagePay(paymentGatewaySagePayId);

		int usedTicketId;

		synchronized (PaymentGatewaySagePayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewaySagePayDeleted.class,
				event -> sendPaymentGatewaySagePayChangedMessage(((PaymentGatewaySagePayDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewaySagePayChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewaySagePay/\" plus one of the following: "
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
