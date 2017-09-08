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
import com.skytala.eCommerce.command.AddGlAccount;
import com.skytala.eCommerce.command.DeleteGlAccount;
import com.skytala.eCommerce.command.UpdateGlAccount;
import com.skytala.eCommerce.entity.GlAccount;
import com.skytala.eCommerce.entity.GlAccountMapper;
import com.skytala.eCommerce.event.GlAccountAdded;
import com.skytala.eCommerce.event.GlAccountDeleted;
import com.skytala.eCommerce.event.GlAccountFound;
import com.skytala.eCommerce.event.GlAccountUpdated;
import com.skytala.eCommerce.query.FindGlAccountsBy;

@RestController
@RequestMapping("/api/glAccount")
public class GlAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccount
	 * @return a List with the GlAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccount> findGlAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountsBy query = new FindGlAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountFound.class,
				event -> sendGlAccountsFoundMessage(((GlAccountFound) event).getGlAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountsFoundMessage(List<GlAccount> glAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccounts);
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
	public boolean createGlAccount(HttpServletRequest request) {

		GlAccount glAccountToBeAdded = new GlAccount();
		try {
			glAccountToBeAdded = GlAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccount(glAccountToBeAdded);

	}

	/**
	 * creates a new GlAccount entry in the ofbiz database
	 * 
	 * @param glAccountToBeAdded
	 *            the GlAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccount(GlAccount glAccountToBeAdded) {

		AddGlAccount com = new AddGlAccount(glAccountToBeAdded);
		int usedTicketId;

		synchronized (GlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountAdded.class,
				event -> sendGlAccountChangedMessage(((GlAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccount(HttpServletRequest request) {

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

		GlAccount glAccountToBeUpdated = new GlAccount();

		try {
			glAccountToBeUpdated = GlAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccount(glAccountToBeUpdated);

	}

	/**
	 * Updates the GlAccount with the specific Id
	 * 
	 * @param glAccountToBeUpdated the GlAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccount(GlAccount glAccountToBeUpdated) {

		UpdateGlAccount com = new UpdateGlAccount(glAccountToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountUpdated.class,
				event -> sendGlAccountChangedMessage(((GlAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccount from the database
	 * 
	 * @param glAccountId:
	 *            the id of the GlAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountById(@RequestParam(value = "glAccountId") String glAccountId) {

		DeleteGlAccount com = new DeleteGlAccount(glAccountId);

		int usedTicketId;

		synchronized (GlAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountDeleted.class,
				event -> sendGlAccountChangedMessage(((GlAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccount/\" plus one of the following: "
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
