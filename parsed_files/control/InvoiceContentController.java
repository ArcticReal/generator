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
import com.skytala.eCommerce.command.AddInvoiceContent;
import com.skytala.eCommerce.command.DeleteInvoiceContent;
import com.skytala.eCommerce.command.UpdateInvoiceContent;
import com.skytala.eCommerce.entity.InvoiceContent;
import com.skytala.eCommerce.entity.InvoiceContentMapper;
import com.skytala.eCommerce.event.InvoiceContentAdded;
import com.skytala.eCommerce.event.InvoiceContentDeleted;
import com.skytala.eCommerce.event.InvoiceContentFound;
import com.skytala.eCommerce.event.InvoiceContentUpdated;
import com.skytala.eCommerce.query.FindInvoiceContentsBy;

@RestController
@RequestMapping("/api/invoiceContent")
public class InvoiceContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceContent
	 * @return a List with the InvoiceContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceContent> findInvoiceContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceContentsBy query = new FindInvoiceContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContentFound.class,
				event -> sendInvoiceContentsFoundMessage(((InvoiceContentFound) event).getInvoiceContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceContentsFoundMessage(List<InvoiceContent> invoiceContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceContents);
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
	public boolean createInvoiceContent(HttpServletRequest request) {

		InvoiceContent invoiceContentToBeAdded = new InvoiceContent();
		try {
			invoiceContentToBeAdded = InvoiceContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceContent(invoiceContentToBeAdded);

	}

	/**
	 * creates a new InvoiceContent entry in the ofbiz database
	 * 
	 * @param invoiceContentToBeAdded
	 *            the InvoiceContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceContent(InvoiceContent invoiceContentToBeAdded) {

		AddInvoiceContent com = new AddInvoiceContent(invoiceContentToBeAdded);
		int usedTicketId;

		synchronized (InvoiceContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContentAdded.class,
				event -> sendInvoiceContentChangedMessage(((InvoiceContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceContent(HttpServletRequest request) {

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

		InvoiceContent invoiceContentToBeUpdated = new InvoiceContent();

		try {
			invoiceContentToBeUpdated = InvoiceContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceContent(invoiceContentToBeUpdated);

	}

	/**
	 * Updates the InvoiceContent with the specific Id
	 * 
	 * @param invoiceContentToBeUpdated the InvoiceContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceContent(InvoiceContent invoiceContentToBeUpdated) {

		UpdateInvoiceContent com = new UpdateInvoiceContent(invoiceContentToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContentUpdated.class,
				event -> sendInvoiceContentChangedMessage(((InvoiceContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceContent from the database
	 * 
	 * @param invoiceContentId:
	 *            the id of the InvoiceContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceContentById(@RequestParam(value = "invoiceContentId") String invoiceContentId) {

		DeleteInvoiceContent com = new DeleteInvoiceContent(invoiceContentId);

		int usedTicketId;

		synchronized (InvoiceContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceContentDeleted.class,
				event -> sendInvoiceContentChangedMessage(((InvoiceContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceContent/\" plus one of the following: "
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
