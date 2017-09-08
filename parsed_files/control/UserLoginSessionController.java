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
import com.skytala.eCommerce.command.AddUserLoginSession;
import com.skytala.eCommerce.command.DeleteUserLoginSession;
import com.skytala.eCommerce.command.UpdateUserLoginSession;
import com.skytala.eCommerce.entity.UserLoginSession;
import com.skytala.eCommerce.entity.UserLoginSessionMapper;
import com.skytala.eCommerce.event.UserLoginSessionAdded;
import com.skytala.eCommerce.event.UserLoginSessionDeleted;
import com.skytala.eCommerce.event.UserLoginSessionFound;
import com.skytala.eCommerce.event.UserLoginSessionUpdated;
import com.skytala.eCommerce.query.FindUserLoginSessionsBy;

@RestController
@RequestMapping("/api/userLoginSession")
public class UserLoginSessionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<UserLoginSession>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public UserLoginSessionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a UserLoginSession
	 * @return a List with the UserLoginSessions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<UserLoginSession> findUserLoginSessionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindUserLoginSessionsBy query = new FindUserLoginSessionsBy(allRequestParams);

		int usedTicketId;

		synchronized (UserLoginSessionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSessionFound.class,
				event -> sendUserLoginSessionsFoundMessage(((UserLoginSessionFound) event).getUserLoginSessions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendUserLoginSessionsFoundMessage(List<UserLoginSession> userLoginSessions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, userLoginSessions);
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
	public boolean createUserLoginSession(HttpServletRequest request) {

		UserLoginSession userLoginSessionToBeAdded = new UserLoginSession();
		try {
			userLoginSessionToBeAdded = UserLoginSessionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createUserLoginSession(userLoginSessionToBeAdded);

	}

	/**
	 * creates a new UserLoginSession entry in the ofbiz database
	 * 
	 * @param userLoginSessionToBeAdded
	 *            the UserLoginSession thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createUserLoginSession(UserLoginSession userLoginSessionToBeAdded) {

		AddUserLoginSession com = new AddUserLoginSession(userLoginSessionToBeAdded);
		int usedTicketId;

		synchronized (UserLoginSessionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSessionAdded.class,
				event -> sendUserLoginSessionChangedMessage(((UserLoginSessionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateUserLoginSession(HttpServletRequest request) {

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

		UserLoginSession userLoginSessionToBeUpdated = new UserLoginSession();

		try {
			userLoginSessionToBeUpdated = UserLoginSessionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateUserLoginSession(userLoginSessionToBeUpdated);

	}

	/**
	 * Updates the UserLoginSession with the specific Id
	 * 
	 * @param userLoginSessionToBeUpdated the UserLoginSession thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateUserLoginSession(UserLoginSession userLoginSessionToBeUpdated) {

		UpdateUserLoginSession com = new UpdateUserLoginSession(userLoginSessionToBeUpdated);

		int usedTicketId;

		synchronized (UserLoginSessionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSessionUpdated.class,
				event -> sendUserLoginSessionChangedMessage(((UserLoginSessionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a UserLoginSession from the database
	 * 
	 * @param userLoginSessionId:
	 *            the id of the UserLoginSession thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteuserLoginSessionById(@RequestParam(value = "userLoginSessionId") String userLoginSessionId) {

		DeleteUserLoginSession com = new DeleteUserLoginSession(userLoginSessionId);

		int usedTicketId;

		synchronized (UserLoginSessionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSessionDeleted.class,
				event -> sendUserLoginSessionChangedMessage(((UserLoginSessionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendUserLoginSessionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/userLoginSession/\" plus one of the following: "
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
