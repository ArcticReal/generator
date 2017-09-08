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
import com.skytala.eCommerce.command.AddPayrollPreference;
import com.skytala.eCommerce.command.DeletePayrollPreference;
import com.skytala.eCommerce.command.UpdatePayrollPreference;
import com.skytala.eCommerce.entity.PayrollPreference;
import com.skytala.eCommerce.entity.PayrollPreferenceMapper;
import com.skytala.eCommerce.event.PayrollPreferenceAdded;
import com.skytala.eCommerce.event.PayrollPreferenceDeleted;
import com.skytala.eCommerce.event.PayrollPreferenceFound;
import com.skytala.eCommerce.event.PayrollPreferenceUpdated;
import com.skytala.eCommerce.query.FindPayrollPreferencesBy;

@RestController
@RequestMapping("/api/payrollPreference")
public class PayrollPreferenceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PayrollPreference>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PayrollPreferenceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PayrollPreference
	 * @return a List with the PayrollPreferences
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PayrollPreference> findPayrollPreferencesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPayrollPreferencesBy query = new FindPayrollPreferencesBy(allRequestParams);

		int usedTicketId;

		synchronized (PayrollPreferenceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayrollPreferenceFound.class,
				event -> sendPayrollPreferencesFoundMessage(((PayrollPreferenceFound) event).getPayrollPreferences(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPayrollPreferencesFoundMessage(List<PayrollPreference> payrollPreferences, int usedTicketId) {
		queryReturnVal.put(usedTicketId, payrollPreferences);
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
	public boolean createPayrollPreference(HttpServletRequest request) {

		PayrollPreference payrollPreferenceToBeAdded = new PayrollPreference();
		try {
			payrollPreferenceToBeAdded = PayrollPreferenceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPayrollPreference(payrollPreferenceToBeAdded);

	}

	/**
	 * creates a new PayrollPreference entry in the ofbiz database
	 * 
	 * @param payrollPreferenceToBeAdded
	 *            the PayrollPreference thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPayrollPreference(PayrollPreference payrollPreferenceToBeAdded) {

		AddPayrollPreference com = new AddPayrollPreference(payrollPreferenceToBeAdded);
		int usedTicketId;

		synchronized (PayrollPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayrollPreferenceAdded.class,
				event -> sendPayrollPreferenceChangedMessage(((PayrollPreferenceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePayrollPreference(HttpServletRequest request) {

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

		PayrollPreference payrollPreferenceToBeUpdated = new PayrollPreference();

		try {
			payrollPreferenceToBeUpdated = PayrollPreferenceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePayrollPreference(payrollPreferenceToBeUpdated);

	}

	/**
	 * Updates the PayrollPreference with the specific Id
	 * 
	 * @param payrollPreferenceToBeUpdated the PayrollPreference thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePayrollPreference(PayrollPreference payrollPreferenceToBeUpdated) {

		UpdatePayrollPreference com = new UpdatePayrollPreference(payrollPreferenceToBeUpdated);

		int usedTicketId;

		synchronized (PayrollPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayrollPreferenceUpdated.class,
				event -> sendPayrollPreferenceChangedMessage(((PayrollPreferenceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PayrollPreference from the database
	 * 
	 * @param payrollPreferenceId:
	 *            the id of the PayrollPreference thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepayrollPreferenceById(@RequestParam(value = "payrollPreferenceId") String payrollPreferenceId) {

		DeletePayrollPreference com = new DeletePayrollPreference(payrollPreferenceId);

		int usedTicketId;

		synchronized (PayrollPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayrollPreferenceDeleted.class,
				event -> sendPayrollPreferenceChangedMessage(((PayrollPreferenceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPayrollPreferenceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/payrollPreference/\" plus one of the following: "
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
