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
import com.skytala.eCommerce.command.AddWebPreferenceType;
import com.skytala.eCommerce.command.DeleteWebPreferenceType;
import com.skytala.eCommerce.command.UpdateWebPreferenceType;
import com.skytala.eCommerce.entity.WebPreferenceType;
import com.skytala.eCommerce.entity.WebPreferenceTypeMapper;
import com.skytala.eCommerce.event.WebPreferenceTypeAdded;
import com.skytala.eCommerce.event.WebPreferenceTypeDeleted;
import com.skytala.eCommerce.event.WebPreferenceTypeFound;
import com.skytala.eCommerce.event.WebPreferenceTypeUpdated;
import com.skytala.eCommerce.query.FindWebPreferenceTypesBy;

@RestController
@RequestMapping("/api/webPreferenceType")
public class WebPreferenceTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WebPreferenceType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WebPreferenceTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WebPreferenceType
	 * @return a List with the WebPreferenceTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WebPreferenceType> findWebPreferenceTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWebPreferenceTypesBy query = new FindWebPreferenceTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (WebPreferenceTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebPreferenceTypeFound.class,
				event -> sendWebPreferenceTypesFoundMessage(((WebPreferenceTypeFound) event).getWebPreferenceTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWebPreferenceTypesFoundMessage(List<WebPreferenceType> webPreferenceTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, webPreferenceTypes);
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
	public boolean createWebPreferenceType(HttpServletRequest request) {

		WebPreferenceType webPreferenceTypeToBeAdded = new WebPreferenceType();
		try {
			webPreferenceTypeToBeAdded = WebPreferenceTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWebPreferenceType(webPreferenceTypeToBeAdded);

	}

	/**
	 * creates a new WebPreferenceType entry in the ofbiz database
	 * 
	 * @param webPreferenceTypeToBeAdded
	 *            the WebPreferenceType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWebPreferenceType(WebPreferenceType webPreferenceTypeToBeAdded) {

		AddWebPreferenceType com = new AddWebPreferenceType(webPreferenceTypeToBeAdded);
		int usedTicketId;

		synchronized (WebPreferenceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebPreferenceTypeAdded.class,
				event -> sendWebPreferenceTypeChangedMessage(((WebPreferenceTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWebPreferenceType(HttpServletRequest request) {

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

		WebPreferenceType webPreferenceTypeToBeUpdated = new WebPreferenceType();

		try {
			webPreferenceTypeToBeUpdated = WebPreferenceTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWebPreferenceType(webPreferenceTypeToBeUpdated);

	}

	/**
	 * Updates the WebPreferenceType with the specific Id
	 * 
	 * @param webPreferenceTypeToBeUpdated the WebPreferenceType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWebPreferenceType(WebPreferenceType webPreferenceTypeToBeUpdated) {

		UpdateWebPreferenceType com = new UpdateWebPreferenceType(webPreferenceTypeToBeUpdated);

		int usedTicketId;

		synchronized (WebPreferenceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebPreferenceTypeUpdated.class,
				event -> sendWebPreferenceTypeChangedMessage(((WebPreferenceTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WebPreferenceType from the database
	 * 
	 * @param webPreferenceTypeId:
	 *            the id of the WebPreferenceType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletewebPreferenceTypeById(@RequestParam(value = "webPreferenceTypeId") String webPreferenceTypeId) {

		DeleteWebPreferenceType com = new DeleteWebPreferenceType(webPreferenceTypeId);

		int usedTicketId;

		synchronized (WebPreferenceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebPreferenceTypeDeleted.class,
				event -> sendWebPreferenceTypeChangedMessage(((WebPreferenceTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWebPreferenceTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/webPreferenceType/\" plus one of the following: "
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
