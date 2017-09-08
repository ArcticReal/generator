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
import com.skytala.eCommerce.command.AddPaymentMethod;
import com.skytala.eCommerce.command.DeletePaymentMethod;
import com.skytala.eCommerce.command.UpdatePaymentMethod;
import com.skytala.eCommerce.entity.PaymentMethod;
import com.skytala.eCommerce.entity.PaymentMethodMapper;
import com.skytala.eCommerce.event.PaymentMethodAdded;
import com.skytala.eCommerce.event.PaymentMethodDeleted;
import com.skytala.eCommerce.event.PaymentMethodFound;
import com.skytala.eCommerce.event.PaymentMethodUpdated;
import com.skytala.eCommerce.query.FindPaymentMethodsBy;

@RestController
@RequestMapping("/api/paymentMethod")
public class PaymentMethodController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentMethod>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentMethodController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentMethod
	 * @return a List with the PaymentMethods
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentMethod> findPaymentMethodsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentMethodsBy query = new FindPaymentMethodsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentMethodController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodFound.class,
				event -> sendPaymentMethodsFoundMessage(((PaymentMethodFound) event).getPaymentMethods(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentMethodsFoundMessage(List<PaymentMethod> paymentMethods, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentMethods);
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
	public boolean createPaymentMethod(HttpServletRequest request) {

		PaymentMethod paymentMethodToBeAdded = new PaymentMethod();
		try {
			paymentMethodToBeAdded = PaymentMethodMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentMethod(paymentMethodToBeAdded);

	}

	/**
	 * creates a new PaymentMethod entry in the ofbiz database
	 * 
	 * @param paymentMethodToBeAdded
	 *            the PaymentMethod thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentMethod(PaymentMethod paymentMethodToBeAdded) {

		AddPaymentMethod com = new AddPaymentMethod(paymentMethodToBeAdded);
		int usedTicketId;

		synchronized (PaymentMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodAdded.class,
				event -> sendPaymentMethodChangedMessage(((PaymentMethodAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentMethod(HttpServletRequest request) {

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

		PaymentMethod paymentMethodToBeUpdated = new PaymentMethod();

		try {
			paymentMethodToBeUpdated = PaymentMethodMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentMethod(paymentMethodToBeUpdated);

	}

	/**
	 * Updates the PaymentMethod with the specific Id
	 * 
	 * @param paymentMethodToBeUpdated the PaymentMethod thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentMethod(PaymentMethod paymentMethodToBeUpdated) {

		UpdatePaymentMethod com = new UpdatePaymentMethod(paymentMethodToBeUpdated);

		int usedTicketId;

		synchronized (PaymentMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodUpdated.class,
				event -> sendPaymentMethodChangedMessage(((PaymentMethodUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentMethod from the database
	 * 
	 * @param paymentMethodId:
	 *            the id of the PaymentMethod thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentMethodById(@RequestParam(value = "paymentMethodId") String paymentMethodId) {

		DeletePaymentMethod com = new DeletePaymentMethod(paymentMethodId);

		int usedTicketId;

		synchronized (PaymentMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodDeleted.class,
				event -> sendPaymentMethodChangedMessage(((PaymentMethodDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentMethodChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentMethod/\" plus one of the following: "
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
