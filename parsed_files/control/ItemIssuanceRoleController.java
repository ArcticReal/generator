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
import com.skytala.eCommerce.command.AddItemIssuanceRole;
import com.skytala.eCommerce.command.DeleteItemIssuanceRole;
import com.skytala.eCommerce.command.UpdateItemIssuanceRole;
import com.skytala.eCommerce.entity.ItemIssuanceRole;
import com.skytala.eCommerce.entity.ItemIssuanceRoleMapper;
import com.skytala.eCommerce.event.ItemIssuanceRoleAdded;
import com.skytala.eCommerce.event.ItemIssuanceRoleDeleted;
import com.skytala.eCommerce.event.ItemIssuanceRoleFound;
import com.skytala.eCommerce.event.ItemIssuanceRoleUpdated;
import com.skytala.eCommerce.query.FindItemIssuanceRolesBy;

@RestController
@RequestMapping("/api/itemIssuanceRole")
public class ItemIssuanceRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ItemIssuanceRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ItemIssuanceRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ItemIssuanceRole
	 * @return a List with the ItemIssuanceRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ItemIssuanceRole> findItemIssuanceRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindItemIssuanceRolesBy query = new FindItemIssuanceRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (ItemIssuanceRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ItemIssuanceRoleFound.class,
				event -> sendItemIssuanceRolesFoundMessage(((ItemIssuanceRoleFound) event).getItemIssuanceRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendItemIssuanceRolesFoundMessage(List<ItemIssuanceRole> itemIssuanceRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, itemIssuanceRoles);
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
	public boolean createItemIssuanceRole(HttpServletRequest request) {

		ItemIssuanceRole itemIssuanceRoleToBeAdded = new ItemIssuanceRole();
		try {
			itemIssuanceRoleToBeAdded = ItemIssuanceRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createItemIssuanceRole(itemIssuanceRoleToBeAdded);

	}

	/**
	 * creates a new ItemIssuanceRole entry in the ofbiz database
	 * 
	 * @param itemIssuanceRoleToBeAdded
	 *            the ItemIssuanceRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createItemIssuanceRole(ItemIssuanceRole itemIssuanceRoleToBeAdded) {

		AddItemIssuanceRole com = new AddItemIssuanceRole(itemIssuanceRoleToBeAdded);
		int usedTicketId;

		synchronized (ItemIssuanceRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ItemIssuanceRoleAdded.class,
				event -> sendItemIssuanceRoleChangedMessage(((ItemIssuanceRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateItemIssuanceRole(HttpServletRequest request) {

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

		ItemIssuanceRole itemIssuanceRoleToBeUpdated = new ItemIssuanceRole();

		try {
			itemIssuanceRoleToBeUpdated = ItemIssuanceRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateItemIssuanceRole(itemIssuanceRoleToBeUpdated);

	}

	/**
	 * Updates the ItemIssuanceRole with the specific Id
	 * 
	 * @param itemIssuanceRoleToBeUpdated the ItemIssuanceRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateItemIssuanceRole(ItemIssuanceRole itemIssuanceRoleToBeUpdated) {

		UpdateItemIssuanceRole com = new UpdateItemIssuanceRole(itemIssuanceRoleToBeUpdated);

		int usedTicketId;

		synchronized (ItemIssuanceRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ItemIssuanceRoleUpdated.class,
				event -> sendItemIssuanceRoleChangedMessage(((ItemIssuanceRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ItemIssuanceRole from the database
	 * 
	 * @param itemIssuanceRoleId:
	 *            the id of the ItemIssuanceRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteitemIssuanceRoleById(@RequestParam(value = "itemIssuanceRoleId") String itemIssuanceRoleId) {

		DeleteItemIssuanceRole com = new DeleteItemIssuanceRole(itemIssuanceRoleId);

		int usedTicketId;

		synchronized (ItemIssuanceRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ItemIssuanceRoleDeleted.class,
				event -> sendItemIssuanceRoleChangedMessage(((ItemIssuanceRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendItemIssuanceRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/itemIssuanceRole/\" plus one of the following: "
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
