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
import com.skytala.eCommerce.command.AddVarianceReasonGlAccount;
import com.skytala.eCommerce.command.DeleteVarianceReasonGlAccount;
import com.skytala.eCommerce.command.UpdateVarianceReasonGlAccount;
import com.skytala.eCommerce.entity.VarianceReasonGlAccount;
import com.skytala.eCommerce.entity.VarianceReasonGlAccountMapper;
import com.skytala.eCommerce.event.VarianceReasonGlAccountAdded;
import com.skytala.eCommerce.event.VarianceReasonGlAccountDeleted;
import com.skytala.eCommerce.event.VarianceReasonGlAccountFound;
import com.skytala.eCommerce.event.VarianceReasonGlAccountUpdated;
import com.skytala.eCommerce.query.FindVarianceReasonGlAccountsBy;

@RestController
@RequestMapping("/api/varianceReasonGlAccount")
public class VarianceReasonGlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<VarianceReasonGlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public VarianceReasonGlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a VarianceReasonGlAccount
	 * @return a List with the VarianceReasonGlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<VarianceReasonGlAccount> findVarianceReasonGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindVarianceReasonGlAccountsBy query = new FindVarianceReasonGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (VarianceReasonGlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VarianceReasonGlAccountFound.class,
				event -> sendVarianceReasonGlAccountsFoundMessage(((VarianceReasonGlAccountFound) event).getVarianceReasonGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendVarianceReasonGlAccountsFoundMessage(List<VarianceReasonGlAccount> varianceReasonGlAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, varianceReasonGlAccounts);
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
	public boolean createVarianceReasonGlAccount(HttpServletRequest request) {

		VarianceReasonGlAccount varianceReasonGlAccountToBeAdded = new VarianceReasonGlAccount();
		try {
			varianceReasonGlAccountToBeAdded = VarianceReasonGlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createVarianceReasonGlAccount(varianceReasonGlAccountToBeAdded);

	}

	/**
	 * creates a new VarianceReasonGlAccount entry in the ofbiz database
	 * 
	 * @param varianceReasonGlAccountToBeAdded
	 *            the VarianceReasonGlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createVarianceReasonGlAccount(VarianceReasonGlAccount varianceReasonGlAccountToBeAdded) {

		AddVarianceReasonGlAccount com = new AddVarianceReasonGlAccount(varianceReasonGlAccountToBeAdded);
		int usedTicketId;

		synchronized (VarianceReasonGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VarianceReasonGlAccountAdded.class,
				event -> sendVarianceReasonGlAccountChangedMessage(((VarianceReasonGlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateVarianceReasonGlAccount(HttpServletRequest request) {

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

		VarianceReasonGlAccount varianceReasonGlAccountToBeUpdated = new VarianceReasonGlAccount();

		try {
			varianceReasonGlAccountToBeUpdated = VarianceReasonGlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateVarianceReasonGlAccount(varianceReasonGlAccountToBeUpdated);

	}

	/**
	 * Updates the VarianceReasonGlAccount with the specific Id
	 * 
	 * @param varianceReasonGlAccountToBeUpdated the VarianceReasonGlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateVarianceReasonGlAccount(VarianceReasonGlAccount varianceReasonGlAccountToBeUpdated) {

		UpdateVarianceReasonGlAccount com = new UpdateVarianceReasonGlAccount(varianceReasonGlAccountToBeUpdated);

		int usedTicketId;

		synchronized (VarianceReasonGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VarianceReasonGlAccountUpdated.class,
				event -> sendVarianceReasonGlAccountChangedMessage(((VarianceReasonGlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a VarianceReasonGlAccount from the database
	 * 
	 * @param varianceReasonGlAccountId:
	 *            the id of the VarianceReasonGlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletevarianceReasonGlAccountById(@RequestParam(value = "varianceReasonGlAccountId") String varianceReasonGlAccountId) {

		DeleteVarianceReasonGlAccount com = new DeleteVarianceReasonGlAccount(varianceReasonGlAccountId);

		int usedTicketId;

		synchronized (VarianceReasonGlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VarianceReasonGlAccountDeleted.class,
				event -> sendVarianceReasonGlAccountChangedMessage(((VarianceReasonGlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendVarianceReasonGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/varianceReasonGlAccount/\" plus one of the following: "
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
