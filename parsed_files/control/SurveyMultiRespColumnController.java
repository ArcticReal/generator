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
import com.skytala.eCommerce.command.AddSurveyMultiRespColumn;
import com.skytala.eCommerce.command.DeleteSurveyMultiRespColumn;
import com.skytala.eCommerce.command.UpdateSurveyMultiRespColumn;
import com.skytala.eCommerce.entity.SurveyMultiRespColumn;
import com.skytala.eCommerce.entity.SurveyMultiRespColumnMapper;
import com.skytala.eCommerce.event.SurveyMultiRespColumnAdded;
import com.skytala.eCommerce.event.SurveyMultiRespColumnDeleted;
import com.skytala.eCommerce.event.SurveyMultiRespColumnFound;
import com.skytala.eCommerce.event.SurveyMultiRespColumnUpdated;
import com.skytala.eCommerce.query.FindSurveyMultiRespColumnsBy;

@RestController
@RequestMapping("/api/surveyMultiRespColumn")
public class SurveyMultiRespColumnController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyMultiRespColumn>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyMultiRespColumnController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyMultiRespColumn
	 * @return a List with the SurveyMultiRespColumns
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyMultiRespColumn> findSurveyMultiRespColumnsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyMultiRespColumnsBy query = new FindSurveyMultiRespColumnsBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyMultiRespColumnController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyMultiRespColumnFound.class,
				event -> sendSurveyMultiRespColumnsFoundMessage(((SurveyMultiRespColumnFound) event).getSurveyMultiRespColumns(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyMultiRespColumnsFoundMessage(List<SurveyMultiRespColumn> surveyMultiRespColumns, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyMultiRespColumns);
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
	public boolean createSurveyMultiRespColumn(HttpServletRequest request) {

		SurveyMultiRespColumn surveyMultiRespColumnToBeAdded = new SurveyMultiRespColumn();
		try {
			surveyMultiRespColumnToBeAdded = SurveyMultiRespColumnMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyMultiRespColumn(surveyMultiRespColumnToBeAdded);

	}

	/**
	 * creates a new SurveyMultiRespColumn entry in the ofbiz database
	 * 
	 * @param surveyMultiRespColumnToBeAdded
	 *            the SurveyMultiRespColumn thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyMultiRespColumn(SurveyMultiRespColumn surveyMultiRespColumnToBeAdded) {

		AddSurveyMultiRespColumn com = new AddSurveyMultiRespColumn(surveyMultiRespColumnToBeAdded);
		int usedTicketId;

		synchronized (SurveyMultiRespColumnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyMultiRespColumnAdded.class,
				event -> sendSurveyMultiRespColumnChangedMessage(((SurveyMultiRespColumnAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyMultiRespColumn(HttpServletRequest request) {

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

		SurveyMultiRespColumn surveyMultiRespColumnToBeUpdated = new SurveyMultiRespColumn();

		try {
			surveyMultiRespColumnToBeUpdated = SurveyMultiRespColumnMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyMultiRespColumn(surveyMultiRespColumnToBeUpdated);

	}

	/**
	 * Updates the SurveyMultiRespColumn with the specific Id
	 * 
	 * @param surveyMultiRespColumnToBeUpdated the SurveyMultiRespColumn thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyMultiRespColumn(SurveyMultiRespColumn surveyMultiRespColumnToBeUpdated) {

		UpdateSurveyMultiRespColumn com = new UpdateSurveyMultiRespColumn(surveyMultiRespColumnToBeUpdated);

		int usedTicketId;

		synchronized (SurveyMultiRespColumnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyMultiRespColumnUpdated.class,
				event -> sendSurveyMultiRespColumnChangedMessage(((SurveyMultiRespColumnUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyMultiRespColumn from the database
	 * 
	 * @param surveyMultiRespColumnId:
	 *            the id of the SurveyMultiRespColumn thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyMultiRespColumnById(@RequestParam(value = "surveyMultiRespColumnId") String surveyMultiRespColumnId) {

		DeleteSurveyMultiRespColumn com = new DeleteSurveyMultiRespColumn(surveyMultiRespColumnId);

		int usedTicketId;

		synchronized (SurveyMultiRespColumnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyMultiRespColumnDeleted.class,
				event -> sendSurveyMultiRespColumnChangedMessage(((SurveyMultiRespColumnDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyMultiRespColumnChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyMultiRespColumn/\" plus one of the following: "
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
