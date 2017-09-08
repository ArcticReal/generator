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
import com.skytala.eCommerce.command.AddEmployment;
import com.skytala.eCommerce.command.DeleteEmployment;
import com.skytala.eCommerce.command.UpdateEmployment;
import com.skytala.eCommerce.entity.Employment;
import com.skytala.eCommerce.entity.EmploymentMapper;
import com.skytala.eCommerce.event.EmploymentAdded;
import com.skytala.eCommerce.event.EmploymentDeleted;
import com.skytala.eCommerce.event.EmploymentFound;
import com.skytala.eCommerce.event.EmploymentUpdated;
import com.skytala.eCommerce.query.FindEmploymentsBy;

@RestController
@RequestMapping("/api/employment")
public class EmploymentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Employment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmploymentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Employment
	 * @return a List with the Employments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Employment> findEmploymentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmploymentsBy query = new FindEmploymentsBy(allRequestParams);

		int usedTicketId;

		synchronized (EmploymentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentFound.class,
				event -> sendEmploymentsFoundMessage(((EmploymentFound) event).getEmployments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmploymentsFoundMessage(List<Employment> employments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, employments);
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
	public boolean createEmployment(HttpServletRequest request) {

		Employment employmentToBeAdded = new Employment();
		try {
			employmentToBeAdded = EmploymentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmployment(employmentToBeAdded);

	}

	/**
	 * creates a new Employment entry in the ofbiz database
	 * 
	 * @param employmentToBeAdded
	 *            the Employment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmployment(Employment employmentToBeAdded) {

		AddEmployment com = new AddEmployment(employmentToBeAdded);
		int usedTicketId;

		synchronized (EmploymentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentAdded.class,
				event -> sendEmploymentChangedMessage(((EmploymentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmployment(HttpServletRequest request) {

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

		Employment employmentToBeUpdated = new Employment();

		try {
			employmentToBeUpdated = EmploymentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmployment(employmentToBeUpdated);

	}

	/**
	 * Updates the Employment with the specific Id
	 * 
	 * @param employmentToBeUpdated the Employment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmployment(Employment employmentToBeUpdated) {

		UpdateEmployment com = new UpdateEmployment(employmentToBeUpdated);

		int usedTicketId;

		synchronized (EmploymentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentUpdated.class,
				event -> sendEmploymentChangedMessage(((EmploymentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Employment from the database
	 * 
	 * @param employmentId:
	 *            the id of the Employment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemploymentById(@RequestParam(value = "employmentId") String employmentId) {

		DeleteEmployment com = new DeleteEmployment(employmentId);

		int usedTicketId;

		synchronized (EmploymentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentDeleted.class,
				event -> sendEmploymentChangedMessage(((EmploymentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmploymentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/employment/\" plus one of the following: "
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
