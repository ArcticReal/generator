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
import com.skytala.eCommerce.command.AddWorkEffortNote;
import com.skytala.eCommerce.command.DeleteWorkEffortNote;
import com.skytala.eCommerce.command.UpdateWorkEffortNote;
import com.skytala.eCommerce.entity.WorkEffortNote;
import com.skytala.eCommerce.entity.WorkEffortNoteMapper;
import com.skytala.eCommerce.event.WorkEffortNoteAdded;
import com.skytala.eCommerce.event.WorkEffortNoteDeleted;
import com.skytala.eCommerce.event.WorkEffortNoteFound;
import com.skytala.eCommerce.event.WorkEffortNoteUpdated;
import com.skytala.eCommerce.query.FindWorkEffortNotesBy;

@RestController
@RequestMapping("/api/workEffortNote")
public class WorkEffortNoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<WorkEffortNote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public WorkEffortNoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a WorkEffortNote
	 * @return a List with the WorkEffortNotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<WorkEffortNote> findWorkEffortNotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindWorkEffortNotesBy query = new FindWorkEffortNotesBy(allRequestParams);

		int usedTicketId;

		synchronized (WorkEffortNoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortNoteFound.class,
				event -> sendWorkEffortNotesFoundMessage(((WorkEffortNoteFound) event).getWorkEffortNotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendWorkEffortNotesFoundMessage(List<WorkEffortNote> workEffortNotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, workEffortNotes);
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
	public boolean createWorkEffortNote(HttpServletRequest request) {

		WorkEffortNote workEffortNoteToBeAdded = new WorkEffortNote();
		try {
			workEffortNoteToBeAdded = WorkEffortNoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createWorkEffortNote(workEffortNoteToBeAdded);

	}

	/**
	 * creates a new WorkEffortNote entry in the ofbiz database
	 * 
	 * @param workEffortNoteToBeAdded
	 *            the WorkEffortNote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createWorkEffortNote(WorkEffortNote workEffortNoteToBeAdded) {

		AddWorkEffortNote com = new AddWorkEffortNote(workEffortNoteToBeAdded);
		int usedTicketId;

		synchronized (WorkEffortNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortNoteAdded.class,
				event -> sendWorkEffortNoteChangedMessage(((WorkEffortNoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateWorkEffortNote(HttpServletRequest request) {

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

		WorkEffortNote workEffortNoteToBeUpdated = new WorkEffortNote();

		try {
			workEffortNoteToBeUpdated = WorkEffortNoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateWorkEffortNote(workEffortNoteToBeUpdated);

	}

	/**
	 * Updates the WorkEffortNote with the specific Id
	 * 
	 * @param workEffortNoteToBeUpdated the WorkEffortNote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateWorkEffortNote(WorkEffortNote workEffortNoteToBeUpdated) {

		UpdateWorkEffortNote com = new UpdateWorkEffortNote(workEffortNoteToBeUpdated);

		int usedTicketId;

		synchronized (WorkEffortNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortNoteUpdated.class,
				event -> sendWorkEffortNoteChangedMessage(((WorkEffortNoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a WorkEffortNote from the database
	 * 
	 * @param workEffortNoteId:
	 *            the id of the WorkEffortNote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteworkEffortNoteById(@RequestParam(value = "workEffortNoteId") String workEffortNoteId) {

		DeleteWorkEffortNote com = new DeleteWorkEffortNote(workEffortNoteId);

		int usedTicketId;

		synchronized (WorkEffortNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(WorkEffortNoteDeleted.class,
				event -> sendWorkEffortNoteChangedMessage(((WorkEffortNoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendWorkEffortNoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/workEffortNote/\" plus one of the following: "
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
