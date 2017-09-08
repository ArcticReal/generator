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
import com.skytala.eCommerce.command.AddFixedAssetMeter;
import com.skytala.eCommerce.command.DeleteFixedAssetMeter;
import com.skytala.eCommerce.command.UpdateFixedAssetMeter;
import com.skytala.eCommerce.entity.FixedAssetMeter;
import com.skytala.eCommerce.entity.FixedAssetMeterMapper;
import com.skytala.eCommerce.event.FixedAssetMeterAdded;
import com.skytala.eCommerce.event.FixedAssetMeterDeleted;
import com.skytala.eCommerce.event.FixedAssetMeterFound;
import com.skytala.eCommerce.event.FixedAssetMeterUpdated;
import com.skytala.eCommerce.query.FindFixedAssetMetersBy;

@RestController
@RequestMapping("/api/fixedAssetMeter")
public class FixedAssetMeterController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetMeter>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetMeterController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetMeter
	 * @return a List with the FixedAssetMeters
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetMeter> findFixedAssetMetersBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetMetersBy query = new FindFixedAssetMetersBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetMeterController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMeterFound.class,
				event -> sendFixedAssetMetersFoundMessage(((FixedAssetMeterFound) event).getFixedAssetMeters(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetMetersFoundMessage(List<FixedAssetMeter> fixedAssetMeters, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetMeters);
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
	public boolean createFixedAssetMeter(HttpServletRequest request) {

		FixedAssetMeter fixedAssetMeterToBeAdded = new FixedAssetMeter();
		try {
			fixedAssetMeterToBeAdded = FixedAssetMeterMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetMeter(fixedAssetMeterToBeAdded);

	}

	/**
	 * creates a new FixedAssetMeter entry in the ofbiz database
	 * 
	 * @param fixedAssetMeterToBeAdded
	 *            the FixedAssetMeter thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetMeter(FixedAssetMeter fixedAssetMeterToBeAdded) {

		AddFixedAssetMeter com = new AddFixedAssetMeter(fixedAssetMeterToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetMeterController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMeterAdded.class,
				event -> sendFixedAssetMeterChangedMessage(((FixedAssetMeterAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetMeter(HttpServletRequest request) {

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

		FixedAssetMeter fixedAssetMeterToBeUpdated = new FixedAssetMeter();

		try {
			fixedAssetMeterToBeUpdated = FixedAssetMeterMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetMeter(fixedAssetMeterToBeUpdated);

	}

	/**
	 * Updates the FixedAssetMeter with the specific Id
	 * 
	 * @param fixedAssetMeterToBeUpdated the FixedAssetMeter thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetMeter(FixedAssetMeter fixedAssetMeterToBeUpdated) {

		UpdateFixedAssetMeter com = new UpdateFixedAssetMeter(fixedAssetMeterToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetMeterController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMeterUpdated.class,
				event -> sendFixedAssetMeterChangedMessage(((FixedAssetMeterUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetMeter from the database
	 * 
	 * @param fixedAssetMeterId:
	 *            the id of the FixedAssetMeter thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetMeterById(@RequestParam(value = "fixedAssetMeterId") String fixedAssetMeterId) {

		DeleteFixedAssetMeter com = new DeleteFixedAssetMeter(fixedAssetMeterId);

		int usedTicketId;

		synchronized (FixedAssetMeterController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetMeterDeleted.class,
				event -> sendFixedAssetMeterChangedMessage(((FixedAssetMeterDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetMeterChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetMeter/\" plus one of the following: "
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
