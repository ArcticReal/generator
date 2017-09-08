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
import com.skytala.eCommerce.command.AddSurvey;
import com.skytala.eCommerce.command.DeleteSurvey;
import com.skytala.eCommerce.command.UpdateSurvey;
import com.skytala.eCommerce.entity.Survey;
import com.skytala.eCommerce.entity.SurveyMapper;
import com.skytala.eCommerce.event.SurveyAdded;
import com.skytala.eCommerce.event.SurveyDeleted;
import com.skytala.eCommerce.event.SurveyFound;
import com.skytala.eCommerce.event.SurveyUpdated;
import com.skytala.eCommerce.query.FindSurveysBy;

@RestController
@RequestMapping("/api/survey")
public class SurveyController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Survey>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Survey
	 * @return a List with the Surveys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Survey> findSurveysBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveysBy query = new FindSurveysBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyFound.class,
				event -> sendSurveysFoundMessage(((SurveyFound) event).getSurveys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveysFoundMessage(List<Survey> surveys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveys);
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
	public boolean createSurvey(HttpServletRequest request) {

		Survey surveyToBeAdded = new Survey();
		try {
			surveyToBeAdded = SurveyMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurvey(surveyToBeAdded);

	}

	/**
	 * creates a new Survey entry in the ofbiz database
	 * 
	 * @param surveyToBeAdded
	 *            the Survey thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurvey(Survey surveyToBeAdded) {

		AddSurvey com = new AddSurvey(surveyToBeAdded);
		int usedTicketId;

		synchronized (SurveyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyAdded.class,
				event -> sendSurveyChangedMessage(((SurveyAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurvey(HttpServletRequest request) {

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

		Survey surveyToBeUpdated = new Survey();

		try {
			surveyToBeUpdated = SurveyMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurvey(surveyToBeUpdated);

	}

	/**
	 * Updates the Survey with the specific Id
	 * 
	 * @param surveyToBeUpdated the Survey thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurvey(Survey surveyToBeUpdated) {

		UpdateSurvey com = new UpdateSurvey(surveyToBeUpdated);

		int usedTicketId;

		synchronized (SurveyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyUpdated.class,
				event -> sendSurveyChangedMessage(((SurveyUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Survey from the database
	 * 
	 * @param surveyId:
	 *            the id of the Survey thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyById(@RequestParam(value = "surveyId") String surveyId) {

		DeleteSurvey com = new DeleteSurvey(surveyId);

		int usedTicketId;

		synchronized (SurveyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyDeleted.class,
				event -> sendSurveyChangedMessage(((SurveyDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/survey/\" plus one of the following: "
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
