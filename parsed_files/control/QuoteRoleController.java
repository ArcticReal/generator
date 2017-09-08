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
import com.skytala.eCommerce.command.AddQuoteRole;
import com.skytala.eCommerce.command.DeleteQuoteRole;
import com.skytala.eCommerce.command.UpdateQuoteRole;
import com.skytala.eCommerce.entity.QuoteRole;
import com.skytala.eCommerce.entity.QuoteRoleMapper;
import com.skytala.eCommerce.event.QuoteRoleAdded;
import com.skytala.eCommerce.event.QuoteRoleDeleted;
import com.skytala.eCommerce.event.QuoteRoleFound;
import com.skytala.eCommerce.event.QuoteRoleUpdated;
import com.skytala.eCommerce.query.FindQuoteRolesBy;

@RestController
@RequestMapping("/api/quoteRole")
public class QuoteRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteRole
	 * @return a List with the QuoteRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteRole> findQuoteRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteRolesBy query = new FindQuoteRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteRoleFound.class,
				event -> sendQuoteRolesFoundMessage(((QuoteRoleFound) event).getQuoteRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteRolesFoundMessage(List<QuoteRole> quoteRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteRoles);
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
	public boolean createQuoteRole(HttpServletRequest request) {

		QuoteRole quoteRoleToBeAdded = new QuoteRole();
		try {
			quoteRoleToBeAdded = QuoteRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteRole(quoteRoleToBeAdded);

	}

	/**
	 * creates a new QuoteRole entry in the ofbiz database
	 * 
	 * @param quoteRoleToBeAdded
	 *            the QuoteRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteRole(QuoteRole quoteRoleToBeAdded) {

		AddQuoteRole com = new AddQuoteRole(quoteRoleToBeAdded);
		int usedTicketId;

		synchronized (QuoteRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteRoleAdded.class,
				event -> sendQuoteRoleChangedMessage(((QuoteRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteRole(HttpServletRequest request) {

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

		QuoteRole quoteRoleToBeUpdated = new QuoteRole();

		try {
			quoteRoleToBeUpdated = QuoteRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteRole(quoteRoleToBeUpdated);

	}

	/**
	 * Updates the QuoteRole with the specific Id
	 * 
	 * @param quoteRoleToBeUpdated the QuoteRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteRole(QuoteRole quoteRoleToBeUpdated) {

		UpdateQuoteRole com = new UpdateQuoteRole(quoteRoleToBeUpdated);

		int usedTicketId;

		synchronized (QuoteRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteRoleUpdated.class,
				event -> sendQuoteRoleChangedMessage(((QuoteRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteRole from the database
	 * 
	 * @param quoteRoleId:
	 *            the id of the QuoteRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteRoleById(@RequestParam(value = "quoteRoleId") String quoteRoleId) {

		DeleteQuoteRole com = new DeleteQuoteRole(quoteRoleId);

		int usedTicketId;

		synchronized (QuoteRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteRoleDeleted.class,
				event -> sendQuoteRoleChangedMessage(((QuoteRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteRole/\" plus one of the following: "
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
