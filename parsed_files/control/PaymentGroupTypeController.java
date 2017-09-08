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
import com.skytala.eCommerce.command.AddPaymentGroupType;
import com.skytala.eCommerce.command.DeletePaymentGroupType;
import com.skytala.eCommerce.command.UpdatePaymentGroupType;
import com.skytala.eCommerce.entity.PaymentGroupType;
import com.skytala.eCommerce.entity.PaymentGroupTypeMapper;
import com.skytala.eCommerce.event.PaymentGroupTypeAdded;
import com.skytala.eCommerce.event.PaymentGroupTypeDeleted;
import com.skytala.eCommerce.event.PaymentGroupTypeFound;
import com.skytala.eCommerce.event.PaymentGroupTypeUpdated;
import com.skytala.eCommerce.query.FindPaymentGroupTypesBy;

@RestController
@RequestMapping("/api/paymentGroupType")
public class PaymentGroupTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGroupType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGroupTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGroupType
	 * @return a List with the PaymentGroupTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGroupType> findPaymentGroupTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGroupTypesBy query = new FindPaymentGroupTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGroupTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupTypeFound.class,
				event -> sendPaymentGroupTypesFoundMessage(((PaymentGroupTypeFound) event).getPaymentGroupTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGroupTypesFoundMessage(List<PaymentGroupType> paymentGroupTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGroupTypes);
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
	public boolean createPaymentGroupType(HttpServletRequest request) {

		PaymentGroupType paymentGroupTypeToBeAdded = new PaymentGroupType();
		try {
			paymentGroupTypeToBeAdded = PaymentGroupTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGroupType(paymentGroupTypeToBeAdded);

	}

	/**
	 * creates a new PaymentGroupType entry in the ofbiz database
	 * 
	 * @param paymentGroupTypeToBeAdded
	 *            the PaymentGroupType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGroupType(PaymentGroupType paymentGroupTypeToBeAdded) {

		AddPaymentGroupType com = new AddPaymentGroupType(paymentGroupTypeToBeAdded);
		int usedTicketId;

		synchronized (PaymentGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupTypeAdded.class,
				event -> sendPaymentGroupTypeChangedMessage(((PaymentGroupTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGroupType(HttpServletRequest request) {

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

		PaymentGroupType paymentGroupTypeToBeUpdated = new PaymentGroupType();

		try {
			paymentGroupTypeToBeUpdated = PaymentGroupTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGroupType(paymentGroupTypeToBeUpdated);

	}

	/**
	 * Updates the PaymentGroupType with the specific Id
	 * 
	 * @param paymentGroupTypeToBeUpdated the PaymentGroupType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGroupType(PaymentGroupType paymentGroupTypeToBeUpdated) {

		UpdatePaymentGroupType com = new UpdatePaymentGroupType(paymentGroupTypeToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupTypeUpdated.class,
				event -> sendPaymentGroupTypeChangedMessage(((PaymentGroupTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGroupType from the database
	 * 
	 * @param paymentGroupTypeId:
	 *            the id of the PaymentGroupType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGroupTypeById(@RequestParam(value = "paymentGroupTypeId") String paymentGroupTypeId) {

		DeletePaymentGroupType com = new DeletePaymentGroupType(paymentGroupTypeId);

		int usedTicketId;

		synchronized (PaymentGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupTypeDeleted.class,
				event -> sendPaymentGroupTypeChangedMessage(((PaymentGroupTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGroupTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGroupType/\" plus one of the following: "
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
