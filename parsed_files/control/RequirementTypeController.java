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
import com.skytala.eCommerce.command.AddRequirementType;
import com.skytala.eCommerce.command.DeleteRequirementType;
import com.skytala.eCommerce.command.UpdateRequirementType;
import com.skytala.eCommerce.entity.RequirementType;
import com.skytala.eCommerce.entity.RequirementTypeMapper;
import com.skytala.eCommerce.event.RequirementTypeAdded;
import com.skytala.eCommerce.event.RequirementTypeDeleted;
import com.skytala.eCommerce.event.RequirementTypeFound;
import com.skytala.eCommerce.event.RequirementTypeUpdated;
import com.skytala.eCommerce.query.FindRequirementTypesBy;

@RestController
@RequestMapping("/api/requirementType")
public class RequirementTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RequirementType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RequirementTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RequirementType
	 * @return a List with the RequirementTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RequirementType> findRequirementTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindRequirementTypesBy query = new FindRequirementTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (RequirementTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementTypeFound.class,
				event -> sendRequirementTypesFoundMessage(((RequirementTypeFound) event).getRequirementTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRequirementTypesFoundMessage(List<RequirementType> requirementTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, requirementTypes);
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
	public boolean createRequirementType(HttpServletRequest request) {

		RequirementType requirementTypeToBeAdded = new RequirementType();
		try {
			requirementTypeToBeAdded = RequirementTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRequirementType(requirementTypeToBeAdded);

	}

	/**
	 * creates a new RequirementType entry in the ofbiz database
	 * 
	 * @param requirementTypeToBeAdded
	 *            the RequirementType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRequirementType(RequirementType requirementTypeToBeAdded) {

		AddRequirementType com = new AddRequirementType(requirementTypeToBeAdded);
		int usedTicketId;

		synchronized (RequirementTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementTypeAdded.class,
				event -> sendRequirementTypeChangedMessage(((RequirementTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRequirementType(HttpServletRequest request) {

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

		RequirementType requirementTypeToBeUpdated = new RequirementType();

		try {
			requirementTypeToBeUpdated = RequirementTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRequirementType(requirementTypeToBeUpdated);

	}

	/**
	 * Updates the RequirementType with the specific Id
	 * 
	 * @param requirementTypeToBeUpdated the RequirementType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRequirementType(RequirementType requirementTypeToBeUpdated) {

		UpdateRequirementType com = new UpdateRequirementType(requirementTypeToBeUpdated);

		int usedTicketId;

		synchronized (RequirementTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementTypeUpdated.class,
				event -> sendRequirementTypeChangedMessage(((RequirementTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RequirementType from the database
	 * 
	 * @param requirementTypeId:
	 *            the id of the RequirementType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterequirementTypeById(@RequestParam(value = "requirementTypeId") String requirementTypeId) {

		DeleteRequirementType com = new DeleteRequirementType(requirementTypeId);

		int usedTicketId;

		synchronized (RequirementTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementTypeDeleted.class,
				event -> sendRequirementTypeChangedMessage(((RequirementTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRequirementTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/requirementType/\" plus one of the following: "
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
