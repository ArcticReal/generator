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
import com.skytala.eCommerce.command.AddUserLoginSecurityGroup;
import com.skytala.eCommerce.command.DeleteUserLoginSecurityGroup;
import com.skytala.eCommerce.command.UpdateUserLoginSecurityGroup;
import com.skytala.eCommerce.entity.UserLoginSecurityGroup;
import com.skytala.eCommerce.entity.UserLoginSecurityGroupMapper;
import com.skytala.eCommerce.event.UserLoginSecurityGroupAdded;
import com.skytala.eCommerce.event.UserLoginSecurityGroupDeleted;
import com.skytala.eCommerce.event.UserLoginSecurityGroupFound;
import com.skytala.eCommerce.event.UserLoginSecurityGroupUpdated;
import com.skytala.eCommerce.query.FindUserLoginSecurityGroupsBy;

@RestController
@RequestMapping("/api/userLoginSecurityGroup")
public class UserLoginSecurityGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<UserLoginSecurityGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public UserLoginSecurityGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a UserLoginSecurityGroup
	 * @return a List with the UserLoginSecurityGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<UserLoginSecurityGroup> findUserLoginSecurityGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindUserLoginSecurityGroupsBy query = new FindUserLoginSecurityGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (UserLoginSecurityGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSecurityGroupFound.class,
				event -> sendUserLoginSecurityGroupsFoundMessage(((UserLoginSecurityGroupFound) event).getUserLoginSecurityGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendUserLoginSecurityGroupsFoundMessage(List<UserLoginSecurityGroup> userLoginSecurityGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, userLoginSecurityGroups);
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
	public boolean createUserLoginSecurityGroup(HttpServletRequest request) {

		UserLoginSecurityGroup userLoginSecurityGroupToBeAdded = new UserLoginSecurityGroup();
		try {
			userLoginSecurityGroupToBeAdded = UserLoginSecurityGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createUserLoginSecurityGroup(userLoginSecurityGroupToBeAdded);

	}

	/**
	 * creates a new UserLoginSecurityGroup entry in the ofbiz database
	 * 
	 * @param userLoginSecurityGroupToBeAdded
	 *            the UserLoginSecurityGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createUserLoginSecurityGroup(UserLoginSecurityGroup userLoginSecurityGroupToBeAdded) {

		AddUserLoginSecurityGroup com = new AddUserLoginSecurityGroup(userLoginSecurityGroupToBeAdded);
		int usedTicketId;

		synchronized (UserLoginSecurityGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSecurityGroupAdded.class,
				event -> sendUserLoginSecurityGroupChangedMessage(((UserLoginSecurityGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateUserLoginSecurityGroup(HttpServletRequest request) {

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

		UserLoginSecurityGroup userLoginSecurityGroupToBeUpdated = new UserLoginSecurityGroup();

		try {
			userLoginSecurityGroupToBeUpdated = UserLoginSecurityGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateUserLoginSecurityGroup(userLoginSecurityGroupToBeUpdated);

	}

	/**
	 * Updates the UserLoginSecurityGroup with the specific Id
	 * 
	 * @param userLoginSecurityGroupToBeUpdated the UserLoginSecurityGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateUserLoginSecurityGroup(UserLoginSecurityGroup userLoginSecurityGroupToBeUpdated) {

		UpdateUserLoginSecurityGroup com = new UpdateUserLoginSecurityGroup(userLoginSecurityGroupToBeUpdated);

		int usedTicketId;

		synchronized (UserLoginSecurityGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSecurityGroupUpdated.class,
				event -> sendUserLoginSecurityGroupChangedMessage(((UserLoginSecurityGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a UserLoginSecurityGroup from the database
	 * 
	 * @param userLoginSecurityGroupId:
	 *            the id of the UserLoginSecurityGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteuserLoginSecurityGroupById(@RequestParam(value = "userLoginSecurityGroupId") String userLoginSecurityGroupId) {

		DeleteUserLoginSecurityGroup com = new DeleteUserLoginSecurityGroup(userLoginSecurityGroupId);

		int usedTicketId;

		synchronized (UserLoginSecurityGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSecurityGroupDeleted.class,
				event -> sendUserLoginSecurityGroupChangedMessage(((UserLoginSecurityGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendUserLoginSecurityGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/userLoginSecurityGroup/\" plus one of the following: "
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
