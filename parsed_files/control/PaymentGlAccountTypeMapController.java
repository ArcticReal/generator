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
import com.skytala.eCommerce.command.AddPaymentGlAccountTypeMap;
import com.skytala.eCommerce.command.DeletePaymentGlAccountTypeMap;
import com.skytala.eCommerce.command.UpdatePaymentGlAccountTypeMap;
import com.skytala.eCommerce.entity.PaymentGlAccountTypeMap;
import com.skytala.eCommerce.entity.PaymentGlAccountTypeMapMapper;
import com.skytala.eCommerce.event.PaymentGlAccountTypeMapAdded;
import com.skytala.eCommerce.event.PaymentGlAccountTypeMapDeleted;
import com.skytala.eCommerce.event.PaymentGlAccountTypeMapFound;
import com.skytala.eCommerce.event.PaymentGlAccountTypeMapUpdated;
import com.skytala.eCommerce.query.FindPaymentGlAccountTypeMapsBy;

@RestController
@RequestMapping("/api/paymentGlAccountTypeMap")
public class PaymentGlAccountTypeMapController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGlAccountTypeMap>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGlAccountTypeMapController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGlAccountTypeMap
	 * @return a List with the PaymentGlAccountTypeMaps
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGlAccountTypeMap> findPaymentGlAccountTypeMapsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGlAccountTypeMapsBy query = new FindPaymentGlAccountTypeMapsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGlAccountTypeMapController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGlAccountTypeMapFound.class,
				event -> sendPaymentGlAccountTypeMapsFoundMessage(((PaymentGlAccountTypeMapFound) event).getPaymentGlAccountTypeMaps(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGlAccountTypeMapsFoundMessage(List<PaymentGlAccountTypeMap> paymentGlAccountTypeMaps, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGlAccountTypeMaps);
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
	public boolean createPaymentGlAccountTypeMap(HttpServletRequest request) {

		PaymentGlAccountTypeMap paymentGlAccountTypeMapToBeAdded = new PaymentGlAccountTypeMap();
		try {
			paymentGlAccountTypeMapToBeAdded = PaymentGlAccountTypeMapMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGlAccountTypeMap(paymentGlAccountTypeMapToBeAdded);

	}

	/**
	 * creates a new PaymentGlAccountTypeMap entry in the ofbiz database
	 * 
	 * @param paymentGlAccountTypeMapToBeAdded
	 *            the PaymentGlAccountTypeMap thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGlAccountTypeMap(PaymentGlAccountTypeMap paymentGlAccountTypeMapToBeAdded) {

		AddPaymentGlAccountTypeMap com = new AddPaymentGlAccountTypeMap(paymentGlAccountTypeMapToBeAdded);
		int usedTicketId;

		synchronized (PaymentGlAccountTypeMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGlAccountTypeMapAdded.class,
				event -> sendPaymentGlAccountTypeMapChangedMessage(((PaymentGlAccountTypeMapAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGlAccountTypeMap(HttpServletRequest request) {

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

		PaymentGlAccountTypeMap paymentGlAccountTypeMapToBeUpdated = new PaymentGlAccountTypeMap();

		try {
			paymentGlAccountTypeMapToBeUpdated = PaymentGlAccountTypeMapMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGlAccountTypeMap(paymentGlAccountTypeMapToBeUpdated);

	}

	/**
	 * Updates the PaymentGlAccountTypeMap with the specific Id
	 * 
	 * @param paymentGlAccountTypeMapToBeUpdated the PaymentGlAccountTypeMap thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGlAccountTypeMap(PaymentGlAccountTypeMap paymentGlAccountTypeMapToBeUpdated) {

		UpdatePaymentGlAccountTypeMap com = new UpdatePaymentGlAccountTypeMap(paymentGlAccountTypeMapToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGlAccountTypeMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGlAccountTypeMapUpdated.class,
				event -> sendPaymentGlAccountTypeMapChangedMessage(((PaymentGlAccountTypeMapUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGlAccountTypeMap from the database
	 * 
	 * @param paymentGlAccountTypeMapId:
	 *            the id of the PaymentGlAccountTypeMap thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGlAccountTypeMapById(@RequestParam(value = "paymentGlAccountTypeMapId") String paymentGlAccountTypeMapId) {

		DeletePaymentGlAccountTypeMap com = new DeletePaymentGlAccountTypeMap(paymentGlAccountTypeMapId);

		int usedTicketId;

		synchronized (PaymentGlAccountTypeMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGlAccountTypeMapDeleted.class,
				event -> sendPaymentGlAccountTypeMapChangedMessage(((PaymentGlAccountTypeMapDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGlAccountTypeMapChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGlAccountTypeMap/\" plus one of the following: "
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
