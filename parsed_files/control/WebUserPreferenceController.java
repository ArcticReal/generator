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
import com.skytala.eCommerce.command.AddWebUserPreference;
import com.skytala.eCommerce.command.DeleteWebUserPreference;
import com.skytala.eCommerce.command.UpdateWebUserPreference;
import com.skytala.eCommerce.entity.WebUserPreference;
import com.skytala.eCommerce.entity.WebUserPreferenceMapper;
import com.skytala.eCommerce.event.WebUserPreferenceAdded;
import com.skytala.eCommerce.event.WebUserPreferenceDeleted;
import com.skytala.eCommerce.event.WebUserPreferenceFound;
import com.skytala.eCommerce.event.WebUserPreferenceUpdated;
import com.skytala.eCommerce.query.FindWebUserPreferencesBy;

@RestController
@RequestMapping("/api/webUserPreference")
public class WebUserPreferenceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WebUserPreference>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WebUserPreferenceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WebUserPreference
	 * @return a List with the WebUserPreferences
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WebUserPreference> findWebUserPreferencesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWebUserPreferencesBy query = new FindWebUserPreferencesBy(allRequestParams);

		int usedTicketId;

		synchronized (WebUserPreferenceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebUserPreferenceFound.class,
				event -> sendWebUserPreferencesFoundMessage(((WebUserPreferenceFound) event).getWebUserPreferences(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWebUserPreferencesFoundMessage(List<WebUserPreference> webUserPreferences, int usedTicketId) {
		queryReturnVal.put(usedTicketId, webUserPreferences);
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
	public boolean createWebUserPreference(HttpServletRequest request) {

		WebUserPreference webUserPreferenceToBeAdded = new WebUserPreference();
		try {
			webUserPreferenceToBeAdded = WebUserPreferenceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWebUserPreference(webUserPreferenceToBeAdded);

	}

	/**
	 * creates a new WebUserPreference entry in the ofbiz database
	 * 
	 * @param webUserPreferenceToBeAdded
	 *            the WebUserPreference thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWebUserPreference(WebUserPreference webUserPreferenceToBeAdded) {

		AddWebUserPreference com = new AddWebUserPreference(webUserPreferenceToBeAdded);
		int usedTicketId;

		synchronized (WebUserPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebUserPreferenceAdded.class,
				event -> sendWebUserPreferenceChangedMessage(((WebUserPreferenceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWebUserPreference(HttpServletRequest request) {

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

		WebUserPreference webUserPreferenceToBeUpdated = new WebUserPreference();

		try {
			webUserPreferenceToBeUpdated = WebUserPreferenceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWebUserPreference(webUserPreferenceToBeUpdated);

	}

	/**
	 * Updates the WebUserPreference with the specific Id
	 * 
	 * @param webUserPreferenceToBeUpdated the WebUserPreference thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWebUserPreference(WebUserPreference webUserPreferenceToBeUpdated) {

		UpdateWebUserPreference com = new UpdateWebUserPreference(webUserPreferenceToBeUpdated);

		int usedTicketId;

		synchronized (WebUserPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebUserPreferenceUpdated.class,
				event -> sendWebUserPreferenceChangedMessage(((WebUserPreferenceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WebUserPreference from the database
	 * 
	 * @param webUserPreferenceId:
	 *            the id of the WebUserPreference thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletewebUserPreferenceById(@RequestParam(value = "webUserPreferenceId") String webUserPreferenceId) {

		DeleteWebUserPreference com = new DeleteWebUserPreference(webUserPreferenceId);

		int usedTicketId;

		synchronized (WebUserPreferenceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebUserPreferenceDeleted.class,
				event -> sendWebUserPreferenceChangedMessage(((WebUserPreferenceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWebUserPreferenceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/webUserPreference/\" plus one of the following: "
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
