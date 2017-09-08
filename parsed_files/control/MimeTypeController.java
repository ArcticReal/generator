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
import com.skytala.eCommerce.command.AddMimeType;
import com.skytala.eCommerce.command.DeleteMimeType;
import com.skytala.eCommerce.command.UpdateMimeType;
import com.skytala.eCommerce.entity.MimeType;
import com.skytala.eCommerce.entity.MimeTypeMapper;
import com.skytala.eCommerce.event.MimeTypeAdded;
import com.skytala.eCommerce.event.MimeTypeDeleted;
import com.skytala.eCommerce.event.MimeTypeFound;
import com.skytala.eCommerce.event.MimeTypeUpdated;
import com.skytala.eCommerce.query.FindMimeTypesBy;

@RestController
@RequestMapping("/api/mimeType")
public class MimeTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<MimeType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public MimeTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a MimeType
	 * @return a List with the MimeTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<MimeType> findMimeTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindMimeTypesBy query = new FindMimeTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (MimeTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MimeTypeFound.class,
				event -> sendMimeTypesFoundMessage(((MimeTypeFound) event).getMimeTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendMimeTypesFoundMessage(List<MimeType> mimeTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, mimeTypes);
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
	public boolean createMimeType(HttpServletRequest request) {

		MimeType mimeTypeToBeAdded = new MimeType();
		try {
			mimeTypeToBeAdded = MimeTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createMimeType(mimeTypeToBeAdded);

	}

	/**
	 * creates a new MimeType entry in the ofbiz database
	 * 
	 * @param mimeTypeToBeAdded
	 *            the MimeType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createMimeType(MimeType mimeTypeToBeAdded) {

		AddMimeType com = new AddMimeType(mimeTypeToBeAdded);
		int usedTicketId;

		synchronized (MimeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MimeTypeAdded.class,
				event -> sendMimeTypeChangedMessage(((MimeTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateMimeType(HttpServletRequest request) {

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

		MimeType mimeTypeToBeUpdated = new MimeType();

		try {
			mimeTypeToBeUpdated = MimeTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateMimeType(mimeTypeToBeUpdated);

	}

	/**
	 * Updates the MimeType with the specific Id
	 * 
	 * @param mimeTypeToBeUpdated the MimeType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateMimeType(MimeType mimeTypeToBeUpdated) {

		UpdateMimeType com = new UpdateMimeType(mimeTypeToBeUpdated);

		int usedTicketId;

		synchronized (MimeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MimeTypeUpdated.class,
				event -> sendMimeTypeChangedMessage(((MimeTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a MimeType from the database
	 * 
	 * @param mimeTypeId:
	 *            the id of the MimeType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletemimeTypeById(@RequestParam(value = "mimeTypeId") String mimeTypeId) {

		DeleteMimeType com = new DeleteMimeType(mimeTypeId);

		int usedTicketId;

		synchronized (MimeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MimeTypeDeleted.class,
				event -> sendMimeTypeChangedMessage(((MimeTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendMimeTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/mimeType/\" plus one of the following: "
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
