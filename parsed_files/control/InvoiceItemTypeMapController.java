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
import com.skytala.eCommerce.command.AddInvoiceItemTypeMap;
import com.skytala.eCommerce.command.DeleteInvoiceItemTypeMap;
import com.skytala.eCommerce.command.UpdateInvoiceItemTypeMap;
import com.skytala.eCommerce.entity.InvoiceItemTypeMap;
import com.skytala.eCommerce.entity.InvoiceItemTypeMapMapper;
import com.skytala.eCommerce.event.InvoiceItemTypeMapAdded;
import com.skytala.eCommerce.event.InvoiceItemTypeMapDeleted;
import com.skytala.eCommerce.event.InvoiceItemTypeMapFound;
import com.skytala.eCommerce.event.InvoiceItemTypeMapUpdated;
import com.skytala.eCommerce.query.FindInvoiceItemTypeMapsBy;

@RestController
@RequestMapping("/api/invoiceItemTypeMap")
public class InvoiceItemTypeMapController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InvoiceItemTypeMap>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InvoiceItemTypeMapController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InvoiceItemTypeMap
	 * @return a List with the InvoiceItemTypeMaps
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InvoiceItemTypeMap> findInvoiceItemTypeMapsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInvoiceItemTypeMapsBy query = new FindInvoiceItemTypeMapsBy(allRequestParams);

		int usedTicketId;

		synchronized (InvoiceItemTypeMapController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeMapFound.class,
				event -> sendInvoiceItemTypeMapsFoundMessage(((InvoiceItemTypeMapFound) event).getInvoiceItemTypeMaps(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInvoiceItemTypeMapsFoundMessage(List<InvoiceItemTypeMap> invoiceItemTypeMaps, int usedTicketId) {
		queryReturnVal.put(usedTicketId, invoiceItemTypeMaps);
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
	public boolean createInvoiceItemTypeMap(HttpServletRequest request) {

		InvoiceItemTypeMap invoiceItemTypeMapToBeAdded = new InvoiceItemTypeMap();
		try {
			invoiceItemTypeMapToBeAdded = InvoiceItemTypeMapMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInvoiceItemTypeMap(invoiceItemTypeMapToBeAdded);

	}

	/**
	 * creates a new InvoiceItemTypeMap entry in the ofbiz database
	 * 
	 * @param invoiceItemTypeMapToBeAdded
	 *            the InvoiceItemTypeMap thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInvoiceItemTypeMap(InvoiceItemTypeMap invoiceItemTypeMapToBeAdded) {

		AddInvoiceItemTypeMap com = new AddInvoiceItemTypeMap(invoiceItemTypeMapToBeAdded);
		int usedTicketId;

		synchronized (InvoiceItemTypeMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeMapAdded.class,
				event -> sendInvoiceItemTypeMapChangedMessage(((InvoiceItemTypeMapAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInvoiceItemTypeMap(HttpServletRequest request) {

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

		InvoiceItemTypeMap invoiceItemTypeMapToBeUpdated = new InvoiceItemTypeMap();

		try {
			invoiceItemTypeMapToBeUpdated = InvoiceItemTypeMapMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInvoiceItemTypeMap(invoiceItemTypeMapToBeUpdated);

	}

	/**
	 * Updates the InvoiceItemTypeMap with the specific Id
	 * 
	 * @param invoiceItemTypeMapToBeUpdated the InvoiceItemTypeMap thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInvoiceItemTypeMap(InvoiceItemTypeMap invoiceItemTypeMapToBeUpdated) {

		UpdateInvoiceItemTypeMap com = new UpdateInvoiceItemTypeMap(invoiceItemTypeMapToBeUpdated);

		int usedTicketId;

		synchronized (InvoiceItemTypeMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeMapUpdated.class,
				event -> sendInvoiceItemTypeMapChangedMessage(((InvoiceItemTypeMapUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InvoiceItemTypeMap from the database
	 * 
	 * @param invoiceItemTypeMapId:
	 *            the id of the InvoiceItemTypeMap thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinvoiceItemTypeMapById(@RequestParam(value = "invoiceItemTypeMapId") String invoiceItemTypeMapId) {

		DeleteInvoiceItemTypeMap com = new DeleteInvoiceItemTypeMap(invoiceItemTypeMapId);

		int usedTicketId;

		synchronized (InvoiceItemTypeMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InvoiceItemTypeMapDeleted.class,
				event -> sendInvoiceItemTypeMapChangedMessage(((InvoiceItemTypeMapDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInvoiceItemTypeMapChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/invoiceItemTypeMap/\" plus one of the following: "
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
