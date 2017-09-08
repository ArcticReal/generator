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
import com.skytala.eCommerce.command.AddPaymentGatewayOrbital;
import com.skytala.eCommerce.command.DeletePaymentGatewayOrbital;
import com.skytala.eCommerce.command.UpdatePaymentGatewayOrbital;
import com.skytala.eCommerce.entity.PaymentGatewayOrbital;
import com.skytala.eCommerce.entity.PaymentGatewayOrbitalMapper;
import com.skytala.eCommerce.event.PaymentGatewayOrbitalAdded;
import com.skytala.eCommerce.event.PaymentGatewayOrbitalDeleted;
import com.skytala.eCommerce.event.PaymentGatewayOrbitalFound;
import com.skytala.eCommerce.event.PaymentGatewayOrbitalUpdated;
import com.skytala.eCommerce.query.FindPaymentGatewayOrbitalsBy;

@RestController
@RequestMapping("/api/paymentGatewayOrbital")
public class PaymentGatewayOrbitalController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGatewayOrbital>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGatewayOrbitalController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGatewayOrbital
	 * @return a List with the PaymentGatewayOrbitals
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGatewayOrbital> findPaymentGatewayOrbitalsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGatewayOrbitalsBy query = new FindPaymentGatewayOrbitalsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGatewayOrbitalController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayOrbitalFound.class,
				event -> sendPaymentGatewayOrbitalsFoundMessage(((PaymentGatewayOrbitalFound) event).getPaymentGatewayOrbitals(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGatewayOrbitalsFoundMessage(List<PaymentGatewayOrbital> paymentGatewayOrbitals, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGatewayOrbitals);
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
	public boolean createPaymentGatewayOrbital(HttpServletRequest request) {

		PaymentGatewayOrbital paymentGatewayOrbitalToBeAdded = new PaymentGatewayOrbital();
		try {
			paymentGatewayOrbitalToBeAdded = PaymentGatewayOrbitalMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGatewayOrbital(paymentGatewayOrbitalToBeAdded);

	}

	/**
	 * creates a new PaymentGatewayOrbital entry in the ofbiz database
	 * 
	 * @param paymentGatewayOrbitalToBeAdded
	 *            the PaymentGatewayOrbital thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGatewayOrbital(PaymentGatewayOrbital paymentGatewayOrbitalToBeAdded) {

		AddPaymentGatewayOrbital com = new AddPaymentGatewayOrbital(paymentGatewayOrbitalToBeAdded);
		int usedTicketId;

		synchronized (PaymentGatewayOrbitalController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayOrbitalAdded.class,
				event -> sendPaymentGatewayOrbitalChangedMessage(((PaymentGatewayOrbitalAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGatewayOrbital(HttpServletRequest request) {

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

		PaymentGatewayOrbital paymentGatewayOrbitalToBeUpdated = new PaymentGatewayOrbital();

		try {
			paymentGatewayOrbitalToBeUpdated = PaymentGatewayOrbitalMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGatewayOrbital(paymentGatewayOrbitalToBeUpdated);

	}

	/**
	 * Updates the PaymentGatewayOrbital with the specific Id
	 * 
	 * @param paymentGatewayOrbitalToBeUpdated the PaymentGatewayOrbital thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGatewayOrbital(PaymentGatewayOrbital paymentGatewayOrbitalToBeUpdated) {

		UpdatePaymentGatewayOrbital com = new UpdatePaymentGatewayOrbital(paymentGatewayOrbitalToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGatewayOrbitalController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayOrbitalUpdated.class,
				event -> sendPaymentGatewayOrbitalChangedMessage(((PaymentGatewayOrbitalUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGatewayOrbital from the database
	 * 
	 * @param paymentGatewayOrbitalId:
	 *            the id of the PaymentGatewayOrbital thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGatewayOrbitalById(@RequestParam(value = "paymentGatewayOrbitalId") String paymentGatewayOrbitalId) {

		DeletePaymentGatewayOrbital com = new DeletePaymentGatewayOrbital(paymentGatewayOrbitalId);

		int usedTicketId;

		synchronized (PaymentGatewayOrbitalController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGatewayOrbitalDeleted.class,
				event -> sendPaymentGatewayOrbitalChangedMessage(((PaymentGatewayOrbitalDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGatewayOrbitalChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGatewayOrbital/\" plus one of the following: "
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
