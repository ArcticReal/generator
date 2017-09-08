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
import com.skytala.eCommerce.command.AddContentRole;
import com.skytala.eCommerce.command.DeleteContentRole;
import com.skytala.eCommerce.command.UpdateContentRole;
import com.skytala.eCommerce.entity.ContentRole;
import com.skytala.eCommerce.entity.ContentRoleMapper;
import com.skytala.eCommerce.event.ContentRoleAdded;
import com.skytala.eCommerce.event.ContentRoleDeleted;
import com.skytala.eCommerce.event.ContentRoleFound;
import com.skytala.eCommerce.event.ContentRoleUpdated;
import com.skytala.eCommerce.query.FindContentRolesBy;

@RestController
@RequestMapping("/api/contentRole")
public class ContentRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentRole
	 * @return a List with the ContentRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentRole> findContentRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentRolesBy query = new FindContentRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRoleFound.class,
				event -> sendContentRolesFoundMessage(((ContentRoleFound) event).getContentRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentRolesFoundMessage(List<ContentRole> contentRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentRoles);
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
	public boolean createContentRole(HttpServletRequest request) {

		ContentRole contentRoleToBeAdded = new ContentRole();
		try {
			contentRoleToBeAdded = ContentRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentRole(contentRoleToBeAdded);

	}

	/**
	 * creates a new ContentRole entry in the ofbiz database
	 * 
	 * @param contentRoleToBeAdded
	 *            the ContentRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentRole(ContentRole contentRoleToBeAdded) {

		AddContentRole com = new AddContentRole(contentRoleToBeAdded);
		int usedTicketId;

		synchronized (ContentRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRoleAdded.class,
				event -> sendContentRoleChangedMessage(((ContentRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentRole(HttpServletRequest request) {

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

		ContentRole contentRoleToBeUpdated = new ContentRole();

		try {
			contentRoleToBeUpdated = ContentRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentRole(contentRoleToBeUpdated);

	}

	/**
	 * Updates the ContentRole with the specific Id
	 * 
	 * @param contentRoleToBeUpdated the ContentRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentRole(ContentRole contentRoleToBeUpdated) {

		UpdateContentRole com = new UpdateContentRole(contentRoleToBeUpdated);

		int usedTicketId;

		synchronized (ContentRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRoleUpdated.class,
				event -> sendContentRoleChangedMessage(((ContentRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentRole from the database
	 * 
	 * @param contentRoleId:
	 *            the id of the ContentRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentRoleById(@RequestParam(value = "contentRoleId") String contentRoleId) {

		DeleteContentRole com = new DeleteContentRole(contentRoleId);

		int usedTicketId;

		synchronized (ContentRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentRoleDeleted.class,
				event -> sendContentRoleChangedMessage(((ContentRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentRole/\" plus one of the following: "
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
