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
import com.skytala.eCommerce.command.AddGlAccountHistory;
import com.skytala.eCommerce.command.DeleteGlAccountHistory;
import com.skytala.eCommerce.command.UpdateGlAccountHistory;
import com.skytala.eCommerce.entity.GlAccountHistory;
import com.skytala.eCommerce.entity.GlAccountHistoryMapper;
import com.skytala.eCommerce.event.GlAccountHistoryAdded;
import com.skytala.eCommerce.event.GlAccountHistoryDeleted;
import com.skytala.eCommerce.event.GlAccountHistoryFound;
import com.skytala.eCommerce.event.GlAccountHistoryUpdated;
import com.skytala.eCommerce.query.FindGlAccountHistorysBy;

@RestController
@RequestMapping("/api/glAccountHistory")
public class GlAccountHistoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccountHistory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountHistoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccountHistory
	 * @return a List with the GlAccountHistorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccountHistory> findGlAccountHistorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountHistorysBy query = new FindGlAccountHistorysBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountHistoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountHistoryFound.class,
				event -> sendGlAccountHistorysFoundMessage(((GlAccountHistoryFound) event).getGlAccountHistorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountHistorysFoundMessage(List<GlAccountHistory> glAccountHistorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccountHistorys);
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
	public boolean createGlAccountHistory(HttpServletRequest request) {

		GlAccountHistory glAccountHistoryToBeAdded = new GlAccountHistory();
		try {
			glAccountHistoryToBeAdded = GlAccountHistoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccountHistory(glAccountHistoryToBeAdded);

	}

	/**
	 * creates a new GlAccountHistory entry in the ofbiz database
	 * 
	 * @param glAccountHistoryToBeAdded
	 *            the GlAccountHistory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccountHistory(GlAccountHistory glAccountHistoryToBeAdded) {

		AddGlAccountHistory com = new AddGlAccountHistory(glAccountHistoryToBeAdded);
		int usedTicketId;

		synchronized (GlAccountHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountHistoryAdded.class,
				event -> sendGlAccountHistoryChangedMessage(((GlAccountHistoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccountHistory(HttpServletRequest request) {

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

		GlAccountHistory glAccountHistoryToBeUpdated = new GlAccountHistory();

		try {
			glAccountHistoryToBeUpdated = GlAccountHistoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccountHistory(glAccountHistoryToBeUpdated);

	}

	/**
	 * Updates the GlAccountHistory with the specific Id
	 * 
	 * @param glAccountHistoryToBeUpdated the GlAccountHistory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccountHistory(GlAccountHistory glAccountHistoryToBeUpdated) {

		UpdateGlAccountHistory com = new UpdateGlAccountHistory(glAccountHistoryToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountHistoryUpdated.class,
				event -> sendGlAccountHistoryChangedMessage(((GlAccountHistoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccountHistory from the database
	 * 
	 * @param glAccountHistoryId:
	 *            the id of the GlAccountHistory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountHistoryById(@RequestParam(value = "glAccountHistoryId") String glAccountHistoryId) {

		DeleteGlAccountHistory com = new DeleteGlAccountHistory(glAccountHistoryId);

		int usedTicketId;

		synchronized (GlAccountHistoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountHistoryDeleted.class,
				event -> sendGlAccountHistoryChangedMessage(((GlAccountHistoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountHistoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccountHistory/\" plus one of the following: "
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
