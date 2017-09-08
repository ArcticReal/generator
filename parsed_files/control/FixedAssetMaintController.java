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
import com.skytala.eCommerce.command.AddFixedAssetMaint;
import com.skytala.eCommerce.command.DeleteFixedAssetMaint;
import com.skytala.eCommerce.command.UpdateFixedAssetMaint;
import com.skytala.eCommerce.entity.FixedAssetMaint;
import com.skytala.eCommerce.entity.FixedAssetMaintMapper;
import com.skytala.eCommerce.event.FixedAssetMaintAdded;
import com.skytala.eCommerce.event.FixedAssetMaintDeleted;
import com.skytala.eCommerce.event.FixedAssetMaintFound;
import com.skytala.eCommerce.event.FixedAssetMaintUpdated;
import com.skytala.eCommerce.query.FindFixedAssetMaintsBy;

@RestController
@RequestMapping("/api/fixedAssetMaint")
public class FixedAssetMaintController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetMaint>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetMaintController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetMaint
	 * @return a List with the FixedAssetMaints
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetMaint> findFixedAssetMaintsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetMaintsBy query = new FindFixedAssetMaintsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetMaintController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintFound.class,
				event -> sendFixedAssetMaintsFoundMessage(((FixedAssetMaintFound) event).getFixedAssetMaints(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetMaintsFoundMessage(List<FixedAssetMaint> fixedAssetMaints, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetMaints);
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
	public boolean createFixedAssetMaint(HttpServletRequest request) {

		FixedAssetMaint fixedAssetMaintToBeAdded = new FixedAssetMaint();
		try {
			fixedAssetMaintToBeAdded = FixedAssetMaintMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetMaint(fixedAssetMaintToBeAdded);

	}

	/**
	 * creates a new FixedAssetMaint entry in the ofbiz database
	 * 
	 * @param fixedAssetMaintToBeAdded
	 *            the FixedAssetMaint thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetMaint(FixedAssetMaint fixedAssetMaintToBeAdded) {

		AddFixedAssetMaint com = new AddFixedAssetMaint(fixedAssetMaintToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetMaintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintAdded.class,
				event -> sendFixedAssetMaintChangedMessage(((FixedAssetMaintAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetMaint(HttpServletRequest request) {

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

		FixedAssetMaint fixedAssetMaintToBeUpdated = new FixedAssetMaint();

		try {
			fixedAssetMaintToBeUpdated = FixedAssetMaintMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetMaint(fixedAssetMaintToBeUpdated);

	}

	/**
	 * Updates the FixedAssetMaint with the specific Id
	 * 
	 * @param fixedAssetMaintToBeUpdated the FixedAssetMaint thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetMaint(FixedAssetMaint fixedAssetMaintToBeUpdated) {

		UpdateFixedAssetMaint com = new UpdateFixedAssetMaint(fixedAssetMaintToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetMaintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintUpdated.class,
				event -> sendFixedAssetMaintChangedMessage(((FixedAssetMaintUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetMaint from the database
	 * 
	 * @param fixedAssetMaintId:
	 *            the id of the FixedAssetMaint thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetMaintById(@RequestParam(value = "fixedAssetMaintId") String fixedAssetMaintId) {

		DeleteFixedAssetMaint com = new DeleteFixedAssetMaint(fixedAssetMaintId);

		int usedTicketId;

		synchronized (FixedAssetMaintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMaintDeleted.class,
				event -> sendFixedAssetMaintChangedMessage(((FixedAssetMaintDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetMaintChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetMaint/\" plus one of the following: "
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
