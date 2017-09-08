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
import com.skytala.eCommerce.command.AddPaymentGatewayCyberSource;
import com.skytala.eCommerce.command.DeletePaymentGatewayCyberSource;
import com.skytala.eCommerce.command.UpdatePaymentGatewayCyberSource;
import com.skytala.eCommerce.entity.PaymentGatewayCyberSource;
import com.skytala.eCommerce.entity.PaymentGatewayCyberSourceMapper;
import com.skytala.eCommerce.event.PaymentGatewayCyberSourceAdded;
import com.skytala.eCommerce.event.PaymentGatewayCyberSourceDeleted;
import com.skytala.eCommerce.event.PaymentGatewayCyberSourceFound;
import com.skytala.eCommerce.event.PaymentGatewayCyberSourceUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayCyberSourcesBy;

@RestController
@RequestMapping("/api/paymentGatewayCyberSource")
public class PaymentGatewayCyberSourceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayCyberSource>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayCyberSourceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayCyberSource
	 * @return a List with the PaymentGatewayCyberSources
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayCyberSource> findPaymentGatewayCyberSourcesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayCyberSourcesBy query = new FindPaymentGatewayCyberSourcesBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayCyberSourceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayCyberSourceFound.class,
				event -> sendPaymentGatewayCyberSourcesFoundMessage(((PaymentGatewayCyberSourceFound) event).getPaymentGatewayCyberSources(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayCyberSourcesFoundMessage(List<PaymentGatewayCyberSource> paymentGatewayCyberSources, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayCyberSources);
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
	public boolean createPaymentGatewayCyberSource(HttpServletRequest request) {

		PaymentGatewayCyberSource paymentGatewayCyberSourceToBeAdded = new PaymentGatewayCyberSource();
		try {
			paymentGatewayCyberSourceToBeAdded = PaymentGatewayCyberSourceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayCyberSource(paymentGatewayCyberSourceToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayCyberSource entry in the ofbiz database
	 * 
	 * @param paymentGatewayCyberSourceToBeAdded
	 *            the PaymentGatewayCyberSource thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayCyberSource(PaymentGatewayCyberSource paymentGatewayCyberSourceToBeAdded) {

		AddPaymentGatewayCyberSource com = new AddPaymentGatewayCyberSource(paymentGatewayCyberSourceToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayCyberSourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayCyberSourceAdded.class,
				event -> sendPaymentGatewayCyberSourceChangedMessage(((PaymentGatewayCyberSourceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayCyberSource(HttpServletRequest request) {

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

		PaymentGatewayCyberSource paymentGatewayCyberSourceToBeUpdated = new PaymentGatewayCyberSource();

		try {
			paymentGatewayCyberSourceToBeUpdated = PaymentGatewayCyberSourceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayCyberSource(paymentGatewayCyberSourceToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayCyberSource with the specific Id
	 * 
	 * @param paymentGatewayCyberSourceToBeUpdated the PaymentGatewayCyberSource thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayCyberSource(PaymentGatewayCyberSource paymentGatewayCyberSourceToBeUpdated) {

		UpdatePaymentGatewayCyberSource com = new UpdatePaymentGatewayCyberSource(paymentGatewayCyberSourceToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayCyberSourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayCyberSourceUpdated.class,
				event -> sendPaymentGatewayCyberSourceChangedMessage(((PaymentGatewayCyberSourceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayCyberSource from the database
	 * 
	 * @param paymentGatewayCyberSourceId:
	 *            the id of the PaymentGatewayCyberSource thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayCyberSourceById(@RequestParam(value = "paymentGatewayCyberSourceId") String paymentGatewayCyberSourceId) {

		DeletePaymentGatewayCyberSource com = new DeletePaymentGatewayCyberSource(paymentGatewayCyberSourceId);

		int usedTicketId;

		synchronized (PaymentGatewayCyberSourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayCyberSourceDeleted.class,
				event -> sendPaymentGatewayCyberSourceChangedMessage(((PaymentGatewayCyberSourceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayCyberSourceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayCyberSource/\" plus one of the following: "
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
