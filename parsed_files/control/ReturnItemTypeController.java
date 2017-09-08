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
import com.skytala.eCommerce.command.AddReturnItemType;
import com.skytala.eCommerce.command.DeleteReturnItemType;
import com.skytala.eCommerce.command.UpdateReturnItemType;
import com.skytala.eCommerce.entity.ReturnItemType;
import com.skytala.eCommerce.entity.ReturnItemTypeMapper;
import com.skytala.eCommerce.event.ReturnItemTypeAdded;
import com.skytala.eCommerce.event.ReturnItemTypeDeleted;
import com.skytala.eCommerce.event.ReturnItemTypeFound;
import com.skytala.eCommerce.event.ReturnItemTypeUpdated;
import com.skytala.eCommerce.query.FindReturnItemTypesBy;

@RestController
@RequestMapping("/api/returnItemType")
public class ReturnItemTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnItemType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnItemTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnItemType
	 * @return a List with the ReturnItemTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnItemType> findReturnItemTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnItemTypesBy query = new FindReturnItemTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnItemTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemTypeFound.class,
				event -> sendReturnItemTypesFoundMessage(((ReturnItemTypeFound) event).getReturnItemTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnItemTypesFoundMessage(List<ReturnItemType> returnItemTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnItemTypes);
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
	public boolean createReturnItemType(HttpServletRequest request) {

		ReturnItemType returnItemTypeToBeAdded = new ReturnItemType();
		try {
			returnItemTypeToBeAdded = ReturnItemTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnItemType(returnItemTypeToBeAdded);

	}

	/**
	 * creates a new ReturnItemType entry in the ofbiz database
	 * 
	 * @param returnItemTypeToBeAdded
	 *            the ReturnItemType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnItemType(ReturnItemType returnItemTypeToBeAdded) {

		AddReturnItemType com = new AddReturnItemType(returnItemTypeToBeAdded);
		int usedTicketId;

		synchronized (ReturnItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemTypeAdded.class,
				event -> sendReturnItemTypeChangedMessage(((ReturnItemTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnItemType(HttpServletRequest request) {

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

		ReturnItemType returnItemTypeToBeUpdated = new ReturnItemType();

		try {
			returnItemTypeToBeUpdated = ReturnItemTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnItemType(returnItemTypeToBeUpdated);

	}

	/**
	 * Updates the ReturnItemType with the specific Id
	 * 
	 * @param returnItemTypeToBeUpdated the ReturnItemType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnItemType(ReturnItemType returnItemTypeToBeUpdated) {

		UpdateReturnItemType com = new UpdateReturnItemType(returnItemTypeToBeUpdated);

		int usedTicketId;

		synchronized (ReturnItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemTypeUpdated.class,
				event -> sendReturnItemTypeChangedMessage(((ReturnItemTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnItemType from the database
	 * 
	 * @param returnItemTypeId:
	 *            the id of the ReturnItemType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnItemTypeById(@RequestParam(value = "returnItemTypeId") String returnItemTypeId) {

		DeleteReturnItemType com = new DeleteReturnItemType(returnItemTypeId);

		int usedTicketId;

		synchronized (ReturnItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemTypeDeleted.class,
				event -> sendReturnItemTypeChangedMessage(((ReturnItemTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnItemTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnItemType/\" plus one of the following: "
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
