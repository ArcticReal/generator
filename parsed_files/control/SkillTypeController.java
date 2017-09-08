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
import com.skytala.eCommerce.command.AddSkillType;
import com.skytala.eCommerce.command.DeleteSkillType;
import com.skytala.eCommerce.command.UpdateSkillType;
import com.skytala.eCommerce.entity.SkillType;
import com.skytala.eCommerce.entity.SkillTypeMapper;
import com.skytala.eCommerce.event.SkillTypeAdded;
import com.skytala.eCommerce.event.SkillTypeDeleted;
import com.skytala.eCommerce.event.SkillTypeFound;
import com.skytala.eCommerce.event.SkillTypeUpdated;
import com.skytala.eCommerce.query.FindSkillTypesBy;

@RestController
@RequestMapping("/api/skillType")
public class SkillTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SkillType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SkillTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SkillType
	 * @return a List with the SkillTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SkillType> findSkillTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSkillTypesBy query = new FindSkillTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (SkillTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SkillTypeFound.class,
				event -> sendSkillTypesFoundMessage(((SkillTypeFound) event).getSkillTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSkillTypesFoundMessage(List<SkillType> skillTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, skillTypes);
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
	public boolean createSkillType(HttpServletRequest request) {

		SkillType skillTypeToBeAdded = new SkillType();
		try {
			skillTypeToBeAdded = SkillTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSkillType(skillTypeToBeAdded);

	}

	/**
	 * creates a new SkillType entry in the ofbiz database
	 * 
	 * @param skillTypeToBeAdded
	 *            the SkillType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSkillType(SkillType skillTypeToBeAdded) {

		AddSkillType com = new AddSkillType(skillTypeToBeAdded);
		int usedTicketId;

		synchronized (SkillTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SkillTypeAdded.class,
				event -> sendSkillTypeChangedMessage(((SkillTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSkillType(HttpServletRequest request) {

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

		SkillType skillTypeToBeUpdated = new SkillType();

		try {
			skillTypeToBeUpdated = SkillTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSkillType(skillTypeToBeUpdated);

	}

	/**
	 * Updates the SkillType with the specific Id
	 * 
	 * @param skillTypeToBeUpdated the SkillType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSkillType(SkillType skillTypeToBeUpdated) {

		UpdateSkillType com = new UpdateSkillType(skillTypeToBeUpdated);

		int usedTicketId;

		synchronized (SkillTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SkillTypeUpdated.class,
				event -> sendSkillTypeChangedMessage(((SkillTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SkillType from the database
	 * 
	 * @param skillTypeId:
	 *            the id of the SkillType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteskillTypeById(@RequestParam(value = "skillTypeId") String skillTypeId) {

		DeleteSkillType com = new DeleteSkillType(skillTypeId);

		int usedTicketId;

		synchronized (SkillTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SkillTypeDeleted.class,
				event -> sendSkillTypeChangedMessage(((SkillTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSkillTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/skillType/\" plus one of the following: "
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
