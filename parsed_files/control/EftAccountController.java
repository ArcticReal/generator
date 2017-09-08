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
import com.skytala.eCommerce.command.AddEftAccount;
import com.skytala.eCommerce.command.DeleteEftAccount;
import com.skytala.eCommerce.command.UpdateEftAccount;
import com.skytala.eCommerce.entity.EftAccount;
import com.skytala.eCommerce.entity.EftAccountMapper;
import com.skytala.eCommerce.event.EftAccountAdded;
import com.skytala.eCommerce.event.EftAccountDeleted;
import com.skytala.eCommerce.event.EftAccountFound;
import com.skytala.eCommerce.event.EftAccountUpdated;
import com.skytala.eCommerce.query.FindEftAccountsBy;

@RestController
@RequestMapping("/api/eftAccount")
public class EftAccountController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EftAccount>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EftAccountController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EftAccount
	 * @return a List with the EftAccounts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EftAccount> findEftAccountsBy(@RequestParam Map<String, String> allRequestParams) {

		FindEftAccountsBy query = new FindEftAccountsBy(allRequestParams);

		int usedTicketId;

		synchronized (EftAccountController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EftAccountFound.class,
				event -> sendEftAccountsFoundMessage(((EftAccountFound) event).getEftAccounts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEftAccountsFoundMessage(List<EftAccount> eftAccounts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, eftAccounts);
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
	public boolean createEftAccount(HttpServletRequest request) {

		EftAccount eftAccountToBeAdded = new EftAccount();
		try {
			eftAccountToBeAdded = EftAccountMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEftAccount(eftAccountToBeAdded);

	}

	/**
	 * creates a new EftAccount entry in the ofbiz database
	 * 
	 * @param eftAccountToBeAdded
	 *            the EftAccount thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEftAccount(EftAccount eftAccountToBeAdded) {

		AddEftAccount com = new AddEftAccount(eftAccountToBeAdded);
		int usedTicketId;

		synchronized (EftAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EftAccountAdded.class,
				event -> sendEftAccountChangedMessage(((EftAccountAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEftAccount(HttpServletRequest request) {

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

		EftAccount eftAccountToBeUpdated = new EftAccount();

		try {
			eftAccountToBeUpdated = EftAccountMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEftAccount(eftAccountToBeUpdated);

	}

	/**
	 * Updates the EftAccount with the specific Id
	 * 
	 * @param eftAccountToBeUpdated the EftAccount thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEftAccount(EftAccount eftAccountToBeUpdated) {

		UpdateEftAccount com = new UpdateEftAccount(eftAccountToBeUpdated);

		int usedTicketId;

		synchronized (EftAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EftAccountUpdated.class,
				event -> sendEftAccountChangedMessage(((EftAccountUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EftAccount from the database
	 * 
	 * @param eftAccountId:
	 *            the id of the EftAccount thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteeftAccountById(@RequestParam(value = "eftAccountId") String eftAccountId) {

		DeleteEftAccount com = new DeleteEftAccount(eftAccountId);

		int usedTicketId;

		synchronized (EftAccountController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EftAccountDeleted.class,
				event -> sendEftAccountChangedMessage(((EftAccountDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEftAccountChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/eftAccount/\" plus one of the following: "
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
