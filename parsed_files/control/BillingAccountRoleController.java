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
import com.skytala.eCommerce.command.AddBillingAccountRole;
import com.skytala.eCommerce.command.DeleteBillingAccountRole;
import com.skytala.eCommerce.command.UpdateBillingAccountRole;
import com.skytala.eCommerce.entity.BillingAccountRole;
import com.skytala.eCommerce.entity.BillingAccountRoleMapper;
import com.skytala.eCommerce.event.BillingAccountRoleAdded;
import com.skytala.eCommerce.event.BillingAccountRoleDeleted;
import com.skytala.eCommerce.event.BillingAccountRoleFound;
import com.skytala.eCommerce.event.BillingAccountRoleUpdated;
import com.skytala.eCommerce.query.FindBillingAccountRolesBy;

@RestController
@RequestMapping("/api/billingAccountRole")
public class BillingAccountRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BillingAccountRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BillingAccountRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BillingAccountRole
	 * @return a List with the BillingAccountRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BillingAccountRole> findBillingAccountRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindBillingAccountRolesBy query = new FindBillingAccountRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (BillingAccountRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountRoleFound.class,
				event -> sendBillingAccountRolesFoundMessage(((BillingAccountRoleFound) event).getBillingAccountRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBillingAccountRolesFoundMessage(List<BillingAccountRole> billingAccountRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, billingAccountRoles);
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
	public boolean createBillingAccountRole(HttpServletRequest request) {

		BillingAccountRole billingAccountRoleToBeAdded = new BillingAccountRole();
		try {
			billingAccountRoleToBeAdded = BillingAccountRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBillingAccountRole(billingAccountRoleToBeAdded);

	}

	/**
	 * creates a new BillingAccountRole entry in the ofbiz database
	 * 
	 * @param billingAccountRoleToBeAdded
	 *            the BillingAccountRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBillingAccountRole(BillingAccountRole billingAccountRoleToBeAdded) {

		AddBillingAccountRole com = new AddBillingAccountRole(billingAccountRoleToBeAdded);
		int usedTicketId;

		synchronized (BillingAccountRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountRoleAdded.class,
				event -> sendBillingAccountRoleChangedMessage(((BillingAccountRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBillingAccountRole(HttpServletRequest request) {

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

		BillingAccountRole billingAccountRoleToBeUpdated = new BillingAccountRole();

		try {
			billingAccountRoleToBeUpdated = BillingAccountRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBillingAccountRole(billingAccountRoleToBeUpdated);

	}

	/**
	 * Updates the BillingAccountRole with the specific Id
	 * 
	 * @param billingAccountRoleToBeUpdated the BillingAccountRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBillingAccountRole(BillingAccountRole billingAccountRoleToBeUpdated) {

		UpdateBillingAccountRole com = new UpdateBillingAccountRole(billingAccountRoleToBeUpdated);

		int usedTicketId;

		synchronized (BillingAccountRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountRoleUpdated.class,
				event -> sendBillingAccountRoleChangedMessage(((BillingAccountRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BillingAccountRole from the database
	 * 
	 * @param billingAccountRoleId:
	 *            the id of the BillingAccountRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebillingAccountRoleById(@RequestParam(value = "billingAccountRoleId") String billingAccountRoleId) {

		DeleteBillingAccountRole com = new DeleteBillingAccountRole(billingAccountRoleId);

		int usedTicketId;

		synchronized (BillingAccountRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountRoleDeleted.class,
				event -> sendBillingAccountRoleChangedMessage(((BillingAccountRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBillingAccountRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/billingAccountRole/\" plus one of the following: "
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
