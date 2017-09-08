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
import com.skytala.eCommerce.command.AddRequirementCustRequest;
import com.skytala.eCommerce.command.DeleteRequirementCustRequest;
import com.skytala.eCommerce.command.UpdateRequirementCustRequest;
import com.skytala.eCommerce.entity.RequirementCustRequest;
import com.skytala.eCommerce.entity.RequirementCustRequestMapper;
import com.skytala.eCommerce.event.RequirementCustRequestAdded;
import com.skytala.eCommerce.event.RequirementCustRequestDeleted;
import com.skytala.eCommerce.event.RequirementCustRequestFound;
import com.skytala.eCommerce.event.RequirementCustRequestUpdated;
import com.skytala.eCommerce.query.FindRequirementCustRequestsBy;

@RestController
@RequestMapping("/api/requirementCustRequest")
public class RequirementCustRequestController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RequirementCustRequest>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RequirementCustRequestController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RequirementCustRequest
	 * @return a List with the RequirementCustRequests
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RequirementCustRequest> findRequirementCustRequestsBy(@RequestParam Map<String, String> allRequestParams) {

		FindRequirementCustRequestsBy query = new FindRequirementCustRequestsBy(allRequestParams);

		int usedTicketId;

		synchronized (RequirementCustRequestController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementCustRequestFound.class,
				event -> sendRequirementCustRequestsFoundMessage(((RequirementCustRequestFound) event).getRequirementCustRequests(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRequirementCustRequestsFoundMessage(List<RequirementCustRequest> requirementCustRequests, int usedTicketId) {
		queryReturnVal.put(usedTicketId, requirementCustRequests);
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
	public boolean createRequirementCustRequest(HttpServletRequest request) {

		RequirementCustRequest requirementCustRequestToBeAdded = new RequirementCustRequest();
		try {
			requirementCustRequestToBeAdded = RequirementCustRequestMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRequirementCustRequest(requirementCustRequestToBeAdded);

	}

	/**
	 * creates a new RequirementCustRequest entry in the ofbiz database
	 * 
	 * @param requirementCustRequestToBeAdded
	 *            the RequirementCustRequest thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRequirementCustRequest(RequirementCustRequest requirementCustRequestToBeAdded) {

		AddRequirementCustRequest com = new AddRequirementCustRequest(requirementCustRequestToBeAdded);
		int usedTicketId;

		synchronized (RequirementCustRequestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementCustRequestAdded.class,
				event -> sendRequirementCustRequestChangedMessage(((RequirementCustRequestAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRequirementCustRequest(HttpServletRequest request) {

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

		RequirementCustRequest requirementCustRequestToBeUpdated = new RequirementCustRequest();

		try {
			requirementCustRequestToBeUpdated = RequirementCustRequestMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRequirementCustRequest(requirementCustRequestToBeUpdated);

	}

	/**
	 * Updates the RequirementCustRequest with the specific Id
	 * 
	 * @param requirementCustRequestToBeUpdated the RequirementCustRequest thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRequirementCustRequest(RequirementCustRequest requirementCustRequestToBeUpdated) {

		UpdateRequirementCustRequest com = new UpdateRequirementCustRequest(requirementCustRequestToBeUpdated);

		int usedTicketId;

		synchronized (RequirementCustRequestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementCustRequestUpdated.class,
				event -> sendRequirementCustRequestChangedMessage(((RequirementCustRequestUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RequirementCustRequest from the database
	 * 
	 * @param requirementCustRequestId:
	 *            the id of the RequirementCustRequest thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterequirementCustRequestById(@RequestParam(value = "requirementCustRequestId") String requirementCustRequestId) {

		DeleteRequirementCustRequest com = new DeleteRequirementCustRequest(requirementCustRequestId);

		int usedTicketId;

		synchronized (RequirementCustRequestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementCustRequestDeleted.class,
				event -> sendRequirementCustRequestChangedMessage(((RequirementCustRequestDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRequirementCustRequestChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/requirementCustRequest/\" plus one of the following: "
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
