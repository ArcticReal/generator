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
import com.skytala.eCommerce.command.AddRequirement;
import com.skytala.eCommerce.command.DeleteRequirement;
import com.skytala.eCommerce.command.UpdateRequirement;
import com.skytala.eCommerce.entity.Requirement;
import com.skytala.eCommerce.entity.RequirementMapper;
import com.skytala.eCommerce.event.RequirementAdded;
import com.skytala.eCommerce.event.RequirementDeleted;
import com.skytala.eCommerce.event.RequirementFound;
import com.skytala.eCommerce.event.RequirementUpdated;
import com.skytala.eCommerce.query.FindRequirementsBy;

@RestController
@RequestMapping("/api/requirement")
public class RequirementController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Requirement>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RequirementController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Requirement
	 * @return a List with the Requirements
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Requirement> findRequirementsBy(@RequestParam Map<String, String> allRequestParams) {

		FindRequirementsBy query = new FindRequirementsBy(allRequestParams);

		int usedTicketId;

		synchronized (RequirementController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementFound.class,
				event -> sendRequirementsFoundMessage(((RequirementFound) event).getRequirements(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRequirementsFoundMessage(List<Requirement> requirements, int usedTicketId) {
		queryReturnVal.put(usedTicketId, requirements);
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
	public boolean createRequirement(HttpServletRequest request) {

		Requirement requirementToBeAdded = new Requirement();
		try {
			requirementToBeAdded = RequirementMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRequirement(requirementToBeAdded);

	}

	/**
	 * creates a new Requirement entry in the ofbiz database
	 * 
	 * @param requirementToBeAdded
	 *            the Requirement thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRequirement(Requirement requirementToBeAdded) {

		AddRequirement com = new AddRequirement(requirementToBeAdded);
		int usedTicketId;

		synchronized (RequirementController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementAdded.class,
				event -> sendRequirementChangedMessage(((RequirementAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRequirement(HttpServletRequest request) {

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

		Requirement requirementToBeUpdated = new Requirement();

		try {
			requirementToBeUpdated = RequirementMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRequirement(requirementToBeUpdated);

	}

	/**
	 * Updates the Requirement with the specific Id
	 * 
	 * @param requirementToBeUpdated the Requirement thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRequirement(Requirement requirementToBeUpdated) {

		UpdateRequirement com = new UpdateRequirement(requirementToBeUpdated);

		int usedTicketId;

		synchronized (RequirementController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementUpdated.class,
				event -> sendRequirementChangedMessage(((RequirementUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Requirement from the database
	 * 
	 * @param requirementId:
	 *            the id of the Requirement thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterequirementById(@RequestParam(value = "requirementId") String requirementId) {

		DeleteRequirement com = new DeleteRequirement(requirementId);

		int usedTicketId;

		synchronized (RequirementController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementDeleted.class,
				event -> sendRequirementChangedMessage(((RequirementDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRequirementChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/requirement/\" plus one of the following: "
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
