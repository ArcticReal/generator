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
import com.skytala.eCommerce.command.AddMrpEventType;
import com.skytala.eCommerce.command.DeleteMrpEventType;
import com.skytala.eCommerce.command.UpdateMrpEventType;
import com.skytala.eCommerce.entity.MrpEventType;
import com.skytala.eCommerce.entity.MrpEventTypeMapper;
import com.skytala.eCommerce.event.MrpEventTypeAdded;
import com.skytala.eCommerce.event.MrpEventTypeDeleted;
import com.skytala.eCommerce.event.MrpEventTypeFound;
import com.skytala.eCommerce.event.MrpEventTypeUpdated;
import com.skytala.eCommerce.query.FindMrpEventTypesBy;

@RestController
@RequestMapping("/api/mrpEventType")
public class MrpEventTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<MrpEventType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public MrpEventTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a MrpEventType
	 * @return a List with the MrpEventTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<MrpEventType> findMrpEventTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindMrpEventTypesBy query = new FindMrpEventTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (MrpEventTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MrpEventTypeFound.class,
				event -> sendMrpEventTypesFoundMessage(((MrpEventTypeFound) event).getMrpEventTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendMrpEventTypesFoundMessage(List<MrpEventType> mrpEventTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, mrpEventTypes);
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
	public boolean createMrpEventType(HttpServletRequest request) {

		MrpEventType mrpEventTypeToBeAdded = new MrpEventType();
		try {
			mrpEventTypeToBeAdded = MrpEventTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createMrpEventType(mrpEventTypeToBeAdded);

	}

	/**
	 * creates a new MrpEventType entry in the ofbiz database
	 * 
	 * @param mrpEventTypeToBeAdded
	 *            the MrpEventType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createMrpEventType(MrpEventType mrpEventTypeToBeAdded) {

		AddMrpEventType com = new AddMrpEventType(mrpEventTypeToBeAdded);
		int usedTicketId;

		synchronized (MrpEventTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MrpEventTypeAdded.class,
				event -> sendMrpEventTypeChangedMessage(((MrpEventTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateMrpEventType(HttpServletRequest request) {

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

		MrpEventType mrpEventTypeToBeUpdated = new MrpEventType();

		try {
			mrpEventTypeToBeUpdated = MrpEventTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateMrpEventType(mrpEventTypeToBeUpdated);

	}

	/**
	 * Updates the MrpEventType with the specific Id
	 * 
	 * @param mrpEventTypeToBeUpdated the MrpEventType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateMrpEventType(MrpEventType mrpEventTypeToBeUpdated) {

		UpdateMrpEventType com = new UpdateMrpEventType(mrpEventTypeToBeUpdated);

		int usedTicketId;

		synchronized (MrpEventTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MrpEventTypeUpdated.class,
				event -> sendMrpEventTypeChangedMessage(((MrpEventTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a MrpEventType from the database
	 * 
	 * @param mrpEventTypeId:
	 *            the id of the MrpEventType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletemrpEventTypeById(@RequestParam(value = "mrpEventTypeId") String mrpEventTypeId) {

		DeleteMrpEventType com = new DeleteMrpEventType(mrpEventTypeId);

		int usedTicketId;

		synchronized (MrpEventTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MrpEventTypeDeleted.class,
				event -> sendMrpEventTypeChangedMessage(((MrpEventTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendMrpEventTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/mrpEventType/\" plus one of the following: "
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
