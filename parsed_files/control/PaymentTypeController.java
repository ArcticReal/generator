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
import com.skytala.eCommerce.command.AddPaymentType;
import com.skytala.eCommerce.command.DeletePaymentType;
import com.skytala.eCommerce.command.UpdatePaymentType;
import com.skytala.eCommerce.entity.PaymentType;
import com.skytala.eCommerce.entity.PaymentTypeMapper;
import com.skytala.eCommerce.event.PaymentTypeAdded;
import com.skytala.eCommerce.event.PaymentTypeDeleted;
import com.skytala.eCommerce.event.PaymentTypeFound;
import com.skytala.eCommerce.event.PaymentTypeUpdated;
import com.skytala.eCommerce.query.FindPaymentTypesBy;

@RestController
@RequestMapping("/api/paymentType")
public class PaymentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentType
	 * @return a List with the PaymentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentType> findPaymentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentTypesBy query = new FindPaymentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentTypeFound.class,
				event -> sendPaymentTypesFoundMessage(((PaymentTypeFound) event).getPaymentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentTypesFoundMessage(List<PaymentType> paymentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentTypes);
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
	public boolean createPaymentType(HttpServletRequest request) {

		PaymentType paymentTypeToBeAdded = new PaymentType();
		try {
			paymentTypeToBeAdded = PaymentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentType(paymentTypeToBeAdded);

	}

	/**
	 * creates a new PaymentType entry in the ofbiz database
	 * 
	 * @param paymentTypeToBeAdded
	 *            the PaymentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentType(PaymentType paymentTypeToBeAdded) {

		AddPaymentType com = new AddPaymentType(paymentTypeToBeAdded);
		int usedTicketId;

		synchronized (PaymentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentTypeAdded.class,
				event -> sendPaymentTypeChangedMessage(((PaymentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentType(HttpServletRequest request) {

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

		PaymentType paymentTypeToBeUpdated = new PaymentType();

		try {
			paymentTypeToBeUpdated = PaymentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentType(paymentTypeToBeUpdated);

	}

	/**
	 * Updates the PaymentType with the specific Id
	 * 
	 * @param paymentTypeToBeUpdated the PaymentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentType(PaymentType paymentTypeToBeUpdated) {

		UpdatePaymentType com = new UpdatePaymentType(paymentTypeToBeUpdated);

		int usedTicketId;

		synchronized (PaymentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentTypeUpdated.class,
				event -> sendPaymentTypeChangedMessage(((PaymentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentType from the database
	 * 
	 * @param paymentTypeId:
	 *            the id of the PaymentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentTypeById(@RequestParam(value = "paymentTypeId") String paymentTypeId) {

		DeletePaymentType com = new DeletePaymentType(paymentTypeId);

		int usedTicketId;

		synchronized (PaymentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentTypeDeleted.class,
				event -> sendPaymentTypeChangedMessage(((PaymentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentType/\" plus one of the following: "
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
