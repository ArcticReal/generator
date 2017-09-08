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
import com.skytala.eCommerce.command.AddSurveyQuestion;
import com.skytala.eCommerce.command.DeleteSurveyQuestion;
import com.skytala.eCommerce.command.UpdateSurveyQuestion;
import com.skytala.eCommerce.entity.SurveyQuestion;
import com.skytala.eCommerce.entity.SurveyQuestionMapper;
import com.skytala.eCommerce.event.SurveyQuestionAdded;
import com.skytala.eCommerce.event.SurveyQuestionDeleted;
import com.skytala.eCommerce.event.SurveyQuestionFound;
import com.skytala.eCommerce.event.SurveyQuestionUpdated;
import com.skytala.eCommerce.query.FindSurveyQuestionsBy;

@RestController
@RequestMapping("/api/surveyQuestion")
public class SurveyQuestionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyQuestion>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyQuestionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyQuestion
	 * @return a List with the SurveyQuestions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyQuestion> findSurveyQuestionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyQuestionsBy query = new FindSurveyQuestionsBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyQuestionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionFound.class,
				event -> sendSurveyQuestionsFoundMessage(((SurveyQuestionFound) event).getSurveyQuestions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyQuestionsFoundMessage(List<SurveyQuestion> surveyQuestions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyQuestions);
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
	public boolean createSurveyQuestion(HttpServletRequest request) {

		SurveyQuestion surveyQuestionToBeAdded = new SurveyQuestion();
		try {
			surveyQuestionToBeAdded = SurveyQuestionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyQuestion(surveyQuestionToBeAdded);

	}

	/**
	 * creates a new SurveyQuestion entry in the ofbiz database
	 * 
	 * @param surveyQuestionToBeAdded
	 *            the SurveyQuestion thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyQuestion(SurveyQuestion surveyQuestionToBeAdded) {

		AddSurveyQuestion com = new AddSurveyQuestion(surveyQuestionToBeAdded);
		int usedTicketId;

		synchronized (SurveyQuestionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionAdded.class,
				event -> sendSurveyQuestionChangedMessage(((SurveyQuestionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyQuestion(HttpServletRequest request) {

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

		SurveyQuestion surveyQuestionToBeUpdated = new SurveyQuestion();

		try {
			surveyQuestionToBeUpdated = SurveyQuestionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyQuestion(surveyQuestionToBeUpdated);

	}

	/**
	 * Updates the SurveyQuestion with the specific Id
	 * 
	 * @param surveyQuestionToBeUpdated the SurveyQuestion thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyQuestion(SurveyQuestion surveyQuestionToBeUpdated) {

		UpdateSurveyQuestion com = new UpdateSurveyQuestion(surveyQuestionToBeUpdated);

		int usedTicketId;

		synchronized (SurveyQuestionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionUpdated.class,
				event -> sendSurveyQuestionChangedMessage(((SurveyQuestionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyQuestion from the database
	 * 
	 * @param surveyQuestionId:
	 *            the id of the SurveyQuestion thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyQuestionById(@RequestParam(value = "surveyQuestionId") String surveyQuestionId) {

		DeleteSurveyQuestion com = new DeleteSurveyQuestion(surveyQuestionId);

		int usedTicketId;

		synchronized (SurveyQuestionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionDeleted.class,
				event -> sendSurveyQuestionChangedMessage(((SurveyQuestionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyQuestionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyQuestion/\" plus one of the following: "
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
