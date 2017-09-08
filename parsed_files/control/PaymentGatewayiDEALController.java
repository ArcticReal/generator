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
import com.skytala.eCommerce.command.AddPaymentGatewayiDEAL;
import com.skytala.eCommerce.command.DeletePaymentGatewayiDEAL;
import com.skytala.eCommerce.command.UpdatePaymentGatewayiDEAL;
import com.skytala.eCommerce.entity.PaymentGatewayiDEAL;
import com.skytala.eCommerce.entity.PaymentGatewayiDEALMapper;
import com.skytala.eCommerce.event.PaymentGatewayiDEALAdded;
import com.skytala.eCommerce.event.PaymentGatewayiDEALDeleted;
import com.skytala.eCommerce.event.PaymentGatewayiDEALFound;
import com.skytala.eCommerce.event.PaymentGatewayiDEALUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayiDEALsBy;

@RestController
@RequestMapping("/api/paymentGatewayiDEAL")
public class PaymentGatewayiDEALController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayiDEAL>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayiDEALController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayiDEAL
	 * @return a List with the PaymentGatewayiDEALs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayiDEAL> findPaymentGatewayiDEALsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayiDEALsBy query = new FindPaymentGatewayiDEALsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayiDEALController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayiDEALFound.class,
				event -> sendPaymentGatewayiDEALsFoundMessage(((PaymentGatewayiDEALFound) event).getPaymentGatewayiDEALs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayiDEALsFoundMessage(List<PaymentGatewayiDEAL> paymentGatewayiDEALs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayiDEALs);
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
	public boolean createPaymentGatewayiDEAL(HttpServletRequest request) {

		PaymentGatewayiDEAL paymentGatewayiDEALToBeAdded = new PaymentGatewayiDEAL();
		try {
			paymentGatewayiDEALToBeAdded = PaymentGatewayiDEALMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayiDEAL(paymentGatewayiDEALToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayiDEAL entry in the ofbiz database
	 * 
	 * @param paymentGatewayiDEALToBeAdded
	 *            the PaymentGatewayiDEAL thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayiDEAL(PaymentGatewayiDEAL paymentGatewayiDEALToBeAdded) {

		AddPaymentGatewayiDEAL com = new AddPaymentGatewayiDEAL(paymentGatewayiDEALToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayiDEALController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayiDEALAdded.class,
				event -> sendPaymentGatewayiDEALChangedMessage(((PaymentGatewayiDEALAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayiDEAL(HttpServletRequest request) {

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

		PaymentGatewayiDEAL paymentGatewayiDEALToBeUpdated = new PaymentGatewayiDEAL();

		try {
			paymentGatewayiDEALToBeUpdated = PaymentGatewayiDEALMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayiDEAL(paymentGatewayiDEALToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayiDEAL with the specific Id
	 * 
	 * @param paymentGatewayiDEALToBeUpdated the PaymentGatewayiDEAL thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayiDEAL(PaymentGatewayiDEAL paymentGatewayiDEALToBeUpdated) {

		UpdatePaymentGatewayiDEAL com = new UpdatePaymentGatewayiDEAL(paymentGatewayiDEALToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayiDEALController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayiDEALUpdated.class,
				event -> sendPaymentGatewayiDEALChangedMessage(((PaymentGatewayiDEALUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayiDEAL from the database
	 * 
	 * @param paymentGatewayiDEALId:
	 *            the id of the PaymentGatewayiDEAL thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayiDEALById(@RequestParam(value = "paymentGatewayiDEALId") String paymentGatewayiDEALId) {

		DeletePaymentGatewayiDEAL com = new DeletePaymentGatewayiDEAL(paymentGatewayiDEALId);

		int usedTicketId;

		synchronized (PaymentGatewayiDEALController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayiDEALDeleted.class,
				event -> sendPaymentGatewayiDEALChangedMessage(((PaymentGatewayiDEALDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayiDEALChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayiDEAL/\" plus one of the following: "
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
