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
import com.skytala.eCommerce.command.AddPayPalPaymentMethod;
import com.skytala.eCommerce.command.DeletePayPalPaymentMethod;
import com.skytala.eCommerce.command.UpdatePayPalPaymentMethod;
import com.skytala.eCommerce.entity.PayPalPaymentMethod;
import com.skytala.eCommerce.entity.PayPalPaymentMethodMapper;
import com.skytala.eCommerce.event.PayPalPaymentMethodAdded;
import com.skytala.eCommerce.event.PayPalPaymentMethodDeleted;
import com.skytala.eCommerce.event.PayPalPaymentMethodFound;
import com.skytala.eCommerce.event.PayPalPaymentMethodUpdated;
import com.skytala.eCommerce.query.FindPayPalPaymentMethodsBy;

@RestController
@RequestMapping("/api/payPalPaymentMethod")
public class PayPalPaymentMethodController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PayPalPaymentMethod>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PayPalPaymentMethodController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PayPalPaymentMethod
	 * @return a List with the PayPalPaymentMethods
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PayPalPaymentMethod> findPayPalPaymentMethodsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPayPalPaymentMethodsBy query = new FindPayPalPaymentMethodsBy(allRequestParams);

		int usedTicketId;

		synchronized (PayPalPaymentMethodController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayPalPaymentMethodFound.class,
				event -> sendPayPalPaymentMethodsFoundMessage(((PayPalPaymentMethodFound) event).getPayPalPaymentMethods(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPayPalPaymentMethodsFoundMessage(List<PayPalPaymentMethod> payPalPaymentMethods, int usedTicketId) {
		queryReturnVal.put(usedTicketId, payPalPaymentMethods);
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
	public boolean createPayPalPaymentMethod(HttpServletRequest request) {

		PayPalPaymentMethod payPalPaymentMethodToBeAdded = new PayPalPaymentMethod();
		try {
			payPalPaymentMethodToBeAdded = PayPalPaymentMethodMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPayPalPaymentMethod(payPalPaymentMethodToBeAdded);

	}

	/**
	 * creates a new PayPalPaymentMethod entry in the ofbiz database
	 * 
	 * @param payPalPaymentMethodToBeAdded
	 *            the PayPalPaymentMethod thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPayPalPaymentMethod(PayPalPaymentMethod payPalPaymentMethodToBeAdded) {

		AddPayPalPaymentMethod com = new AddPayPalPaymentMethod(payPalPaymentMethodToBeAdded);
		int usedTicketId;

		synchronized (PayPalPaymentMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayPalPaymentMethodAdded.class,
				event -> sendPayPalPaymentMethodChangedMessage(((PayPalPaymentMethodAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePayPalPaymentMethod(HttpServletRequest request) {

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

		PayPalPaymentMethod payPalPaymentMethodToBeUpdated = new PayPalPaymentMethod();

		try {
			payPalPaymentMethodToBeUpdated = PayPalPaymentMethodMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePayPalPaymentMethod(payPalPaymentMethodToBeUpdated);

	}

	/**
	 * Updates the PayPalPaymentMethod with the specific Id
	 * 
	 * @param payPalPaymentMethodToBeUpdated the PayPalPaymentMethod thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePayPalPaymentMethod(PayPalPaymentMethod payPalPaymentMethodToBeUpdated) {

		UpdatePayPalPaymentMethod com = new UpdatePayPalPaymentMethod(payPalPaymentMethodToBeUpdated);

		int usedTicketId;

		synchronized (PayPalPaymentMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayPalPaymentMethodUpdated.class,
				event -> sendPayPalPaymentMethodChangedMessage(((PayPalPaymentMethodUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PayPalPaymentMethod from the database
	 * 
	 * @param payPalPaymentMethodId:
	 *            the id of the PayPalPaymentMethod thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepayPalPaymentMethodById(@RequestParam(value = "payPalPaymentMethodId") String payPalPaymentMethodId) {

		DeletePayPalPaymentMethod com = new DeletePayPalPaymentMethod(payPalPaymentMethodId);

		int usedTicketId;

		synchronized (PayPalPaymentMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayPalPaymentMethodDeleted.class,
				event -> sendPayPalPaymentMethodChangedMessage(((PayPalPaymentMethodDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPayPalPaymentMethodChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/payPalPaymentMethod/\" plus one of the following: "
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
