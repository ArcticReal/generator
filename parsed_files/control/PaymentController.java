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
import com.skytala.eCommerce.command.AddPayment;
import com.skytala.eCommerce.command.DeletePayment;
import com.skytala.eCommerce.command.UpdatePayment;
import com.skytala.eCommerce.entity.Payment;
import com.skytala.eCommerce.entity.PaymentMapper;
import com.skytala.eCommerce.event.PaymentAdded;
import com.skytala.eCommerce.event.PaymentDeleted;
import com.skytala.eCommerce.event.PaymentFound;
import com.skytala.eCommerce.event.PaymentUpdated;
import com.skytala.eCommerce.query.FindPaymentsBy;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Payment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Payment
	 * @return a List with the Payments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Payment> findPaymentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentsBy query = new FindPaymentsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentFound.class,
				event -> sendPaymentsFoundMessage(((PaymentFound) event).getPayments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentsFoundMessage(List<Payment> payments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, payments);
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
	public boolean createPayment(HttpServletRequest request) {

		Payment paymentToBeAdded = new Payment();
		try {
			paymentToBeAdded = PaymentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPayment(paymentToBeAdded);

	}

	/**
	 * creates a new Payment entry in the ofbiz database
	 * 
	 * @param paymentToBeAdded
	 *            the Payment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPayment(Payment paymentToBeAdded) {

		AddPayment com = new AddPayment(paymentToBeAdded);
		int usedTicketId;

		synchronized (PaymentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentAdded.class,
				event -> sendPaymentChangedMessage(((PaymentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePayment(HttpServletRequest request) {

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

		Payment paymentToBeUpdated = new Payment();

		try {
			paymentToBeUpdated = PaymentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePayment(paymentToBeUpdated);

	}

	/**
	 * Updates the Payment with the specific Id
	 * 
	 * @param paymentToBeUpdated the Payment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePayment(Payment paymentToBeUpdated) {

		UpdatePayment com = new UpdatePayment(paymentToBeUpdated);

		int usedTicketId;

		synchronized (PaymentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentUpdated.class,
				event -> sendPaymentChangedMessage(((PaymentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Payment from the database
	 * 
	 * @param paymentId:
	 *            the id of the Payment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentById(@RequestParam(value = "paymentId") String paymentId) {

		DeletePayment com = new DeletePayment(paymentId);

		int usedTicketId;

		synchronized (PaymentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentDeleted.class,
				event -> sendPaymentChangedMessage(((PaymentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/payment/\" plus one of the following: "
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
