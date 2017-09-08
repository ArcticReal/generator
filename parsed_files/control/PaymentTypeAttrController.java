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
import com.skytala.eCommerce.command.AddPaymentTypeAttr;
import com.skytala.eCommerce.command.DeletePaymentTypeAttr;
import com.skytala.eCommerce.command.UpdatePaymentTypeAttr;
import com.skytala.eCommerce.entity.PaymentTypeAttr;
import com.skytala.eCommerce.entity.PaymentTypeAttrMapper;
import com.skytala.eCommerce.event.PaymentTypeAttrAdded;
import com.skytala.eCommerce.event.PaymentTypeAttrDeleted;
import com.skytala.eCommerce.event.PaymentTypeAttrFound;
import com.skytala.eCommerce.event.PaymentTypeAttrUpdated;
import com.skytala.eCommerce.query.FindPaymentTypeAttrsBy;

@RestController
@RequestMapping("/api/paymentTypeAttr")
public class PaymentTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentTypeAttr
	 * @return a List with the PaymentTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentTypeAttr> findPaymentTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentTypeAttrsBy query = new FindPaymentTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentTypeAttrFound.class,
				event -> sendPaymentTypeAttrsFoundMessage(((PaymentTypeAttrFound) event).getPaymentTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentTypeAttrsFoundMessage(List<PaymentTypeAttr> paymentTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentTypeAttrs);
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
	public boolean createPaymentTypeAttr(HttpServletRequest request) {

		PaymentTypeAttr paymentTypeAttrToBeAdded = new PaymentTypeAttr();
		try {
			paymentTypeAttrToBeAdded = PaymentTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentTypeAttr(paymentTypeAttrToBeAdded);

	}

	/**
	 * creates a new PaymentTypeAttr entry in the ofbiz database
	 * 
	 * @param paymentTypeAttrToBeAdded
	 *            the PaymentTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentTypeAttr(PaymentTypeAttr paymentTypeAttrToBeAdded) {

		AddPaymentTypeAttr com = new AddPaymentTypeAttr(paymentTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (PaymentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentTypeAttrAdded.class,
				event -> sendPaymentTypeAttrChangedMessage(((PaymentTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentTypeAttr(HttpServletRequest request) {

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

		PaymentTypeAttr paymentTypeAttrToBeUpdated = new PaymentTypeAttr();

		try {
			paymentTypeAttrToBeUpdated = PaymentTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentTypeAttr(paymentTypeAttrToBeUpdated);

	}

	/**
	 * Updates the PaymentTypeAttr with the specific Id
	 * 
	 * @param paymentTypeAttrToBeUpdated the PaymentTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentTypeAttr(PaymentTypeAttr paymentTypeAttrToBeUpdated) {

		UpdatePaymentTypeAttr com = new UpdatePaymentTypeAttr(paymentTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (PaymentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentTypeAttrUpdated.class,
				event -> sendPaymentTypeAttrChangedMessage(((PaymentTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentTypeAttr from the database
	 * 
	 * @param paymentTypeAttrId:
	 *            the id of the PaymentTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentTypeAttrById(@RequestParam(value = "paymentTypeAttrId") String paymentTypeAttrId) {

		DeletePaymentTypeAttr com = new DeletePaymentTypeAttr(paymentTypeAttrId);

		int usedTicketId;

		synchronized (PaymentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentTypeAttrDeleted.class,
				event -> sendPaymentTypeAttrChangedMessage(((PaymentTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentTypeAttr/\" plus one of the following: "
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
