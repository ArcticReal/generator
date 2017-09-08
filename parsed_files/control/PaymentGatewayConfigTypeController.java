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
import com.skytala.eCommerce.command.AddPaymentGatewayConfigType;
import com.skytala.eCommerce.command.DeletePaymentGatewayConfigType;
import com.skytala.eCommerce.command.UpdatePaymentGatewayConfigType;
import com.skytala.eCommerce.entity.PaymentGatewayConfigType;
import com.skytala.eCommerce.entity.PaymentGatewayConfigTypeMapper;
import com.skytala.eCommerce.event.PaymentGatewayConfigTypeAdded;
import com.skytala.eCommerce.event.PaymentGatewayConfigTypeDeleted;
import com.skytala.eCommerce.event.PaymentGatewayConfigTypeFound;
import com.skytala.eCommerce.event.PaymentGatewayConfigTypeUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayConfigTypesBy;

@RestController
@RequestMapping("/api/paymentGatewayConfigType")
public class PaymentGatewayConfigTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayConfigType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayConfigTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayConfigType
	 * @return a List with the PaymentGatewayConfigTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayConfigType> findPaymentGatewayConfigTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayConfigTypesBy query = new FindPaymentGatewayConfigTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayConfigTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayConfigTypeFound.class,
				event -> sendPaymentGatewayConfigTypesFoundMessage(((PaymentGatewayConfigTypeFound) event).getPaymentGatewayConfigTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayConfigTypesFoundMessage(List<PaymentGatewayConfigType> paymentGatewayConfigTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayConfigTypes);
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
	public boolean createPaymentGatewayConfigType(HttpServletRequest request) {

		PaymentGatewayConfigType paymentGatewayConfigTypeToBeAdded = new PaymentGatewayConfigType();
		try {
			paymentGatewayConfigTypeToBeAdded = PaymentGatewayConfigTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayConfigType(paymentGatewayConfigTypeToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayConfigType entry in the ofbiz database
	 * 
	 * @param paymentGatewayConfigTypeToBeAdded
	 *            the PaymentGatewayConfigType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayConfigType(PaymentGatewayConfigType paymentGatewayConfigTypeToBeAdded) {

		AddPaymentGatewayConfigType com = new AddPaymentGatewayConfigType(paymentGatewayConfigTypeToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayConfigTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayConfigTypeAdded.class,
				event -> sendPaymentGatewayConfigTypeChangedMessage(((PaymentGatewayConfigTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayConfigType(HttpServletRequest request) {

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

		PaymentGatewayConfigType paymentGatewayConfigTypeToBeUpdated = new PaymentGatewayConfigType();

		try {
			paymentGatewayConfigTypeToBeUpdated = PaymentGatewayConfigTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayConfigType(paymentGatewayConfigTypeToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayConfigType with the specific Id
	 * 
	 * @param paymentGatewayConfigTypeToBeUpdated the PaymentGatewayConfigType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayConfigType(PaymentGatewayConfigType paymentGatewayConfigTypeToBeUpdated) {

		UpdatePaymentGatewayConfigType com = new UpdatePaymentGatewayConfigType(paymentGatewayConfigTypeToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayConfigTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayConfigTypeUpdated.class,
				event -> sendPaymentGatewayConfigTypeChangedMessage(((PaymentGatewayConfigTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayConfigType from the database
	 * 
	 * @param paymentGatewayConfigTypeId:
	 *            the id of the PaymentGatewayConfigType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayConfigTypeById(@RequestParam(value = "paymentGatewayConfigTypeId") String paymentGatewayConfigTypeId) {

		DeletePaymentGatewayConfigType com = new DeletePaymentGatewayConfigType(paymentGatewayConfigTypeId);

		int usedTicketId;

		synchronized (PaymentGatewayConfigTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayConfigTypeDeleted.class,
				event -> sendPaymentGatewayConfigTypeChangedMessage(((PaymentGatewayConfigTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayConfigTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayConfigType/\" plus one of the following: "
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
