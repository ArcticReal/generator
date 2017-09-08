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
import com.skytala.eCommerce.command.AddSurveyQuestionOption;
import com.skytala.eCommerce.command.DeleteSurveyQuestionOption;
import com.skytala.eCommerce.command.UpdateSurveyQuestionOption;
import com.skytala.eCommerce.entity.SurveyQuestionOption;
import com.skytala.eCommerce.entity.SurveyQuestionOptionMapper;
import com.skytala.eCommerce.event.SurveyQuestionOptionAdded;
import com.skytala.eCommerce.event.SurveyQuestionOptionDeleted;
import com.skytala.eCommerce.event.SurveyQuestionOptionFound;
import com.skytala.eCommerce.event.SurveyQuestionOptionUpdated;
import com.skytala.eCommerce.query.FindSurveyQuestionOptionsBy;

@RestController
@RequestMapping("/api/surveyQuestionOption")
public class SurveyQuestionOptionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyQuestionOption>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyQuestionOptionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyQuestionOption
	 * @return a List with the SurveyQuestionOptions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyQuestionOption> findSurveyQuestionOptionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyQuestionOptionsBy query = new FindSurveyQuestionOptionsBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyQuestionOptionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionOptionFound.class,
				event -> sendSurveyQuestionOptionsFoundMessage(((SurveyQuestionOptionFound) event).getSurveyQuestionOptions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyQuestionOptionsFoundMessage(List<SurveyQuestionOption> surveyQuestionOptions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyQuestionOptions);
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
	public boolean createSurveyQuestionOption(HttpServletRequest request) {

		SurveyQuestionOption surveyQuestionOptionToBeAdded = new SurveyQuestionOption();
		try {
			surveyQuestionOptionToBeAdded = SurveyQuestionOptionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyQuestionOption(surveyQuestionOptionToBeAdded);

	}

	/**
	 * creates a new SurveyQuestionOption entry in the ofbiz database
	 * 
	 * @param surveyQuestionOptionToBeAdded
	 *            the SurveyQuestionOption thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyQuestionOption(SurveyQuestionOption surveyQuestionOptionToBeAdded) {

		AddSurveyQuestionOption com = new AddSurveyQuestionOption(surveyQuestionOptionToBeAdded);
		int usedTicketId;

		synchronized (SurveyQuestionOptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionOptionAdded.class,
				event -> sendSurveyQuestionOptionChangedMessage(((SurveyQuestionOptionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyQuestionOption(HttpServletRequest request) {

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

		SurveyQuestionOption surveyQuestionOptionToBeUpdated = new SurveyQuestionOption();

		try {
			surveyQuestionOptionToBeUpdated = SurveyQuestionOptionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyQuestionOption(surveyQuestionOptionToBeUpdated);

	}

	/**
	 * Updates the SurveyQuestionOption with the specific Id
	 * 
	 * @param surveyQuestionOptionToBeUpdated the SurveyQuestionOption thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyQuestionOption(SurveyQuestionOption surveyQuestionOptionToBeUpdated) {

		UpdateSurveyQuestionOption com = new UpdateSurveyQuestionOption(surveyQuestionOptionToBeUpdated);

		int usedTicketId;

		synchronized (SurveyQuestionOptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionOptionUpdated.class,
				event -> sendSurveyQuestionOptionChangedMessage(((SurveyQuestionOptionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyQuestionOption from the database
	 * 
	 * @param surveyQuestionOptionId:
	 *            the id of the SurveyQuestionOption thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyQuestionOptionById(@RequestParam(value = "surveyQuestionOptionId") String surveyQuestionOptionId) {

		DeleteSurveyQuestionOption com = new DeleteSurveyQuestionOption(surveyQuestionOptionId);

		int usedTicketId;

		synchronized (SurveyQuestionOptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionOptionDeleted.class,
				event -> sendSurveyQuestionOptionChangedMessage(((SurveyQuestionOptionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyQuestionOptionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyQuestionOption/\" plus one of the following: "
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
