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
import com.skytala.eCommerce.command.AddPaymentGroupMember;
import com.skytala.eCommerce.command.DeletePaymentGroupMember;
import com.skytala.eCommerce.command.UpdatePaymentGroupMember;
import com.skytala.eCommerce.entity.PaymentGroupMember;
import com.skytala.eCommerce.entity.PaymentGroupMemberMapper;
import com.skytala.eCommerce.event.PaymentGroupMemberAdded;
import com.skytala.eCommerce.event.PaymentGroupMemberDeleted;
import com.skytala.eCommerce.event.PaymentGroupMemberFound;
import com.skytala.eCommerce.event.PaymentGroupMemberUpdated;
import com.skytala.eCommerce.query.FindPaymentGroupMembersBy;

@RestController
@RequestMapping("/api/paymentGroupMember")
public class PaymentGroupMemberController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PaymentGroupMember>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PaymentGroupMemberController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PaymentGroupMember
	 * @return a List with the PaymentGroupMembers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PaymentGroupMember> findPaymentGroupMembersBy(@RequestParam Map<String, String> allRequestParams) {

		FindPaymentGroupMembersBy query = new FindPaymentGroupMembersBy(allRequestParams);

		int usedTicketId;

		synchronized (PaymentGroupMemberController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupMemberFound.class,
				event -> sendPaymentGroupMembersFoundMessage(((PaymentGroupMemberFound) event).getPaymentGroupMembers(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPaymentGroupMembersFoundMessage(List<PaymentGroupMember> paymentGroupMembers, int usedTicketId) {
		queryReturnVal.put(usedTicketId, paymentGroupMembers);
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
	public boolean createPaymentGroupMember(HttpServletRequest request) {

		PaymentGroupMember paymentGroupMemberToBeAdded = new PaymentGroupMember();
		try {
			paymentGroupMemberToBeAdded = PaymentGroupMemberMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPaymentGroupMember(paymentGroupMemberToBeAdded);

	}

	/**
	 * creates a new PaymentGroupMember entry in the ofbiz database
	 * 
	 * @param paymentGroupMemberToBeAdded
	 *            the PaymentGroupMember thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPaymentGroupMember(PaymentGroupMember paymentGroupMemberToBeAdded) {

		AddPaymentGroupMember com = new AddPaymentGroupMember(paymentGroupMemberToBeAdded);
		int usedTicketId;

		synchronized (PaymentGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupMemberAdded.class,
				event -> sendPaymentGroupMemberChangedMessage(((PaymentGroupMemberAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePaymentGroupMember(HttpServletRequest request) {

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

		PaymentGroupMember paymentGroupMemberToBeUpdated = new PaymentGroupMember();

		try {
			paymentGroupMemberToBeUpdated = PaymentGroupMemberMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePaymentGroupMember(paymentGroupMemberToBeUpdated);

	}

	/**
	 * Updates the PaymentGroupMember with the specific Id
	 * 
	 * @param paymentGroupMemberToBeUpdated the PaymentGroupMember thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePaymentGroupMember(PaymentGroupMember paymentGroupMemberToBeUpdated) {

		UpdatePaymentGroupMember com = new UpdatePaymentGroupMember(paymentGroupMemberToBeUpdated);

		int usedTicketId;

		synchronized (PaymentGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupMemberUpdated.class,
				event -> sendPaymentGroupMemberChangedMessage(((PaymentGroupMemberUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PaymentGroupMember from the database
	 * 
	 * @param paymentGroupMemberId:
	 *            the id of the PaymentGroupMember thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepaymentGroupMemberById(@RequestParam(value = "paymentGroupMemberId") String paymentGroupMemberId) {

		DeletePaymentGroupMember com = new DeletePaymentGroupMember(paymentGroupMemberId);

		int usedTicketId;

		synchronized (PaymentGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PaymentGroupMemberDeleted.class,
				event -> sendPaymentGroupMemberChangedMessage(((PaymentGroupMemberDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPaymentGroupMemberChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/paymentGroupMember/\" plus one of the following: "
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
