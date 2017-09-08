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
import com.skytala.eCommerce.command.AddWebSitePathAlias;
import com.skytala.eCommerce.command.DeleteWebSitePathAlias;
import com.skytala.eCommerce.command.UpdateWebSitePathAlias;
import com.skytala.eCommerce.entity.WebSitePathAlias;
import com.skytala.eCommerce.entity.WebSitePathAliasMapper;
import com.skytala.eCommerce.event.WebSitePathAliasAdded;
import com.skytala.eCommerce.event.WebSitePathAliasDeleted;
import com.skytala.eCommerce.event.WebSitePathAliasFound;
import com.skytala.eCommerce.event.WebSitePathAliasUpdated;
import com.skytala.eCommerce.query.FindWebSitePathAliassBy;

@RestController
@RequestMapping("/api/webSitePathAlias")
public class WebSitePathAliasController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WebSitePathAlias>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WebSitePathAliasController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WebSitePathAlias
	 * @return a List with the WebSitePathAliass
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WebSitePathAlias> findWebSitePathAliassBy(@RequestParam Map<String, String> allRequestParams) {

		FindWebSitePathAliassBy query = new FindWebSitePathAliassBy(allRequestParams);

		int usedTicketId;

		synchronized (WebSitePathAliasController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSitePathAliasFound.class,
				event -> sendWebSitePathAliassFoundMessage(((WebSitePathAliasFound) event).getWebSitePathAliass(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWebSitePathAliassFoundMessage(List<WebSitePathAlias> webSitePathAliass, int usedTicketId) {
		queryReturnVal.put(usedTicketId, webSitePathAliass);
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
	public boolean createWebSitePathAlias(HttpServletRequest request) {

		WebSitePathAlias webSitePathAliasToBeAdded = new WebSitePathAlias();
		try {
			webSitePathAliasToBeAdded = WebSitePathAliasMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWebSitePathAlias(webSitePathAliasToBeAdded);

	}

	/**
	 * creates a new WebSitePathAlias entry in the ofbiz database
	 * 
	 * @param webSitePathAliasToBeAdded
	 *            the WebSitePathAlias thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWebSitePathAlias(WebSitePathAlias webSitePathAliasToBeAdded) {

		AddWebSitePathAlias com = new AddWebSitePathAlias(webSitePathAliasToBeAdded);
		int usedTicketId;

		synchronized (WebSitePathAliasController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSitePathAliasAdded.class,
				event -> sendWebSitePathAliasChangedMessage(((WebSitePathAliasAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWebSitePathAlias(HttpServletRequest request) {

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

		WebSitePathAlias webSitePathAliasToBeUpdated = new WebSitePathAlias();

		try {
			webSitePathAliasToBeUpdated = WebSitePathAliasMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWebSitePathAlias(webSitePathAliasToBeUpdated);

	}

	/**
	 * Updates the WebSitePathAlias with the specific Id
	 * 
	 * @param webSitePathAliasToBeUpdated the WebSitePathAlias thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWebSitePathAlias(WebSitePathAlias webSitePathAliasToBeUpdated) {

		UpdateWebSitePathAlias com = new UpdateWebSitePathAlias(webSitePathAliasToBeUpdated);

		int usedTicketId;

		synchronized (WebSitePathAliasController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSitePathAliasUpdated.class,
				event -> sendWebSitePathAliasChangedMessage(((WebSitePathAliasUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WebSitePathAlias from the database
	 * 
	 * @param webSitePathAliasId:
	 *            the id of the WebSitePathAlias thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletewebSitePathAliasById(@RequestParam(value = "webSitePathAliasId") String webSitePathAliasId) {

		DeleteWebSitePathAlias com = new DeleteWebSitePathAlias(webSitePathAliasId);

		int usedTicketId;

		synchronized (WebSitePathAliasController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WebSitePathAliasDeleted.class,
				event -> sendWebSitePathAliasChangedMessage(((WebSitePathAliasDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWebSitePathAliasChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/webSitePathAlias/\" plus one of the following: "
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
