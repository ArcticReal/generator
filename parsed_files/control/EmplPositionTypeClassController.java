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
import com.skytala.eCommerce.command.AddEmplPositionTypeClass;
import com.skytala.eCommerce.command.DeleteEmplPositionTypeClass;
import com.skytala.eCommerce.command.UpdateEmplPositionTypeClass;
import com.skytala.eCommerce.entity.EmplPositionTypeClass;
import com.skytala.eCommerce.entity.EmplPositionTypeClassMapper;
import com.skytala.eCommerce.event.EmplPositionTypeClassAdded;
import com.skytala.eCommerce.event.EmplPositionTypeClassDeleted;
import com.skytala.eCommerce.event.EmplPositionTypeClassFound;
import com.skytala.eCommerce.event.EmplPositionTypeClassUpdated;
import com.skytala.eCommerce.query.FindEmplPositionTypeClasssBy;

@RestController
@RequestMapping("/api/emplPositionTypeClass")
public class EmplPositionTypeClassController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmplPositionTypeClass>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmplPositionTypeClassController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmplPositionTypeClass
	 * @return a List with the EmplPositionTypeClasss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmplPositionTypeClass> findEmplPositionTypeClasssBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmplPositionTypeClasssBy query = new FindEmplPositionTypeClasssBy(allRequestParams);

		int usedTicketId;

		synchronized (EmplPositionTypeClassController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeClassFound.class,
				event -> sendEmplPositionTypeClasssFoundMessage(((EmplPositionTypeClassFound) event).getEmplPositionTypeClasss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmplPositionTypeClasssFoundMessage(List<EmplPositionTypeClass> emplPositionTypeClasss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emplPositionTypeClasss);
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
	public boolean createEmplPositionTypeClass(HttpServletRequest request) {

		EmplPositionTypeClass emplPositionTypeClassToBeAdded = new EmplPositionTypeClass();
		try {
			emplPositionTypeClassToBeAdded = EmplPositionTypeClassMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmplPositionTypeClass(emplPositionTypeClassToBeAdded);

	}

	/**
	 * creates a new EmplPositionTypeClass entry in the ofbiz database
	 * 
	 * @param emplPositionTypeClassToBeAdded
	 *            the EmplPositionTypeClass thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmplPositionTypeClass(EmplPositionTypeClass emplPositionTypeClassToBeAdded) {

		AddEmplPositionTypeClass com = new AddEmplPositionTypeClass(emplPositionTypeClassToBeAdded);
		int usedTicketId;

		synchronized (EmplPositionTypeClassController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeClassAdded.class,
				event -> sendEmplPositionTypeClassChangedMessage(((EmplPositionTypeClassAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmplPositionTypeClass(HttpServletRequest request) {

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

		EmplPositionTypeClass emplPositionTypeClassToBeUpdated = new EmplPositionTypeClass();

		try {
			emplPositionTypeClassToBeUpdated = EmplPositionTypeClassMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmplPositionTypeClass(emplPositionTypeClassToBeUpdated);

	}

	/**
	 * Updates the EmplPositionTypeClass with the specific Id
	 * 
	 * @param emplPositionTypeClassToBeUpdated the EmplPositionTypeClass thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmplPositionTypeClass(EmplPositionTypeClass emplPositionTypeClassToBeUpdated) {

		UpdateEmplPositionTypeClass com = new UpdateEmplPositionTypeClass(emplPositionTypeClassToBeUpdated);

		int usedTicketId;

		synchronized (EmplPositionTypeClassController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeClassUpdated.class,
				event -> sendEmplPositionTypeClassChangedMessage(((EmplPositionTypeClassUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmplPositionTypeClass from the database
	 * 
	 * @param emplPositionTypeClassId:
	 *            the id of the EmplPositionTypeClass thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemplPositionTypeClassById(@RequestParam(value = "emplPositionTypeClassId") String emplPositionTypeClassId) {

		DeleteEmplPositionTypeClass com = new DeleteEmplPositionTypeClass(emplPositionTypeClassId);

		int usedTicketId;

		synchronized (EmplPositionTypeClassController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionTypeClassDeleted.class,
				event -> sendEmplPositionTypeClassChangedMessage(((EmplPositionTypeClassDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmplPositionTypeClassChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emplPositionTypeClass/\" plus one of the following: "
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
