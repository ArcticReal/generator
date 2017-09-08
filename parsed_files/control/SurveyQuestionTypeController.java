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
import com.skytala.eCommerce.command.AddSurveyQuestionType;
import com.skytala.eCommerce.command.DeleteSurveyQuestionType;
import com.skytala.eCommerce.command.UpdateSurveyQuestionType;
import com.skytala.eCommerce.entity.SurveyQuestionType;
import com.skytala.eCommerce.entity.SurveyQuestionTypeMapper;
import com.skytala.eCommerce.event.SurveyQuestionTypeAdded;
import com.skytala.eCommerce.event.SurveyQuestionTypeDeleted;
import com.skytala.eCommerce.event.SurveyQuestionTypeFound;
import com.skytala.eCommerce.event.SurveyQuestionTypeUpdated;
import com.skytala.eCommerce.query.FindSurveyQuestionTypesBy;

@RestController
@RequestMapping("/api/surveyQuestionType")
public class SurveyQuestionTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyQuestionType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyQuestionTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyQuestionType
	 * @return a List with the SurveyQuestionTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyQuestionType> findSurveyQuestionTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyQuestionTypesBy query = new FindSurveyQuestionTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyQuestionTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionTypeFound.class,
				event -> sendSurveyQuestionTypesFoundMessage(((SurveyQuestionTypeFound) event).getSurveyQuestionTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyQuestionTypesFoundMessage(List<SurveyQuestionType> surveyQuestionTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyQuestionTypes);
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
	public boolean createSurveyQuestionType(HttpServletRequest request) {

		SurveyQuestionType surveyQuestionTypeToBeAdded = new SurveyQuestionType();
		try {
			surveyQuestionTypeToBeAdded = SurveyQuestionTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyQuestionType(surveyQuestionTypeToBeAdded);

	}

	/**
	 * creates a new SurveyQuestionType entry in the ofbiz database
	 * 
	 * @param surveyQuestionTypeToBeAdded
	 *            the SurveyQuestionType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyQuestionType(SurveyQuestionType surveyQuestionTypeToBeAdded) {

		AddSurveyQuestionType com = new AddSurveyQuestionType(surveyQuestionTypeToBeAdded);
		int usedTicketId;

		synchronized (SurveyQuestionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionTypeAdded.class,
				event -> sendSurveyQuestionTypeChangedMessage(((SurveyQuestionTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyQuestionType(HttpServletRequest request) {

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

		SurveyQuestionType surveyQuestionTypeToBeUpdated = new SurveyQuestionType();

		try {
			surveyQuestionTypeToBeUpdated = SurveyQuestionTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyQuestionType(surveyQuestionTypeToBeUpdated);

	}

	/**
	 * Updates the SurveyQuestionType with the specific Id
	 * 
	 * @param surveyQuestionTypeToBeUpdated the SurveyQuestionType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyQuestionType(SurveyQuestionType surveyQuestionTypeToBeUpdated) {

		UpdateSurveyQuestionType com = new UpdateSurveyQuestionType(surveyQuestionTypeToBeUpdated);

		int usedTicketId;

		synchronized (SurveyQuestionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionTypeUpdated.class,
				event -> sendSurveyQuestionTypeChangedMessage(((SurveyQuestionTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyQuestionType from the database
	 * 
	 * @param surveyQuestionTypeId:
	 *            the id of the SurveyQuestionType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyQuestionTypeById(@RequestParam(value = "surveyQuestionTypeId") String surveyQuestionTypeId) {

		DeleteSurveyQuestionType com = new DeleteSurveyQuestionType(surveyQuestionTypeId);

		int usedTicketId;

		synchronized (SurveyQuestionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionTypeDeleted.class,
				event -> sendSurveyQuestionTypeChangedMessage(((SurveyQuestionTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyQuestionTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyQuestionType/\" plus one of the following: "
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
