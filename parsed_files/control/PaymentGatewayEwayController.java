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
import com.skytala.eCommerce.command.AddPaymentGatewayEway;
import com.skytala.eCommerce.command.DeletePaymentGatewayEway;
import com.skytala.eCommerce.command.UpdatePaymentGatewayEway;
import com.skytala.eCommerce.entity.PaymentGatewayEway;
import com.skytala.eCommerce.entity.PaymentGatewayEwayMapper;
import com.skytala.eCommerce.event.PaymentGatewayEwayAdded;
import com.skytala.eCommerce.event.PaymentGatewayEwayDeleted;
import com.skytala.eCommerce.event.PaymentGatewayEwayFound;
import com.skytala.eCommerce.event.PaymentGatewayEwayUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayEwaysBy;

@RestController
@RequestMapping("/api/paymentGatewayEway")
public class PaymentGatewayEwayController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayEway>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayEwayController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayEway
	 * @return a List with the PaymentGatewayEways
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayEway> findPaymentGatewayEwaysBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayEwaysBy query = new FindPaymentGatewayEwaysBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayEwayController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayEwayFound.class,
				event -> sendPaymentGatewayEwaysFoundMessage(((PaymentGatewayEwayFound) event).getPaymentGatewayEways(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayEwaysFoundMessage(List<PaymentGatewayEway> paymentGatewayEways, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayEways);
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
	public boolean createPaymentGatewayEway(HttpServletRequest request) {

		PaymentGatewayEway paymentGatewayEwayToBeAdded = new PaymentGatewayEway();
		try {
			paymentGatewayEwayToBeAdded = PaymentGatewayEwayMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayEway(paymentGatewayEwayToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayEway entry in the ofbiz database
	 * 
	 * @param paymentGatewayEwayToBeAdded
	 *            the PaymentGatewayEway thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayEway(PaymentGatewayEway paymentGatewayEwayToBeAdded) {

		AddPaymentGatewayEway com = new AddPaymentGatewayEway(paymentGatewayEwayToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayEwayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayEwayAdded.class,
				event -> sendPaymentGatewayEwayChangedMessage(((PaymentGatewayEwayAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayEway(HttpServletRequest request) {

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

		PaymentGatewayEway paymentGatewayEwayToBeUpdated = new PaymentGatewayEway();

		try {
			paymentGatewayEwayToBeUpdated = PaymentGatewayEwayMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayEway(paymentGatewayEwayToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayEway with the specific Id
	 * 
	 * @param paymentGatewayEwayToBeUpdated the PaymentGatewayEway thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayEway(PaymentGatewayEway paymentGatewayEwayToBeUpdated) {

		UpdatePaymentGatewayEway com = new UpdatePaymentGatewayEway(paymentGatewayEwayToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayEwayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayEwayUpdated.class,
				event -> sendPaymentGatewayEwayChangedMessage(((PaymentGatewayEwayUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayEway from the database
	 * 
	 * @param paymentGatewayEwayId:
	 *            the id of the PaymentGatewayEway thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayEwayById(@RequestParam(value = "paymentGatewayEwayId") String paymentGatewayEwayId) {

		DeletePaymentGatewayEway com = new DeletePaymentGatewayEway(paymentGatewayEwayId);

		int usedTicketId;

		synchronized (PaymentGatewayEwayController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayEwayDeleted.class,
				event -> sendPaymentGatewayEwayChangedMessage(((PaymentGatewayEwayDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayEwayChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayEway/\" plus one of the following: "
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
