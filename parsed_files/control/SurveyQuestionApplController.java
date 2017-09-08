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
import com.skytala.eCommerce.command.AddSurveyQuestionAppl;
import com.skytala.eCommerce.command.DeleteSurveyQuestionAppl;
import com.skytala.eCommerce.command.UpdateSurveyQuestionAppl;
import com.skytala.eCommerce.entity.SurveyQuestionAppl;
import com.skytala.eCommerce.entity.SurveyQuestionApplMapper;
import com.skytala.eCommerce.event.SurveyQuestionApplAdded;
import com.skytala.eCommerce.event.SurveyQuestionApplDeleted;
import com.skytala.eCommerce.event.SurveyQuestionApplFound;
import com.skytala.eCommerce.event.SurveyQuestionApplUpdated;
import com.skytala.eCommerce.query.FindSurveyQuestionApplsBy;

@RestController
@RequestMapping("/api/surveyQuestionAppl")
public class SurveyQuestionApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyQuestionAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyQuestionApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyQuestionAppl
	 * @return a List with the SurveyQuestionAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyQuestionAppl> findSurveyQuestionApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyQuestionApplsBy query = new FindSurveyQuestionApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyQuestionApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionApplFound.class,
				event -> sendSurveyQuestionApplsFoundMessage(((SurveyQuestionApplFound) event).getSurveyQuestionAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyQuestionApplsFoundMessage(List<SurveyQuestionAppl> surveyQuestionAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyQuestionAppls);
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
	public boolean createSurveyQuestionAppl(HttpServletRequest request) {

		SurveyQuestionAppl surveyQuestionApplToBeAdded = new SurveyQuestionAppl();
		try {
			surveyQuestionApplToBeAdded = SurveyQuestionApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyQuestionAppl(surveyQuestionApplToBeAdded);

	}

	/**
	 * creates a new SurveyQuestionAppl entry in the ofbiz database
	 * 
	 * @param surveyQuestionApplToBeAdded
	 *            the SurveyQuestionAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyQuestionAppl(SurveyQuestionAppl surveyQuestionApplToBeAdded) {

		AddSurveyQuestionAppl com = new AddSurveyQuestionAppl(surveyQuestionApplToBeAdded);
		int usedTicketId;

		synchronized (SurveyQuestionApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionApplAdded.class,
				event -> sendSurveyQuestionApplChangedMessage(((SurveyQuestionApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyQuestionAppl(HttpServletRequest request) {

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

		SurveyQuestionAppl surveyQuestionApplToBeUpdated = new SurveyQuestionAppl();

		try {
			surveyQuestionApplToBeUpdated = SurveyQuestionApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyQuestionAppl(surveyQuestionApplToBeUpdated);

	}

	/**
	 * Updates the SurveyQuestionAppl with the specific Id
	 * 
	 * @param surveyQuestionApplToBeUpdated the SurveyQuestionAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyQuestionAppl(SurveyQuestionAppl surveyQuestionApplToBeUpdated) {

		UpdateSurveyQuestionAppl com = new UpdateSurveyQuestionAppl(surveyQuestionApplToBeUpdated);

		int usedTicketId;

		synchronized (SurveyQuestionApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionApplUpdated.class,
				event -> sendSurveyQuestionApplChangedMessage(((SurveyQuestionApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyQuestionAppl from the database
	 * 
	 * @param surveyQuestionApplId:
	 *            the id of the SurveyQuestionAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyQuestionApplById(@RequestParam(value = "surveyQuestionApplId") String surveyQuestionApplId) {

		DeleteSurveyQuestionAppl com = new DeleteSurveyQuestionAppl(surveyQuestionApplId);

		int usedTicketId;

		synchronized (SurveyQuestionApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyQuestionApplDeleted.class,
				event -> sendSurveyQuestionApplChangedMessage(((SurveyQuestionApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyQuestionApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyQuestionAppl/\" plus one of the following: "
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
