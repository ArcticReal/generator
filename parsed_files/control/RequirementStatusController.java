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
import com.skytala.eCommerce.command.AddRequirementStatus;
import com.skytala.eCommerce.command.DeleteRequirementStatus;
import com.skytala.eCommerce.command.UpdateRequirementStatus;
import com.skytala.eCommerce.entity.RequirementStatus;
import com.skytala.eCommerce.entity.RequirementStatusMapper;
import com.skytala.eCommerce.event.RequirementStatusAdded;
import com.skytala.eCommerce.event.RequirementStatusDeleted;
import com.skytala.eCommerce.event.RequirementStatusFound;
import com.skytala.eCommerce.event.RequirementStatusUpdated;
import com.skytala.eCommerce.query.FindRequirementStatussBy;

@RestController
@RequestMapping("/api/requirementStatus")
public class RequirementStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RequirementStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RequirementStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RequirementStatus
	 * @return a List with the RequirementStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RequirementStatus> findRequirementStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindRequirementStatussBy query = new FindRequirementStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (RequirementStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementStatusFound.class,
				event -> sendRequirementStatussFoundMessage(((RequirementStatusFound) event).getRequirementStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRequirementStatussFoundMessage(List<RequirementStatus> requirementStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, requirementStatuss);
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
	public boolean createRequirementStatus(HttpServletRequest request) {

		RequirementStatus requirementStatusToBeAdded = new RequirementStatus();
		try {
			requirementStatusToBeAdded = RequirementStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRequirementStatus(requirementStatusToBeAdded);

	}

	/**
	 * creates a new RequirementStatus entry in the ofbiz database
	 * 
	 * @param requirementStatusToBeAdded
	 *            the RequirementStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRequirementStatus(RequirementStatus requirementStatusToBeAdded) {

		AddRequirementStatus com = new AddRequirementStatus(requirementStatusToBeAdded);
		int usedTicketId;

		synchronized (RequirementStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementStatusAdded.class,
				event -> sendRequirementStatusChangedMessage(((RequirementStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRequirementStatus(HttpServletRequest request) {

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

		RequirementStatus requirementStatusToBeUpdated = new RequirementStatus();

		try {
			requirementStatusToBeUpdated = RequirementStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRequirementStatus(requirementStatusToBeUpdated);

	}

	/**
	 * Updates the RequirementStatus with the specific Id
	 * 
	 * @param requirementStatusToBeUpdated the RequirementStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRequirementStatus(RequirementStatus requirementStatusToBeUpdated) {

		UpdateRequirementStatus com = new UpdateRequirementStatus(requirementStatusToBeUpdated);

		int usedTicketId;

		synchronized (RequirementStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementStatusUpdated.class,
				event -> sendRequirementStatusChangedMessage(((RequirementStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RequirementStatus from the database
	 * 
	 * @param requirementStatusId:
	 *            the id of the RequirementStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterequirementStatusById(@RequestParam(value = "requirementStatusId") String requirementStatusId) {

		DeleteRequirementStatus com = new DeleteRequirementStatus(requirementStatusId);

		int usedTicketId;

		synchronized (RequirementStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementStatusDeleted.class,
				event -> sendRequirementStatusChangedMessage(((RequirementStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRequirementStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/requirementStatus/\" plus one of the following: "
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
