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
import com.skytala.eCommerce.command.AddWebSiteContentType;
import com.skytala.eCommerce.command.DeleteWebSiteContentType;
import com.skytala.eCommerce.command.UpdateWebSiteContentType;
import com.skytala.eCommerce.entity.WebSiteContentType;
import com.skytala.eCommerce.entity.WebSiteContentTypeMapper;
import com.skytala.eCommerce.event.WebSiteContentTypeAdded;
import com.skytala.eCommerce.event.WebSiteContentTypeDeleted;
import com.skytala.eCommerce.event.WebSiteContentTypeFound;
import com.skytala.eCommerce.event.WebSiteContentTypeUpdated;
import com.skytala.eCommerce.query.FindWebSiteContentTypesBy;

@RestController
@RequestMapping("/api/webSiteContentType")
public class WebSiteContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WebSiteContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WebSiteContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WebSiteContentType
	 * @return a List with the WebSiteContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WebSiteContentType> findWebSiteContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWebSiteContentTypesBy query = new FindWebSiteContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (WebSiteContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteContentTypeFound.class,
				event -> sendWebSiteContentTypesFoundMessage(((WebSiteContentTypeFound) event).getWebSiteContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWebSiteContentTypesFoundMessage(List<WebSiteContentType> webSiteContentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, webSiteContentTypes);
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
	public boolean createWebSiteContentType(HttpServletRequest request) {

		WebSiteContentType webSiteContentTypeToBeAdded = new WebSiteContentType();
		try {
			webSiteContentTypeToBeAdded = WebSiteContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWebSiteContentType(webSiteContentTypeToBeAdded);

	}

	/**
	 * creates a new WebSiteContentType entry in the ofbiz database
	 * 
	 * @param webSiteContentTypeToBeAdded
	 *            the WebSiteContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWebSiteContentType(WebSiteContentType webSiteContentTypeToBeAdded) {

		AddWebSiteContentType com = new AddWebSiteContentType(webSiteContentTypeToBeAdded);
		int usedTicketId;

		synchronized (WebSiteContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteContentTypeAdded.class,
				event -> sendWebSiteContentTypeChangedMessage(((WebSiteContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWebSiteContentType(HttpServletRequest request) {

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

		WebSiteContentType webSiteContentTypeToBeUpdated = new WebSiteContentType();

		try {
			webSiteContentTypeToBeUpdated = WebSiteContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWebSiteContentType(webSiteContentTypeToBeUpdated);

	}

	/**
	 * Updates the WebSiteContentType with the specific Id
	 * 
	 * @param webSiteContentTypeToBeUpdated the WebSiteContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWebSiteContentType(WebSiteContentType webSiteContentTypeToBeUpdated) {

		UpdateWebSiteContentType com = new UpdateWebSiteContentType(webSiteContentTypeToBeUpdated);

		int usedTicketId;

		synchronized (WebSiteContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteContentTypeUpdated.class,
				event -> sendWebSiteContentTypeChangedMessage(((WebSiteContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WebSiteContentType from the database
	 * 
	 * @param webSiteContentTypeId:
	 *            the id of the WebSiteContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletewebSiteContentTypeById(@RequestParam(value = "webSiteContentTypeId") String webSiteContentTypeId) {

		DeleteWebSiteContentType com = new DeleteWebSiteContentType(webSiteContentTypeId);

		int usedTicketId;

		synchronized (WebSiteContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteContentTypeDeleted.class,
				event -> sendWebSiteContentTypeChangedMessage(((WebSiteContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWebSiteContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/webSiteContentType/\" plus one of the following: "
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
