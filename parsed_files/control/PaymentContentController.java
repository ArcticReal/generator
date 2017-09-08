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
import com.skytala.eCommerce.command.AddPaymentContent;
import com.skytala.eCommerce.command.DeletePaymentContent;
import com.skytala.eCommerce.command.UpdatePaymentContent;
import com.skytala.eCommerce.entity.PaymentContent;
import com.skytala.eCommerce.entity.PaymentContentMapper;
import com.skytala.eCommerce.event.PaymentContentAdded;
import com.skytala.eCommerce.event.PaymentContentDeleted;
import com.skytala.eCommerce.event.PaymentContentFound;
import com.skytala.eCommerce.event.PaymentContentUpdated;
import com.skytala.eCommerce.query.FindPaymentContentsBy;

@RestController
@RequestMapping("/api/paymentContent")
public class PaymentContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentContent
	 * @return a List with the PaymentContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentContent> findPaymentContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentContentsBy query = new FindPaymentContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentContentFound.class,
				event -> sendPaymentContentsFoundMessage(((PaymentContentFound) event).getPaymentContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentContentsFoundMessage(List<PaymentContent> paymentContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentContents);
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
	public boolean createPaymentContent(HttpServletRequest request) {

		PaymentContent paymentContentToBeAdded = new PaymentContent();
		try {
			paymentContentToBeAdded = PaymentContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentContent(paymentContentToBeAdded);

	}

	/**
	 * creates a new PaymentContent entry in the ofbiz database
	 * 
	 * @param paymentContentToBeAdded
	 *            the PaymentContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentContent(PaymentContent paymentContentToBeAdded) {

		AddPaymentContent com = new AddPaymentContent(paymentContentToBeAdded);
		int usedTicketId;

		synchronized (PaymentContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentContentAdded.class,
				event -> sendPaymentContentChangedMessage(((PaymentContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentContent(HttpServletRequest request) {

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

		PaymentContent paymentContentToBeUpdated = new PaymentContent();

		try {
			paymentContentToBeUpdated = PaymentContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentContent(paymentContentToBeUpdated);

	}

	/**
	 * Updates the PaymentContent with the specific Id
	 * 
	 * @param paymentContentToBeUpdated the PaymentContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentContent(PaymentContent paymentContentToBeUpdated) {

		UpdatePaymentContent com = new UpdatePaymentContent(paymentContentToBeUpdated);

		int usedTicketId;

		synchronized (PaymentContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentContentUpdated.class,
				event -> sendPaymentContentChangedMessage(((PaymentContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentContent from the database
	 * 
	 * @param paymentContentId:
	 *            the id of the PaymentContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentContentById(@RequestParam(value = "paymentContentId") String paymentContentId) {

		DeletePaymentContent com = new DeletePaymentContent(paymentContentId);

		int usedTicketId;

		synchronized (PaymentContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentContentDeleted.class,
				event -> sendPaymentContentChangedMessage(((PaymentContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentContent/\" plus one of the following: "
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
