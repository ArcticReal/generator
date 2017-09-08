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
import com.skytala.eCommerce.command.AddFileExtension;
import com.skytala.eCommerce.command.DeleteFileExtension;
import com.skytala.eCommerce.command.UpdateFileExtension;
import com.skytala.eCommerce.entity.FileExtension;
import com.skytala.eCommerce.entity.FileExtensionMapper;
import com.skytala.eCommerce.event.FileExtensionAdded;
import com.skytala.eCommerce.event.FileExtensionDeleted;
import com.skytala.eCommerce.event.FileExtensionFound;
import com.skytala.eCommerce.event.FileExtensionUpdated;
import com.skytala.eCommerce.query.FindFileExtensionsBy;

@RestController
@RequestMapping("/api/fileExtension")
public class FileExtensionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FileExtension>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FileExtensionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FileExtension
	 * @return a List with the FileExtensions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FileExtension> findFileExtensionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFileExtensionsBy query = new FindFileExtensionsBy(allRequestParams);

		int usedTicketId;

		synchronized (FileExtensionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FileExtensionFound.class,
				event -> sendFileExtensionsFoundMessage(((FileExtensionFound) event).getFileExtensions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFileExtensionsFoundMessage(List<FileExtension> fileExtensions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fileExtensions);
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
	public boolean createFileExtension(HttpServletRequest request) {

		FileExtension fileExtensionToBeAdded = new FileExtension();
		try {
			fileExtensionToBeAdded = FileExtensionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFileExtension(fileExtensionToBeAdded);

	}

	/**
	 * creates a new FileExtension entry in the ofbiz database
	 * 
	 * @param fileExtensionToBeAdded
	 *            the FileExtension thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFileExtension(FileExtension fileExtensionToBeAdded) {

		AddFileExtension com = new AddFileExtension(fileExtensionToBeAdded);
		int usedTicketId;

		synchronized (FileExtensionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FileExtensionAdded.class,
				event -> sendFileExtensionChangedMessage(((FileExtensionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFileExtension(HttpServletRequest request) {

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

		FileExtension fileExtensionToBeUpdated = new FileExtension();

		try {
			fileExtensionToBeUpdated = FileExtensionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFileExtension(fileExtensionToBeUpdated);

	}

	/**
	 * Updates the FileExtension with the specific Id
	 * 
	 * @param fileExtensionToBeUpdated the FileExtension thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFileExtension(FileExtension fileExtensionToBeUpdated) {

		UpdateFileExtension com = new UpdateFileExtension(fileExtensionToBeUpdated);

		int usedTicketId;

		synchronized (FileExtensionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FileExtensionUpdated.class,
				event -> sendFileExtensionChangedMessage(((FileExtensionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FileExtension from the database
	 * 
	 * @param fileExtensionId:
	 *            the id of the FileExtension thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefileExtensionById(@RequestParam(value = "fileExtensionId") String fileExtensionId) {

		DeleteFileExtension com = new DeleteFileExtension(fileExtensionId);

		int usedTicketId;

		synchronized (FileExtensionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FileExtensionDeleted.class,
				event -> sendFileExtensionChangedMessage(((FileExtensionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFileExtensionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fileExtension/\" plus one of the following: "
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
