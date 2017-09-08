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
import com.skytala.eCommerce.command.AddPaymentGatewayRespMsg;
import com.skytala.eCommerce.command.DeletePaymentGatewayRespMsg;
import com.skytala.eCommerce.command.UpdatePaymentGatewayRespMsg;
import com.skytala.eCommerce.entity.PaymentGatewayRespMsg;
import com.skytala.eCommerce.entity.PaymentGatewayRespMsgMapper;
import com.skytala.eCommerce.event.PaymentGatewayRespMsgAdded;
import com.skytala.eCommerce.event.PaymentGatewayRespMsgDeleted;
import com.skytala.eCommerce.event.PaymentGatewayRespMsgFound;
import com.skytala.eCommerce.event.PaymentGatewayRespMsgUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayRespMsgsBy;

@RestController
@RequestMapping("/api/paymentGatewayRespMsg")
public class PaymentGatewayRespMsgController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayRespMsg>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayRespMsgController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayRespMsg
	 * @return a List with the PaymentGatewayRespMsgs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayRespMsg> findPaymentGatewayRespMsgsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayRespMsgsBy query = new FindPaymentGatewayRespMsgsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayRespMsgController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayRespMsgFound.class,
				event -> sendPaymentGatewayRespMsgsFoundMessage(((PaymentGatewayRespMsgFound) event).getPaymentGatewayRespMsgs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayRespMsgsFoundMessage(List<PaymentGatewayRespMsg> paymentGatewayRespMsgs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayRespMsgs);
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
	public boolean createPaymentGatewayRespMsg(HttpServletRequest request) {

		PaymentGatewayRespMsg paymentGatewayRespMsgToBeAdded = new PaymentGatewayRespMsg();
		try {
			paymentGatewayRespMsgToBeAdded = PaymentGatewayRespMsgMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayRespMsg(paymentGatewayRespMsgToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayRespMsg entry in the ofbiz database
	 * 
	 * @param paymentGatewayRespMsgToBeAdded
	 *            the PaymentGatewayRespMsg thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayRespMsg(PaymentGatewayRespMsg paymentGatewayRespMsgToBeAdded) {

		AddPaymentGatewayRespMsg com = new AddPaymentGatewayRespMsg(paymentGatewayRespMsgToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayRespMsgController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayRespMsgAdded.class,
				event -> sendPaymentGatewayRespMsgChangedMessage(((PaymentGatewayRespMsgAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayRespMsg(HttpServletRequest request) {

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

		PaymentGatewayRespMsg paymentGatewayRespMsgToBeUpdated = new PaymentGatewayRespMsg();

		try {
			paymentGatewayRespMsgToBeUpdated = PaymentGatewayRespMsgMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayRespMsg(paymentGatewayRespMsgToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayRespMsg with the specific Id
	 * 
	 * @param paymentGatewayRespMsgToBeUpdated the PaymentGatewayRespMsg thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayRespMsg(PaymentGatewayRespMsg paymentGatewayRespMsgToBeUpdated) {

		UpdatePaymentGatewayRespMsg com = new UpdatePaymentGatewayRespMsg(paymentGatewayRespMsgToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayRespMsgController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayRespMsgUpdated.class,
				event -> sendPaymentGatewayRespMsgChangedMessage(((PaymentGatewayRespMsgUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayRespMsg from the database
	 * 
	 * @param paymentGatewayRespMsgId:
	 *            the id of the PaymentGatewayRespMsg thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayRespMsgById(@RequestParam(value = "paymentGatewayRespMsgId") String paymentGatewayRespMsgId) {

		DeletePaymentGatewayRespMsg com = new DeletePaymentGatewayRespMsg(paymentGatewayRespMsgId);

		int usedTicketId;

		synchronized (PaymentGatewayRespMsgController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayRespMsgDeleted.class,
				event -> sendPaymentGatewayRespMsgChangedMessage(((PaymentGatewayRespMsgDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayRespMsgChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayRespMsg/\" plus one of the following: "
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
