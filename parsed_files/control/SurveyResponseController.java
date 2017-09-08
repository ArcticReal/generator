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
import com.skytala.eCommerce.command.AddSurveyResponse;
import com.skytala.eCommerce.command.DeleteSurveyResponse;
import com.skytala.eCommerce.command.UpdateSurveyResponse;
import com.skytala.eCommerce.entity.SurveyResponse;
import com.skytala.eCommerce.entity.SurveyResponseMapper;
import com.skytala.eCommerce.event.SurveyResponseAdded;
import com.skytala.eCommerce.event.SurveyResponseDeleted;
import com.skytala.eCommerce.event.SurveyResponseFound;
import com.skytala.eCommerce.event.SurveyResponseUpdated;
import com.skytala.eCommerce.query.FindSurveyResponsesBy;

@RestController
@RequestMapping("/api/surveyResponse")
public class SurveyResponseController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyResponse>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyResponseController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyResponse
	 * @return a List with the SurveyResponses
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyResponse> findSurveyResponsesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyResponsesBy query = new FindSurveyResponsesBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyResponseController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyResponseFound.class,
				event -> sendSurveyResponsesFoundMessage(((SurveyResponseFound) event).getSurveyResponses(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyResponsesFoundMessage(List<SurveyResponse> surveyResponses, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyResponses);
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
	public boolean createSurveyResponse(HttpServletRequest request) {

		SurveyResponse surveyResponseToBeAdded = new SurveyResponse();
		try {
			surveyResponseToBeAdded = SurveyResponseMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyResponse(surveyResponseToBeAdded);

	}

	/**
	 * creates a new SurveyResponse entry in the ofbiz database
	 * 
	 * @param surveyResponseToBeAdded
	 *            the SurveyResponse thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyResponse(SurveyResponse surveyResponseToBeAdded) {

		AddSurveyResponse com = new AddSurveyResponse(surveyResponseToBeAdded);
		int usedTicketId;

		synchronized (SurveyResponseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyResponseAdded.class,
				event -> sendSurveyResponseChangedMessage(((SurveyResponseAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyResponse(HttpServletRequest request) {

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

		SurveyResponse surveyResponseToBeUpdated = new SurveyResponse();

		try {
			surveyResponseToBeUpdated = SurveyResponseMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyResponse(surveyResponseToBeUpdated);

	}

	/**
	 * Updates the SurveyResponse with the specific Id
	 * 
	 * @param surveyResponseToBeUpdated the SurveyResponse thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyResponse(SurveyResponse surveyResponseToBeUpdated) {

		UpdateSurveyResponse com = new UpdateSurveyResponse(surveyResponseToBeUpdated);

		int usedTicketId;

		synchronized (SurveyResponseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyResponseUpdated.class,
				event -> sendSurveyResponseChangedMessage(((SurveyResponseUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyResponse from the database
	 * 
	 * @param surveyResponseId:
	 *            the id of the SurveyResponse thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyResponseById(@RequestParam(value = "surveyResponseId") String surveyResponseId) {

		DeleteSurveyResponse com = new DeleteSurveyResponse(surveyResponseId);

		int usedTicketId;

		synchronized (SurveyResponseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyResponseDeleted.class,
				event -> sendSurveyResponseChangedMessage(((SurveyResponseDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyResponseChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyResponse/\" plus one of the following: "
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
