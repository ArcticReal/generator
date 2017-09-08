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
import com.skytala.eCommerce.command.AddUserLoginSecurityQuestion;
import com.skytala.eCommerce.command.DeleteUserLoginSecurityQuestion;
import com.skytala.eCommerce.command.UpdateUserLoginSecurityQuestion;
import com.skytala.eCommerce.entity.UserLoginSecurityQuestion;
import com.skytala.eCommerce.entity.UserLoginSecurityQuestionMapper;
import com.skytala.eCommerce.event.UserLoginSecurityQuestionAdded;
import com.skytala.eCommerce.event.UserLoginSecurityQuestionDeleted;
import com.skytala.eCommerce.event.UserLoginSecurityQuestionFound;
import com.skytala.eCommerce.event.UserLoginSecurityQuestionUpdated;
import com.skytala.eCommerce.query.FindUserLoginSecurityQuestionsBy;

@RestController
@RequestMapping("/api/userLoginSecurityQuestion")
public class UserLoginSecurityQuestionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<UserLoginSecurityQuestion>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public UserLoginSecurityQuestionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a UserLoginSecurityQuestion
	 * @return a List with the UserLoginSecurityQuestions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<UserLoginSecurityQuestion> findUserLoginSecurityQuestionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindUserLoginSecurityQuestionsBy query = new FindUserLoginSecurityQuestionsBy(allRequestParams);

		int usedTicketId;

		synchronized (UserLoginSecurityQuestionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSecurityQuestionFound.class,
				event -> sendUserLoginSecurityQuestionsFoundMessage(((UserLoginSecurityQuestionFound) event).getUserLoginSecurityQuestions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendUserLoginSecurityQuestionsFoundMessage(List<UserLoginSecurityQuestion> userLoginSecurityQuestions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, userLoginSecurityQuestions);
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
	public boolean createUserLoginSecurityQuestion(HttpServletRequest request) {

		UserLoginSecurityQuestion userLoginSecurityQuestionToBeAdded = new UserLoginSecurityQuestion();
		try {
			userLoginSecurityQuestionToBeAdded = UserLoginSecurityQuestionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createUserLoginSecurityQuestion(userLoginSecurityQuestionToBeAdded);

	}

	/**
	 * creates a new UserLoginSecurityQuestion entry in the ofbiz database
	 * 
	 * @param userLoginSecurityQuestionToBeAdded
	 *            the UserLoginSecurityQuestion thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createUserLoginSecurityQuestion(UserLoginSecurityQuestion userLoginSecurityQuestionToBeAdded) {

		AddUserLoginSecurityQuestion com = new AddUserLoginSecurityQuestion(userLoginSecurityQuestionToBeAdded);
		int usedTicketId;

		synchronized (UserLoginSecurityQuestionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSecurityQuestionAdded.class,
				event -> sendUserLoginSecurityQuestionChangedMessage(((UserLoginSecurityQuestionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateUserLoginSecurityQuestion(HttpServletRequest request) {

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

		UserLoginSecurityQuestion userLoginSecurityQuestionToBeUpdated = new UserLoginSecurityQuestion();

		try {
			userLoginSecurityQuestionToBeUpdated = UserLoginSecurityQuestionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateUserLoginSecurityQuestion(userLoginSecurityQuestionToBeUpdated);

	}

	/**
	 * Updates the UserLoginSecurityQuestion with the specific Id
	 * 
	 * @param userLoginSecurityQuestionToBeUpdated the UserLoginSecurityQuestion thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateUserLoginSecurityQuestion(UserLoginSecurityQuestion userLoginSecurityQuestionToBeUpdated) {

		UpdateUserLoginSecurityQuestion com = new UpdateUserLoginSecurityQuestion(userLoginSecurityQuestionToBeUpdated);

		int usedTicketId;

		synchronized (UserLoginSecurityQuestionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSecurityQuestionUpdated.class,
				event -> sendUserLoginSecurityQuestionChangedMessage(((UserLoginSecurityQuestionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a UserLoginSecurityQuestion from the database
	 * 
	 * @param userLoginSecurityQuestionId:
	 *            the id of the UserLoginSecurityQuestion thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteuserLoginSecurityQuestionById(@RequestParam(value = "userLoginSecurityQuestionId") String userLoginSecurityQuestionId) {

		DeleteUserLoginSecurityQuestion com = new DeleteUserLoginSecurityQuestion(userLoginSecurityQuestionId);

		int usedTicketId;

		synchronized (UserLoginSecurityQuestionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UserLoginSecurityQuestionDeleted.class,
				event -> sendUserLoginSecurityQuestionChangedMessage(((UserLoginSecurityQuestionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendUserLoginSecurityQuestionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/userLoginSecurityQuestion/\" plus one of the following: "
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
