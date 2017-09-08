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
import com.skytala.eCommerce.command.AddEmplPosition;
import com.skytala.eCommerce.command.DeleteEmplPosition;
import com.skytala.eCommerce.command.UpdateEmplPosition;
import com.skytala.eCommerce.entity.EmplPosition;
import com.skytala.eCommerce.entity.EmplPositionMapper;
import com.skytala.eCommerce.event.EmplPositionAdded;
import com.skytala.eCommerce.event.EmplPositionDeleted;
import com.skytala.eCommerce.event.EmplPositionFound;
import com.skytala.eCommerce.event.EmplPositionUpdated;
import com.skytala.eCommerce.query.FindEmplPositionsBy;

@RestController
@RequestMapping("/api/emplPosition")
public class EmplPositionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmplPosition>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmplPositionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmplPosition
	 * @return a List with the EmplPositions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmplPosition> findEmplPositionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmplPositionsBy query = new FindEmplPositionsBy(allRequestParams);

		int usedTicketId;

		synchronized (EmplPositionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionFound.class,
				event -> sendEmplPositionsFoundMessage(((EmplPositionFound) event).getEmplPositions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmplPositionsFoundMessage(List<EmplPosition> emplPositions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emplPositions);
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
	public boolean createEmplPosition(HttpServletRequest request) {

		EmplPosition emplPositionToBeAdded = new EmplPosition();
		try {
			emplPositionToBeAdded = EmplPositionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmplPosition(emplPositionToBeAdded);

	}

	/**
	 * creates a new EmplPosition entry in the ofbiz database
	 * 
	 * @param emplPositionToBeAdded
	 *            the EmplPosition thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmplPosition(EmplPosition emplPositionToBeAdded) {

		AddEmplPosition com = new AddEmplPosition(emplPositionToBeAdded);
		int usedTicketId;

		synchronized (EmplPositionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionAdded.class,
				event -> sendEmplPositionChangedMessage(((EmplPositionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmplPosition(HttpServletRequest request) {

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

		EmplPosition emplPositionToBeUpdated = new EmplPosition();

		try {
			emplPositionToBeUpdated = EmplPositionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmplPosition(emplPositionToBeUpdated);

	}

	/**
	 * Updates the EmplPosition with the specific Id
	 * 
	 * @param emplPositionToBeUpdated the EmplPosition thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmplPosition(EmplPosition emplPositionToBeUpdated) {

		UpdateEmplPosition com = new UpdateEmplPosition(emplPositionToBeUpdated);

		int usedTicketId;

		synchronized (EmplPositionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionUpdated.class,
				event -> sendEmplPositionChangedMessage(((EmplPositionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmplPosition from the database
	 * 
	 * @param emplPositionId:
	 *            the id of the EmplPosition thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemplPositionById(@RequestParam(value = "emplPositionId") String emplPositionId) {

		DeleteEmplPosition com = new DeleteEmplPosition(emplPositionId);

		int usedTicketId;

		synchronized (EmplPositionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionDeleted.class,
				event -> sendEmplPositionChangedMessage(((EmplPositionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmplPositionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emplPosition/\" plus one of the following: "
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
