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
import com.skytala.eCommerce.command.AddEmploymentApp;
import com.skytala.eCommerce.command.DeleteEmploymentApp;
import com.skytala.eCommerce.command.UpdateEmploymentApp;
import com.skytala.eCommerce.entity.EmploymentApp;
import com.skytala.eCommerce.entity.EmploymentAppMapper;
import com.skytala.eCommerce.event.EmploymentAppAdded;
import com.skytala.eCommerce.event.EmploymentAppDeleted;
import com.skytala.eCommerce.event.EmploymentAppFound;
import com.skytala.eCommerce.event.EmploymentAppUpdated;
import com.skytala.eCommerce.query.FindEmploymentAppsBy;

@RestController
@RequestMapping("/api/employmentApp")
public class EmploymentAppController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmploymentApp>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmploymentAppController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmploymentApp
	 * @return a List with the EmploymentApps
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmploymentApp> findEmploymentAppsBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmploymentAppsBy query = new FindEmploymentAppsBy(allRequestParams);

		int usedTicketId;

		synchronized (EmploymentAppController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentAppFound.class,
				event -> sendEmploymentAppsFoundMessage(((EmploymentAppFound) event).getEmploymentApps(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmploymentAppsFoundMessage(List<EmploymentApp> employmentApps, int usedTicketId) {
		queryReturnVal.put(usedTicketId, employmentApps);
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
	public boolean createEmploymentApp(HttpServletRequest request) {

		EmploymentApp employmentAppToBeAdded = new EmploymentApp();
		try {
			employmentAppToBeAdded = EmploymentAppMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmploymentApp(employmentAppToBeAdded);

	}

	/**
	 * creates a new EmploymentApp entry in the ofbiz database
	 * 
	 * @param employmentAppToBeAdded
	 *            the EmploymentApp thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmploymentApp(EmploymentApp employmentAppToBeAdded) {

		AddEmploymentApp com = new AddEmploymentApp(employmentAppToBeAdded);
		int usedTicketId;

		synchronized (EmploymentAppController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentAppAdded.class,
				event -> sendEmploymentAppChangedMessage(((EmploymentAppAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateEmploymentApp(HttpServletRequest request) {

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

		EmploymentApp employmentAppToBeUpdated = new EmploymentApp();

		try {
			employmentAppToBeUpdated = EmploymentAppMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmploymentApp(employmentAppToBeUpdated);

	}

	/**
	 * Updates the EmploymentApp with the specific Id
	 * 
	 * @param employmentAppToBeUpdated the EmploymentApp thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmploymentApp(EmploymentApp employmentAppToBeUpdated) {

		UpdateEmploymentApp com = new UpdateEmploymentApp(employmentAppToBeUpdated);

		int usedTicketId;

		synchronized (EmploymentAppController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentAppUpdated.class,
				event -> sendEmploymentAppChangedMessage(((EmploymentAppUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a EmploymentApp from the database
	 * 
	 * @param employmentAppId:
	 *            the id of the EmploymentApp thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemploymentAppById(@RequestParam(value = "employmentAppId") String employmentAppId) {

		DeleteEmploymentApp com = new DeleteEmploymentApp(employmentAppId);

		int usedTicketId;

		synchronized (EmploymentAppController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmploymentAppDeleted.class,
				event -> sendEmploymentAppChangedMessage(((EmploymentAppDeleted) event).isSuccess(), usedTicketId));

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

	public void sendEmploymentAppChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/employmentApp/\" plus one of the following: "
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
