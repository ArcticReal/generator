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
import com.skytala.eCommerce.command.AddPaymentGatewayAuthorizeNet;
import com.skytala.eCommerce.command.DeletePaymentGatewayAuthorizeNet;
import com.skytala.eCommerce.command.UpdatePaymentGatewayAuthorizeNet;
import com.skytala.eCommerce.entity.PaymentGatewayAuthorizeNet;
import com.skytala.eCommerce.entity.PaymentGatewayAuthorizeNetMapper;
import com.skytala.eCommerce.event.PaymentGatewayAuthorizeNetAdded;
import com.skytala.eCommerce.event.PaymentGatewayAuthorizeNetDeleted;
import com.skytala.eCommerce.event.PaymentGatewayAuthorizeNetFound;
import com.skytala.eCommerce.event.PaymentGatewayAuthorizeNetUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayAuthorizeNetsBy;

@RestController
@RequestMapping("/api/paymentGatewayAuthorizeNet")
public class PaymentGatewayAuthorizeNetController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayAuthorizeNet>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayAuthorizeNetController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayAuthorizeNet
	 * @return a List with the PaymentGatewayAuthorizeNets
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayAuthorizeNet> findPaymentGatewayAuthorizeNetsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayAuthorizeNetsBy query = new FindPaymentGatewayAuthorizeNetsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayAuthorizeNetController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayAuthorizeNetFound.class,
				event -> sendPaymentGatewayAuthorizeNetsFoundMessage(((PaymentGatewayAuthorizeNetFound) event).getPaymentGatewayAuthorizeNets(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayAuthorizeNetsFoundMessage(List<PaymentGatewayAuthorizeNet> paymentGatewayAuthorizeNets, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayAuthorizeNets);
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
	public boolean createPaymentGatewayAuthorizeNet(HttpServletRequest request) {

		PaymentGatewayAuthorizeNet paymentGatewayAuthorizeNetToBeAdded = new PaymentGatewayAuthorizeNet();
		try {
			paymentGatewayAuthorizeNetToBeAdded = PaymentGatewayAuthorizeNetMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayAuthorizeNet(paymentGatewayAuthorizeNetToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayAuthorizeNet entry in the ofbiz database
	 * 
	 * @param paymentGatewayAuthorizeNetToBeAdded
	 *            the PaymentGatewayAuthorizeNet thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayAuthorizeNet(PaymentGatewayAuthorizeNet paymentGatewayAuthorizeNetToBeAdded) {

		AddPaymentGatewayAuthorizeNet com = new AddPaymentGatewayAuthorizeNet(paymentGatewayAuthorizeNetToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayAuthorizeNetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayAuthorizeNetAdded.class,
				event -> sendPaymentGatewayAuthorizeNetChangedMessage(((PaymentGatewayAuthorizeNetAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayAuthorizeNet(HttpServletRequest request) {

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

		PaymentGatewayAuthorizeNet paymentGatewayAuthorizeNetToBeUpdated = new PaymentGatewayAuthorizeNet();

		try {
			paymentGatewayAuthorizeNetToBeUpdated = PaymentGatewayAuthorizeNetMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayAuthorizeNet(paymentGatewayAuthorizeNetToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayAuthorizeNet with the specific Id
	 * 
	 * @param paymentGatewayAuthorizeNetToBeUpdated the PaymentGatewayAuthorizeNet thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayAuthorizeNet(PaymentGatewayAuthorizeNet paymentGatewayAuthorizeNetToBeUpdated) {

		UpdatePaymentGatewayAuthorizeNet com = new UpdatePaymentGatewayAuthorizeNet(paymentGatewayAuthorizeNetToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayAuthorizeNetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayAuthorizeNetUpdated.class,
				event -> sendPaymentGatewayAuthorizeNetChangedMessage(((PaymentGatewayAuthorizeNetUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayAuthorizeNet from the database
	 * 
	 * @param paymentGatewayAuthorizeNetId:
	 *            the id of the PaymentGatewayAuthorizeNet thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayAuthorizeNetById(@RequestParam(value = "paymentGatewayAuthorizeNetId") String paymentGatewayAuthorizeNetId) {

		DeletePaymentGatewayAuthorizeNet com = new DeletePaymentGatewayAuthorizeNet(paymentGatewayAuthorizeNetId);

		int usedTicketId;

		synchronized (PaymentGatewayAuthorizeNetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayAuthorizeNetDeleted.class,
				event -> sendPaymentGatewayAuthorizeNetChangedMessage(((PaymentGatewayAuthorizeNetDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayAuthorizeNetChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayAuthorizeNet/\" plus one of the following: "
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
