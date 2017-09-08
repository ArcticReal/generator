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
import com.skytala.eCommerce.command.AddInvoiceItemTypeAttr;
import com.skytala.eCommerce.command.DeleteInvoiceItemTypeAttr;
import com.skytala.eCommerce.command.UpdateInvoiceItemTypeAttr;
import com.skytala.eCommerce.entity.InvoiceItemTypeAttr;
import com.skytala.eCommerce.entity.InvoiceItemTypeAttrMapper;
import com.skytala.eCommerce.event.InvoiceItemTypeAttrAdded;
import com.skytala.eCommerce.event.InvoiceItemTypeAttrDeleted;
import com.skytala.eCommerce.event.InvoiceItemTypeAttrFound;
import com.skytala.eCommerce.event.InvoiceItemTypeAttrUpdated;
import com.skytala.eCommerce.query.FindInvoiceItemTypeAttrsBy;

@RestController
@RequestMapping("/api/invoiceItemTypeAttr")
public class InvoiceItemTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceItemTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceItemTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceItemTypeAttr
	 * @return a List with the InvoiceItemTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceItemTypeAttr> findInvoiceItemTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceItemTypeAttrsBy query = new FindInvoiceItemTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceItemTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeAttrFound.class,
				event -> sendInvoiceItemTypeAttrsFoundMessage(((InvoiceItemTypeAttrFound) event).getInvoiceItemTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceItemTypeAttrsFoundMessage(List<InvoiceItemTypeAttr> invoiceItemTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceItemTypeAttrs);
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
	public boolean createInvoiceItemTypeAttr(HttpServletRequest request) {

		InvoiceItemTypeAttr invoiceItemTypeAttrToBeAdded = new InvoiceItemTypeAttr();
		try {
			invoiceItemTypeAttrToBeAdded = InvoiceItemTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceItemTypeAttr(invoiceItemTypeAttrToBeAdded);

	}

	/**
	 * creates a new InvoiceItemTypeAttr entry in the ofbiz database
	 * 
	 * @param invoiceItemTypeAttrToBeAdded
	 *            the InvoiceItemTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceItemTypeAttr(InvoiceItemTypeAttr invoiceItemTypeAttrToBeAdded) {

		AddInvoiceItemTypeAttr com = new AddInvoiceItemTypeAttr(invoiceItemTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (InvoiceItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeAttrAdded.class,
				event -> sendInvoiceItemTypeAttrChangedMessage(((InvoiceItemTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceItemTypeAttr(HttpServletRequest request) {

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

		InvoiceItemTypeAttr invoiceItemTypeAttrToBeUpdated = new InvoiceItemTypeAttr();

		try {
			invoiceItemTypeAttrToBeUpdated = InvoiceItemTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceItemTypeAttr(invoiceItemTypeAttrToBeUpdated);

	}

	/**
	 * Updates the InvoiceItemTypeAttr with the specific Id
	 * 
	 * @param invoiceItemTypeAttrToBeUpdated the InvoiceItemTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceItemTypeAttr(InvoiceItemTypeAttr invoiceItemTypeAttrToBeUpdated) {

		UpdateInvoiceItemTypeAttr com = new UpdateInvoiceItemTypeAttr(invoiceItemTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeAttrUpdated.class,
				event -> sendInvoiceItemTypeAttrChangedMessage(((InvoiceItemTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceItemTypeAttr from the database
	 * 
	 * @param invoiceItemTypeAttrId:
	 *            the id of the InvoiceItemTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceItemTypeAttrById(@RequestParam(value = "invoiceItemTypeAttrId") String invoiceItemTypeAttrId) {

		DeleteInvoiceItemTypeAttr com = new DeleteInvoiceItemTypeAttr(invoiceItemTypeAttrId);

		int usedTicketId;

		synchronized (InvoiceItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeAttrDeleted.class,
				event -> sendInvoiceItemTypeAttrChangedMessage(((InvoiceItemTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceItemTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceItemTypeAttr/\" plus one of the following: "
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
