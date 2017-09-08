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
import com.skytala.eCommerce.command.AddInvoiceTerm;
import com.skytala.eCommerce.command.DeleteInvoiceTerm;
import com.skytala.eCommerce.command.UpdateInvoiceTerm;
import com.skytala.eCommerce.entity.InvoiceTerm;
import com.skytala.eCommerce.entity.InvoiceTermMapper;
import com.skytala.eCommerce.event.InvoiceTermAdded;
import com.skytala.eCommerce.event.InvoiceTermDeleted;
import com.skytala.eCommerce.event.InvoiceTermFound;
import com.skytala.eCommerce.event.InvoiceTermUpdated;
import com.skytala.eCommerce.query.FindInvoiceTermsBy;

@RestController
@RequestMapping("/api/invoiceTerm")
public class InvoiceTermController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceTerm>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceTermController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceTerm
	 * @return a List with the InvoiceTerms
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceTerm> findInvoiceTermsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceTermsBy query = new FindInvoiceTermsBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceTermController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTermFound.class,
				event -> sendInvoiceTermsFoundMessage(((InvoiceTermFound) event).getInvoiceTerms(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceTermsFoundMessage(List<InvoiceTerm> invoiceTerms, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceTerms);
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
	public boolean createInvoiceTerm(HttpServletRequest request) {

		InvoiceTerm invoiceTermToBeAdded = new InvoiceTerm();
		try {
			invoiceTermToBeAdded = InvoiceTermMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceTerm(invoiceTermToBeAdded);

	}

	/**
	 * creates a new InvoiceTerm entry in the ofbiz database
	 * 
	 * @param invoiceTermToBeAdded
	 *            the InvoiceTerm thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceTerm(InvoiceTerm invoiceTermToBeAdded) {

		AddInvoiceTerm com = new AddInvoiceTerm(invoiceTermToBeAdded);
		int usedTicketId;

		synchronized (InvoiceTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTermAdded.class,
				event -> sendInvoiceTermChangedMessage(((InvoiceTermAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceTerm(HttpServletRequest request) {

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

		InvoiceTerm invoiceTermToBeUpdated = new InvoiceTerm();

		try {
			invoiceTermToBeUpdated = InvoiceTermMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceTerm(invoiceTermToBeUpdated);

	}

	/**
	 * Updates the InvoiceTerm with the specific Id
	 * 
	 * @param invoiceTermToBeUpdated the InvoiceTerm thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceTerm(InvoiceTerm invoiceTermToBeUpdated) {

		UpdateInvoiceTerm com = new UpdateInvoiceTerm(invoiceTermToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTermUpdated.class,
				event -> sendInvoiceTermChangedMessage(((InvoiceTermUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceTerm from the database
	 * 
	 * @param invoiceTermId:
	 *            the id of the InvoiceTerm thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceTermById(@RequestParam(value = "invoiceTermId") String invoiceTermId) {

		DeleteInvoiceTerm com = new DeleteInvoiceTerm(invoiceTermId);

		int usedTicketId;

		synchronized (InvoiceTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceTermDeleted.class,
				event -> sendInvoiceTermChangedMessage(((InvoiceTermDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceTermChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceTerm/\" plus one of the following: "
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
