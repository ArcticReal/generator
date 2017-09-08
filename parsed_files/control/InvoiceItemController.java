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
import com.skytala.eCommerce.command.AddInvoiceItem;
import com.skytala.eCommerce.command.DeleteInvoiceItem;
import com.skytala.eCommerce.command.UpdateInvoiceItem;
import com.skytala.eCommerce.entity.InvoiceItem;
import com.skytala.eCommerce.entity.InvoiceItemMapper;
import com.skytala.eCommerce.event.InvoiceItemAdded;
import com.skytala.eCommerce.event.InvoiceItemDeleted;
import com.skytala.eCommerce.event.InvoiceItemFound;
import com.skytala.eCommerce.event.InvoiceItemUpdated;
import com.skytala.eCommerce.query.FindInvoiceItemsBy;

@RestController
@RequestMapping("/api/invoiceItem")
public class InvoiceItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceItem
	 * @return a List with the InvoiceItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceItem> findInvoiceItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceItemsBy query = new FindInvoiceItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemFound.class,
				event -> sendInvoiceItemsFoundMessage(((InvoiceItemFound) event).getInvoiceItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceItemsFoundMessage(List<InvoiceItem> invoiceItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceItems);
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
	public boolean createInvoiceItem(HttpServletRequest request) {

		InvoiceItem invoiceItemToBeAdded = new InvoiceItem();
		try {
			invoiceItemToBeAdded = InvoiceItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceItem(invoiceItemToBeAdded);

	}

	/**
	 * creates a new InvoiceItem entry in the ofbiz database
	 * 
	 * @param invoiceItemToBeAdded
	 *            the InvoiceItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceItem(InvoiceItem invoiceItemToBeAdded) {

		AddInvoiceItem com = new AddInvoiceItem(invoiceItemToBeAdded);
		int usedTicketId;

		synchronized (InvoiceItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemAdded.class,
				event -> sendInvoiceItemChangedMessage(((InvoiceItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceItem(HttpServletRequest request) {

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

		InvoiceItem invoiceItemToBeUpdated = new InvoiceItem();

		try {
			invoiceItemToBeUpdated = InvoiceItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceItem(invoiceItemToBeUpdated);

	}

	/**
	 * Updates the InvoiceItem with the specific Id
	 * 
	 * @param invoiceItemToBeUpdated the InvoiceItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceItem(InvoiceItem invoiceItemToBeUpdated) {

		UpdateInvoiceItem com = new UpdateInvoiceItem(invoiceItemToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemUpdated.class,
				event -> sendInvoiceItemChangedMessage(((InvoiceItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceItem from the database
	 * 
	 * @param invoiceItemId:
	 *            the id of the InvoiceItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceItemById(@RequestParam(value = "invoiceItemId") String invoiceItemId) {

		DeleteInvoiceItem com = new DeleteInvoiceItem(invoiceItemId);

		int usedTicketId;

		synchronized (InvoiceItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemDeleted.class,
				event -> sendInvoiceItemChangedMessage(((InvoiceItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceItem/\" plus one of the following: "
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
