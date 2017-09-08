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
import com.skytala.eCommerce.command.AddZipSalesTaxLookup;
import com.skytala.eCommerce.command.DeleteZipSalesTaxLookup;
import com.skytala.eCommerce.command.UpdateZipSalesTaxLookup;
import com.skytala.eCommerce.entity.ZipSalesTaxLookup;
import com.skytala.eCommerce.entity.ZipSalesTaxLookupMapper;
import com.skytala.eCommerce.event.ZipSalesTaxLookupAdded;
import com.skytala.eCommerce.event.ZipSalesTaxLookupDeleted;
import com.skytala.eCommerce.event.ZipSalesTaxLookupFound;
import com.skytala.eCommerce.event.ZipSalesTaxLookupUpdated;
import com.skytala.eCommerce.query.FindZipSalesTaxLookupsBy;

@RestController
@RequestMapping("/api/zipSalesTaxLookup")
public class ZipSalesTaxLookupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ZipSalesTaxLookup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ZipSalesTaxLookupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ZipSalesTaxLookup
	 * @return a List with the ZipSalesTaxLookups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ZipSalesTaxLookup> findZipSalesTaxLookupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindZipSalesTaxLookupsBy query = new FindZipSalesTaxLookupsBy(allRequestParams);

		int usedTicketId;

		synchronized (ZipSalesTaxLookupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ZipSalesTaxLookupFound.class,
				event -> sendZipSalesTaxLookupsFoundMessage(((ZipSalesTaxLookupFound) event).getZipSalesTaxLookups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendZipSalesTaxLookupsFoundMessage(List<ZipSalesTaxLookup> zipSalesTaxLookups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, zipSalesTaxLookups);
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
	public boolean createZipSalesTaxLookup(HttpServletRequest request) {

		ZipSalesTaxLookup zipSalesTaxLookupToBeAdded = new ZipSalesTaxLookup();
		try {
			zipSalesTaxLookupToBeAdded = ZipSalesTaxLookupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createZipSalesTaxLookup(zipSalesTaxLookupToBeAdded);

	}

	/**
	 * creates a new ZipSalesTaxLookup entry in the ofbiz database
	 * 
	 * @param zipSalesTaxLookupToBeAdded
	 *            the ZipSalesTaxLookup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createZipSalesTaxLookup(ZipSalesTaxLookup zipSalesTaxLookupToBeAdded) {

		AddZipSalesTaxLookup com = new AddZipSalesTaxLookup(zipSalesTaxLookupToBeAdded);
		int usedTicketId;

		synchronized (ZipSalesTaxLookupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ZipSalesTaxLookupAdded.class,
				event -> sendZipSalesTaxLookupChangedMessage(((ZipSalesTaxLookupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateZipSalesTaxLookup(HttpServletRequest request) {

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

		ZipSalesTaxLookup zipSalesTaxLookupToBeUpdated = new ZipSalesTaxLookup();

		try {
			zipSalesTaxLookupToBeUpdated = ZipSalesTaxLookupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateZipSalesTaxLookup(zipSalesTaxLookupToBeUpdated);

	}

	/**
	 * Updates the ZipSalesTaxLookup with the specific Id
	 * 
	 * @param zipSalesTaxLookupToBeUpdated the ZipSalesTaxLookup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateZipSalesTaxLookup(ZipSalesTaxLookup zipSalesTaxLookupToBeUpdated) {

		UpdateZipSalesTaxLookup com = new UpdateZipSalesTaxLookup(zipSalesTaxLookupToBeUpdated);

		int usedTicketId;

		synchronized (ZipSalesTaxLookupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ZipSalesTaxLookupUpdated.class,
				event -> sendZipSalesTaxLookupChangedMessage(((ZipSalesTaxLookupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ZipSalesTaxLookup from the database
	 * 
	 * @param zipSalesTaxLookupId:
	 *            the id of the ZipSalesTaxLookup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletezipSalesTaxLookupById(@RequestParam(value = "zipSalesTaxLookupId") String zipSalesTaxLookupId) {

		DeleteZipSalesTaxLookup com = new DeleteZipSalesTaxLookup(zipSalesTaxLookupId);

		int usedTicketId;

		synchronized (ZipSalesTaxLookupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ZipSalesTaxLookupDeleted.class,
				event -> sendZipSalesTaxLookupChangedMessage(((ZipSalesTaxLookupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendZipSalesTaxLookupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/zipSalesTaxLookup/\" plus one of the following: "
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
