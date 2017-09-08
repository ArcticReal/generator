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
import com.skytala.eCommerce.command.AddEmplPositionReportingStruct;
import com.skytala.eCommerce.command.DeleteEmplPositionReportingStruct;
import com.skytala.eCommerce.command.UpdateEmplPositionReportingStruct;
import com.skytala.eCommerce.entity.EmplPositionReportingStruct;
import com.skytala.eCommerce.entity.EmplPositionReportingStructMapper;
import com.skytala.eCommerce.event.EmplPositionReportingStructAdded;
import com.skytala.eCommerce.event.EmplPositionReportingStructDeleted;
import com.skytala.eCommerce.event.EmplPositionReportingStructFound;
import com.skytala.eCommerce.event.EmplPositionReportingStructUpdated;
import com.skytala.eCommerce.query.FindEmplPositionReportingStructsBy;

@RestController
@RequestMapping("/api/emplPositionReportingStruct")
public class EmplPositionReportingStructController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmplPositionReportingStruct>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmplPositionReportingStructController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmplPositionReportingStruct
	 * @return a List with the EmplPositionReportingStructs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmplPositionReportingStruct> findEmplPositionReportingStructsBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmplPositionReportingStructsBy query = new FindEmplPositionReportingStructsBy(allRequestParams);

		int usedTicketId;

		synchronized (EmplPositionReportingStructController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionReportingStructFound.class,
				event -> sendEmplPositionReportingStructsFoundMessage(((EmplPositionReportingStructFound) event).getEmplPositionReportingStructs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmplPositionReportingStructsFoundMessage(List<EmplPositionReportingStruct> emplPositionReportingStructs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emplPositionReportingStructs);
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
	public boolean createEmplPositionReportingStruct(HttpServletRequest request) {

		EmplPositionReportingStruct emplPositionReportingStructToBeAdded = new EmplPositionReportingStruct();
		try {
			emplPositionReportingStructToBeAdded = EmplPositionReportingStructMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmplPositionReportingStruct(emplPositionReportingStructToBeAdded);

	}

	/**
	 * creates a new EmplPositionReportingStruct entry in the ofbiz database
	 * 
	 * @param emplPositionReportingStructToBeAdded
	 *            the EmplPositionReportingStruct thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmplPositionReportingStruct(EmplPositionReportingStruct emplPositionReportingStructToBeAdded) {

		AddEmplPositionReportingStruct com = new AddEmplPositionReportingStruct(emplPositionReportingStructToBeAdded);
		int usedTicketId;

		synchronized (EmplPositionReportingStructController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionReportingStructAdded.class,
				event -> sendEmplPositionReportingStructChangedMessage(((EmplPositionReportingStructAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmplPositionReportingStruct(HttpServletRequest request) {

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

		EmplPositionReportingStruct emplPositionReportingStructToBeUpdated = new EmplPositionReportingStruct();

		try {
			emplPositionReportingStructToBeUpdated = EmplPositionReportingStructMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmplPositionReportingStruct(emplPositionReportingStructToBeUpdated);

	}

	/**
	 * Updates the EmplPositionReportingStruct with the specific Id
	 * 
	 * @param emplPositionReportingStructToBeUpdated the EmplPositionReportingStruct thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmplPositionReportingStruct(EmplPositionReportingStruct emplPositionReportingStructToBeUpdated) {

		UpdateEmplPositionReportingStruct com = new UpdateEmplPositionReportingStruct(emplPositionReportingStructToBeUpdated);

		int usedTicketId;

		synchronized (EmplPositionReportingStructController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionReportingStructUpdated.class,
				event -> sendEmplPositionReportingStructChangedMessage(((EmplPositionReportingStructUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmplPositionReportingStruct from the database
	 * 
	 * @param emplPositionReportingStructId:
	 *            the id of the EmplPositionReportingStruct thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemplPositionReportingStructById(@RequestParam(value = "emplPositionReportingStructId") String emplPositionReportingStructId) {

		DeleteEmplPositionReportingStruct com = new DeleteEmplPositionReportingStruct(emplPositionReportingStructId);

		int usedTicketId;

		synchronized (EmplPositionReportingStructController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmplPositionReportingStructDeleted.class,
				event -> sendEmplPositionReportingStructChangedMessage(((EmplPositionReportingStructDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmplPositionReportingStructChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emplPositionReportingStruct/\" plus one of the following: "
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
