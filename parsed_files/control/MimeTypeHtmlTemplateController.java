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
import com.skytala.eCommerce.command.AddMimeTypeHtmlTemplate;
import com.skytala.eCommerce.command.DeleteMimeTypeHtmlTemplate;
import com.skytala.eCommerce.command.UpdateMimeTypeHtmlTemplate;
import com.skytala.eCommerce.entity.MimeTypeHtmlTemplate;
import com.skytala.eCommerce.entity.MimeTypeHtmlTemplateMapper;
import com.skytala.eCommerce.event.MimeTypeHtmlTemplateAdded;
import com.skytala.eCommerce.event.MimeTypeHtmlTemplateDeleted;
import com.skytala.eCommerce.event.MimeTypeHtmlTemplateFound;
import com.skytala.eCommerce.event.MimeTypeHtmlTemplateUpdated;
import com.skytala.eCommerce.query.FindMimeTypeHtmlTemplatesBy;

@RestController
@RequestMapping("/api/mimeTypeHtmlTemplate")
public class MimeTypeHtmlTemplateController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<MimeTypeHtmlTemplate>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public MimeTypeHtmlTemplateController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a MimeTypeHtmlTemplate
	 * @return a List with the MimeTypeHtmlTemplates
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<MimeTypeHtmlTemplate> findMimeTypeHtmlTemplatesBy(@RequestParam Map<String, String> allRequestParams) {

		FindMimeTypeHtmlTemplatesBy query = new FindMimeTypeHtmlTemplatesBy(allRequestParams);

		int usedTicketId;

		synchronized (MimeTypeHtmlTemplateController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MimeTypeHtmlTemplateFound.class,
				event -> sendMimeTypeHtmlTemplatesFoundMessage(((MimeTypeHtmlTemplateFound) event).getMimeTypeHtmlTemplates(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendMimeTypeHtmlTemplatesFoundMessage(List<MimeTypeHtmlTemplate> mimeTypeHtmlTemplates, int usedTicketId) {
		queryReturnVal.put(usedTicketId, mimeTypeHtmlTemplates);
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
	public boolean createMimeTypeHtmlTemplate(HttpServletRequest request) {

		MimeTypeHtmlTemplate mimeTypeHtmlTemplateToBeAdded = new MimeTypeHtmlTemplate();
		try {
			mimeTypeHtmlTemplateToBeAdded = MimeTypeHtmlTemplateMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createMimeTypeHtmlTemplate(mimeTypeHtmlTemplateToBeAdded);

	}

	/**
	 * creates a new MimeTypeHtmlTemplate entry in the ofbiz database
	 * 
	 * @param mimeTypeHtmlTemplateToBeAdded
	 *            the MimeTypeHtmlTemplate thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createMimeTypeHtmlTemplate(MimeTypeHtmlTemplate mimeTypeHtmlTemplateToBeAdded) {

		AddMimeTypeHtmlTemplate com = new AddMimeTypeHtmlTemplate(mimeTypeHtmlTemplateToBeAdded);
		int usedTicketId;

		synchronized (MimeTypeHtmlTemplateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MimeTypeHtmlTemplateAdded.class,
				event -> sendMimeTypeHtmlTemplateChangedMessage(((MimeTypeHtmlTemplateAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateMimeTypeHtmlTemplate(HttpServletRequest request) {

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

		MimeTypeHtmlTemplate mimeTypeHtmlTemplateToBeUpdated = new MimeTypeHtmlTemplate();

		try {
			mimeTypeHtmlTemplateToBeUpdated = MimeTypeHtmlTemplateMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateMimeTypeHtmlTemplate(mimeTypeHtmlTemplateToBeUpdated);

	}

	/**
	 * Updates the MimeTypeHtmlTemplate with the specific Id
	 * 
	 * @param mimeTypeHtmlTemplateToBeUpdated the MimeTypeHtmlTemplate thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateMimeTypeHtmlTemplate(MimeTypeHtmlTemplate mimeTypeHtmlTemplateToBeUpdated) {

		UpdateMimeTypeHtmlTemplate com = new UpdateMimeTypeHtmlTemplate(mimeTypeHtmlTemplateToBeUpdated);

		int usedTicketId;

		synchronized (MimeTypeHtmlTemplateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MimeTypeHtmlTemplateUpdated.class,
				event -> sendMimeTypeHtmlTemplateChangedMessage(((MimeTypeHtmlTemplateUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a MimeTypeHtmlTemplate from the database
	 * 
	 * @param mimeTypeHtmlTemplateId:
	 *            the id of the MimeTypeHtmlTemplate thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletemimeTypeHtmlTemplateById(@RequestParam(value = "mimeTypeHtmlTemplateId") String mimeTypeHtmlTemplateId) {

		DeleteMimeTypeHtmlTemplate com = new DeleteMimeTypeHtmlTemplate(mimeTypeHtmlTemplateId);

		int usedTicketId;

		synchronized (MimeTypeHtmlTemplateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(MimeTypeHtmlTemplateDeleted.class,
				event -> sendMimeTypeHtmlTemplateChangedMessage(((MimeTypeHtmlTemplateDeleted) event).isSuccess(), usedTicketId));

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

	public void sendMimeTypeHtmlTemplateChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/mimeTypeHtmlTemplate/\" plus one of the following: "
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
