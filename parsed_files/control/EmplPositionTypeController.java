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
import com.skytala.eCommerce.command.AddEmplPositionType;
import com.skytala.eCommerce.command.DeleteEmplPositionType;
import com.skytala.eCommerce.command.UpdateEmplPositionType;
import com.skytala.eCommerce.entity.EmplPositionType;
import com.skytala.eCommerce.entity.EmplPositionTypeMapper;
import com.skytala.eCommerce.event.EmplPositionTypeAdded;
import com.skytala.eCommerce.event.EmplPositionTypeDeleted;
import com.skytala.eCommerce.event.EmplPositionTypeFound;
import com.skytala.eCommerce.event.EmplPositionTypeUpdated;
import com.skytala.eCommerce.query.FindEmplPositionTypesBy;

@RestController
@RequestMapping("/api/emplPositionType")
public class EmplPositionTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmplPositionType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmplPositionTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmplPositionType
	 * @return a List with the EmplPositionTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmplPositionType> findEmplPositionTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmplPositionTypesBy query = new FindEmplPositionTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (EmplPositionTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeFound.class,
				event -> sendEmplPositionTypesFoundMessage(((EmplPositionTypeFound) event).getEmplPositionTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmplPositionTypesFoundMessage(List<EmplPositionType> emplPositionTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emplPositionTypes);
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
	public boolean createEmplPositionType(HttpServletRequest request) {

		EmplPositionType emplPositionTypeToBeAdded = new EmplPositionType();
		try {
			emplPositionTypeToBeAdded = EmplPositionTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmplPositionType(emplPositionTypeToBeAdded);

	}

	/**
	 * creates a new EmplPositionType entry in the ofbiz database
	 * 
	 * @param emplPositionTypeToBeAdded
	 *            the EmplPositionType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmplPositionType(EmplPositionType emplPositionTypeToBeAdded) {

		AddEmplPositionType com = new AddEmplPositionType(emplPositionTypeToBeAdded);
		int usedTicketId;

		synchronized (EmplPositionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeAdded.class,
				event -> sendEmplPositionTypeChangedMessage(((EmplPositionTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmplPositionType(HttpServletRequest request) {

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

		EmplPositionType emplPositionTypeToBeUpdated = new EmplPositionType();

		try {
			emplPositionTypeToBeUpdated = EmplPositionTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmplPositionType(emplPositionTypeToBeUpdated);

	}

	/**
	 * Updates the EmplPositionType with the specific Id
	 * 
	 * @param emplPositionTypeToBeUpdated the EmplPositionType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmplPositionType(EmplPositionType emplPositionTypeToBeUpdated) {

		UpdateEmplPositionType com = new UpdateEmplPositionType(emplPositionTypeToBeUpdated);

		int usedTicketId;

		synchronized (EmplPositionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeUpdated.class,
				event -> sendEmplPositionTypeChangedMessage(((EmplPositionTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmplPositionType from the database
	 * 
	 * @param emplPositionTypeId:
	 *            the id of the EmplPositionType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemplPositionTypeById(@RequestParam(value = "emplPositionTypeId") String emplPositionTypeId) {

		DeleteEmplPositionType com = new DeleteEmplPositionType(emplPositionTypeId);

		int usedTicketId;

		synchronized (EmplPositionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeDeleted.class,
				event -> sendEmplPositionTypeChangedMessage(((EmplPositionTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmplPositionTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emplPositionType/\" plus one of the following: "
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
