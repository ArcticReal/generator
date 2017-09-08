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
import com.skytala.eCommerce.command.AddRequirementTypeAttr;
import com.skytala.eCommerce.command.DeleteRequirementTypeAttr;
import com.skytala.eCommerce.command.UpdateRequirementTypeAttr;
import com.skytala.eCommerce.entity.RequirementTypeAttr;
import com.skytala.eCommerce.entity.RequirementTypeAttrMapper;
import com.skytala.eCommerce.event.RequirementTypeAttrAdded;
import com.skytala.eCommerce.event.RequirementTypeAttrDeleted;
import com.skytala.eCommerce.event.RequirementTypeAttrFound;
import com.skytala.eCommerce.event.RequirementTypeAttrUpdated;
import com.skytala.eCommerce.query.FindRequirementTypeAttrsBy;

@RestController
@RequestMapping("/api/requirementTypeAttr")
public class RequirementTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RequirementTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RequirementTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RequirementTypeAttr
	 * @return a List with the RequirementTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RequirementTypeAttr> findRequirementTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindRequirementTypeAttrsBy query = new FindRequirementTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (RequirementTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementTypeAttrFound.class,
				event -> sendRequirementTypeAttrsFoundMessage(((RequirementTypeAttrFound) event).getRequirementTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRequirementTypeAttrsFoundMessage(List<RequirementTypeAttr> requirementTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, requirementTypeAttrs);
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
	public boolean createRequirementTypeAttr(HttpServletRequest request) {

		RequirementTypeAttr requirementTypeAttrToBeAdded = new RequirementTypeAttr();
		try {
			requirementTypeAttrToBeAdded = RequirementTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRequirementTypeAttr(requirementTypeAttrToBeAdded);

	}

	/**
	 * creates a new RequirementTypeAttr entry in the ofbiz database
	 * 
	 * @param requirementTypeAttrToBeAdded
	 *            the RequirementTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRequirementTypeAttr(RequirementTypeAttr requirementTypeAttrToBeAdded) {

		AddRequirementTypeAttr com = new AddRequirementTypeAttr(requirementTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (RequirementTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementTypeAttrAdded.class,
				event -> sendRequirementTypeAttrChangedMessage(((RequirementTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRequirementTypeAttr(HttpServletRequest request) {

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

		RequirementTypeAttr requirementTypeAttrToBeUpdated = new RequirementTypeAttr();

		try {
			requirementTypeAttrToBeUpdated = RequirementTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRequirementTypeAttr(requirementTypeAttrToBeUpdated);

	}

	/**
	 * Updates the RequirementTypeAttr with the specific Id
	 * 
	 * @param requirementTypeAttrToBeUpdated the RequirementTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRequirementTypeAttr(RequirementTypeAttr requirementTypeAttrToBeUpdated) {

		UpdateRequirementTypeAttr com = new UpdateRequirementTypeAttr(requirementTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (RequirementTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementTypeAttrUpdated.class,
				event -> sendRequirementTypeAttrChangedMessage(((RequirementTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RequirementTypeAttr from the database
	 * 
	 * @param requirementTypeAttrId:
	 *            the id of the RequirementTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterequirementTypeAttrById(@RequestParam(value = "requirementTypeAttrId") String requirementTypeAttrId) {

		DeleteRequirementTypeAttr com = new DeleteRequirementTypeAttr(requirementTypeAttrId);

		int usedTicketId;

		synchronized (RequirementTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementTypeAttrDeleted.class,
				event -> sendRequirementTypeAttrChangedMessage(((RequirementTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRequirementTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/requirementTypeAttr/\" plus one of the following: "
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
