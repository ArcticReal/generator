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
import com.skytala.eCommerce.command.AddPaymentGatewayResponse;
import com.skytala.eCommerce.command.DeletePaymentGatewayResponse;
import com.skytala.eCommerce.command.UpdatePaymentGatewayResponse;
import com.skytala.eCommerce.entity.PaymentGatewayResponse;
import com.skytala.eCommerce.entity.PaymentGatewayResponseMapper;
import com.skytala.eCommerce.event.PaymentGatewayResponseAdded;
import com.skytala.eCommerce.event.PaymentGatewayResponseDeleted;
import com.skytala.eCommerce.event.PaymentGatewayResponseFound;
import com.skytala.eCommerce.event.PaymentGatewayResponseUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayResponsesBy;

@RestController
@RequestMapping("/api/paymentGatewayResponse")
public class PaymentGatewayResponseController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayResponse>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayResponseController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayResponse
	 * @return a List with the PaymentGatewayResponses
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayResponse> findPaymentGatewayResponsesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayResponsesBy query = new FindPaymentGatewayResponsesBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayResponseController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayResponseFound.class,
				event -> sendPaymentGatewayResponsesFoundMessage(((PaymentGatewayResponseFound) event).getPaymentGatewayResponses(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayResponsesFoundMessage(List<PaymentGatewayResponse> paymentGatewayResponses, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayResponses);
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
	public boolean createPaymentGatewayResponse(HttpServletRequest request) {

		PaymentGatewayResponse paymentGatewayResponseToBeAdded = new PaymentGatewayResponse();
		try {
			paymentGatewayResponseToBeAdded = PaymentGatewayResponseMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayResponse(paymentGatewayResponseToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayResponse entry in the ofbiz database
	 * 
	 * @param paymentGatewayResponseToBeAdded
	 *            the PaymentGatewayResponse thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayResponse(PaymentGatewayResponse paymentGatewayResponseToBeAdded) {

		AddPaymentGatewayResponse com = new AddPaymentGatewayResponse(paymentGatewayResponseToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayResponseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayResponseAdded.class,
				event -> sendPaymentGatewayResponseChangedMessage(((PaymentGatewayResponseAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayResponse(HttpServletRequest request) {

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

		PaymentGatewayResponse paymentGatewayResponseToBeUpdated = new PaymentGatewayResponse();

		try {
			paymentGatewayResponseToBeUpdated = PaymentGatewayResponseMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayResponse(paymentGatewayResponseToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayResponse with the specific Id
	 * 
	 * @param paymentGatewayResponseToBeUpdated the PaymentGatewayResponse thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayResponse(PaymentGatewayResponse paymentGatewayResponseToBeUpdated) {

		UpdatePaymentGatewayResponse com = new UpdatePaymentGatewayResponse(paymentGatewayResponseToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayResponseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayResponseUpdated.class,
				event -> sendPaymentGatewayResponseChangedMessage(((PaymentGatewayResponseUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayResponse from the database
	 * 
	 * @param paymentGatewayResponseId:
	 *            the id of the PaymentGatewayResponse thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayResponseById(@RequestParam(value = "paymentGatewayResponseId") String paymentGatewayResponseId) {

		DeletePaymentGatewayResponse com = new DeletePaymentGatewayResponse(paymentGatewayResponseId);

		int usedTicketId;

		synchronized (PaymentGatewayResponseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayResponseDeleted.class,
				event -> sendPaymentGatewayResponseChangedMessage(((PaymentGatewayResponseDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayResponseChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayResponse/\" plus one of the following: "
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
