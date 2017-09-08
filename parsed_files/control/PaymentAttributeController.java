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
import com.skytala.eCommerce.command.AddPaymentAttribute;
import com.skytala.eCommerce.command.DeletePaymentAttribute;
import com.skytala.eCommerce.command.UpdatePaymentAttribute;
import com.skytala.eCommerce.entity.PaymentAttribute;
import com.skytala.eCommerce.entity.PaymentAttributeMapper;
import com.skytala.eCommerce.event.PaymentAttributeAdded;
import com.skytala.eCommerce.event.PaymentAttributeDeleted;
import com.skytala.eCommerce.event.PaymentAttributeFound;
import com.skytala.eCommerce.event.PaymentAttributeUpdated;
import com.skytala.eCommerce.query.FindPaymentAttributesBy;

@RestController
@RequestMapping("/api/paymentAttribute")
public class PaymentAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentAttribute
	 * @return a List with the PaymentAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentAttribute> findPaymentAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentAttributesBy query = new FindPaymentAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentAttributeFound.class,
				event -> sendPaymentAttributesFoundMessage(((PaymentAttributeFound) event).getPaymentAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentAttributesFoundMessage(List<PaymentAttribute> paymentAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentAttributes);
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
	public boolean createPaymentAttribute(HttpServletRequest request) {

		PaymentAttribute paymentAttributeToBeAdded = new PaymentAttribute();
		try {
			paymentAttributeToBeAdded = PaymentAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentAttribute(paymentAttributeToBeAdded);

	}

	/**
	 * creates a new PaymentAttribute entry in the ofbiz database
	 * 
	 * @param paymentAttributeToBeAdded
	 *            the PaymentAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentAttribute(PaymentAttribute paymentAttributeToBeAdded) {

		AddPaymentAttribute com = new AddPaymentAttribute(paymentAttributeToBeAdded);
		int usedTicketId;

		synchronized (PaymentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentAttributeAdded.class,
				event -> sendPaymentAttributeChangedMessage(((PaymentAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentAttribute(HttpServletRequest request) {

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

		PaymentAttribute paymentAttributeToBeUpdated = new PaymentAttribute();

		try {
			paymentAttributeToBeUpdated = PaymentAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentAttribute(paymentAttributeToBeUpdated);

	}

	/**
	 * Updates the PaymentAttribute with the specific Id
	 * 
	 * @param paymentAttributeToBeUpdated the PaymentAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentAttribute(PaymentAttribute paymentAttributeToBeUpdated) {

		UpdatePaymentAttribute com = new UpdatePaymentAttribute(paymentAttributeToBeUpdated);

		int usedTicketId;

		synchronized (PaymentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentAttributeUpdated.class,
				event -> sendPaymentAttributeChangedMessage(((PaymentAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentAttribute from the database
	 * 
	 * @param paymentAttributeId:
	 *            the id of the PaymentAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentAttributeById(@RequestParam(value = "paymentAttributeId") String paymentAttributeId) {

		DeletePaymentAttribute com = new DeletePaymentAttribute(paymentAttributeId);

		int usedTicketId;

		synchronized (PaymentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentAttributeDeleted.class,
				event -> sendPaymentAttributeChangedMessage(((PaymentAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentAttribute/\" plus one of the following: "
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
