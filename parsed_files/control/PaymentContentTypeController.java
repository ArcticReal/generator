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
import com.skytala.eCommerce.command.AddPaymentContentType;
import com.skytala.eCommerce.command.DeletePaymentContentType;
import com.skytala.eCommerce.command.UpdatePaymentContentType;
import com.skytala.eCommerce.entity.PaymentContentType;
import com.skytala.eCommerce.entity.PaymentContentTypeMapper;
import com.skytala.eCommerce.event.PaymentContentTypeAdded;
import com.skytala.eCommerce.event.PaymentContentTypeDeleted;
import com.skytala.eCommerce.event.PaymentContentTypeFound;
import com.skytala.eCommerce.event.PaymentContentTypeUpdated;
import com.skytala.eCommerce.query.FindPaymentContentTypesBy;

@RestController
@RequestMapping("/api/paymentContentType")
public class PaymentContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentContentType
	 * @return a List with the PaymentContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentContentType> findPaymentContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentContentTypesBy query = new FindPaymentContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentContentTypeFound.class,
				event -> sendPaymentContentTypesFoundMessage(((PaymentContentTypeFound) event).getPaymentContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentContentTypesFoundMessage(List<PaymentContentType> paymentContentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentContentTypes);
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
	public boolean createPaymentContentType(HttpServletRequest request) {

		PaymentContentType paymentContentTypeToBeAdded = new PaymentContentType();
		try {
			paymentContentTypeToBeAdded = PaymentContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentContentType(paymentContentTypeToBeAdded);

	}

	/**
	 * creates a new PaymentContentType entry in the ofbiz database
	 * 
	 * @param paymentContentTypeToBeAdded
	 *            the PaymentContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentContentType(PaymentContentType paymentContentTypeToBeAdded) {

		AddPaymentContentType com = new AddPaymentContentType(paymentContentTypeToBeAdded);
		int usedTicketId;

		synchronized (PaymentContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentContentTypeAdded.class,
				event -> sendPaymentContentTypeChangedMessage(((PaymentContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentContentType(HttpServletRequest request) {

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

		PaymentContentType paymentContentTypeToBeUpdated = new PaymentContentType();

		try {
			paymentContentTypeToBeUpdated = PaymentContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentContentType(paymentContentTypeToBeUpdated);

	}

	/**
	 * Updates the PaymentContentType with the specific Id
	 * 
	 * @param paymentContentTypeToBeUpdated the PaymentContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentContentType(PaymentContentType paymentContentTypeToBeUpdated) {

		UpdatePaymentContentType com = new UpdatePaymentContentType(paymentContentTypeToBeUpdated);

		int usedTicketId;

		synchronized (PaymentContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentContentTypeUpdated.class,
				event -> sendPaymentContentTypeChangedMessage(((PaymentContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentContentType from the database
	 * 
	 * @param paymentContentTypeId:
	 *            the id of the PaymentContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentContentTypeById(@RequestParam(value = "paymentContentTypeId") String paymentContentTypeId) {

		DeletePaymentContentType com = new DeletePaymentContentType(paymentContentTypeId);

		int usedTicketId;

		synchronized (PaymentContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentContentTypeDeleted.class,
				event -> sendPaymentContentTypeChangedMessage(((PaymentContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentContentType/\" plus one of the following: "
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
