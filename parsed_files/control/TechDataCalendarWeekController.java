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
import com.skytala.eCommerce.command.AddTechDataCalendarWeek;
import com.skytala.eCommerce.command.DeleteTechDataCalendarWeek;
import com.skytala.eCommerce.command.UpdateTechDataCalendarWeek;
import com.skytala.eCommerce.entity.TechDataCalendarWeek;
import com.skytala.eCommerce.entity.TechDataCalendarWeekMapper;
import com.skytala.eCommerce.event.TechDataCalendarWeekAdded;
import com.skytala.eCommerce.event.TechDataCalendarWeekDeleted;
import com.skytala.eCommerce.event.TechDataCalendarWeekFound;
import com.skytala.eCommerce.event.TechDataCalendarWeekUpdated;
import com.skytala.eCommerce.query.FindTechDataCalendarWeeksBy;

@RestController
@RequestMapping("/api/techDataCalendarWeek")
public class TechDataCalendarWeekController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TechDataCalendarWeek>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TechDataCalendarWeekController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TechDataCalendarWeek
	 * @return a List with the TechDataCalendarWeeks
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TechDataCalendarWeek> findTechDataCalendarWeeksBy(@RequestParam Map<String, String> allRequestParams) {

		FindTechDataCalendarWeeksBy query = new FindTechDataCalendarWeeksBy(allRequestParams);

		int usedTicketId;

		synchronized (TechDataCalendarWeekController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarWeekFound.class,
				event -> sendTechDataCalendarWeeksFoundMessage(((TechDataCalendarWeekFound) event).getTechDataCalendarWeeks(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTechDataCalendarWeeksFoundMessage(List<TechDataCalendarWeek> techDataCalendarWeeks, int usedTicketId) {
		queryReturnVal.put(usedTicketId, techDataCalendarWeeks);
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
	public boolean createTechDataCalendarWeek(HttpServletRequest request) {

		TechDataCalendarWeek techDataCalendarWeekToBeAdded = new TechDataCalendarWeek();
		try {
			techDataCalendarWeekToBeAdded = TechDataCalendarWeekMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTechDataCalendarWeek(techDataCalendarWeekToBeAdded);

	}

	/**
	 * creates a new TechDataCalendarWeek entry in the ofbiz database
	 * 
	 * @param techDataCalendarWeekToBeAdded
	 *            the TechDataCalendarWeek thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTechDataCalendarWeek(TechDataCalendarWeek techDataCalendarWeekToBeAdded) {

		AddTechDataCalendarWeek com = new AddTechDataCalendarWeek(techDataCalendarWeekToBeAdded);
		int usedTicketId;

		synchronized (TechDataCalendarWeekController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarWeekAdded.class,
				event -> sendTechDataCalendarWeekChangedMessage(((TechDataCalendarWeekAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTechDataCalendarWeek(HttpServletRequest request) {

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

		TechDataCalendarWeek techDataCalendarWeekToBeUpdated = new TechDataCalendarWeek();

		try {
			techDataCalendarWeekToBeUpdated = TechDataCalendarWeekMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTechDataCalendarWeek(techDataCalendarWeekToBeUpdated);

	}

	/**
	 * Updates the TechDataCalendarWeek with the specific Id
	 * 
	 * @param techDataCalendarWeekToBeUpdated the TechDataCalendarWeek thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTechDataCalendarWeek(TechDataCalendarWeek techDataCalendarWeekToBeUpdated) {

		UpdateTechDataCalendarWeek com = new UpdateTechDataCalendarWeek(techDataCalendarWeekToBeUpdated);

		int usedTicketId;

		synchronized (TechDataCalendarWeekController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarWeekUpdated.class,
				event -> sendTechDataCalendarWeekChangedMessage(((TechDataCalendarWeekUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TechDataCalendarWeek from the database
	 * 
	 * @param techDataCalendarWeekId:
	 *            the id of the TechDataCalendarWeek thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetechDataCalendarWeekById(@RequestParam(value = "techDataCalendarWeekId") String techDataCalendarWeekId) {

		DeleteTechDataCalendarWeek com = new DeleteTechDataCalendarWeek(techDataCalendarWeekId);

		int usedTicketId;

		synchronized (TechDataCalendarWeekController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarWeekDeleted.class,
				event -> sendTechDataCalendarWeekChangedMessage(((TechDataCalendarWeekDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTechDataCalendarWeekChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/techDataCalendarWeek/\" plus one of the following: "
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
