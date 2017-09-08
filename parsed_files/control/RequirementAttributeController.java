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
import com.skytala.eCommerce.command.AddRequirementAttribute;
import com.skytala.eCommerce.command.DeleteRequirementAttribute;
import com.skytala.eCommerce.command.UpdateRequirementAttribute;
import com.skytala.eCommerce.entity.RequirementAttribute;
import com.skytala.eCommerce.entity.RequirementAttributeMapper;
import com.skytala.eCommerce.event.RequirementAttributeAdded;
import com.skytala.eCommerce.event.RequirementAttributeDeleted;
import com.skytala.eCommerce.event.RequirementAttributeFound;
import com.skytala.eCommerce.event.RequirementAttributeUpdated;
import com.skytala.eCommerce.query.FindRequirementAttributesBy;

@RestController
@RequestMapping("/api/requirementAttribute")
public class RequirementAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RequirementAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RequirementAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RequirementAttribute
	 * @return a List with the RequirementAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RequirementAttribute> findRequirementAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindRequirementAttributesBy query = new FindRequirementAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (RequirementAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementAttributeFound.class,
				event -> sendRequirementAttributesFoundMessage(((RequirementAttributeFound) event).getRequirementAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRequirementAttributesFoundMessage(List<RequirementAttribute> requirementAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, requirementAttributes);
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
	public boolean createRequirementAttribute(HttpServletRequest request) {

		RequirementAttribute requirementAttributeToBeAdded = new RequirementAttribute();
		try {
			requirementAttributeToBeAdded = RequirementAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRequirementAttribute(requirementAttributeToBeAdded);

	}

	/**
	 * creates a new RequirementAttribute entry in the ofbiz database
	 * 
	 * @param requirementAttributeToBeAdded
	 *            the RequirementAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRequirementAttribute(RequirementAttribute requirementAttributeToBeAdded) {

		AddRequirementAttribute com = new AddRequirementAttribute(requirementAttributeToBeAdded);
		int usedTicketId;

		synchronized (RequirementAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementAttributeAdded.class,
				event -> sendRequirementAttributeChangedMessage(((RequirementAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRequirementAttribute(HttpServletRequest request) {

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

		RequirementAttribute requirementAttributeToBeUpdated = new RequirementAttribute();

		try {
			requirementAttributeToBeUpdated = RequirementAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRequirementAttribute(requirementAttributeToBeUpdated);

	}

	/**
	 * Updates the RequirementAttribute with the specific Id
	 * 
	 * @param requirementAttributeToBeUpdated the RequirementAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRequirementAttribute(RequirementAttribute requirementAttributeToBeUpdated) {

		UpdateRequirementAttribute com = new UpdateRequirementAttribute(requirementAttributeToBeUpdated);

		int usedTicketId;

		synchronized (RequirementAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementAttributeUpdated.class,
				event -> sendRequirementAttributeChangedMessage(((RequirementAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RequirementAttribute from the database
	 * 
	 * @param requirementAttributeId:
	 *            the id of the RequirementAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterequirementAttributeById(@RequestParam(value = "requirementAttributeId") String requirementAttributeId) {

		DeleteRequirementAttribute com = new DeleteRequirementAttribute(requirementAttributeId);

		int usedTicketId;

		synchronized (RequirementAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RequirementAttributeDeleted.class,
				event -> sendRequirementAttributeChangedMessage(((RequirementAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRequirementAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/requirementAttribute/\" plus one of the following: "
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
