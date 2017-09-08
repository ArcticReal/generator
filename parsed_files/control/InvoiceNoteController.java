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
import com.skytala.eCommerce.command.AddInvoiceNote;
import com.skytala.eCommerce.command.DeleteInvoiceNote;
import com.skytala.eCommerce.command.UpdateInvoiceNote;
import com.skytala.eCommerce.entity.InvoiceNote;
import com.skytala.eCommerce.entity.InvoiceNoteMapper;
import com.skytala.eCommerce.event.InvoiceNoteAdded;
import com.skytala.eCommerce.event.InvoiceNoteDeleted;
import com.skytala.eCommerce.event.InvoiceNoteFound;
import com.skytala.eCommerce.event.InvoiceNoteUpdated;
import com.skytala.eCommerce.query.FindInvoiceNotesBy;

@RestController
@RequestMapping("/api/invoiceNote")
public class InvoiceNoteController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceNote>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceNoteController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceNote
	 * @return a List with the InvoiceNotes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceNote> findInvoiceNotesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceNotesBy query = new FindInvoiceNotesBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceNoteController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceNoteFound.class,
				event -> sendInvoiceNotesFoundMessage(((InvoiceNoteFound) event).getInvoiceNotes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceNotesFoundMessage(List<InvoiceNote> invoiceNotes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceNotes);
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
	public boolean createInvoiceNote(HttpServletRequest request) {

		InvoiceNote invoiceNoteToBeAdded = new InvoiceNote();
		try {
			invoiceNoteToBeAdded = InvoiceNoteMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceNote(invoiceNoteToBeAdded);

	}

	/**
	 * creates a new InvoiceNote entry in the ofbiz database
	 * 
	 * @param invoiceNoteToBeAdded
	 *            the InvoiceNote thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceNote(InvoiceNote invoiceNoteToBeAdded) {

		AddInvoiceNote com = new AddInvoiceNote(invoiceNoteToBeAdded);
		int usedTicketId;

		synchronized (InvoiceNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceNoteAdded.class,
				event -> sendInvoiceNoteChangedMessage(((InvoiceNoteAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceNote(HttpServletRequest request) {

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

		InvoiceNote invoiceNoteToBeUpdated = new InvoiceNote();

		try {
			invoiceNoteToBeUpdated = InvoiceNoteMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceNote(invoiceNoteToBeUpdated);

	}

	/**
	 * Updates the InvoiceNote with the specific Id
	 * 
	 * @param invoiceNoteToBeUpdated the InvoiceNote thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceNote(InvoiceNote invoiceNoteToBeUpdated) {

		UpdateInvoiceNote com = new UpdateInvoiceNote(invoiceNoteToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceNoteUpdated.class,
				event -> sendInvoiceNoteChangedMessage(((InvoiceNoteUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceNote from the database
	 * 
	 * @param invoiceNoteId:
	 *            the id of the InvoiceNote thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceNoteById(@RequestParam(value = "invoiceNoteId") String invoiceNoteId) {

		DeleteInvoiceNote com = new DeleteInvoiceNote(invoiceNoteId);

		int usedTicketId;

		synchronized (InvoiceNoteController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceNoteDeleted.class,
				event -> sendInvoiceNoteChangedMessage(((InvoiceNoteDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceNoteChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceNote/\" plus one of the following: "
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
