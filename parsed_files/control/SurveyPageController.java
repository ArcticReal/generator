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
import com.skytala.eCommerce.command.AddSurveyPage;
import com.skytala.eCommerce.command.DeleteSurveyPage;
import com.skytala.eCommerce.command.UpdateSurveyPage;
import com.skytala.eCommerce.entity.SurveyPage;
import com.skytala.eCommerce.entity.SurveyPageMapper;
import com.skytala.eCommerce.event.SurveyPageAdded;
import com.skytala.eCommerce.event.SurveyPageDeleted;
import com.skytala.eCommerce.event.SurveyPageFound;
import com.skytala.eCommerce.event.SurveyPageUpdated;
import com.skytala.eCommerce.query.FindSurveyPagesBy;

@RestController
@RequestMapping("/api/surveyPage")
public class SurveyPageController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyPage>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyPageController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyPage
	 * @return a List with the SurveyPages
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyPage> findSurveyPagesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyPagesBy query = new FindSurveyPagesBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyPageController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyPageFound.class,
				event -> sendSurveyPagesFoundMessage(((SurveyPageFound) event).getSurveyPages(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyPagesFoundMessage(List<SurveyPage> surveyPages, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyPages);
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
	public boolean createSurveyPage(HttpServletRequest request) {

		SurveyPage surveyPageToBeAdded = new SurveyPage();
		try {
			surveyPageToBeAdded = SurveyPageMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyPage(surveyPageToBeAdded);

	}

	/**
	 * creates a new SurveyPage entry in the ofbiz database
	 * 
	 * @param surveyPageToBeAdded
	 *            the SurveyPage thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyPage(SurveyPage surveyPageToBeAdded) {

		AddSurveyPage com = new AddSurveyPage(surveyPageToBeAdded);
		int usedTicketId;

		synchronized (SurveyPageController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyPageAdded.class,
				event -> sendSurveyPageChangedMessage(((SurveyPageAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyPage(HttpServletRequest request) {

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

		SurveyPage surveyPageToBeUpdated = new SurveyPage();

		try {
			surveyPageToBeUpdated = SurveyPageMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyPage(surveyPageToBeUpdated);

	}

	/**
	 * Updates the SurveyPage with the specific Id
	 * 
	 * @param surveyPageToBeUpdated the SurveyPage thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyPage(SurveyPage surveyPageToBeUpdated) {

		UpdateSurveyPage com = new UpdateSurveyPage(surveyPageToBeUpdated);

		int usedTicketId;

		synchronized (SurveyPageController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyPageUpdated.class,
				event -> sendSurveyPageChangedMessage(((SurveyPageUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyPage from the database
	 * 
	 * @param surveyPageId:
	 *            the id of the SurveyPage thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyPageById(@RequestParam(value = "surveyPageId") String surveyPageId) {

		DeleteSurveyPage com = new DeleteSurveyPage(surveyPageId);

		int usedTicketId;

		synchronized (SurveyPageController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyPageDeleted.class,
				event -> sendSurveyPageChangedMessage(((SurveyPageDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyPageChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyPage/\" plus one of the following: "
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
