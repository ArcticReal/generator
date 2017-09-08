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
import com.skytala.eCommerce.command.AddPaymentGatewayWorldPay;
import com.skytala.eCommerce.command.DeletePaymentGatewayWorldPay;
import com.skytala.eCommerce.command.UpdatePaymentGatewayWorldPay;
import com.skytala.eCommerce.entity.PaymentGatewayWorldPay;
import com.skytala.eCommerce.entity.PaymentGatewayWorldPayMapper;
import com.skytala.eCommerce.event.PaymentGatewayWorldPayAdded;
import com.skytala.eCommerce.event.PaymentGatewayWorldPayDeleted;
import com.skytala.eCommerce.event.PaymentGatewayWorldPayFound;
import com.skytala.eCommerce.event.PaymentGatewayWorldPayUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayWorldPaysBy;

@RestController
@RequestMapping("/api/paymentGatewayWorldPay")
public class PaymentGatewayWorldPayController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayWorldPay>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayWorldPayController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayWorldPay
	 * @return a List with the PaymentGatewayWorldPays
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayWorldPay> findPaymentGatewayWorldPaysBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayWorldPaysBy query = new FindPaymentGatewayWorldPaysBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayWorldPayController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayWorldPayFound.class,
				event -> sendPaymentGatewayWorldPaysFoundMessage(((PaymentGatewayWorldPayFound) event).getPaymentGatewayWorldPays(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayWorldPaysFoundMessage(List<PaymentGatewayWorldPay> paymentGatewayWorldPays, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayWorldPays);
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
	public boolean createPaymentGatewayWorldPay(HttpServletRequest request) {

		PaymentGatewayWorldPay paymentGatewayWorldPayToBeAdded = new PaymentGatewayWorldPay();
		try {
			paymentGatewayWorldPayToBeAdded = PaymentGatewayWorldPayMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayWorldPay(paymentGatewayWorldPayToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayWorldPay entry in the ofbiz database
	 * 
	 * @param paymentGatewayWorldPayToBeAdded
	 *            the PaymentGatewayWorldPay thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayWorldPay(PaymentGatewayWorldPay paymentGatewayWorldPayToBeAdded) {

		AddPaymentGatewayWorldPay com = new AddPaymentGatewayWorldPay(paymentGatewayWorldPayToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayWorldPayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayWorldPayAdded.class,
				event -> sendPaymentGatewayWorldPayChangedMessage(((PaymentGatewayWorldPayAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayWorldPay(HttpServletRequest request) {

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

		PaymentGatewayWorldPay paymentGatewayWorldPayToBeUpdated = new PaymentGatewayWorldPay();

		try {
			paymentGatewayWorldPayToBeUpdated = PaymentGatewayWorldPayMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayWorldPay(paymentGatewayWorldPayToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayWorldPay with the specific Id
	 * 
	 * @param paymentGatewayWorldPayToBeUpdated the PaymentGatewayWorldPay thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayWorldPay(PaymentGatewayWorldPay paymentGatewayWorldPayToBeUpdated) {

		UpdatePaymentGatewayWorldPay com = new UpdatePaymentGatewayWorldPay(paymentGatewayWorldPayToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayWorldPayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayWorldPayUpdated.class,
				event -> sendPaymentGatewayWorldPayChangedMessage(((PaymentGatewayWorldPayUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayWorldPay from the database
	 * 
	 * @param paymentGatewayWorldPayId:
	 *            the id of the PaymentGatewayWorldPay thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayWorldPayById(@RequestParam(value = "paymentGatewayWorldPayId") String paymentGatewayWorldPayId) {

		DeletePaymentGatewayWorldPay com = new DeletePaymentGatewayWorldPay(paymentGatewayWorldPayId);

		int usedTicketId;

		synchronized (PaymentGatewayWorldPayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayWorldPayDeleted.class,
				event -> sendPaymentGatewayWorldPayChangedMessage(((PaymentGatewayWorldPayDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayWorldPayChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayWorldPay/\" plus one of the following: "
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
