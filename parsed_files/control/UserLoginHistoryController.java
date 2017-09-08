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
import com.skytala.eCommerce.command.AddUserLoginHistory;
import com.skytala.eCommerce.command.DeleteUserLoginHistory;
import com.skytala.eCommerce.command.UpdateUserLoginHistory;
import com.skytala.eCommerce.entity.UserLoginHistory;
import com.skytala.eCommerce.entity.UserLoginHistoryMapper;
import com.skytala.eCommerce.event.UserLoginHistoryAdded;
import com.skytala.eCommerce.event.UserLoginHistoryDeleted;
import com.skytala.eCommerce.event.UserLoginHistoryFound;
import com.skytala.eCommerce.event.UserLoginHistoryUpdated;
import com.skytala.eCommerce.query.FindUserLoginHistorysBy;

@RestController
@RequestMapping("/api/userLoginHistory")
public class UserLoginHistoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<UserLoginHistory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public UserLoginHistoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a UserLoginHistory
	 * @return a List with the UserLoginHistorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<UserLoginHistory> findUserLoginHistorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindUserLoginHistorysBy query = new FindUserLoginHistorysBy(allRequestParams);

		int usedTicketId;

		synchronized (UserLoginHistoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginHistoryFound.class,
				event -> sendUserLoginHistorysFoundMessage(((UserLoginHistoryFound) event).getUserLoginHistorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendUserLoginHistorysFoundMessage(List<UserLoginHistory> userLoginHistorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, userLoginHistorys);
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
	public boolean createUserLoginHistory(HttpServletRequest request) {

		UserLoginHistory userLoginHistoryToBeAdded = new UserLoginHistory();
		try {
			userLoginHistoryToBeAdded = UserLoginHistoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createUserLoginHistory(userLoginHistoryToBeAdded);

	}

	/**
	 * creates a new UserLoginHistory entry in the ofbiz database
	 * 
	 * @param userLoginHistoryToBeAdded
	 *            the UserLoginHistory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createUserLoginHistory(UserLoginHistory userLoginHistoryToBeAdded) {

		AddUserLoginHistory com = new AddUserLoginHistory(userLoginHistoryToBeAdded);
		int usedTicketId;

		synchronized (UserLoginHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginHistoryAdded.class,
				event -> sendUserLoginHistoryChangedMessage(((UserLoginHistoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateUserLoginHistory(HttpServletRequest request) {

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

		UserLoginHistory userLoginHistoryToBeUpdated = new UserLoginHistory();

		try {
			userLoginHistoryToBeUpdated = UserLoginHistoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateUserLoginHistory(userLoginHistoryToBeUpdated);

	}

	/**
	 * Updates the UserLoginHistory with the specific Id
	 * 
	 * @param userLoginHistoryToBeUpdated the UserLoginHistory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateUserLoginHistory(UserLoginHistory userLoginHistoryToBeUpdated) {

		UpdateUserLoginHistory com = new UpdateUserLoginHistory(userLoginHistoryToBeUpdated);

		int usedTicketId;

		synchronized (UserLoginHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginHistoryUpdated.class,
				event -> sendUserLoginHistoryChangedMessage(((UserLoginHistoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a UserLoginHistory from the database
	 * 
	 * @param userLoginHistoryId:
	 *            the id of the UserLoginHistory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteuserLoginHistoryById(@RequestParam(value = "userLoginHistoryId") String userLoginHistoryId) {

		DeleteUserLoginHistory com = new DeleteUserLoginHistory(userLoginHistoryId);

		int usedTicketId;

		synchronized (UserLoginHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginHistoryDeleted.class,
				event -> sendUserLoginHistoryChangedMessage(((UserLoginHistoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendUserLoginHistoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/userLoginHistory/\" plus one of the following: "
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
