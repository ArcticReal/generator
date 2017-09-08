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
import com.skytala.eCommerce.command.AddInvoiceTypeAttr;
import com.skytala.eCommerce.command.DeleteInvoiceTypeAttr;
import com.skytala.eCommerce.command.UpdateInvoiceTypeAttr;
import com.skytala.eCommerce.entity.InvoiceTypeAttr;
import com.skytala.eCommerce.entity.InvoiceTypeAttrMapper;
import com.skytala.eCommerce.event.InvoiceTypeAttrAdded;
import com.skytala.eCommerce.event.InvoiceTypeAttrDeleted;
import com.skytala.eCommerce.event.InvoiceTypeAttrFound;
import com.skytala.eCommerce.event.InvoiceTypeAttrUpdated;
import com.skytala.eCommerce.query.FindInvoiceTypeAttrsBy;

@RestController
@RequestMapping("/api/invoiceTypeAttr")
public class InvoiceTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceTypeAttr
	 * @return a List with the InvoiceTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceTypeAttr> findInvoiceTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceTypeAttrsBy query = new FindInvoiceTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTypeAttrFound.class,
				event -> sendInvoiceTypeAttrsFoundMessage(((InvoiceTypeAttrFound) event).getInvoiceTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceTypeAttrsFoundMessage(List<InvoiceTypeAttr> invoiceTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceTypeAttrs);
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
	public boolean createInvoiceTypeAttr(HttpServletRequest request) {

		InvoiceTypeAttr invoiceTypeAttrToBeAdded = new InvoiceTypeAttr();
		try {
			invoiceTypeAttrToBeAdded = InvoiceTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceTypeAttr(invoiceTypeAttrToBeAdded);

	}

	/**
	 * creates a new InvoiceTypeAttr entry in the ofbiz database
	 * 
	 * @param invoiceTypeAttrToBeAdded
	 *            the InvoiceTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceTypeAttr(InvoiceTypeAttr invoiceTypeAttrToBeAdded) {

		AddInvoiceTypeAttr com = new AddInvoiceTypeAttr(invoiceTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (InvoiceTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTypeAttrAdded.class,
				event -> sendInvoiceTypeAttrChangedMessage(((InvoiceTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceTypeAttr(HttpServletRequest request) {

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

		InvoiceTypeAttr invoiceTypeAttrToBeUpdated = new InvoiceTypeAttr();

		try {
			invoiceTypeAttrToBeUpdated = InvoiceTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceTypeAttr(invoiceTypeAttrToBeUpdated);

	}

	/**
	 * Updates the InvoiceTypeAttr with the specific Id
	 * 
	 * @param invoiceTypeAttrToBeUpdated the InvoiceTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceTypeAttr(InvoiceTypeAttr invoiceTypeAttrToBeUpdated) {

		UpdateInvoiceTypeAttr com = new UpdateInvoiceTypeAttr(invoiceTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTypeAttrUpdated.class,
				event -> sendInvoiceTypeAttrChangedMessage(((InvoiceTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceTypeAttr from the database
	 * 
	 * @param invoiceTypeAttrId:
	 *            the id of the InvoiceTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceTypeAttrById(@RequestParam(value = "invoiceTypeAttrId") String invoiceTypeAttrId) {

		DeleteInvoiceTypeAttr com = new DeleteInvoiceTypeAttr(invoiceTypeAttrId);

		int usedTicketId;

		synchronized (InvoiceTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTypeAttrDeleted.class,
				event -> sendInvoiceTypeAttrChangedMessage(((InvoiceTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceTypeAttr/\" plus one of the following: "
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
