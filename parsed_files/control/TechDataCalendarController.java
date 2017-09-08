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
import com.skytala.eCommerce.command.AddTechDataCalendar;
import com.skytala.eCommerce.command.DeleteTechDataCalendar;
import com.skytala.eCommerce.command.UpdateTechDataCalendar;
import com.skytala.eCommerce.entity.TechDataCalendar;
import com.skytala.eCommerce.entity.TechDataCalendarMapper;
import com.skytala.eCommerce.event.TechDataCalendarAdded;
import com.skytala.eCommerce.event.TechDataCalendarDeleted;
import com.skytala.eCommerce.event.TechDataCalendarFound;
import com.skytala.eCommerce.event.TechDataCalendarUpdated;
import com.skytala.eCommerce.query.FindTechDataCalendarsBy;

@RestController
@RequestMapping("/api/techDataCalendar")
public class TechDataCalendarController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TechDataCalendar>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TechDataCalendarController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TechDataCalendar
	 * @return a List with the TechDataCalendars
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TechDataCalendar> findTechDataCalendarsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTechDataCalendarsBy query = new FindTechDataCalendarsBy(allRequestParams);

		int usedTicketId;

		synchronized (TechDataCalendarController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarFound.class,
				event -> sendTechDataCalendarsFoundMessage(((TechDataCalendarFound) event).getTechDataCalendars(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTechDataCalendarsFoundMessage(List<TechDataCalendar> techDataCalendars, int usedTicketId) {
		queryReturnVal.put(usedTicketId, techDataCalendars);
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
	public boolean createTechDataCalendar(HttpServletRequest request) {

		TechDataCalendar techDataCalendarToBeAdded = new TechDataCalendar();
		try {
			techDataCalendarToBeAdded = TechDataCalendarMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTechDataCalendar(techDataCalendarToBeAdded);

	}

	/**
	 * creates a new TechDataCalendar entry in the ofbiz database
	 * 
	 * @param techDataCalendarToBeAdded
	 *            the TechDataCalendar thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTechDataCalendar(TechDataCalendar techDataCalendarToBeAdded) {

		AddTechDataCalendar com = new AddTechDataCalendar(techDataCalendarToBeAdded);
		int usedTicketId;

		synchronized (TechDataCalendarController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarAdded.class,
				event -> sendTechDataCalendarChangedMessage(((TechDataCalendarAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTechDataCalendar(HttpServletRequest request) {

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

		TechDataCalendar techDataCalendarToBeUpdated = new TechDataCalendar();

		try {
			techDataCalendarToBeUpdated = TechDataCalendarMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTechDataCalendar(techDataCalendarToBeUpdated);

	}

	/**
	 * Updates the TechDataCalendar with the specific Id
	 * 
	 * @param techDataCalendarToBeUpdated the TechDataCalendar thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTechDataCalendar(TechDataCalendar techDataCalendarToBeUpdated) {

		UpdateTechDataCalendar com = new UpdateTechDataCalendar(techDataCalendarToBeUpdated);

		int usedTicketId;

		synchronized (TechDataCalendarController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarUpdated.class,
				event -> sendTechDataCalendarChangedMessage(((TechDataCalendarUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TechDataCalendar from the database
	 * 
	 * @param techDataCalendarId:
	 *            the id of the TechDataCalendar thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetechDataCalendarById(@RequestParam(value = "techDataCalendarId") String techDataCalendarId) {

		DeleteTechDataCalendar com = new DeleteTechDataCalendar(techDataCalendarId);

		int usedTicketId;

		synchronized (TechDataCalendarController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarDeleted.class,
				event -> sendTechDataCalendarChangedMessage(((TechDataCalendarDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTechDataCalendarChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/techDataCalendar/\" plus one of the following: "
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
