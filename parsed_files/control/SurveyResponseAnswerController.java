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
import com.skytala.eCommerce.command.AddSurveyResponseAnswer;
import com.skytala.eCommerce.command.DeleteSurveyResponseAnswer;
import com.skytala.eCommerce.command.UpdateSurveyResponseAnswer;
import com.skytala.eCommerce.entity.SurveyResponseAnswer;
import com.skytala.eCommerce.entity.SurveyResponseAnswerMapper;
import com.skytala.eCommerce.event.SurveyResponseAnswerAdded;
import com.skytala.eCommerce.event.SurveyResponseAnswerDeleted;
import com.skytala.eCommerce.event.SurveyResponseAnswerFound;
import com.skytala.eCommerce.event.SurveyResponseAnswerUpdated;
import com.skytala.eCommerce.query.FindSurveyResponseAnswersBy;

@RestController
@RequestMapping("/api/surveyResponseAnswer")
public class SurveyResponseAnswerController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyResponseAnswer>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyResponseAnswerController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyResponseAnswer
	 * @return a List with the SurveyResponseAnswers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyResponseAnswer> findSurveyResponseAnswersBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyResponseAnswersBy query = new FindSurveyResponseAnswersBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyResponseAnswerController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyResponseAnswerFound.class,
				event -> sendSurveyResponseAnswersFoundMessage(((SurveyResponseAnswerFound) event).getSurveyResponseAnswers(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyResponseAnswersFoundMessage(List<SurveyResponseAnswer> surveyResponseAnswers, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyResponseAnswers);
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
	public boolean createSurveyResponseAnswer(HttpServletRequest request) {

		SurveyResponseAnswer surveyResponseAnswerToBeAdded = new SurveyResponseAnswer();
		try {
			surveyResponseAnswerToBeAdded = SurveyResponseAnswerMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyResponseAnswer(surveyResponseAnswerToBeAdded);

	}

	/**
	 * creates a new SurveyResponseAnswer entry in the ofbiz database
	 * 
	 * @param surveyResponseAnswerToBeAdded
	 *            the SurveyResponseAnswer thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyResponseAnswer(SurveyResponseAnswer surveyResponseAnswerToBeAdded) {

		AddSurveyResponseAnswer com = new AddSurveyResponseAnswer(surveyResponseAnswerToBeAdded);
		int usedTicketId;

		synchronized (SurveyResponseAnswerController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyResponseAnswerAdded.class,
				event -> sendSurveyResponseAnswerChangedMessage(((SurveyResponseAnswerAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyResponseAnswer(HttpServletRequest request) {

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

		SurveyResponseAnswer surveyResponseAnswerToBeUpdated = new SurveyResponseAnswer();

		try {
			surveyResponseAnswerToBeUpdated = SurveyResponseAnswerMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyResponseAnswer(surveyResponseAnswerToBeUpdated);

	}

	/**
	 * Updates the SurveyResponseAnswer with the specific Id
	 * 
	 * @param surveyResponseAnswerToBeUpdated the SurveyResponseAnswer thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyResponseAnswer(SurveyResponseAnswer surveyResponseAnswerToBeUpdated) {

		UpdateSurveyResponseAnswer com = new UpdateSurveyResponseAnswer(surveyResponseAnswerToBeUpdated);

		int usedTicketId;

		synchronized (SurveyResponseAnswerController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyResponseAnswerUpdated.class,
				event -> sendSurveyResponseAnswerChangedMessage(((SurveyResponseAnswerUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyResponseAnswer from the database
	 * 
	 * @param surveyResponseAnswerId:
	 *            the id of the SurveyResponseAnswer thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyResponseAnswerById(@RequestParam(value = "surveyResponseAnswerId") String surveyResponseAnswerId) {

		DeleteSurveyResponseAnswer com = new DeleteSurveyResponseAnswer(surveyResponseAnswerId);

		int usedTicketId;

		synchronized (SurveyResponseAnswerController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyResponseAnswerDeleted.class,
				event -> sendSurveyResponseAnswerChangedMessage(((SurveyResponseAnswerDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyResponseAnswerChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyResponseAnswer/\" plus one of the following: "
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
