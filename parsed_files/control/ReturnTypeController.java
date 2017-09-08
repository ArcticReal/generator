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
import com.skytala.eCommerce.command.AddReturnType;
import com.skytala.eCommerce.command.DeleteReturnType;
import com.skytala.eCommerce.command.UpdateReturnType;
import com.skytala.eCommerce.entity.ReturnType;
import com.skytala.eCommerce.entity.ReturnTypeMapper;
import com.skytala.eCommerce.event.ReturnTypeAdded;
import com.skytala.eCommerce.event.ReturnTypeDeleted;
import com.skytala.eCommerce.event.ReturnTypeFound;
import com.skytala.eCommerce.event.ReturnTypeUpdated;
import com.skytala.eCommerce.query.FindReturnTypesBy;

@RestController
@RequestMapping("/api/returnType")
public class ReturnTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnType
	 * @return a List with the ReturnTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnType> findReturnTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnTypesBy query = new FindReturnTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnTypeFound.class,
				event -> sendReturnTypesFoundMessage(((ReturnTypeFound) event).getReturnTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnTypesFoundMessage(List<ReturnType> returnTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnTypes);
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
	public boolean createReturnType(HttpServletRequest request) {

		ReturnType returnTypeToBeAdded = new ReturnType();
		try {
			returnTypeToBeAdded = ReturnTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnType(returnTypeToBeAdded);

	}

	/**
	 * creates a new ReturnType entry in the ofbiz database
	 * 
	 * @param returnTypeToBeAdded
	 *            the ReturnType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnType(ReturnType returnTypeToBeAdded) {

		AddReturnType com = new AddReturnType(returnTypeToBeAdded);
		int usedTicketId;

		synchronized (ReturnTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnTypeAdded.class,
				event -> sendReturnTypeChangedMessage(((ReturnTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnType(HttpServletRequest request) {

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

		ReturnType returnTypeToBeUpdated = new ReturnType();

		try {
			returnTypeToBeUpdated = ReturnTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnType(returnTypeToBeUpdated);

	}

	/**
	 * Updates the ReturnType with the specific Id
	 * 
	 * @param returnTypeToBeUpdated the ReturnType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnType(ReturnType returnTypeToBeUpdated) {

		UpdateReturnType com = new UpdateReturnType(returnTypeToBeUpdated);

		int usedTicketId;

		synchronized (ReturnTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnTypeUpdated.class,
				event -> sendReturnTypeChangedMessage(((ReturnTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnType from the database
	 * 
	 * @param returnTypeId:
	 *            the id of the ReturnType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnTypeById(@RequestParam(value = "returnTypeId") String returnTypeId) {

		DeleteReturnType com = new DeleteReturnType(returnTypeId);

		int usedTicketId;

		synchronized (ReturnTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnTypeDeleted.class,
				event -> sendReturnTypeChangedMessage(((ReturnTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnType/\" plus one of the following: "
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
