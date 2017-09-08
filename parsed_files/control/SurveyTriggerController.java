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
import com.skytala.eCommerce.command.AddSurveyTrigger;
import com.skytala.eCommerce.command.DeleteSurveyTrigger;
import com.skytala.eCommerce.command.UpdateSurveyTrigger;
import com.skytala.eCommerce.entity.SurveyTrigger;
import com.skytala.eCommerce.entity.SurveyTriggerMapper;
import com.skytala.eCommerce.event.SurveyTriggerAdded;
import com.skytala.eCommerce.event.SurveyTriggerDeleted;
import com.skytala.eCommerce.event.SurveyTriggerFound;
import com.skytala.eCommerce.event.SurveyTriggerUpdated;
import com.skytala.eCommerce.query.FindSurveyTriggersBy;

@RestController
@RequestMapping("/api/surveyTrigger")
public class SurveyTriggerController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyTrigger>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyTriggerController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyTrigger
	 * @return a List with the SurveyTriggers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyTrigger> findSurveyTriggersBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyTriggersBy query = new FindSurveyTriggersBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyTriggerController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyTriggerFound.class,
				event -> sendSurveyTriggersFoundMessage(((SurveyTriggerFound) event).getSurveyTriggers(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyTriggersFoundMessage(List<SurveyTrigger> surveyTriggers, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyTriggers);
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
	public boolean createSurveyTrigger(HttpServletRequest request) {

		SurveyTrigger surveyTriggerToBeAdded = new SurveyTrigger();
		try {
			surveyTriggerToBeAdded = SurveyTriggerMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyTrigger(surveyTriggerToBeAdded);

	}

	/**
	 * creates a new SurveyTrigger entry in the ofbiz database
	 * 
	 * @param surveyTriggerToBeAdded
	 *            the SurveyTrigger thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyTrigger(SurveyTrigger surveyTriggerToBeAdded) {

		AddSurveyTrigger com = new AddSurveyTrigger(surveyTriggerToBeAdded);
		int usedTicketId;

		synchronized (SurveyTriggerController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyTriggerAdded.class,
				event -> sendSurveyTriggerChangedMessage(((SurveyTriggerAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyTrigger(HttpServletRequest request) {

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

		SurveyTrigger surveyTriggerToBeUpdated = new SurveyTrigger();

		try {
			surveyTriggerToBeUpdated = SurveyTriggerMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyTrigger(surveyTriggerToBeUpdated);

	}

	/**
	 * Updates the SurveyTrigger with the specific Id
	 * 
	 * @param surveyTriggerToBeUpdated the SurveyTrigger thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyTrigger(SurveyTrigger surveyTriggerToBeUpdated) {

		UpdateSurveyTrigger com = new UpdateSurveyTrigger(surveyTriggerToBeUpdated);

		int usedTicketId;

		synchronized (SurveyTriggerController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyTriggerUpdated.class,
				event -> sendSurveyTriggerChangedMessage(((SurveyTriggerUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyTrigger from the database
	 * 
	 * @param surveyTriggerId:
	 *            the id of the SurveyTrigger thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyTriggerById(@RequestParam(value = "surveyTriggerId") String surveyTriggerId) {

		DeleteSurveyTrigger com = new DeleteSurveyTrigger(surveyTriggerId);

		int usedTicketId;

		synchronized (SurveyTriggerController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyTriggerDeleted.class,
				event -> sendSurveyTriggerChangedMessage(((SurveyTriggerDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyTriggerChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyTrigger/\" plus one of the following: "
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
