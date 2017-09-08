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
import com.skytala.eCommerce.command.AddCustRequestNote;
import com.skytala.eCommerce.command.DeleteCustRequestNote;
import com.skytala.eCommerce.command.UpdateCustRequestNote;
import com.skytala.eCommerce.entity.CustRequestNote;
import com.skytala.eCommerce.entity.CustRequestNoteMapper;
import com.skytala.eCommerce.event.CustRequestNoteAdded;
import com.skytala.eCommerce.event.CustRequestNoteDeleted;
import com.skytala.eCommerce.event.CustRequestNoteFound;
import com.skytala.eCommerce.event.CustRequestNoteUpdated;
import com.skytala.eCommerce.query.FindCustRequestNotesBy;

@RestController
@RequestMapping("/api/custRequestNote")
public class CustRequestNoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestNote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestNoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestNote
	 * @return a List with the CustRequestNotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestNote> findCustRequestNotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestNotesBy query = new FindCustRequestNotesBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestNoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestNoteFound.class,
				event -> sendCustRequestNotesFoundMessage(((CustRequestNoteFound) event).getCustRequestNotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestNotesFoundMessage(List<CustRequestNote> custRequestNotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestNotes);
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
	public boolean createCustRequestNote(HttpServletRequest request) {

		CustRequestNote custRequestNoteToBeAdded = new CustRequestNote();
		try {
			custRequestNoteToBeAdded = CustRequestNoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestNote(custRequestNoteToBeAdded);

	}

	/**
	 * creates a new CustRequestNote entry in the ofbiz database
	 * 
	 * @param custRequestNoteToBeAdded
	 *            the CustRequestNote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestNote(CustRequestNote custRequestNoteToBeAdded) {

		AddCustRequestNote com = new AddCustRequestNote(custRequestNoteToBeAdded);
		int usedTicketId;

		synchronized (CustRequestNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestNoteAdded.class,
				event -> sendCustRequestNoteChangedMessage(((CustRequestNoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestNote(HttpServletRequest request) {

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

		CustRequestNote custRequestNoteToBeUpdated = new CustRequestNote();

		try {
			custRequestNoteToBeUpdated = CustRequestNoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestNote(custRequestNoteToBeUpdated);

	}

	/**
	 * Updates the CustRequestNote with the specific Id
	 * 
	 * @param custRequestNoteToBeUpdated the CustRequestNote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestNote(CustRequestNote custRequestNoteToBeUpdated) {

		UpdateCustRequestNote com = new UpdateCustRequestNote(custRequestNoteToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestNoteUpdated.class,
				event -> sendCustRequestNoteChangedMessage(((CustRequestNoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestNote from the database
	 * 
	 * @param custRequestNoteId:
	 *            the id of the CustRequestNote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestNoteById(@RequestParam(value = "custRequestNoteId") String custRequestNoteId) {

		DeleteCustRequestNote com = new DeleteCustRequestNote(custRequestNoteId);

		int usedTicketId;

		synchronized (CustRequestNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestNoteDeleted.class,
				event -> sendCustRequestNoteChangedMessage(((CustRequestNoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestNoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestNote/\" plus one of the following: "
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
