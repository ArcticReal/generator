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
import com.skytala.eCommerce.command.AddPaymentMethodType;
import com.skytala.eCommerce.command.DeletePaymentMethodType;
import com.skytala.eCommerce.command.UpdatePaymentMethodType;
import com.skytala.eCommerce.entity.PaymentMethodType;
import com.skytala.eCommerce.entity.PaymentMethodTypeMapper;
import com.skytala.eCommerce.event.PaymentMethodTypeAdded;
import com.skytala.eCommerce.event.PaymentMethodTypeDeleted;
import com.skytala.eCommerce.event.PaymentMethodTypeFound;
import com.skytala.eCommerce.event.PaymentMethodTypeUpdated;
import com.skytala.eCommerce.query.FindPaymentMethodTypesBy;

@RestController
@RequestMapping("/api/paymentMethodType")
public class PaymentMethodTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentMethodType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentMethodTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentMethodType
	 * @return a List with the PaymentMethodTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentMethodType> findPaymentMethodTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentMethodTypesBy query = new FindPaymentMethodTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentMethodTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodTypeFound.class,
				event -> sendPaymentMethodTypesFoundMessage(((PaymentMethodTypeFound) event).getPaymentMethodTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentMethodTypesFoundMessage(List<PaymentMethodType> paymentMethodTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentMethodTypes);
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
	public boolean createPaymentMethodType(HttpServletRequest request) {

		PaymentMethodType paymentMethodTypeToBeAdded = new PaymentMethodType();
		try {
			paymentMethodTypeToBeAdded = PaymentMethodTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentMethodType(paymentMethodTypeToBeAdded);

	}

	/**
	 * creates a new PaymentMethodType entry in the ofbiz database
	 * 
	 * @param paymentMethodTypeToBeAdded
	 *            the PaymentMethodType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentMethodType(PaymentMethodType paymentMethodTypeToBeAdded) {

		AddPaymentMethodType com = new AddPaymentMethodType(paymentMethodTypeToBeAdded);
		int usedTicketId;

		synchronized (PaymentMethodTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodTypeAdded.class,
				event -> sendPaymentMethodTypeChangedMessage(((PaymentMethodTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentMethodType(HttpServletRequest request) {

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

		PaymentMethodType paymentMethodTypeToBeUpdated = new PaymentMethodType();

		try {
			paymentMethodTypeToBeUpdated = PaymentMethodTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentMethodType(paymentMethodTypeToBeUpdated);

	}

	/**
	 * Updates the PaymentMethodType with the specific Id
	 * 
	 * @param paymentMethodTypeToBeUpdated the PaymentMethodType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentMethodType(PaymentMethodType paymentMethodTypeToBeUpdated) {

		UpdatePaymentMethodType com = new UpdatePaymentMethodType(paymentMethodTypeToBeUpdated);

		int usedTicketId;

		synchronized (PaymentMethodTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodTypeUpdated.class,
				event -> sendPaymentMethodTypeChangedMessage(((PaymentMethodTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentMethodType from the database
	 * 
	 * @param paymentMethodTypeId:
	 *            the id of the PaymentMethodType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentMethodTypeById(@RequestParam(value = "paymentMethodTypeId") String paymentMethodTypeId) {

		DeletePaymentMethodType com = new DeletePaymentMethodType(paymentMethodTypeId);

		int usedTicketId;

		synchronized (PaymentMethodTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentMethodTypeDeleted.class,
				event -> sendPaymentMethodTypeChangedMessage(((PaymentMethodTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentMethodTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentMethodType/\" plus one of the following: "
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
