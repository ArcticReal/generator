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
import com.skytala.eCommerce.command.AddEmplPositionClassType;
import com.skytala.eCommerce.command.DeleteEmplPositionClassType;
import com.skytala.eCommerce.command.UpdateEmplPositionClassType;
import com.skytala.eCommerce.entity.EmplPositionClassType;
import com.skytala.eCommerce.entity.EmplPositionClassTypeMapper;
import com.skytala.eCommerce.event.EmplPositionClassTypeAdded;
import com.skytala.eCommerce.event.EmplPositionClassTypeDeleted;
import com.skytala.eCommerce.event.EmplPositionClassTypeFound;
import com.skytala.eCommerce.event.EmplPositionClassTypeUpdated;
import com.skytala.eCommerce.query.FindEmplPositionClassTypesBy;

@RestController
@RequestMapping("/api/emplPositionClassType")
public class EmplPositionClassTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmplPositionClassType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmplPositionClassTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmplPositionClassType
	 * @return a List with the EmplPositionClassTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmplPositionClassType> findEmplPositionClassTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmplPositionClassTypesBy query = new FindEmplPositionClassTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (EmplPositionClassTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionClassTypeFound.class,
				event -> sendEmplPositionClassTypesFoundMessage(((EmplPositionClassTypeFound) event).getEmplPositionClassTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmplPositionClassTypesFoundMessage(List<EmplPositionClassType> emplPositionClassTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emplPositionClassTypes);
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
	public boolean createEmplPositionClassType(HttpServletRequest request) {

		EmplPositionClassType emplPositionClassTypeToBeAdded = new EmplPositionClassType();
		try {
			emplPositionClassTypeToBeAdded = EmplPositionClassTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmplPositionClassType(emplPositionClassTypeToBeAdded);

	}

	/**
	 * creates a new EmplPositionClassType entry in the ofbiz database
	 * 
	 * @param emplPositionClassTypeToBeAdded
	 *            the EmplPositionClassType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmplPositionClassType(EmplPositionClassType emplPositionClassTypeToBeAdded) {

		AddEmplPositionClassType com = new AddEmplPositionClassType(emplPositionClassTypeToBeAdded);
		int usedTicketId;

		synchronized (EmplPositionClassTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionClassTypeAdded.class,
				event -> sendEmplPositionClassTypeChangedMessage(((EmplPositionClassTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmplPositionClassType(HttpServletRequest request) {

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

		EmplPositionClassType emplPositionClassTypeToBeUpdated = new EmplPositionClassType();

		try {
			emplPositionClassTypeToBeUpdated = EmplPositionClassTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmplPositionClassType(emplPositionClassTypeToBeUpdated);

	}

	/**
	 * Updates the EmplPositionClassType with the specific Id
	 * 
	 * @param emplPositionClassTypeToBeUpdated the EmplPositionClassType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmplPositionClassType(EmplPositionClassType emplPositionClassTypeToBeUpdated) {

		UpdateEmplPositionClassType com = new UpdateEmplPositionClassType(emplPositionClassTypeToBeUpdated);

		int usedTicketId;

		synchronized (EmplPositionClassTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionClassTypeUpdated.class,
				event -> sendEmplPositionClassTypeChangedMessage(((EmplPositionClassTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmplPositionClassType from the database
	 * 
	 * @param emplPositionClassTypeId:
	 *            the id of the EmplPositionClassType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemplPositionClassTypeById(@RequestParam(value = "emplPositionClassTypeId") String emplPositionClassTypeId) {

		DeleteEmplPositionClassType com = new DeleteEmplPositionClassType(emplPositionClassTypeId);

		int usedTicketId;

		synchronized (EmplPositionClassTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionClassTypeDeleted.class,
				event -> sendEmplPositionClassTypeChangedMessage(((EmplPositionClassTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmplPositionClassTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emplPositionClassType/\" plus one of the following: "
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
