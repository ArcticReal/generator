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
import com.skytala.eCommerce.command.AddPaymentBudgetAllocation;
import com.skytala.eCommerce.command.DeletePaymentBudgetAllocation;
import com.skytala.eCommerce.command.UpdatePaymentBudgetAllocation;
import com.skytala.eCommerce.entity.PaymentBudgetAllocation;
import com.skytala.eCommerce.entity.PaymentBudgetAllocationMapper;
import com.skytala.eCommerce.event.PaymentBudgetAllocationAdded;
import com.skytala.eCommerce.event.PaymentBudgetAllocationDeleted;
import com.skytala.eCommerce.event.PaymentBudgetAllocationFound;
import com.skytala.eCommerce.event.PaymentBudgetAllocationUpdated;
import com.skytala.eCommerce.query.FindPaymentBudgetAllocationsBy;

@RestController
@RequestMapping("/api/paymentBudgetAllocation")
public class PaymentBudgetAllocationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentBudgetAllocation>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentBudgetAllocationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentBudgetAllocation
	 * @return a List with the PaymentBudgetAllocations
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentBudgetAllocation> findPaymentBudgetAllocationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentBudgetAllocationsBy query = new FindPaymentBudgetAllocationsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentBudgetAllocationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentBudgetAllocationFound.class,
				event -> sendPaymentBudgetAllocationsFoundMessage(((PaymentBudgetAllocationFound) event).getPaymentBudgetAllocations(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentBudgetAllocationsFoundMessage(List<PaymentBudgetAllocation> paymentBudgetAllocations, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentBudgetAllocations);
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
	public boolean createPaymentBudgetAllocation(HttpServletRequest request) {

		PaymentBudgetAllocation paymentBudgetAllocationToBeAdded = new PaymentBudgetAllocation();
		try {
			paymentBudgetAllocationToBeAdded = PaymentBudgetAllocationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentBudgetAllocation(paymentBudgetAllocationToBeAdded);

	}

	/**
	 * creates a new PaymentBudgetAllocation entry in the ofbiz database
	 * 
	 * @param paymentBudgetAllocationToBeAdded
	 *            the PaymentBudgetAllocation thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentBudgetAllocation(PaymentBudgetAllocation paymentBudgetAllocationToBeAdded) {

		AddPaymentBudgetAllocation com = new AddPaymentBudgetAllocation(paymentBudgetAllocationToBeAdded);
		int usedTicketId;

		synchronized (PaymentBudgetAllocationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentBudgetAllocationAdded.class,
				event -> sendPaymentBudgetAllocationChangedMessage(((PaymentBudgetAllocationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentBudgetAllocation(HttpServletRequest request) {

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

		PaymentBudgetAllocation paymentBudgetAllocationToBeUpdated = new PaymentBudgetAllocation();

		try {
			paymentBudgetAllocationToBeUpdated = PaymentBudgetAllocationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentBudgetAllocation(paymentBudgetAllocationToBeUpdated);

	}

	/**
	 * Updates the PaymentBudgetAllocation with the specific Id
	 * 
	 * @param paymentBudgetAllocationToBeUpdated the PaymentBudgetAllocation thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentBudgetAllocation(PaymentBudgetAllocation paymentBudgetAllocationToBeUpdated) {

		UpdatePaymentBudgetAllocation com = new UpdatePaymentBudgetAllocation(paymentBudgetAllocationToBeUpdated);

		int usedTicketId;

		synchronized (PaymentBudgetAllocationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentBudgetAllocationUpdated.class,
				event -> sendPaymentBudgetAllocationChangedMessage(((PaymentBudgetAllocationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentBudgetAllocation from the database
	 * 
	 * @param paymentBudgetAllocationId:
	 *            the id of the PaymentBudgetAllocation thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentBudgetAllocationById(@RequestParam(value = "paymentBudgetAllocationId") String paymentBudgetAllocationId) {

		DeletePaymentBudgetAllocation com = new DeletePaymentBudgetAllocation(paymentBudgetAllocationId);

		int usedTicketId;

		synchronized (PaymentBudgetAllocationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentBudgetAllocationDeleted.class,
				event -> sendPaymentBudgetAllocationChangedMessage(((PaymentBudgetAllocationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentBudgetAllocationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentBudgetAllocation/\" plus one of the following: "
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
