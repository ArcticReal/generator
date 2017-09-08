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
import com.skytala.eCommerce.command.AddReturnHeaderType;
import com.skytala.eCommerce.command.DeleteReturnHeaderType;
import com.skytala.eCommerce.command.UpdateReturnHeaderType;
import com.skytala.eCommerce.entity.ReturnHeaderType;
import com.skytala.eCommerce.entity.ReturnHeaderTypeMapper;
import com.skytala.eCommerce.event.ReturnHeaderTypeAdded;
import com.skytala.eCommerce.event.ReturnHeaderTypeDeleted;
import com.skytala.eCommerce.event.ReturnHeaderTypeFound;
import com.skytala.eCommerce.event.ReturnHeaderTypeUpdated;
import com.skytala.eCommerce.query.FindReturnHeaderTypesBy;

@RestController
@RequestMapping("/api/returnHeaderType")
public class ReturnHeaderTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnHeaderType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnHeaderTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnHeaderType
	 * @return a List with the ReturnHeaderTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnHeaderType> findReturnHeaderTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnHeaderTypesBy query = new FindReturnHeaderTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnHeaderTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnHeaderTypeFound.class,
				event -> sendReturnHeaderTypesFoundMessage(((ReturnHeaderTypeFound) event).getReturnHeaderTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnHeaderTypesFoundMessage(List<ReturnHeaderType> returnHeaderTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnHeaderTypes);
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
	public boolean createReturnHeaderType(HttpServletRequest request) {

		ReturnHeaderType returnHeaderTypeToBeAdded = new ReturnHeaderType();
		try {
			returnHeaderTypeToBeAdded = ReturnHeaderTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnHeaderType(returnHeaderTypeToBeAdded);

	}

	/**
	 * creates a new ReturnHeaderType entry in the ofbiz database
	 * 
	 * @param returnHeaderTypeToBeAdded
	 *            the ReturnHeaderType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnHeaderType(ReturnHeaderType returnHeaderTypeToBeAdded) {

		AddReturnHeaderType com = new AddReturnHeaderType(returnHeaderTypeToBeAdded);
		int usedTicketId;

		synchronized (ReturnHeaderTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnHeaderTypeAdded.class,
				event -> sendReturnHeaderTypeChangedMessage(((ReturnHeaderTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnHeaderType(HttpServletRequest request) {

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

		ReturnHeaderType returnHeaderTypeToBeUpdated = new ReturnHeaderType();

		try {
			returnHeaderTypeToBeUpdated = ReturnHeaderTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnHeaderType(returnHeaderTypeToBeUpdated);

	}

	/**
	 * Updates the ReturnHeaderType with the specific Id
	 * 
	 * @param returnHeaderTypeToBeUpdated the ReturnHeaderType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnHeaderType(ReturnHeaderType returnHeaderTypeToBeUpdated) {

		UpdateReturnHeaderType com = new UpdateReturnHeaderType(returnHeaderTypeToBeUpdated);

		int usedTicketId;

		synchronized (ReturnHeaderTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnHeaderTypeUpdated.class,
				event -> sendReturnHeaderTypeChangedMessage(((ReturnHeaderTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnHeaderType from the database
	 * 
	 * @param returnHeaderTypeId:
	 *            the id of the ReturnHeaderType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnHeaderTypeById(@RequestParam(value = "returnHeaderTypeId") String returnHeaderTypeId) {

		DeleteReturnHeaderType com = new DeleteReturnHeaderType(returnHeaderTypeId);

		int usedTicketId;

		synchronized (ReturnHeaderTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnHeaderTypeDeleted.class,
				event -> sendReturnHeaderTypeChangedMessage(((ReturnHeaderTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnHeaderTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnHeaderType/\" plus one of the following: "
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
