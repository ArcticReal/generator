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
import com.skytala.eCommerce.command.AddDataResourceRole;
import com.skytala.eCommerce.command.DeleteDataResourceRole;
import com.skytala.eCommerce.command.UpdateDataResourceRole;
import com.skytala.eCommerce.entity.DataResourceRole;
import com.skytala.eCommerce.entity.DataResourceRoleMapper;
import com.skytala.eCommerce.event.DataResourceRoleAdded;
import com.skytala.eCommerce.event.DataResourceRoleDeleted;
import com.skytala.eCommerce.event.DataResourceRoleFound;
import com.skytala.eCommerce.event.DataResourceRoleUpdated;
import com.skytala.eCommerce.query.FindDataResourceRolesBy;

@RestController
@RequestMapping("/api/dataResourceRole")
public class DataResourceRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DataResourceRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DataResourceRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DataResourceRole
	 * @return a List with the DataResourceRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DataResourceRole> findDataResourceRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDataResourceRolesBy query = new FindDataResourceRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (DataResourceRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceRoleFound.class,
				event -> sendDataResourceRolesFoundMessage(((DataResourceRoleFound) event).getDataResourceRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDataResourceRolesFoundMessage(List<DataResourceRole> dataResourceRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, dataResourceRoles);
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
	public boolean createDataResourceRole(HttpServletRequest request) {

		DataResourceRole dataResourceRoleToBeAdded = new DataResourceRole();
		try {
			dataResourceRoleToBeAdded = DataResourceRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDataResourceRole(dataResourceRoleToBeAdded);

	}

	/**
	 * creates a new DataResourceRole entry in the ofbiz database
	 * 
	 * @param dataResourceRoleToBeAdded
	 *            the DataResourceRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDataResourceRole(DataResourceRole dataResourceRoleToBeAdded) {

		AddDataResourceRole com = new AddDataResourceRole(dataResourceRoleToBeAdded);
		int usedTicketId;

		synchronized (DataResourceRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceRoleAdded.class,
				event -> sendDataResourceRoleChangedMessage(((DataResourceRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDataResourceRole(HttpServletRequest request) {

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

		DataResourceRole dataResourceRoleToBeUpdated = new DataResourceRole();

		try {
			dataResourceRoleToBeUpdated = DataResourceRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDataResourceRole(dataResourceRoleToBeUpdated);

	}

	/**
	 * Updates the DataResourceRole with the specific Id
	 * 
	 * @param dataResourceRoleToBeUpdated the DataResourceRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDataResourceRole(DataResourceRole dataResourceRoleToBeUpdated) {

		UpdateDataResourceRole com = new UpdateDataResourceRole(dataResourceRoleToBeUpdated);

		int usedTicketId;

		synchronized (DataResourceRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceRoleUpdated.class,
				event -> sendDataResourceRoleChangedMessage(((DataResourceRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DataResourceRole from the database
	 * 
	 * @param dataResourceRoleId:
	 *            the id of the DataResourceRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedataResourceRoleById(@RequestParam(value = "dataResourceRoleId") String dataResourceRoleId) {

		DeleteDataResourceRole com = new DeleteDataResourceRole(dataResourceRoleId);

		int usedTicketId;

		synchronized (DataResourceRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceRoleDeleted.class,
				event -> sendDataResourceRoleChangedMessage(((DataResourceRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDataResourceRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/dataResourceRole/\" plus one of the following: "
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
