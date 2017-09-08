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
import com.skytala.eCommerce.command.AddApplicationSandbox;
import com.skytala.eCommerce.command.DeleteApplicationSandbox;
import com.skytala.eCommerce.command.UpdateApplicationSandbox;
import com.skytala.eCommerce.entity.ApplicationSandbox;
import com.skytala.eCommerce.entity.ApplicationSandboxMapper;
import com.skytala.eCommerce.event.ApplicationSandboxAdded;
import com.skytala.eCommerce.event.ApplicationSandboxDeleted;
import com.skytala.eCommerce.event.ApplicationSandboxFound;
import com.skytala.eCommerce.event.ApplicationSandboxUpdated;
import com.skytala.eCommerce.query.FindApplicationSandboxsBy;

@RestController
@RequestMapping("/api/applicationSandbox")
public class ApplicationSandboxController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ApplicationSandbox>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ApplicationSandboxController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ApplicationSandbox
	 * @return a List with the ApplicationSandboxs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ApplicationSandbox> findApplicationSandboxsBy(@RequestParam Map<String, String> allRequestParams) {

		FindApplicationSandboxsBy query = new FindApplicationSandboxsBy(allRequestParams);

		int usedTicketId;

		synchronized (ApplicationSandboxController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ApplicationSandboxFound.class,
				event -> sendApplicationSandboxsFoundMessage(((ApplicationSandboxFound) event).getApplicationSandboxs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendApplicationSandboxsFoundMessage(List<ApplicationSandbox> applicationSandboxs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, applicationSandboxs);
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
	public boolean createApplicationSandbox(HttpServletRequest request) {

		ApplicationSandbox applicationSandboxToBeAdded = new ApplicationSandbox();
		try {
			applicationSandboxToBeAdded = ApplicationSandboxMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createApplicationSandbox(applicationSandboxToBeAdded);

	}

	/**
	 * creates a new ApplicationSandbox entry in the ofbiz database
	 * 
	 * @param applicationSandboxToBeAdded
	 *            the ApplicationSandbox thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createApplicationSandbox(ApplicationSandbox applicationSandboxToBeAdded) {

		AddApplicationSandbox com = new AddApplicationSandbox(applicationSandboxToBeAdded);
		int usedTicketId;

		synchronized (ApplicationSandboxController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ApplicationSandboxAdded.class,
				event -> sendApplicationSandboxChangedMessage(((ApplicationSandboxAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateApplicationSandbox(HttpServletRequest request) {

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

		ApplicationSandbox applicationSandboxToBeUpdated = new ApplicationSandbox();

		try {
			applicationSandboxToBeUpdated = ApplicationSandboxMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateApplicationSandbox(applicationSandboxToBeUpdated);

	}

	/**
	 * Updates the ApplicationSandbox with the specific Id
	 * 
	 * @param applicationSandboxToBeUpdated the ApplicationSandbox thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateApplicationSandbox(ApplicationSandbox applicationSandboxToBeUpdated) {

		UpdateApplicationSandbox com = new UpdateApplicationSandbox(applicationSandboxToBeUpdated);

		int usedTicketId;

		synchronized (ApplicationSandboxController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ApplicationSandboxUpdated.class,
				event -> sendApplicationSandboxChangedMessage(((ApplicationSandboxUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ApplicationSandbox from the database
	 * 
	 * @param applicationSandboxId:
	 *            the id of the ApplicationSandbox thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteapplicationSandboxById(@RequestParam(value = "applicationSandboxId") String applicationSandboxId) {

		DeleteApplicationSandbox com = new DeleteApplicationSandbox(applicationSandboxId);

		int usedTicketId;

		synchronized (ApplicationSandboxController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ApplicationSandboxDeleted.class,
				event -> sendApplicationSandboxChangedMessage(((ApplicationSandboxDeleted) event).isSuccess(), usedTicketId));

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

	public void sendApplicationSandboxChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/applicationSandbox/\" plus one of the following: "
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
