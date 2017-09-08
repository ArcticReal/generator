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
import com.skytala.eCommerce.command.AddPaymentGatewayPayflowPro;
import com.skytala.eCommerce.command.DeletePaymentGatewayPayflowPro;
import com.skytala.eCommerce.command.UpdatePaymentGatewayPayflowPro;
import com.skytala.eCommerce.entity.PaymentGatewayPayflowPro;
import com.skytala.eCommerce.entity.PaymentGatewayPayflowProMapper;
import com.skytala.eCommerce.event.PaymentGatewayPayflowProAdded;
import com.skytala.eCommerce.event.PaymentGatewayPayflowProDeleted;
import com.skytala.eCommerce.event.PaymentGatewayPayflowProFound;
import com.skytala.eCommerce.event.PaymentGatewayPayflowProUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayPayflowProsBy;

@RestController
@RequestMapping("/api/paymentGatewayPayflowPro")
public class PaymentGatewayPayflowProController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayPayflowPro>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayPayflowProController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayPayflowPro
	 * @return a List with the PaymentGatewayPayflowPros
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayPayflowPro> findPaymentGatewayPayflowProsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayPayflowProsBy query = new FindPaymentGatewayPayflowProsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayPayflowProController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayPayflowProFound.class,
				event -> sendPaymentGatewayPayflowProsFoundMessage(((PaymentGatewayPayflowProFound) event).getPaymentGatewayPayflowPros(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayPayflowProsFoundMessage(List<PaymentGatewayPayflowPro> paymentGatewayPayflowPros, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayPayflowPros);
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
	public boolean createPaymentGatewayPayflowPro(HttpServletRequest request) {

		PaymentGatewayPayflowPro paymentGatewayPayflowProToBeAdded = new PaymentGatewayPayflowPro();
		try {
			paymentGatewayPayflowProToBeAdded = PaymentGatewayPayflowProMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayPayflowPro(paymentGatewayPayflowProToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayPayflowPro entry in the ofbiz database
	 * 
	 * @param paymentGatewayPayflowProToBeAdded
	 *            the PaymentGatewayPayflowPro thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayPayflowPro(PaymentGatewayPayflowPro paymentGatewayPayflowProToBeAdded) {

		AddPaymentGatewayPayflowPro com = new AddPaymentGatewayPayflowPro(paymentGatewayPayflowProToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayPayflowProController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayPayflowProAdded.class,
				event -> sendPaymentGatewayPayflowProChangedMessage(((PaymentGatewayPayflowProAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayPayflowPro(HttpServletRequest request) {

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

		PaymentGatewayPayflowPro paymentGatewayPayflowProToBeUpdated = new PaymentGatewayPayflowPro();

		try {
			paymentGatewayPayflowProToBeUpdated = PaymentGatewayPayflowProMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayPayflowPro(paymentGatewayPayflowProToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayPayflowPro with the specific Id
	 * 
	 * @param paymentGatewayPayflowProToBeUpdated the PaymentGatewayPayflowPro thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayPayflowPro(PaymentGatewayPayflowPro paymentGatewayPayflowProToBeUpdated) {

		UpdatePaymentGatewayPayflowPro com = new UpdatePaymentGatewayPayflowPro(paymentGatewayPayflowProToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayPayflowProController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayPayflowProUpdated.class,
				event -> sendPaymentGatewayPayflowProChangedMessage(((PaymentGatewayPayflowProUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayPayflowPro from the database
	 * 
	 * @param paymentGatewayPayflowProId:
	 *            the id of the PaymentGatewayPayflowPro thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayPayflowProById(@RequestParam(value = "paymentGatewayPayflowProId") String paymentGatewayPayflowProId) {

		DeletePaymentGatewayPayflowPro com = new DeletePaymentGatewayPayflowPro(paymentGatewayPayflowProId);

		int usedTicketId;

		synchronized (PaymentGatewayPayflowProController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayPayflowProDeleted.class,
				event -> sendPaymentGatewayPayflowProChangedMessage(((PaymentGatewayPayflowProDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayPayflowProChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayPayflowPro/\" plus one of the following: "
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
