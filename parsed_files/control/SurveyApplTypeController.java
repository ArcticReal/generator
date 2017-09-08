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
import com.skytala.eCommerce.command.AddSurveyApplType;
import com.skytala.eCommerce.command.DeleteSurveyApplType;
import com.skytala.eCommerce.command.UpdateSurveyApplType;
import com.skytala.eCommerce.entity.SurveyApplType;
import com.skytala.eCommerce.entity.SurveyApplTypeMapper;
import com.skytala.eCommerce.event.SurveyApplTypeAdded;
import com.skytala.eCommerce.event.SurveyApplTypeDeleted;
import com.skytala.eCommerce.event.SurveyApplTypeFound;
import com.skytala.eCommerce.event.SurveyApplTypeUpdated;
import com.skytala.eCommerce.query.FindSurveyApplTypesBy;

@RestController
@RequestMapping("/api/surveyApplType")
public class SurveyApplTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SurveyApplType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SurveyApplTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SurveyApplType
	 * @return a List with the SurveyApplTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SurveyApplType> findSurveyApplTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSurveyApplTypesBy query = new FindSurveyApplTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (SurveyApplTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyApplTypeFound.class,
				event -> sendSurveyApplTypesFoundMessage(((SurveyApplTypeFound) event).getSurveyApplTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSurveyApplTypesFoundMessage(List<SurveyApplType> surveyApplTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, surveyApplTypes);
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
	public boolean createSurveyApplType(HttpServletRequest request) {

		SurveyApplType surveyApplTypeToBeAdded = new SurveyApplType();
		try {
			surveyApplTypeToBeAdded = SurveyApplTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSurveyApplType(surveyApplTypeToBeAdded);

	}

	/**
	 * creates a new SurveyApplType entry in the ofbiz database
	 * 
	 * @param surveyApplTypeToBeAdded
	 *            the SurveyApplType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSurveyApplType(SurveyApplType surveyApplTypeToBeAdded) {

		AddSurveyApplType com = new AddSurveyApplType(surveyApplTypeToBeAdded);
		int usedTicketId;

		synchronized (SurveyApplTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyApplTypeAdded.class,
				event -> sendSurveyApplTypeChangedMessage(((SurveyApplTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSurveyApplType(HttpServletRequest request) {

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

		SurveyApplType surveyApplTypeToBeUpdated = new SurveyApplType();

		try {
			surveyApplTypeToBeUpdated = SurveyApplTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSurveyApplType(surveyApplTypeToBeUpdated);

	}

	/**
	 * Updates the SurveyApplType with the specific Id
	 * 
	 * @param surveyApplTypeToBeUpdated the SurveyApplType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSurveyApplType(SurveyApplType surveyApplTypeToBeUpdated) {

		UpdateSurveyApplType com = new UpdateSurveyApplType(surveyApplTypeToBeUpdated);

		int usedTicketId;

		synchronized (SurveyApplTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyApplTypeUpdated.class,
				event -> sendSurveyApplTypeChangedMessage(((SurveyApplTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SurveyApplType from the database
	 * 
	 * @param surveyApplTypeId:
	 *            the id of the SurveyApplType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesurveyApplTypeById(@RequestParam(value = "surveyApplTypeId") String surveyApplTypeId) {

		DeleteSurveyApplType com = new DeleteSurveyApplType(surveyApplTypeId);

		int usedTicketId;

		synchronized (SurveyApplTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SurveyApplTypeDeleted.class,
				event -> sendSurveyApplTypeChangedMessage(((SurveyApplTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSurveyApplTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/surveyApplType/\" plus one of the following: "
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
