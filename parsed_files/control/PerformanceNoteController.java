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
import com.skytala.eCommerce.command.AddPerformanceNote;
import com.skytala.eCommerce.command.DeletePerformanceNote;
import com.skytala.eCommerce.command.UpdatePerformanceNote;
import com.skytala.eCommerce.entity.PerformanceNote;
import com.skytala.eCommerce.entity.PerformanceNoteMapper;
import com.skytala.eCommerce.event.PerformanceNoteAdded;
import com.skytala.eCommerce.event.PerformanceNoteDeleted;
import com.skytala.eCommerce.event.PerformanceNoteFound;
import com.skytala.eCommerce.event.PerformanceNoteUpdated;
import com.skytala.eCommerce.query.FindPerformanceNotesBy;

@RestController
@RequestMapping("/api/performanceNote")
public class PerformanceNoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PerformanceNote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PerformanceNoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PerformanceNote
	 * @return a List with the PerformanceNotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PerformanceNote> findPerformanceNotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPerformanceNotesBy query = new FindPerformanceNotesBy(allRequestParams);

		int usedTicketId;

		synchronized (PerformanceNoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerformanceNoteFound.class,
				event -> sendPerformanceNotesFoundMessage(((PerformanceNoteFound) event).getPerformanceNotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPerformanceNotesFoundMessage(List<PerformanceNote> performanceNotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, performanceNotes);
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
	public boolean createPerformanceNote(HttpServletRequest request) {

		PerformanceNote performanceNoteToBeAdded = new PerformanceNote();
		try {
			performanceNoteToBeAdded = PerformanceNoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPerformanceNote(performanceNoteToBeAdded);

	}

	/**
	 * creates a new PerformanceNote entry in the ofbiz database
	 * 
	 * @param performanceNoteToBeAdded
	 *            the PerformanceNote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPerformanceNote(PerformanceNote performanceNoteToBeAdded) {

		AddPerformanceNote com = new AddPerformanceNote(performanceNoteToBeAdded);
		int usedTicketId;

		synchronized (PerformanceNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerformanceNoteAdded.class,
				event -> sendPerformanceNoteChangedMessage(((PerformanceNoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePerformanceNote(HttpServletRequest request) {

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

		PerformanceNote performanceNoteToBeUpdated = new PerformanceNote();

		try {
			performanceNoteToBeUpdated = PerformanceNoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePerformanceNote(performanceNoteToBeUpdated);

	}

	/**
	 * Updates the PerformanceNote with the specific Id
	 * 
	 * @param performanceNoteToBeUpdated the PerformanceNote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePerformanceNote(PerformanceNote performanceNoteToBeUpdated) {

		UpdatePerformanceNote com = new UpdatePerformanceNote(performanceNoteToBeUpdated);

		int usedTicketId;

		synchronized (PerformanceNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerformanceNoteUpdated.class,
				event -> sendPerformanceNoteChangedMessage(((PerformanceNoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PerformanceNote from the database
	 * 
	 * @param performanceNoteId:
	 *            the id of the PerformanceNote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteperformanceNoteById(@RequestParam(value = "performanceNoteId") String performanceNoteId) {

		DeletePerformanceNote com = new DeletePerformanceNote(performanceNoteId);

		int usedTicketId;

		synchronized (PerformanceNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerformanceNoteDeleted.class,
				event -> sendPerformanceNoteChangedMessage(((PerformanceNoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPerformanceNoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/performanceNote/\" plus one of the following: "
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
