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
import com.skytala.eCommerce.command.AddEmplLeaveReasonType;
import com.skytala.eCommerce.command.DeleteEmplLeaveReasonType;
import com.skytala.eCommerce.command.UpdateEmplLeaveReasonType;
import com.skytala.eCommerce.entity.EmplLeaveReasonType;
import com.skytala.eCommerce.entity.EmplLeaveReasonTypeMapper;
import com.skytala.eCommerce.event.EmplLeaveReasonTypeAdded;
import com.skytala.eCommerce.event.EmplLeaveReasonTypeDeleted;
import com.skytala.eCommerce.event.EmplLeaveReasonTypeFound;
import com.skytala.eCommerce.event.EmplLeaveReasonTypeUpdated;
import com.skytala.eCommerce.query.FindEmplLeaveReasonTypesBy;

@RestController
@RequestMapping("/api/emplLeaveReasonType")
public class EmplLeaveReasonTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmplLeaveReasonType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmplLeaveReasonTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmplLeaveReasonType
	 * @return a List with the EmplLeaveReasonTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmplLeaveReasonType> findEmplLeaveReasonTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmplLeaveReasonTypesBy query = new FindEmplLeaveReasonTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (EmplLeaveReasonTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplLeaveReasonTypeFound.class,
				event -> sendEmplLeaveReasonTypesFoundMessage(((EmplLeaveReasonTypeFound) event).getEmplLeaveReasonTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmplLeaveReasonTypesFoundMessage(List<EmplLeaveReasonType> emplLeaveReasonTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emplLeaveReasonTypes);
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
	public boolean createEmplLeaveReasonType(HttpServletRequest request) {

		EmplLeaveReasonType emplLeaveReasonTypeToBeAdded = new EmplLeaveReasonType();
		try {
			emplLeaveReasonTypeToBeAdded = EmplLeaveReasonTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmplLeaveReasonType(emplLeaveReasonTypeToBeAdded);

	}

	/**
	 * creates a new EmplLeaveReasonType entry in the ofbiz database
	 * 
	 * @param emplLeaveReasonTypeToBeAdded
	 *            the EmplLeaveReasonType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmplLeaveReasonType(EmplLeaveReasonType emplLeaveReasonTypeToBeAdded) {

		AddEmplLeaveReasonType com = new AddEmplLeaveReasonType(emplLeaveReasonTypeToBeAdded);
		int usedTicketId;

		synchronized (EmplLeaveReasonTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplLeaveReasonTypeAdded.class,
				event -> sendEmplLeaveReasonTypeChangedMessage(((EmplLeaveReasonTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmplLeaveReasonType(HttpServletRequest request) {

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

		EmplLeaveReasonType emplLeaveReasonTypeToBeUpdated = new EmplLeaveReasonType();

		try {
			emplLeaveReasonTypeToBeUpdated = EmplLeaveReasonTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmplLeaveReasonType(emplLeaveReasonTypeToBeUpdated);

	}

	/**
	 * Updates the EmplLeaveReasonType with the specific Id
	 * 
	 * @param emplLeaveReasonTypeToBeUpdated the EmplLeaveReasonType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmplLeaveReasonType(EmplLeaveReasonType emplLeaveReasonTypeToBeUpdated) {

		UpdateEmplLeaveReasonType com = new UpdateEmplLeaveReasonType(emplLeaveReasonTypeToBeUpdated);

		int usedTicketId;

		synchronized (EmplLeaveReasonTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplLeaveReasonTypeUpdated.class,
				event -> sendEmplLeaveReasonTypeChangedMessage(((EmplLeaveReasonTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmplLeaveReasonType from the database
	 * 
	 * @param emplLeaveReasonTypeId:
	 *            the id of the EmplLeaveReasonType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemplLeaveReasonTypeById(@RequestParam(value = "emplLeaveReasonTypeId") String emplLeaveReasonTypeId) {

		DeleteEmplLeaveReasonType com = new DeleteEmplLeaveReasonType(emplLeaveReasonTypeId);

		int usedTicketId;

		synchronized (EmplLeaveReasonTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplLeaveReasonTypeDeleted.class,
				event -> sendEmplLeaveReasonTypeChangedMessage(((EmplLeaveReasonTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmplLeaveReasonTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emplLeaveReasonType/\" plus one of the following: "
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
