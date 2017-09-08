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
import com.skytala.eCommerce.command.AddEmploymentAppSourceType;
import com.skytala.eCommerce.command.DeleteEmploymentAppSourceType;
import com.skytala.eCommerce.command.UpdateEmploymentAppSourceType;
import com.skytala.eCommerce.entity.EmploymentAppSourceType;
import com.skytala.eCommerce.entity.EmploymentAppSourceTypeMapper;
import com.skytala.eCommerce.event.EmploymentAppSourceTypeAdded;
import com.skytala.eCommerce.event.EmploymentAppSourceTypeDeleted;
import com.skytala.eCommerce.event.EmploymentAppSourceTypeFound;
import com.skytala.eCommerce.event.EmploymentAppSourceTypeUpdated;
import com.skytala.eCommerce.query.FindEmploymentAppSourceTypesBy;

@RestController
@RequestMapping("/api/employmentAppSourceType")
public class EmploymentAppSourceTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmploymentAppSourceType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmploymentAppSourceTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmploymentAppSourceType
	 * @return a List with the EmploymentAppSourceTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmploymentAppSourceType> findEmploymentAppSourceTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmploymentAppSourceTypesBy query = new FindEmploymentAppSourceTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (EmploymentAppSourceTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentAppSourceTypeFound.class,
				event -> sendEmploymentAppSourceTypesFoundMessage(((EmploymentAppSourceTypeFound) event).getEmploymentAppSourceTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmploymentAppSourceTypesFoundMessage(List<EmploymentAppSourceType> employmentAppSourceTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, employmentAppSourceTypes);
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
	public boolean createEmploymentAppSourceType(HttpServletRequest request) {

		EmploymentAppSourceType employmentAppSourceTypeToBeAdded = new EmploymentAppSourceType();
		try {
			employmentAppSourceTypeToBeAdded = EmploymentAppSourceTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmploymentAppSourceType(employmentAppSourceTypeToBeAdded);

	}

	/**
	 * creates a new EmploymentAppSourceType entry in the ofbiz database
	 * 
	 * @param employmentAppSourceTypeToBeAdded
	 *            the EmploymentAppSourceType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmploymentAppSourceType(EmploymentAppSourceType employmentAppSourceTypeToBeAdded) {

		AddEmploymentAppSourceType com = new AddEmploymentAppSourceType(employmentAppSourceTypeToBeAdded);
		int usedTicketId;

		synchronized (EmploymentAppSourceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentAppSourceTypeAdded.class,
				event -> sendEmploymentAppSourceTypeChangedMessage(((EmploymentAppSourceTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmploymentAppSourceType(HttpServletRequest request) {

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

		EmploymentAppSourceType employmentAppSourceTypeToBeUpdated = new EmploymentAppSourceType();

		try {
			employmentAppSourceTypeToBeUpdated = EmploymentAppSourceTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmploymentAppSourceType(employmentAppSourceTypeToBeUpdated);

	}

	/**
	 * Updates the EmploymentAppSourceType with the specific Id
	 * 
	 * @param employmentAppSourceTypeToBeUpdated the EmploymentAppSourceType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmploymentAppSourceType(EmploymentAppSourceType employmentAppSourceTypeToBeUpdated) {

		UpdateEmploymentAppSourceType com = new UpdateEmploymentAppSourceType(employmentAppSourceTypeToBeUpdated);

		int usedTicketId;

		synchronized (EmploymentAppSourceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentAppSourceTypeUpdated.class,
				event -> sendEmploymentAppSourceTypeChangedMessage(((EmploymentAppSourceTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmploymentAppSourceType from the database
	 * 
	 * @param employmentAppSourceTypeId:
	 *            the id of the EmploymentAppSourceType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemploymentAppSourceTypeById(@RequestParam(value = "employmentAppSourceTypeId") String employmentAppSourceTypeId) {

		DeleteEmploymentAppSourceType com = new DeleteEmploymentAppSourceType(employmentAppSourceTypeId);

		int usedTicketId;

		synchronized (EmploymentAppSourceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentAppSourceTypeDeleted.class,
				event -> sendEmploymentAppSourceTypeChangedMessage(((EmploymentAppSourceTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmploymentAppSourceTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/employmentAppSourceType/\" plus one of the following: "
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
