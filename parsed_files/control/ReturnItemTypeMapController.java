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
import com.skytala.eCommerce.command.AddReturnItemTypeMap;
import com.skytala.eCommerce.command.DeleteReturnItemTypeMap;
import com.skytala.eCommerce.command.UpdateReturnItemTypeMap;
import com.skytala.eCommerce.entity.ReturnItemTypeMap;
import com.skytala.eCommerce.entity.ReturnItemTypeMapMapper;
import com.skytala.eCommerce.event.ReturnItemTypeMapAdded;
import com.skytala.eCommerce.event.ReturnItemTypeMapDeleted;
import com.skytala.eCommerce.event.ReturnItemTypeMapFound;
import com.skytala.eCommerce.event.ReturnItemTypeMapUpdated;
import com.skytala.eCommerce.query.FindReturnItemTypeMapsBy;

@RestController
@RequestMapping("/api/returnItemTypeMap")
public class ReturnItemTypeMapController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnItemTypeMap>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnItemTypeMapController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnItemTypeMap
	 * @return a List with the ReturnItemTypeMaps
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnItemTypeMap> findReturnItemTypeMapsBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnItemTypeMapsBy query = new FindReturnItemTypeMapsBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnItemTypeMapController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemTypeMapFound.class,
				event -> sendReturnItemTypeMapsFoundMessage(((ReturnItemTypeMapFound) event).getReturnItemTypeMaps(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnItemTypeMapsFoundMessage(List<ReturnItemTypeMap> returnItemTypeMaps, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnItemTypeMaps);
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
	public boolean createReturnItemTypeMap(HttpServletRequest request) {

		ReturnItemTypeMap returnItemTypeMapToBeAdded = new ReturnItemTypeMap();
		try {
			returnItemTypeMapToBeAdded = ReturnItemTypeMapMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnItemTypeMap(returnItemTypeMapToBeAdded);

	}

	/**
	 * creates a new ReturnItemTypeMap entry in the ofbiz database
	 * 
	 * @param returnItemTypeMapToBeAdded
	 *            the ReturnItemTypeMap thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnItemTypeMap(ReturnItemTypeMap returnItemTypeMapToBeAdded) {

		AddReturnItemTypeMap com = new AddReturnItemTypeMap(returnItemTypeMapToBeAdded);
		int usedTicketId;

		synchronized (ReturnItemTypeMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemTypeMapAdded.class,
				event -> sendReturnItemTypeMapChangedMessage(((ReturnItemTypeMapAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnItemTypeMap(HttpServletRequest request) {

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

		ReturnItemTypeMap returnItemTypeMapToBeUpdated = new ReturnItemTypeMap();

		try {
			returnItemTypeMapToBeUpdated = ReturnItemTypeMapMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnItemTypeMap(returnItemTypeMapToBeUpdated);

	}

	/**
	 * Updates the ReturnItemTypeMap with the specific Id
	 * 
	 * @param returnItemTypeMapToBeUpdated the ReturnItemTypeMap thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnItemTypeMap(ReturnItemTypeMap returnItemTypeMapToBeUpdated) {

		UpdateReturnItemTypeMap com = new UpdateReturnItemTypeMap(returnItemTypeMapToBeUpdated);

		int usedTicketId;

		synchronized (ReturnItemTypeMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemTypeMapUpdated.class,
				event -> sendReturnItemTypeMapChangedMessage(((ReturnItemTypeMapUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnItemTypeMap from the database
	 * 
	 * @param returnItemTypeMapId:
	 *            the id of the ReturnItemTypeMap thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnItemTypeMapById(@RequestParam(value = "returnItemTypeMapId") String returnItemTypeMapId) {

		DeleteReturnItemTypeMap com = new DeleteReturnItemTypeMap(returnItemTypeMapId);

		int usedTicketId;

		synchronized (ReturnItemTypeMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemTypeMapDeleted.class,
				event -> sendReturnItemTypeMapChangedMessage(((ReturnItemTypeMapDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnItemTypeMapChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnItemTypeMap/\" plus one of the following: "
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
