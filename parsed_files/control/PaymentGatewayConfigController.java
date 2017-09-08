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
import com.skytala.eCommerce.command.AddPaymentGatewayConfig;
import com.skytala.eCommerce.command.DeletePaymentGatewayConfig;
import com.skytala.eCommerce.command.UpdatePaymentGatewayConfig;
import com.skytala.eCommerce.entity.PaymentGatewayConfig;
import com.skytala.eCommerce.entity.PaymentGatewayConfigMapper;
import com.skytala.eCommerce.event.PaymentGatewayConfigAdded;
import com.skytala.eCommerce.event.PaymentGatewayConfigDeleted;
import com.skytala.eCommerce.event.PaymentGatewayConfigFound;
import com.skytala.eCommerce.event.PaymentGatewayConfigUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayConfigsBy;

@RestController
@RequestMapping("/api/paymentGatewayConfig")
public class PaymentGatewayConfigController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayConfig>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayConfigController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayConfig
	 * @return a List with the PaymentGatewayConfigs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayConfig> findPaymentGatewayConfigsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayConfigsBy query = new FindPaymentGatewayConfigsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayConfigController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayConfigFound.class,
				event -> sendPaymentGatewayConfigsFoundMessage(((PaymentGatewayConfigFound) event).getPaymentGatewayConfigs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayConfigsFoundMessage(List<PaymentGatewayConfig> paymentGatewayConfigs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayConfigs);
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
	public boolean createPaymentGatewayConfig(HttpServletRequest request) {

		PaymentGatewayConfig paymentGatewayConfigToBeAdded = new PaymentGatewayConfig();
		try {
			paymentGatewayConfigToBeAdded = PaymentGatewayConfigMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayConfig(paymentGatewayConfigToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayConfig entry in the ofbiz database
	 * 
	 * @param paymentGatewayConfigToBeAdded
	 *            the PaymentGatewayConfig thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayConfig(PaymentGatewayConfig paymentGatewayConfigToBeAdded) {

		AddPaymentGatewayConfig com = new AddPaymentGatewayConfig(paymentGatewayConfigToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayConfigAdded.class,
				event -> sendPaymentGatewayConfigChangedMessage(((PaymentGatewayConfigAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayConfig(HttpServletRequest request) {

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

		PaymentGatewayConfig paymentGatewayConfigToBeUpdated = new PaymentGatewayConfig();

		try {
			paymentGatewayConfigToBeUpdated = PaymentGatewayConfigMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayConfig(paymentGatewayConfigToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayConfig with the specific Id
	 * 
	 * @param paymentGatewayConfigToBeUpdated the PaymentGatewayConfig thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayConfig(PaymentGatewayConfig paymentGatewayConfigToBeUpdated) {

		UpdatePaymentGatewayConfig com = new UpdatePaymentGatewayConfig(paymentGatewayConfigToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayConfigUpdated.class,
				event -> sendPaymentGatewayConfigChangedMessage(((PaymentGatewayConfigUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayConfig from the database
	 * 
	 * @param paymentGatewayConfigId:
	 *            the id of the PaymentGatewayConfig thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayConfigById(@RequestParam(value = "paymentGatewayConfigId") String paymentGatewayConfigId) {

		DeletePaymentGatewayConfig com = new DeletePaymentGatewayConfig(paymentGatewayConfigId);

		int usedTicketId;

		synchronized (PaymentGatewayConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayConfigDeleted.class,
				event -> sendPaymentGatewayConfigChangedMessage(((PaymentGatewayConfigDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayConfigChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayConfig/\" plus one of the following: "
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
