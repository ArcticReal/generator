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
import com.skytala.eCommerce.command.AddCustRequestItemNote;
import com.skytala.eCommerce.command.DeleteCustRequestItemNote;
import com.skytala.eCommerce.command.UpdateCustRequestItemNote;
import com.skytala.eCommerce.entity.CustRequestItemNote;
import com.skytala.eCommerce.entity.CustRequestItemNoteMapper;
import com.skytala.eCommerce.event.CustRequestItemNoteAdded;
import com.skytala.eCommerce.event.CustRequestItemNoteDeleted;
import com.skytala.eCommerce.event.CustRequestItemNoteFound;
import com.skytala.eCommerce.event.CustRequestItemNoteUpdated;
import com.skytala.eCommerce.query.FindCustRequestItemNotesBy;

@RestController
@RequestMapping("/api/custRequestItemNote")
public class CustRequestItemNoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestItemNote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestItemNoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestItemNote
	 * @return a List with the CustRequestItemNotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestItemNote> findCustRequestItemNotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestItemNotesBy query = new FindCustRequestItemNotesBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestItemNoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemNoteFound.class,
				event -> sendCustRequestItemNotesFoundMessage(((CustRequestItemNoteFound) event).getCustRequestItemNotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestItemNotesFoundMessage(List<CustRequestItemNote> custRequestItemNotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestItemNotes);
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
	public boolean createCustRequestItemNote(HttpServletRequest request) {

		CustRequestItemNote custRequestItemNoteToBeAdded = new CustRequestItemNote();
		try {
			custRequestItemNoteToBeAdded = CustRequestItemNoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestItemNote(custRequestItemNoteToBeAdded);

	}

	/**
	 * creates a new CustRequestItemNote entry in the ofbiz database
	 * 
	 * @param custRequestItemNoteToBeAdded
	 *            the CustRequestItemNote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestItemNote(CustRequestItemNote custRequestItemNoteToBeAdded) {

		AddCustRequestItemNote com = new AddCustRequestItemNote(custRequestItemNoteToBeAdded);
		int usedTicketId;

		synchronized (CustRequestItemNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemNoteAdded.class,
				event -> sendCustRequestItemNoteChangedMessage(((CustRequestItemNoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestItemNote(HttpServletRequest request) {

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

		CustRequestItemNote custRequestItemNoteToBeUpdated = new CustRequestItemNote();

		try {
			custRequestItemNoteToBeUpdated = CustRequestItemNoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestItemNote(custRequestItemNoteToBeUpdated);

	}

	/**
	 * Updates the CustRequestItemNote with the specific Id
	 * 
	 * @param custRequestItemNoteToBeUpdated the CustRequestItemNote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestItemNote(CustRequestItemNote custRequestItemNoteToBeUpdated) {

		UpdateCustRequestItemNote com = new UpdateCustRequestItemNote(custRequestItemNoteToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestItemNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemNoteUpdated.class,
				event -> sendCustRequestItemNoteChangedMessage(((CustRequestItemNoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestItemNote from the database
	 * 
	 * @param custRequestItemNoteId:
	 *            the id of the CustRequestItemNote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestItemNoteById(@RequestParam(value = "custRequestItemNoteId") String custRequestItemNoteId) {

		DeleteCustRequestItemNote com = new DeleteCustRequestItemNote(custRequestItemNoteId);

		int usedTicketId;

		synchronized (CustRequestItemNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestItemNoteDeleted.class,
				event -> sendCustRequestItemNoteChangedMessage(((CustRequestItemNoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestItemNoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestItemNote/\" plus one of the following: "
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
