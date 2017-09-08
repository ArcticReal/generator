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
import com.skytala.eCommerce.command.AddTechDataCalendarExcWeek;
import com.skytala.eCommerce.command.DeleteTechDataCalendarExcWeek;
import com.skytala.eCommerce.command.UpdateTechDataCalendarExcWeek;
import com.skytala.eCommerce.entity.TechDataCalendarExcWeek;
import com.skytala.eCommerce.entity.TechDataCalendarExcWeekMapper;
import com.skytala.eCommerce.event.TechDataCalendarExcWeekAdded;
import com.skytala.eCommerce.event.TechDataCalendarExcWeekDeleted;
import com.skytala.eCommerce.event.TechDataCalendarExcWeekFound;
import com.skytala.eCommerce.event.TechDataCalendarExcWeekUpdated;
import com.skytala.eCommerce.query.FindTechDataCalendarExcWeeksBy;

@RestController
@RequestMapping("/api/techDataCalendarExcWeek")
public class TechDataCalendarExcWeekController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TechDataCalendarExcWeek>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TechDataCalendarExcWeekController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TechDataCalendarExcWeek
	 * @return a List with the TechDataCalendarExcWeeks
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TechDataCalendarExcWeek> findTechDataCalendarExcWeeksBy(@RequestParam Map<String, String> allRequestParams) {

		FindTechDataCalendarExcWeeksBy query = new FindTechDataCalendarExcWeeksBy(allRequestParams);

		int usedTicketId;

		synchronized (TechDataCalendarExcWeekController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarExcWeekFound.class,
				event -> sendTechDataCalendarExcWeeksFoundMessage(((TechDataCalendarExcWeekFound) event).getTechDataCalendarExcWeeks(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTechDataCalendarExcWeeksFoundMessage(List<TechDataCalendarExcWeek> techDataCalendarExcWeeks, int usedTicketId) {
		queryReturnVal.put(usedTicketId, techDataCalendarExcWeeks);
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
	public boolean createTechDataCalendarExcWeek(HttpServletRequest request) {

		TechDataCalendarExcWeek techDataCalendarExcWeekToBeAdded = new TechDataCalendarExcWeek();
		try {
			techDataCalendarExcWeekToBeAdded = TechDataCalendarExcWeekMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTechDataCalendarExcWeek(techDataCalendarExcWeekToBeAdded);

	}

	/**
	 * creates a new TechDataCalendarExcWeek entry in the ofbiz database
	 * 
	 * @param techDataCalendarExcWeekToBeAdded
	 *            the TechDataCalendarExcWeek thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTechDataCalendarExcWeek(TechDataCalendarExcWeek techDataCalendarExcWeekToBeAdded) {

		AddTechDataCalendarExcWeek com = new AddTechDataCalendarExcWeek(techDataCalendarExcWeekToBeAdded);
		int usedTicketId;

		synchronized (TechDataCalendarExcWeekController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarExcWeekAdded.class,
				event -> sendTechDataCalendarExcWeekChangedMessage(((TechDataCalendarExcWeekAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTechDataCalendarExcWeek(HttpServletRequest request) {

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

		TechDataCalendarExcWeek techDataCalendarExcWeekToBeUpdated = new TechDataCalendarExcWeek();

		try {
			techDataCalendarExcWeekToBeUpdated = TechDataCalendarExcWeekMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTechDataCalendarExcWeek(techDataCalendarExcWeekToBeUpdated);

	}

	/**
	 * Updates the TechDataCalendarExcWeek with the specific Id
	 * 
	 * @param techDataCalendarExcWeekToBeUpdated the TechDataCalendarExcWeek thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTechDataCalendarExcWeek(TechDataCalendarExcWeek techDataCalendarExcWeekToBeUpdated) {

		UpdateTechDataCalendarExcWeek com = new UpdateTechDataCalendarExcWeek(techDataCalendarExcWeekToBeUpdated);

		int usedTicketId;

		synchronized (TechDataCalendarExcWeekController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarExcWeekUpdated.class,
				event -> sendTechDataCalendarExcWeekChangedMessage(((TechDataCalendarExcWeekUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TechDataCalendarExcWeek from the database
	 * 
	 * @param techDataCalendarExcWeekId:
	 *            the id of the TechDataCalendarExcWeek thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetechDataCalendarExcWeekById(@RequestParam(value = "techDataCalendarExcWeekId") String techDataCalendarExcWeekId) {

		DeleteTechDataCalendarExcWeek com = new DeleteTechDataCalendarExcWeek(techDataCalendarExcWeekId);

		int usedTicketId;

		synchronized (TechDataCalendarExcWeekController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TechDataCalendarExcWeekDeleted.class,
				event -> sendTechDataCalendarExcWeekChangedMessage(((TechDataCalendarExcWeekDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTechDataCalendarExcWeekChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/techDataCalendarExcWeek/\" plus one of the following: "
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
