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
import com.skytala.eCommerce.command.AddQuoteNote;
import com.skytala.eCommerce.command.DeleteQuoteNote;
import com.skytala.eCommerce.command.UpdateQuoteNote;
import com.skytala.eCommerce.entity.QuoteNote;
import com.skytala.eCommerce.entity.QuoteNoteMapper;
import com.skytala.eCommerce.event.QuoteNoteAdded;
import com.skytala.eCommerce.event.QuoteNoteDeleted;
import com.skytala.eCommerce.event.QuoteNoteFound;
import com.skytala.eCommerce.event.QuoteNoteUpdated;
import com.skytala.eCommerce.query.FindQuoteNotesBy;

@RestController
@RequestMapping("/api/quoteNote")
public class QuoteNoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<QuoteNote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public QuoteNoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a QuoteNote
	 * @return a List with the QuoteNotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<QuoteNote> findQuoteNotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindQuoteNotesBy query = new FindQuoteNotesBy(allRequestParams);

		int usedTicketId;

		synchronized (QuoteNoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteNoteFound.class,
				event -> sendQuoteNotesFoundMessage(((QuoteNoteFound) event).getQuoteNotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendQuoteNotesFoundMessage(List<QuoteNote> quoteNotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, quoteNotes);
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
	public boolean createQuoteNote(HttpServletRequest request) {

		QuoteNote quoteNoteToBeAdded = new QuoteNote();
		try {
			quoteNoteToBeAdded = QuoteNoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createQuoteNote(quoteNoteToBeAdded);

	}

	/**
	 * creates a new QuoteNote entry in the ofbiz database
	 * 
	 * @param quoteNoteToBeAdded
	 *            the QuoteNote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createQuoteNote(QuoteNote quoteNoteToBeAdded) {

		AddQuoteNote com = new AddQuoteNote(quoteNoteToBeAdded);
		int usedTicketId;

		synchronized (QuoteNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteNoteAdded.class,
				event -> sendQuoteNoteChangedMessage(((QuoteNoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateQuoteNote(HttpServletRequest request) {

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

		QuoteNote quoteNoteToBeUpdated = new QuoteNote();

		try {
			quoteNoteToBeUpdated = QuoteNoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateQuoteNote(quoteNoteToBeUpdated);

	}

	/**
	 * Updates the QuoteNote with the specific Id
	 * 
	 * @param quoteNoteToBeUpdated the QuoteNote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateQuoteNote(QuoteNote quoteNoteToBeUpdated) {

		UpdateQuoteNote com = new UpdateQuoteNote(quoteNoteToBeUpdated);

		int usedTicketId;

		synchronized (QuoteNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteNoteUpdated.class,
				event -> sendQuoteNoteChangedMessage(((QuoteNoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a QuoteNote from the database
	 * 
	 * @param quoteNoteId:
	 *            the id of the QuoteNote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletequoteNoteById(@RequestParam(value = "quoteNoteId") String quoteNoteId) {

		DeleteQuoteNote com = new DeleteQuoteNote(quoteNoteId);

		int usedTicketId;

		synchronized (QuoteNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(QuoteNoteDeleted.class,
				event -> sendQuoteNoteChangedMessage(((QuoteNoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendQuoteNoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/quoteNote/\" plus one of the following: "
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
