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
import com.skytala.eCommerce.command.AddPaymentGroup;
import com.skytala.eCommerce.command.DeletePaymentGroup;
import com.skytala.eCommerce.command.UpdatePaymentGroup;
import com.skytala.eCommerce.entity.PaymentGroup;
import com.skytala.eCommerce.entity.PaymentGroupMapper;
import com.skytala.eCommerce.event.PaymentGroupAdded;
import com.skytala.eCommerce.event.PaymentGroupDeleted;
import com.skytala.eCommerce.event.PaymentGroupFound;
import com.skytala.eCommerce.event.PaymentGroupUpdated;
import com.skytala.eCommerce.query.FindPaymentGroupsBy;

@RestController
@RequestMapping("/api/paymentGroup")
public class PaymentGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGroup
	 * @return a List with the PaymentGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGroup> findPaymentGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGroupsBy query = new FindPaymentGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupFound.class,
				event -> sendPaymentGroupsFoundMessage(((PaymentGroupFound) event).getPaymentGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGroupsFoundMessage(List<PaymentGroup> paymentGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGroups);
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
	public boolean createPaymentGroup(HttpServletRequest request) {

		PaymentGroup paymentGroupToBeAdded = new PaymentGroup();
		try {
			paymentGroupToBeAdded = PaymentGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGroup(paymentGroupToBeAdded);

	}

	/**
	 * creates a new PaymentGroup entry in the ofbiz database
	 * 
	 * @param paymentGroupToBeAdded
	 *            the PaymentGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGroup(PaymentGroup paymentGroupToBeAdded) {

		AddPaymentGroup com = new AddPaymentGroup(paymentGroupToBeAdded);
		int usedTicketId;

		synchronized (PaymentGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupAdded.class,
				event -> sendPaymentGroupChangedMessage(((PaymentGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGroup(HttpServletRequest request) {

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

		PaymentGroup paymentGroupToBeUpdated = new PaymentGroup();

		try {
			paymentGroupToBeUpdated = PaymentGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGroup(paymentGroupToBeUpdated);

	}

	/**
	 * Updates the PaymentGroup with the specific Id
	 * 
	 * @param paymentGroupToBeUpdated the PaymentGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGroup(PaymentGroup paymentGroupToBeUpdated) {

		UpdatePaymentGroup com = new UpdatePaymentGroup(paymentGroupToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupUpdated.class,
				event -> sendPaymentGroupChangedMessage(((PaymentGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGroup from the database
	 * 
	 * @param paymentGroupId:
	 *            the id of the PaymentGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGroupById(@RequestParam(value = "paymentGroupId") String paymentGroupId) {

		DeletePaymentGroup com = new DeletePaymentGroup(paymentGroupId);

		int usedTicketId;

		synchronized (PaymentGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupDeleted.class,
				event -> sendPaymentGroupChangedMessage(((PaymentGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGroup/\" plus one of the following: "
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
