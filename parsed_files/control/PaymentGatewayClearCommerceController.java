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
import com.skytala.eCommerce.command.AddPaymentGatewayClearCommerce;
import com.skytala.eCommerce.command.DeletePaymentGatewayClearCommerce;
import com.skytala.eCommerce.command.UpdatePaymentGatewayClearCommerce;
import com.skytala.eCommerce.entity.PaymentGatewayClearCommerce;
import com.skytala.eCommerce.entity.PaymentGatewayClearCommerceMapper;
import com.skytala.eCommerce.event.PaymentGatewayClearCommerceAdded;
import com.skytala.eCommerce.event.PaymentGatewayClearCommerceDeleted;
import com.skytala.eCommerce.event.PaymentGatewayClearCommerceFound;
import com.skytala.eCommerce.event.PaymentGatewayClearCommerceUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayClearCommercesBy;

@RestController
@RequestMapping("/api/paymentGatewayClearCommerce")
public class PaymentGatewayClearCommerceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayClearCommerce>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayClearCommerceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayClearCommerce
	 * @return a List with the PaymentGatewayClearCommerces
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayClearCommerce> findPaymentGatewayClearCommercesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayClearCommercesBy query = new FindPaymentGatewayClearCommercesBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayClearCommerceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayClearCommerceFound.class,
				event -> sendPaymentGatewayClearCommercesFoundMessage(((PaymentGatewayClearCommerceFound) event).getPaymentGatewayClearCommerces(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayClearCommercesFoundMessage(List<PaymentGatewayClearCommerce> paymentGatewayClearCommerces, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayClearCommerces);
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
	public boolean createPaymentGatewayClearCommerce(HttpServletRequest request) {

		PaymentGatewayClearCommerce paymentGatewayClearCommerceToBeAdded = new PaymentGatewayClearCommerce();
		try {
			paymentGatewayClearCommerceToBeAdded = PaymentGatewayClearCommerceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayClearCommerce(paymentGatewayClearCommerceToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayClearCommerce entry in the ofbiz database
	 * 
	 * @param paymentGatewayClearCommerceToBeAdded
	 *            the PaymentGatewayClearCommerce thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayClearCommerce(PaymentGatewayClearCommerce paymentGatewayClearCommerceToBeAdded) {

		AddPaymentGatewayClearCommerce com = new AddPaymentGatewayClearCommerce(paymentGatewayClearCommerceToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayClearCommerceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayClearCommerceAdded.class,
				event -> sendPaymentGatewayClearCommerceChangedMessage(((PaymentGatewayClearCommerceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayClearCommerce(HttpServletRequest request) {

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

		PaymentGatewayClearCommerce paymentGatewayClearCommerceToBeUpdated = new PaymentGatewayClearCommerce();

		try {
			paymentGatewayClearCommerceToBeUpdated = PaymentGatewayClearCommerceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayClearCommerce(paymentGatewayClearCommerceToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayClearCommerce with the specific Id
	 * 
	 * @param paymentGatewayClearCommerceToBeUpdated the PaymentGatewayClearCommerce thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayClearCommerce(PaymentGatewayClearCommerce paymentGatewayClearCommerceToBeUpdated) {

		UpdatePaymentGatewayClearCommerce com = new UpdatePaymentGatewayClearCommerce(paymentGatewayClearCommerceToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayClearCommerceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayClearCommerceUpdated.class,
				event -> sendPaymentGatewayClearCommerceChangedMessage(((PaymentGatewayClearCommerceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayClearCommerce from the database
	 * 
	 * @param paymentGatewayClearCommerceId:
	 *            the id of the PaymentGatewayClearCommerce thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayClearCommerceById(@RequestParam(value = "paymentGatewayClearCommerceId") String paymentGatewayClearCommerceId) {

		DeletePaymentGatewayClearCommerce com = new DeletePaymentGatewayClearCommerce(paymentGatewayClearCommerceId);

		int usedTicketId;

		synchronized (PaymentGatewayClearCommerceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayClearCommerceDeleted.class,
				event -> sendPaymentGatewayClearCommerceChangedMessage(((PaymentGatewayClearCommerceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayClearCommerceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayClearCommerce/\" plus one of the following: "
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
