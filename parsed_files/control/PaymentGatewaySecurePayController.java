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
import com.skytala.eCommerce.command.AddPaymentGatewaySecurePay;
import com.skytala.eCommerce.command.DeletePaymentGatewaySecurePay;
import com.skytala.eCommerce.command.UpdatePaymentGatewaySecurePay;
import com.skytala.eCommerce.entity.PaymentGatewaySecurePay;
import com.skytala.eCommerce.entity.PaymentGatewaySecurePayMapper;
import com.skytala.eCommerce.event.PaymentGatewaySecurePayAdded;
import com.skytala.eCommerce.event.PaymentGatewaySecurePayDeleted;
import com.skytala.eCommerce.event.PaymentGatewaySecurePayFound;
import com.skytala.eCommerce.event.PaymentGatewaySecurePayUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewaySecurePaysBy;

@RestController
@RequestMapping("/api/paymentGatewaySecurePay")
public class PaymentGatewaySecurePayController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewaySecurePay>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewaySecurePayController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewaySecurePay
	 * @return a List with the PaymentGatewaySecurePays
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewaySecurePay> findPaymentGatewaySecurePaysBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewaySecurePaysBy query = new FindPaymentGatewaySecurePaysBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewaySecurePayController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewaySecurePayFound.class,
				event -> sendPaymentGatewaySecurePaysFoundMessage(((PaymentGatewaySecurePayFound) event).getPaymentGatewaySecurePays(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewaySecurePaysFoundMessage(List<PaymentGatewaySecurePay> paymentGatewaySecurePays, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewaySecurePays);
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
	public boolean createPaymentGatewaySecurePay(HttpServletRequest request) {

		PaymentGatewaySecurePay paymentGatewaySecurePayToBeAdded = new PaymentGatewaySecurePay();
		try {
			paymentGatewaySecurePayToBeAdded = PaymentGatewaySecurePayMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewaySecurePay(paymentGatewaySecurePayToBeAdded);

	}

	/**
	 * creates a new PaymentGatewaySecurePay entry in the ofbiz database
	 * 
	 * @param paymentGatewaySecurePayToBeAdded
	 *            the PaymentGatewaySecurePay thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewaySecurePay(PaymentGatewaySecurePay paymentGatewaySecurePayToBeAdded) {

		AddPaymentGatewaySecurePay com = new AddPaymentGatewaySecurePay(paymentGatewaySecurePayToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewaySecurePayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewaySecurePayAdded.class,
				event -> sendPaymentGatewaySecurePayChangedMessage(((PaymentGatewaySecurePayAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewaySecurePay(HttpServletRequest request) {

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

		PaymentGatewaySecurePay paymentGatewaySecurePayToBeUpdated = new PaymentGatewaySecurePay();

		try {
			paymentGatewaySecurePayToBeUpdated = PaymentGatewaySecurePayMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewaySecurePay(paymentGatewaySecurePayToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewaySecurePay with the specific Id
	 * 
	 * @param paymentGatewaySecurePayToBeUpdated the PaymentGatewaySecurePay thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewaySecurePay(PaymentGatewaySecurePay paymentGatewaySecurePayToBeUpdated) {

		UpdatePaymentGatewaySecurePay com = new UpdatePaymentGatewaySecurePay(paymentGatewaySecurePayToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewaySecurePayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewaySecurePayUpdated.class,
				event -> sendPaymentGatewaySecurePayChangedMessage(((PaymentGatewaySecurePayUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewaySecurePay from the database
	 * 
	 * @param paymentGatewaySecurePayId:
	 *            the id of the PaymentGatewaySecurePay thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewaySecurePayById(@RequestParam(value = "paymentGatewaySecurePayId") String paymentGatewaySecurePayId) {

		DeletePaymentGatewaySecurePay com = new DeletePaymentGatewaySecurePay(paymentGatewaySecurePayId);

		int usedTicketId;

		synchronized (PaymentGatewaySecurePayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewaySecurePayDeleted.class,
				event -> sendPaymentGatewaySecurePayChangedMessage(((PaymentGatewaySecurePayDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewaySecurePayChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewaySecurePay/\" plus one of the following: "
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
