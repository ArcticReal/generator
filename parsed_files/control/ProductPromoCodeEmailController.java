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
import com.skytala.eCommerce.command.AddProductPromoCodeEmail;
import com.skytala.eCommerce.command.DeleteProductPromoCodeEmail;
import com.skytala.eCommerce.command.UpdateProductPromoCodeEmail;
import com.skytala.eCommerce.entity.ProductPromoCodeEmail;
import com.skytala.eCommerce.entity.ProductPromoCodeEmailMapper;
import com.skytala.eCommerce.event.ProductPromoCodeEmailAdded;
import com.skytala.eCommerce.event.ProductPromoCodeEmailDeleted;
import com.skytala.eCommerce.event.ProductPromoCodeEmailFound;
import com.skytala.eCommerce.event.ProductPromoCodeEmailUpdated;
import com.skytala.eCommerce.query.FindProductPromoCodeEmailsBy;

@RestController
@RequestMapping("/api/productPromoCodeEmail")
public class ProductPromoCodeEmailController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromoCodeEmail>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoCodeEmailController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromoCodeEmail
	 * @return a List with the ProductPromoCodeEmails
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromoCodeEmail> findProductPromoCodeEmailsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromoCodeEmailsBy query = new FindProductPromoCodeEmailsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoCodeEmailController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodeEmailFound.class,
				event -> sendProductPromoCodeEmailsFoundMessage(((ProductPromoCodeEmailFound) event).getProductPromoCodeEmails(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromoCodeEmailsFoundMessage(List<ProductPromoCodeEmail> productPromoCodeEmails, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromoCodeEmails);
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
	public boolean createProductPromoCodeEmail(HttpServletRequest request) {

		ProductPromoCodeEmail productPromoCodeEmailToBeAdded = new ProductPromoCodeEmail();
		try {
			productPromoCodeEmailToBeAdded = ProductPromoCodeEmailMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromoCodeEmail(productPromoCodeEmailToBeAdded);

	}

	/**
	 * creates a new ProductPromoCodeEmail entry in the ofbiz database
	 * 
	 * @param productPromoCodeEmailToBeAdded
	 *            the ProductPromoCodeEmail thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromoCodeEmail(ProductPromoCodeEmail productPromoCodeEmailToBeAdded) {

		AddProductPromoCodeEmail com = new AddProductPromoCodeEmail(productPromoCodeEmailToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoCodeEmailController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodeEmailAdded.class,
				event -> sendProductPromoCodeEmailChangedMessage(((ProductPromoCodeEmailAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromoCodeEmail(HttpServletRequest request) {

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

		ProductPromoCodeEmail productPromoCodeEmailToBeUpdated = new ProductPromoCodeEmail();

		try {
			productPromoCodeEmailToBeUpdated = ProductPromoCodeEmailMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromoCodeEmail(productPromoCodeEmailToBeUpdated);

	}

	/**
	 * Updates the ProductPromoCodeEmail with the specific Id
	 * 
	 * @param productPromoCodeEmailToBeUpdated the ProductPromoCodeEmail thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromoCodeEmail(ProductPromoCodeEmail productPromoCodeEmailToBeUpdated) {

		UpdateProductPromoCodeEmail com = new UpdateProductPromoCodeEmail(productPromoCodeEmailToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoCodeEmailController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodeEmailUpdated.class,
				event -> sendProductPromoCodeEmailChangedMessage(((ProductPromoCodeEmailUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromoCodeEmail from the database
	 * 
	 * @param productPromoCodeEmailId:
	 *            the id of the ProductPromoCodeEmail thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoCodeEmailById(@RequestParam(value = "productPromoCodeEmailId") String productPromoCodeEmailId) {

		DeleteProductPromoCodeEmail com = new DeleteProductPromoCodeEmail(productPromoCodeEmailId);

		int usedTicketId;

		synchronized (ProductPromoCodeEmailController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodeEmailDeleted.class,
				event -> sendProductPromoCodeEmailChangedMessage(((ProductPromoCodeEmailDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoCodeEmailChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromoCodeEmail/\" plus one of the following: "
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
