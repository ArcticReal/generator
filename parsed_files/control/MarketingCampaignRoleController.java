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
import com.skytala.eCommerce.command.AddMarketingCampaignRole;
import com.skytala.eCommerce.command.DeleteMarketingCampaignRole;
import com.skytala.eCommerce.command.UpdateMarketingCampaignRole;
import com.skytala.eCommerce.entity.MarketingCampaignRole;
import com.skytala.eCommerce.entity.MarketingCampaignRoleMapper;
import com.skytala.eCommerce.event.MarketingCampaignRoleAdded;
import com.skytala.eCommerce.event.MarketingCampaignRoleDeleted;
import com.skytala.eCommerce.event.MarketingCampaignRoleFound;
import com.skytala.eCommerce.event.MarketingCampaignRoleUpdated;
import com.skytala.eCommerce.query.FindMarketingCampaignRolesBy;

@RestController
@RequestMapping("/api/marketingCampaignRole")
public class MarketingCampaignRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<MarketingCampaignRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public MarketingCampaignRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a MarketingCampaignRole
	 * @return a List with the MarketingCampaignRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<MarketingCampaignRole> findMarketingCampaignRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindMarketingCampaignRolesBy query = new FindMarketingCampaignRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (MarketingCampaignRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignRoleFound.class,
				event -> sendMarketingCampaignRolesFoundMessage(((MarketingCampaignRoleFound) event).getMarketingCampaignRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendMarketingCampaignRolesFoundMessage(List<MarketingCampaignRole> marketingCampaignRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, marketingCampaignRoles);
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
	public boolean createMarketingCampaignRole(HttpServletRequest request) {

		MarketingCampaignRole marketingCampaignRoleToBeAdded = new MarketingCampaignRole();
		try {
			marketingCampaignRoleToBeAdded = MarketingCampaignRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createMarketingCampaignRole(marketingCampaignRoleToBeAdded);

	}

	/**
	 * creates a new MarketingCampaignRole entry in the ofbiz database
	 * 
	 * @param marketingCampaignRoleToBeAdded
	 *            the MarketingCampaignRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createMarketingCampaignRole(MarketingCampaignRole marketingCampaignRoleToBeAdded) {

		AddMarketingCampaignRole com = new AddMarketingCampaignRole(marketingCampaignRoleToBeAdded);
		int usedTicketId;

		synchronized (MarketingCampaignRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignRoleAdded.class,
				event -> sendMarketingCampaignRoleChangedMessage(((MarketingCampaignRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateMarketingCampaignRole(HttpServletRequest request) {

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

		MarketingCampaignRole marketingCampaignRoleToBeUpdated = new MarketingCampaignRole();

		try {
			marketingCampaignRoleToBeUpdated = MarketingCampaignRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateMarketingCampaignRole(marketingCampaignRoleToBeUpdated);

	}

	/**
	 * Updates the MarketingCampaignRole with the specific Id
	 * 
	 * @param marketingCampaignRoleToBeUpdated the MarketingCampaignRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateMarketingCampaignRole(MarketingCampaignRole marketingCampaignRoleToBeUpdated) {

		UpdateMarketingCampaignRole com = new UpdateMarketingCampaignRole(marketingCampaignRoleToBeUpdated);

		int usedTicketId;

		synchronized (MarketingCampaignRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignRoleUpdated.class,
				event -> sendMarketingCampaignRoleChangedMessage(((MarketingCampaignRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a MarketingCampaignRole from the database
	 * 
	 * @param marketingCampaignRoleId:
	 *            the id of the MarketingCampaignRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletemarketingCampaignRoleById(@RequestParam(value = "marketingCampaignRoleId") String marketingCampaignRoleId) {

		DeleteMarketingCampaignRole com = new DeleteMarketingCampaignRole(marketingCampaignRoleId);

		int usedTicketId;

		synchronized (MarketingCampaignRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MarketingCampaignRoleDeleted.class,
				event -> sendMarketingCampaignRoleChangedMessage(((MarketingCampaignRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendMarketingCampaignRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/marketingCampaignRole/\" plus one of the following: "
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
