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
import com.skytala.eCommerce.command.AddSegmentGroupGeo;
import com.skytala.eCommerce.command.DeleteSegmentGroupGeo;
import com.skytala.eCommerce.command.UpdateSegmentGroupGeo;
import com.skytala.eCommerce.entity.SegmentGroupGeo;
import com.skytala.eCommerce.entity.SegmentGroupGeoMapper;
import com.skytala.eCommerce.event.SegmentGroupGeoAdded;
import com.skytala.eCommerce.event.SegmentGroupGeoDeleted;
import com.skytala.eCommerce.event.SegmentGroupGeoFound;
import com.skytala.eCommerce.event.SegmentGroupGeoUpdated;
import com.skytala.eCommerce.query.FindSegmentGroupGeosBy;

@RestController
@RequestMapping("/api/segmentGroupGeo")
public class SegmentGroupGeoController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SegmentGroupGeo>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SegmentGroupGeoController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SegmentGroupGeo
	 * @return a List with the SegmentGroupGeos
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SegmentGroupGeo> findSegmentGroupGeosBy(@RequestParam Map<String, String> allRequestParams) {

		FindSegmentGroupGeosBy query = new FindSegmentGroupGeosBy(allRequestParams);

		int usedTicketId;

		synchronized (SegmentGroupGeoController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupGeoFound.class,
				event -> sendSegmentGroupGeosFoundMessage(((SegmentGroupGeoFound) event).getSegmentGroupGeos(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSegmentGroupGeosFoundMessage(List<SegmentGroupGeo> segmentGroupGeos, int usedTicketId) {
		queryReturnVal.put(usedTicketId, segmentGroupGeos);
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
	public boolean createSegmentGroupGeo(HttpServletRequest request) {

		SegmentGroupGeo segmentGroupGeoToBeAdded = new SegmentGroupGeo();
		try {
			segmentGroupGeoToBeAdded = SegmentGroupGeoMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSegmentGroupGeo(segmentGroupGeoToBeAdded);

	}

	/**
	 * creates a new SegmentGroupGeo entry in the ofbiz database
	 * 
	 * @param segmentGroupGeoToBeAdded
	 *            the SegmentGroupGeo thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSegmentGroupGeo(SegmentGroupGeo segmentGroupGeoToBeAdded) {

		AddSegmentGroupGeo com = new AddSegmentGroupGeo(segmentGroupGeoToBeAdded);
		int usedTicketId;

		synchronized (SegmentGroupGeoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupGeoAdded.class,
				event -> sendSegmentGroupGeoChangedMessage(((SegmentGroupGeoAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSegmentGroupGeo(HttpServletRequest request) {

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

		SegmentGroupGeo segmentGroupGeoToBeUpdated = new SegmentGroupGeo();

		try {
			segmentGroupGeoToBeUpdated = SegmentGroupGeoMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSegmentGroupGeo(segmentGroupGeoToBeUpdated);

	}

	/**
	 * Updates the SegmentGroupGeo with the specific Id
	 * 
	 * @param segmentGroupGeoToBeUpdated the SegmentGroupGeo thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSegmentGroupGeo(SegmentGroupGeo segmentGroupGeoToBeUpdated) {

		UpdateSegmentGroupGeo com = new UpdateSegmentGroupGeo(segmentGroupGeoToBeUpdated);

		int usedTicketId;

		synchronized (SegmentGroupGeoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupGeoUpdated.class,
				event -> sendSegmentGroupGeoChangedMessage(((SegmentGroupGeoUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SegmentGroupGeo from the database
	 * 
	 * @param segmentGroupGeoId:
	 *            the id of the SegmentGroupGeo thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesegmentGroupGeoById(@RequestParam(value = "segmentGroupGeoId") String segmentGroupGeoId) {

		DeleteSegmentGroupGeo com = new DeleteSegmentGroupGeo(segmentGroupGeoId);

		int usedTicketId;

		synchronized (SegmentGroupGeoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupGeoDeleted.class,
				event -> sendSegmentGroupGeoChangedMessage(((SegmentGroupGeoDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSegmentGroupGeoChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/segmentGroupGeo/\" plus one of the following: "
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
