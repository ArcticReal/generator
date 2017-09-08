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
import com.skytala.eCommerce.command.AddSurveyQuestionCategory;
import com.skytala.eCommerce.command.DeleteSurveyQuestionCategory;
import com.skytala.eCommerce.command.UpdateSurveyQuestionCategory;
import com.skytala.eCommerce.entity.SurveyQuestionCategory;
import com.skytala.eCommerce.entity.SurveyQuestionCategoryMapper;
import com.skytala.eCommerce.event.SurveyQuestionCategoryAdded;
import com.skytala.eCommerce.event.SurveyQuestionCategoryDeleted;
import com.skytala.eCommerce.event.SurveyQuestionCategoryFound;
import com.skytala.eCommerce.event.SurveyQuestionCategoryUpdated;
import com.skytala.eCommerce.query.FindSurveyQuestionCategorysBy;

@RestController
@RequestMapping("/api/surveyQuestionCategory")
public class SurveyQuestionCategoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyQuestionCategory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyQuestionCategoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyQuestionCategory
	 * @return a List with the SurveyQuestionCategorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyQuestionCategory> findSurveyQuestionCategorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyQuestionCategorysBy query = new FindSurveyQuestionCategorysBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyQuestionCategoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionCategoryFound.class,
				event -> sendSurveyQuestionCategorysFoundMessage(((SurveyQuestionCategoryFound) event).getSurveyQuestionCategorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyQuestionCategorysFoundMessage(List<SurveyQuestionCategory> surveyQuestionCategorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyQuestionCategorys);
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
	public boolean createSurveyQuestionCategory(HttpServletRequest request) {

		SurveyQuestionCategory surveyQuestionCategoryToBeAdded = new SurveyQuestionCategory();
		try {
			surveyQuestionCategoryToBeAdded = SurveyQuestionCategoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyQuestionCategory(surveyQuestionCategoryToBeAdded);

	}

	/**
	 * creates a new SurveyQuestionCategory entry in the ofbiz database
	 * 
	 * @param surveyQuestionCategoryToBeAdded
	 *            the SurveyQuestionCategory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyQuestionCategory(SurveyQuestionCategory surveyQuestionCategoryToBeAdded) {

		AddSurveyQuestionCategory com = new AddSurveyQuestionCategory(surveyQuestionCategoryToBeAdded);
		int usedTicketId;

		synchronized (SurveyQuestionCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionCategoryAdded.class,
				event -> sendSurveyQuestionCategoryChangedMessage(((SurveyQuestionCategoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyQuestionCategory(HttpServletRequest request) {

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

		SurveyQuestionCategory surveyQuestionCategoryToBeUpdated = new SurveyQuestionCategory();

		try {
			surveyQuestionCategoryToBeUpdated = SurveyQuestionCategoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyQuestionCategory(surveyQuestionCategoryToBeUpdated);

	}

	/**
	 * Updates the SurveyQuestionCategory with the specific Id
	 * 
	 * @param surveyQuestionCategoryToBeUpdated the SurveyQuestionCategory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyQuestionCategory(SurveyQuestionCategory surveyQuestionCategoryToBeUpdated) {

		UpdateSurveyQuestionCategory com = new UpdateSurveyQuestionCategory(surveyQuestionCategoryToBeUpdated);

		int usedTicketId;

		synchronized (SurveyQuestionCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionCategoryUpdated.class,
				event -> sendSurveyQuestionCategoryChangedMessage(((SurveyQuestionCategoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyQuestionCategory from the database
	 * 
	 * @param surveyQuestionCategoryId:
	 *            the id of the SurveyQuestionCategory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyQuestionCategoryById(@RequestParam(value = "surveyQuestionCategoryId") String surveyQuestionCategoryId) {

		DeleteSurveyQuestionCategory com = new DeleteSurveyQuestionCategory(surveyQuestionCategoryId);

		int usedTicketId;

		synchronized (SurveyQuestionCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionCategoryDeleted.class,
				event -> sendSurveyQuestionCategoryChangedMessage(((SurveyQuestionCategoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyQuestionCategoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyQuestionCategory/\" plus one of the following: "
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
