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
import com.skytala.eCommerce.command.AddReturnAdjustmentType;
import com.skytala.eCommerce.command.DeleteReturnAdjustmentType;
import com.skytala.eCommerce.command.UpdateReturnAdjustmentType;
import com.skytala.eCommerce.entity.ReturnAdjustmentType;
import com.skytala.eCommerce.entity.ReturnAdjustmentTypeMapper;
import com.skytala.eCommerce.event.ReturnAdjustmentTypeAdded;
import com.skytala.eCommerce.event.ReturnAdjustmentTypeDeleted;
import com.skytala.eCommerce.event.ReturnAdjustmentTypeFound;
import com.skytala.eCommerce.event.ReturnAdjustmentTypeUpdated;
import com.skytala.eCommerce.query.FindReturnAdjustmentTypesBy;

@RestController
@RequestMapping("/api/returnAdjustmentType")
public class ReturnAdjustmentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnAdjustmentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnAdjustmentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnAdjustmentType
	 * @return a List with the ReturnAdjustmentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnAdjustmentType> findReturnAdjustmentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnAdjustmentTypesBy query = new FindReturnAdjustmentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnAdjustmentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnAdjustmentTypeFound.class,
				event -> sendReturnAdjustmentTypesFoundMessage(((ReturnAdjustmentTypeFound) event).getReturnAdjustmentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnAdjustmentTypesFoundMessage(List<ReturnAdjustmentType> returnAdjustmentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnAdjustmentTypes);
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
	public boolean createReturnAdjustmentType(HttpServletRequest request) {

		ReturnAdjustmentType returnAdjustmentTypeToBeAdded = new ReturnAdjustmentType();
		try {
			returnAdjustmentTypeToBeAdded = ReturnAdjustmentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnAdjustmentType(returnAdjustmentTypeToBeAdded);

	}

	/**
	 * creates a new ReturnAdjustmentType entry in the ofbiz database
	 * 
	 * @param returnAdjustmentTypeToBeAdded
	 *            the ReturnAdjustmentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnAdjustmentType(ReturnAdjustmentType returnAdjustmentTypeToBeAdded) {

		AddReturnAdjustmentType com = new AddReturnAdjustmentType(returnAdjustmentTypeToBeAdded);
		int usedTicketId;

		synchronized (ReturnAdjustmentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnAdjustmentTypeAdded.class,
				event -> sendReturnAdjustmentTypeChangedMessage(((ReturnAdjustmentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnAdjustmentType(HttpServletRequest request) {

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

		ReturnAdjustmentType returnAdjustmentTypeToBeUpdated = new ReturnAdjustmentType();

		try {
			returnAdjustmentTypeToBeUpdated = ReturnAdjustmentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnAdjustmentType(returnAdjustmentTypeToBeUpdated);

	}

	/**
	 * Updates the ReturnAdjustmentType with the specific Id
	 * 
	 * @param returnAdjustmentTypeToBeUpdated the ReturnAdjustmentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnAdjustmentType(ReturnAdjustmentType returnAdjustmentTypeToBeUpdated) {

		UpdateReturnAdjustmentType com = new UpdateReturnAdjustmentType(returnAdjustmentTypeToBeUpdated);

		int usedTicketId;

		synchronized (ReturnAdjustmentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnAdjustmentTypeUpdated.class,
				event -> sendReturnAdjustmentTypeChangedMessage(((ReturnAdjustmentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnAdjustmentType from the database
	 * 
	 * @param returnAdjustmentTypeId:
	 *            the id of the ReturnAdjustmentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnAdjustmentTypeById(@RequestParam(value = "returnAdjustmentTypeId") String returnAdjustmentTypeId) {

		DeleteReturnAdjustmentType com = new DeleteReturnAdjustmentType(returnAdjustmentTypeId);

		int usedTicketId;

		synchronized (ReturnAdjustmentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnAdjustmentTypeDeleted.class,
				event -> sendReturnAdjustmentTypeChangedMessage(((ReturnAdjustmentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnAdjustmentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnAdjustmentType/\" plus one of the following: "
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
