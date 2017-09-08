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
import com.skytala.eCommerce.command.AddSurveyMultiResp;
import com.skytala.eCommerce.command.DeleteSurveyMultiResp;
import com.skytala.eCommerce.command.UpdateSurveyMultiResp;
import com.skytala.eCommerce.entity.SurveyMultiResp;
import com.skytala.eCommerce.entity.SurveyMultiRespMapper;
import com.skytala.eCommerce.event.SurveyMultiRespAdded;
import com.skytala.eCommerce.event.SurveyMultiRespDeleted;
import com.skytala.eCommerce.event.SurveyMultiRespFound;
import com.skytala.eCommerce.event.SurveyMultiRespUpdated;
import com.skytala.eCommerce.query.FindSurveyMultiRespsBy;

@RestController
@RequestMapping("/api/surveyMultiResp")
public class SurveyMultiRespController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyMultiResp>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyMultiRespController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyMultiResp
	 * @return a List with the SurveyMultiResps
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyMultiResp> findSurveyMultiRespsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyMultiRespsBy query = new FindSurveyMultiRespsBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyMultiRespController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyMultiRespFound.class,
				event -> sendSurveyMultiRespsFoundMessage(((SurveyMultiRespFound) event).getSurveyMultiResps(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyMultiRespsFoundMessage(List<SurveyMultiResp> surveyMultiResps, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyMultiResps);
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
	public boolean createSurveyMultiResp(HttpServletRequest request) {

		SurveyMultiResp surveyMultiRespToBeAdded = new SurveyMultiResp();
		try {
			surveyMultiRespToBeAdded = SurveyMultiRespMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyMultiResp(surveyMultiRespToBeAdded);

	}

	/**
	 * creates a new SurveyMultiResp entry in the ofbiz database
	 * 
	 * @param surveyMultiRespToBeAdded
	 *            the SurveyMultiResp thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyMultiResp(SurveyMultiResp surveyMultiRespToBeAdded) {

		AddSurveyMultiResp com = new AddSurveyMultiResp(surveyMultiRespToBeAdded);
		int usedTicketId;

		synchronized (SurveyMultiRespController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyMultiRespAdded.class,
				event -> sendSurveyMultiRespChangedMessage(((SurveyMultiRespAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyMultiResp(HttpServletRequest request) {

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

		SurveyMultiResp surveyMultiRespToBeUpdated = new SurveyMultiResp();

		try {
			surveyMultiRespToBeUpdated = SurveyMultiRespMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyMultiResp(surveyMultiRespToBeUpdated);

	}

	/**
	 * Updates the SurveyMultiResp with the specific Id
	 * 
	 * @param surveyMultiRespToBeUpdated the SurveyMultiResp thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyMultiResp(SurveyMultiResp surveyMultiRespToBeUpdated) {

		UpdateSurveyMultiResp com = new UpdateSurveyMultiResp(surveyMultiRespToBeUpdated);

		int usedTicketId;

		synchronized (SurveyMultiRespController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyMultiRespUpdated.class,
				event -> sendSurveyMultiRespChangedMessage(((SurveyMultiRespUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyMultiResp from the database
	 * 
	 * @param surveyMultiRespId:
	 *            the id of the SurveyMultiResp thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyMultiRespById(@RequestParam(value = "surveyMultiRespId") String surveyMultiRespId) {

		DeleteSurveyMultiResp com = new DeleteSurveyMultiResp(surveyMultiRespId);

		int usedTicketId;

		synchronized (SurveyMultiRespController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyMultiRespDeleted.class,
				event -> sendSurveyMultiRespChangedMessage(((SurveyMultiRespDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyMultiRespChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyMultiResp/\" plus one of the following: "
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
