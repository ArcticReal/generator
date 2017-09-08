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
import com.skytala.eCommerce.command.AddZipSalesRuleLookup;
import com.skytala.eCommerce.command.DeleteZipSalesRuleLookup;
import com.skytala.eCommerce.command.UpdateZipSalesRuleLookup;
import com.skytala.eCommerce.entity.ZipSalesRuleLookup;
import com.skytala.eCommerce.entity.ZipSalesRuleLookupMapper;
import com.skytala.eCommerce.event.ZipSalesRuleLookupAdded;
import com.skytala.eCommerce.event.ZipSalesRuleLookupDeleted;
import com.skytala.eCommerce.event.ZipSalesRuleLookupFound;
import com.skytala.eCommerce.event.ZipSalesRuleLookupUpdated;
import com.skytala.eCommerce.query.FindZipSalesRuleLookupsBy;

@RestController
@RequestMapping("/api/zipSalesRuleLookup")
public class ZipSalesRuleLookupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ZipSalesRuleLookup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ZipSalesRuleLookupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ZipSalesRuleLookup
	 * @return a List with the ZipSalesRuleLookups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ZipSalesRuleLookup> findZipSalesRuleLookupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindZipSalesRuleLookupsBy query = new FindZipSalesRuleLookupsBy(allRequestParams);

		int usedTicketId;

		synchronized (ZipSalesRuleLookupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ZipSalesRuleLookupFound.class,
				event -> sendZipSalesRuleLookupsFoundMessage(((ZipSalesRuleLookupFound) event).getZipSalesRuleLookups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendZipSalesRuleLookupsFoundMessage(List<ZipSalesRuleLookup> zipSalesRuleLookups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, zipSalesRuleLookups);
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
	public boolean createZipSalesRuleLookup(HttpServletRequest request) {

		ZipSalesRuleLookup zipSalesRuleLookupToBeAdded = new ZipSalesRuleLookup();
		try {
			zipSalesRuleLookupToBeAdded = ZipSalesRuleLookupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createZipSalesRuleLookup(zipSalesRuleLookupToBeAdded);

	}

	/**
	 * creates a new ZipSalesRuleLookup entry in the ofbiz database
	 * 
	 * @param zipSalesRuleLookupToBeAdded
	 *            the ZipSalesRuleLookup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createZipSalesRuleLookup(ZipSalesRuleLookup zipSalesRuleLookupToBeAdded) {

		AddZipSalesRuleLookup com = new AddZipSalesRuleLookup(zipSalesRuleLookupToBeAdded);
		int usedTicketId;

		synchronized (ZipSalesRuleLookupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ZipSalesRuleLookupAdded.class,
				event -> sendZipSalesRuleLookupChangedMessage(((ZipSalesRuleLookupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateZipSalesRuleLookup(HttpServletRequest request) {

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

		ZipSalesRuleLookup zipSalesRuleLookupToBeUpdated = new ZipSalesRuleLookup();

		try {
			zipSalesRuleLookupToBeUpdated = ZipSalesRuleLookupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateZipSalesRuleLookup(zipSalesRuleLookupToBeUpdated);

	}

	/**
	 * Updates the ZipSalesRuleLookup with the specific Id
	 * 
	 * @param zipSalesRuleLookupToBeUpdated the ZipSalesRuleLookup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateZipSalesRuleLookup(ZipSalesRuleLookup zipSalesRuleLookupToBeUpdated) {

		UpdateZipSalesRuleLookup com = new UpdateZipSalesRuleLookup(zipSalesRuleLookupToBeUpdated);

		int usedTicketId;

		synchronized (ZipSalesRuleLookupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ZipSalesRuleLookupUpdated.class,
				event -> sendZipSalesRuleLookupChangedMessage(((ZipSalesRuleLookupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ZipSalesRuleLookup from the database
	 * 
	 * @param zipSalesRuleLookupId:
	 *            the id of the ZipSalesRuleLookup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletezipSalesRuleLookupById(@RequestParam(value = "zipSalesRuleLookupId") String zipSalesRuleLookupId) {

		DeleteZipSalesRuleLookup com = new DeleteZipSalesRuleLookup(zipSalesRuleLookupId);

		int usedTicketId;

		synchronized (ZipSalesRuleLookupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ZipSalesRuleLookupDeleted.class,
				event -> sendZipSalesRuleLookupChangedMessage(((ZipSalesRuleLookupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendZipSalesRuleLookupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/zipSalesRuleLookup/\" plus one of the following: "
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
