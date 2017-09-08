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
import com.skytala.eCommerce.command.AddSalesOpportunityRole;
import com.skytala.eCommerce.command.DeleteSalesOpportunityRole;
import com.skytala.eCommerce.command.UpdateSalesOpportunityRole;
import com.skytala.eCommerce.entity.SalesOpportunityRole;
import com.skytala.eCommerce.entity.SalesOpportunityRoleMapper;
import com.skytala.eCommerce.event.SalesOpportunityRoleAdded;
import com.skytala.eCommerce.event.SalesOpportunityRoleDeleted;
import com.skytala.eCommerce.event.SalesOpportunityRoleFound;
import com.skytala.eCommerce.event.SalesOpportunityRoleUpdated;
import com.skytala.eCommerce.query.FindSalesOpportunityRolesBy;

@RestController
@RequestMapping("/api/salesOpportunityRole")
public class SalesOpportunityRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SalesOpportunityRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SalesOpportunityRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SalesOpportunityRole
	 * @return a List with the SalesOpportunityRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SalesOpportunityRole> findSalesOpportunityRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSalesOpportunityRolesBy query = new FindSalesOpportunityRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (SalesOpportunityRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityRoleFound.class,
				event -> sendSalesOpportunityRolesFoundMessage(((SalesOpportunityRoleFound) event).getSalesOpportunityRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSalesOpportunityRolesFoundMessage(List<SalesOpportunityRole> salesOpportunityRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, salesOpportunityRoles);
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
	public boolean createSalesOpportunityRole(HttpServletRequest request) {

		SalesOpportunityRole salesOpportunityRoleToBeAdded = new SalesOpportunityRole();
		try {
			salesOpportunityRoleToBeAdded = SalesOpportunityRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSalesOpportunityRole(salesOpportunityRoleToBeAdded);

	}

	/**
	 * creates a new SalesOpportunityRole entry in the ofbiz database
	 * 
	 * @param salesOpportunityRoleToBeAdded
	 *            the SalesOpportunityRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSalesOpportunityRole(SalesOpportunityRole salesOpportunityRoleToBeAdded) {

		AddSalesOpportunityRole com = new AddSalesOpportunityRole(salesOpportunityRoleToBeAdded);
		int usedTicketId;

		synchronized (SalesOpportunityRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityRoleAdded.class,
				event -> sendSalesOpportunityRoleChangedMessage(((SalesOpportunityRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSalesOpportunityRole(HttpServletRequest request) {

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

		SalesOpportunityRole salesOpportunityRoleToBeUpdated = new SalesOpportunityRole();

		try {
			salesOpportunityRoleToBeUpdated = SalesOpportunityRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSalesOpportunityRole(salesOpportunityRoleToBeUpdated);

	}

	/**
	 * Updates the SalesOpportunityRole with the specific Id
	 * 
	 * @param salesOpportunityRoleToBeUpdated the SalesOpportunityRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSalesOpportunityRole(SalesOpportunityRole salesOpportunityRoleToBeUpdated) {

		UpdateSalesOpportunityRole com = new UpdateSalesOpportunityRole(salesOpportunityRoleToBeUpdated);

		int usedTicketId;

		synchronized (SalesOpportunityRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityRoleUpdated.class,
				event -> sendSalesOpportunityRoleChangedMessage(((SalesOpportunityRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SalesOpportunityRole from the database
	 * 
	 * @param salesOpportunityRoleId:
	 *            the id of the SalesOpportunityRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesalesOpportunityRoleById(@RequestParam(value = "salesOpportunityRoleId") String salesOpportunityRoleId) {

		DeleteSalesOpportunityRole com = new DeleteSalesOpportunityRole(salesOpportunityRoleId);

		int usedTicketId;

		synchronized (SalesOpportunityRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SalesOpportunityRoleDeleted.class,
				event -> sendSalesOpportunityRoleChangedMessage(((SalesOpportunityRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSalesOpportunityRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/salesOpportunityRole/\" plus one of the following: "
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
