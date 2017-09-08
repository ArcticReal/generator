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
import com.skytala.eCommerce.command.AddPaymentApplication;
import com.skytala.eCommerce.command.DeletePaymentApplication;
import com.skytala.eCommerce.command.UpdatePaymentApplication;
import com.skytala.eCommerce.entity.PaymentApplication;
import com.skytala.eCommerce.entity.PaymentApplicationMapper;
import com.skytala.eCommerce.event.PaymentApplicationAdded;
import com.skytala.eCommerce.event.PaymentApplicationDeleted;
import com.skytala.eCommerce.event.PaymentApplicationFound;
import com.skytala.eCommerce.event.PaymentApplicationUpdated;
import com.skytala.eCommerce.query.FindPaymentApplicationsBy;

@RestController
@RequestMapping("/api/paymentApplication")
public class PaymentApplicationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentApplication>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentApplicationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentApplication
	 * @return a List with the PaymentApplications
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentApplication> findPaymentApplicationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentApplicationsBy query = new FindPaymentApplicationsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentApplicationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentApplicationFound.class,
				event -> sendPaymentApplicationsFoundMessage(((PaymentApplicationFound) event).getPaymentApplications(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentApplicationsFoundMessage(List<PaymentApplication> paymentApplications, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentApplications);
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
	public boolean createPaymentApplication(HttpServletRequest request) {

		PaymentApplication paymentApplicationToBeAdded = new PaymentApplication();
		try {
			paymentApplicationToBeAdded = PaymentApplicationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentApplication(paymentApplicationToBeAdded);

	}

	/**
	 * creates a new PaymentApplication entry in the ofbiz database
	 * 
	 * @param paymentApplicationToBeAdded
	 *            the PaymentApplication thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentApplication(PaymentApplication paymentApplicationToBeAdded) {

		AddPaymentApplication com = new AddPaymentApplication(paymentApplicationToBeAdded);
		int usedTicketId;

		synchronized (PaymentApplicationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentApplicationAdded.class,
				event -> sendPaymentApplicationChangedMessage(((PaymentApplicationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentApplication(HttpServletRequest request) {

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

		PaymentApplication paymentApplicationToBeUpdated = new PaymentApplication();

		try {
			paymentApplicationToBeUpdated = PaymentApplicationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentApplication(paymentApplicationToBeUpdated);

	}

	/**
	 * Updates the PaymentApplication with the specific Id
	 * 
	 * @param paymentApplicationToBeUpdated the PaymentApplication thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentApplication(PaymentApplication paymentApplicationToBeUpdated) {

		UpdatePaymentApplication com = new UpdatePaymentApplication(paymentApplicationToBeUpdated);

		int usedTicketId;

		synchronized (PaymentApplicationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentApplicationUpdated.class,
				event -> sendPaymentApplicationChangedMessage(((PaymentApplicationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentApplication from the database
	 * 
	 * @param paymentApplicationId:
	 *            the id of the PaymentApplication thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentApplicationById(@RequestParam(value = "paymentApplicationId") String paymentApplicationId) {

		DeletePaymentApplication com = new DeletePaymentApplication(paymentApplicationId);

		int usedTicketId;

		synchronized (PaymentApplicationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentApplicationDeleted.class,
				event -> sendPaymentApplicationChangedMessage(((PaymentApplicationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentApplicationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentApplication/\" plus one of the following: "
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
