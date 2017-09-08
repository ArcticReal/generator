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
import com.skytala.eCommerce.command.AddGlAccountGroup;
import com.skytala.eCommerce.command.DeleteGlAccountGroup;
import com.skytala.eCommerce.command.UpdateGlAccountGroup;
import com.skytala.eCommerce.entity.GlAccountGroup;
import com.skytala.eCommerce.entity.GlAccountGroupMapper;
import com.skytala.eCommerce.event.GlAccountGroupAdded;
import com.skytala.eCommerce.event.GlAccountGroupDeleted;
import com.skytala.eCommerce.event.GlAccountGroupFound;
import com.skytala.eCommerce.event.GlAccountGroupUpdated;
import com.skytala.eCommerce.query.FindGlAccountGroupsBy;

@RestController
@RequestMapping("/api/glAccountGroup")
public class GlAccountGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccountGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccountGroup
	 * @return a List with the GlAccountGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccountGroup> findGlAccountGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountGroupsBy query = new FindGlAccountGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupFound.class,
				event -> sendGlAccountGroupsFoundMessage(((GlAccountGroupFound) event).getGlAccountGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountGroupsFoundMessage(List<GlAccountGroup> glAccountGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccountGroups);
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
	public boolean createGlAccountGroup(HttpServletRequest request) {

		GlAccountGroup glAccountGroupToBeAdded = new GlAccountGroup();
		try {
			glAccountGroupToBeAdded = GlAccountGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccountGroup(glAccountGroupToBeAdded);

	}

	/**
	 * creates a new GlAccountGroup entry in the ofbiz database
	 * 
	 * @param glAccountGroupToBeAdded
	 *            the GlAccountGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccountGroup(GlAccountGroup glAccountGroupToBeAdded) {

		AddGlAccountGroup com = new AddGlAccountGroup(glAccountGroupToBeAdded);
		int usedTicketId;

		synchronized (GlAccountGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupAdded.class,
				event -> sendGlAccountGroupChangedMessage(((GlAccountGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccountGroup(HttpServletRequest request) {

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

		GlAccountGroup glAccountGroupToBeUpdated = new GlAccountGroup();

		try {
			glAccountGroupToBeUpdated = GlAccountGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccountGroup(glAccountGroupToBeUpdated);

	}

	/**
	 * Updates the GlAccountGroup with the specific Id
	 * 
	 * @param glAccountGroupToBeUpdated the GlAccountGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccountGroup(GlAccountGroup glAccountGroupToBeUpdated) {

		UpdateGlAccountGroup com = new UpdateGlAccountGroup(glAccountGroupToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupUpdated.class,
				event -> sendGlAccountGroupChangedMessage(((GlAccountGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccountGroup from the database
	 * 
	 * @param glAccountGroupId:
	 *            the id of the GlAccountGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountGroupById(@RequestParam(value = "glAccountGroupId") String glAccountGroupId) {

		DeleteGlAccountGroup com = new DeleteGlAccountGroup(glAccountGroupId);

		int usedTicketId;

		synchronized (GlAccountGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupDeleted.class,
				event -> sendGlAccountGroupChangedMessage(((GlAccountGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccountGroup/\" plus one of the following: "
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
