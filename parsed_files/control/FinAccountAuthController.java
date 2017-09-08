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
import com.skytala.eCommerce.command.AddFinAccountAuth;
import com.skytala.eCommerce.command.DeleteFinAccountAuth;
import com.skytala.eCommerce.command.UpdateFinAccountAuth;
import com.skytala.eCommerce.entity.FinAccountAuth;
import com.skytala.eCommerce.entity.FinAccountAuthMapper;
import com.skytala.eCommerce.event.FinAccountAuthAdded;
import com.skytala.eCommerce.event.FinAccountAuthDeleted;
import com.skytala.eCommerce.event.FinAccountAuthFound;
import com.skytala.eCommerce.event.FinAccountAuthUpdated;
import com.skytala.eCommerce.query.FindFinAccountAuthsBy;

@RestController
@RequestMapping("/api/finAccountAuth")
public class FinAccountAuthController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccountAuth>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountAuthController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccountAuth
	 * @return a List with the FinAccountAuths
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccountAuth> findFinAccountAuthsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountAuthsBy query = new FindFinAccountAuthsBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountAuthController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountAuthFound.class,
				event -> sendFinAccountAuthsFoundMessage(((FinAccountAuthFound) event).getFinAccountAuths(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountAuthsFoundMessage(List<FinAccountAuth> finAccountAuths, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccountAuths);
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
	public boolean createFinAccountAuth(HttpServletRequest request) {

		FinAccountAuth finAccountAuthToBeAdded = new FinAccountAuth();
		try {
			finAccountAuthToBeAdded = FinAccountAuthMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccountAuth(finAccountAuthToBeAdded);

	}

	/**
	 * creates a new FinAccountAuth entry in the ofbiz database
	 * 
	 * @param finAccountAuthToBeAdded
	 *            the FinAccountAuth thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccountAuth(FinAccountAuth finAccountAuthToBeAdded) {

		AddFinAccountAuth com = new AddFinAccountAuth(finAccountAuthToBeAdded);
		int usedTicketId;

		synchronized (FinAccountAuthController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountAuthAdded.class,
				event -> sendFinAccountAuthChangedMessage(((FinAccountAuthAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccountAuth(HttpServletRequest request) {

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

		FinAccountAuth finAccountAuthToBeUpdated = new FinAccountAuth();

		try {
			finAccountAuthToBeUpdated = FinAccountAuthMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccountAuth(finAccountAuthToBeUpdated);

	}

	/**
	 * Updates the FinAccountAuth with the specific Id
	 * 
	 * @param finAccountAuthToBeUpdated the FinAccountAuth thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccountAuth(FinAccountAuth finAccountAuthToBeUpdated) {

		UpdateFinAccountAuth com = new UpdateFinAccountAuth(finAccountAuthToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountAuthController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountAuthUpdated.class,
				event -> sendFinAccountAuthChangedMessage(((FinAccountAuthUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccountAuth from the database
	 * 
	 * @param finAccountAuthId:
	 *            the id of the FinAccountAuth thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountAuthById(@RequestParam(value = "finAccountAuthId") String finAccountAuthId) {

		DeleteFinAccountAuth com = new DeleteFinAccountAuth(finAccountAuthId);

		int usedTicketId;

		synchronized (FinAccountAuthController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountAuthDeleted.class,
				event -> sendFinAccountAuthChangedMessage(((FinAccountAuthDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountAuthChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccountAuth/\" plus one of the following: "
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
