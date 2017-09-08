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
import com.skytala.eCommerce.command.AddWebAnalyticsType;
import com.skytala.eCommerce.command.DeleteWebAnalyticsType;
import com.skytala.eCommerce.command.UpdateWebAnalyticsType;
import com.skytala.eCommerce.entity.WebAnalyticsType;
import com.skytala.eCommerce.entity.WebAnalyticsTypeMapper;
import com.skytala.eCommerce.event.WebAnalyticsTypeAdded;
import com.skytala.eCommerce.event.WebAnalyticsTypeDeleted;
import com.skytala.eCommerce.event.WebAnalyticsTypeFound;
import com.skytala.eCommerce.event.WebAnalyticsTypeUpdated;
import com.skytala.eCommerce.query.FindWebAnalyticsTypesBy;

@RestController
@RequestMapping("/api/webAnalyticsType")
public class WebAnalyticsTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WebAnalyticsType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WebAnalyticsTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WebAnalyticsType
	 * @return a List with the WebAnalyticsTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WebAnalyticsType> findWebAnalyticsTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWebAnalyticsTypesBy query = new FindWebAnalyticsTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (WebAnalyticsTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebAnalyticsTypeFound.class,
				event -> sendWebAnalyticsTypesFoundMessage(((WebAnalyticsTypeFound) event).getWebAnalyticsTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWebAnalyticsTypesFoundMessage(List<WebAnalyticsType> webAnalyticsTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, webAnalyticsTypes);
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
	public boolean createWebAnalyticsType(HttpServletRequest request) {

		WebAnalyticsType webAnalyticsTypeToBeAdded = new WebAnalyticsType();
		try {
			webAnalyticsTypeToBeAdded = WebAnalyticsTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWebAnalyticsType(webAnalyticsTypeToBeAdded);

	}

	/**
	 * creates a new WebAnalyticsType entry in the ofbiz database
	 * 
	 * @param webAnalyticsTypeToBeAdded
	 *            the WebAnalyticsType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWebAnalyticsType(WebAnalyticsType webAnalyticsTypeToBeAdded) {

		AddWebAnalyticsType com = new AddWebAnalyticsType(webAnalyticsTypeToBeAdded);
		int usedTicketId;

		synchronized (WebAnalyticsTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebAnalyticsTypeAdded.class,
				event -> sendWebAnalyticsTypeChangedMessage(((WebAnalyticsTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWebAnalyticsType(HttpServletRequest request) {

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

		WebAnalyticsType webAnalyticsTypeToBeUpdated = new WebAnalyticsType();

		try {
			webAnalyticsTypeToBeUpdated = WebAnalyticsTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWebAnalyticsType(webAnalyticsTypeToBeUpdated);

	}

	/**
	 * Updates the WebAnalyticsType with the specific Id
	 * 
	 * @param webAnalyticsTypeToBeUpdated the WebAnalyticsType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWebAnalyticsType(WebAnalyticsType webAnalyticsTypeToBeUpdated) {

		UpdateWebAnalyticsType com = new UpdateWebAnalyticsType(webAnalyticsTypeToBeUpdated);

		int usedTicketId;

		synchronized (WebAnalyticsTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebAnalyticsTypeUpdated.class,
				event -> sendWebAnalyticsTypeChangedMessage(((WebAnalyticsTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WebAnalyticsType from the database
	 * 
	 * @param webAnalyticsTypeId:
	 *            the id of the WebAnalyticsType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletewebAnalyticsTypeById(@RequestParam(value = "webAnalyticsTypeId") String webAnalyticsTypeId) {

		DeleteWebAnalyticsType com = new DeleteWebAnalyticsType(webAnalyticsTypeId);

		int usedTicketId;

		synchronized (WebAnalyticsTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebAnalyticsTypeDeleted.class,
				event -> sendWebAnalyticsTypeChangedMessage(((WebAnalyticsTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWebAnalyticsTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/webAnalyticsType/\" plus one of the following: "
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
