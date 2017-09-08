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
import com.skytala.eCommerce.command.AddWebSiteContent;
import com.skytala.eCommerce.command.DeleteWebSiteContent;
import com.skytala.eCommerce.command.UpdateWebSiteContent;
import com.skytala.eCommerce.entity.WebSiteContent;
import com.skytala.eCommerce.entity.WebSiteContentMapper;
import com.skytala.eCommerce.event.WebSiteContentAdded;
import com.skytala.eCommerce.event.WebSiteContentDeleted;
import com.skytala.eCommerce.event.WebSiteContentFound;
import com.skytala.eCommerce.event.WebSiteContentUpdated;
import com.skytala.eCommerce.query.FindWebSiteContentsBy;

@RestController
@RequestMapping("/api/webSiteContent")
public class WebSiteContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WebSiteContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WebSiteContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WebSiteContent
	 * @return a List with the WebSiteContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WebSiteContent> findWebSiteContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindWebSiteContentsBy query = new FindWebSiteContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (WebSiteContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteContentFound.class,
				event -> sendWebSiteContentsFoundMessage(((WebSiteContentFound) event).getWebSiteContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWebSiteContentsFoundMessage(List<WebSiteContent> webSiteContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, webSiteContents);
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
	public boolean createWebSiteContent(HttpServletRequest request) {

		WebSiteContent webSiteContentToBeAdded = new WebSiteContent();
		try {
			webSiteContentToBeAdded = WebSiteContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWebSiteContent(webSiteContentToBeAdded);

	}

	/**
	 * creates a new WebSiteContent entry in the ofbiz database
	 * 
	 * @param webSiteContentToBeAdded
	 *            the WebSiteContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWebSiteContent(WebSiteContent webSiteContentToBeAdded) {

		AddWebSiteContent com = new AddWebSiteContent(webSiteContentToBeAdded);
		int usedTicketId;

		synchronized (WebSiteContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteContentAdded.class,
				event -> sendWebSiteContentChangedMessage(((WebSiteContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWebSiteContent(HttpServletRequest request) {

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

		WebSiteContent webSiteContentToBeUpdated = new WebSiteContent();

		try {
			webSiteContentToBeUpdated = WebSiteContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWebSiteContent(webSiteContentToBeUpdated);

	}

	/**
	 * Updates the WebSiteContent with the specific Id
	 * 
	 * @param webSiteContentToBeUpdated the WebSiteContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWebSiteContent(WebSiteContent webSiteContentToBeUpdated) {

		UpdateWebSiteContent com = new UpdateWebSiteContent(webSiteContentToBeUpdated);

		int usedTicketId;

		synchronized (WebSiteContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteContentUpdated.class,
				event -> sendWebSiteContentChangedMessage(((WebSiteContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WebSiteContent from the database
	 * 
	 * @param webSiteContentId:
	 *            the id of the WebSiteContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletewebSiteContentById(@RequestParam(value = "webSiteContentId") String webSiteContentId) {

		DeleteWebSiteContent com = new DeleteWebSiteContent(webSiteContentId);

		int usedTicketId;

		synchronized (WebSiteContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSiteContentDeleted.class,
				event -> sendWebSiteContentChangedMessage(((WebSiteContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWebSiteContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/webSiteContent/\" plus one of the following: "
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
