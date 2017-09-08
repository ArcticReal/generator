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
import com.skytala.eCommerce.command.AddProductPromoContent;
import com.skytala.eCommerce.command.DeleteProductPromoContent;
import com.skytala.eCommerce.command.UpdateProductPromoContent;
import com.skytala.eCommerce.entity.ProductPromoContent;
import com.skytala.eCommerce.entity.ProductPromoContentMapper;
import com.skytala.eCommerce.event.ProductPromoContentAdded;
import com.skytala.eCommerce.event.ProductPromoContentDeleted;
import com.skytala.eCommerce.event.ProductPromoContentFound;
import com.skytala.eCommerce.event.ProductPromoContentUpdated;
import com.skytala.eCommerce.query.FindProductPromoContentsBy;

@RestController
@RequestMapping("/api/productPromoContent")
public class ProductPromoContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromoContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromoContent
	 * @return a List with the ProductPromoContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromoContent> findProductPromoContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromoContentsBy query = new FindProductPromoContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoContentFound.class,
				event -> sendProductPromoContentsFoundMessage(((ProductPromoContentFound) event).getProductPromoContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromoContentsFoundMessage(List<ProductPromoContent> productPromoContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromoContents);
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
	public boolean createProductPromoContent(HttpServletRequest request) {

		ProductPromoContent productPromoContentToBeAdded = new ProductPromoContent();
		try {
			productPromoContentToBeAdded = ProductPromoContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromoContent(productPromoContentToBeAdded);

	}

	/**
	 * creates a new ProductPromoContent entry in the ofbiz database
	 * 
	 * @param productPromoContentToBeAdded
	 *            the ProductPromoContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromoContent(ProductPromoContent productPromoContentToBeAdded) {

		AddProductPromoContent com = new AddProductPromoContent(productPromoContentToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoContentAdded.class,
				event -> sendProductPromoContentChangedMessage(((ProductPromoContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromoContent(HttpServletRequest request) {

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

		ProductPromoContent productPromoContentToBeUpdated = new ProductPromoContent();

		try {
			productPromoContentToBeUpdated = ProductPromoContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromoContent(productPromoContentToBeUpdated);

	}

	/**
	 * Updates the ProductPromoContent with the specific Id
	 * 
	 * @param productPromoContentToBeUpdated the ProductPromoContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromoContent(ProductPromoContent productPromoContentToBeUpdated) {

		UpdateProductPromoContent com = new UpdateProductPromoContent(productPromoContentToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoContentUpdated.class,
				event -> sendProductPromoContentChangedMessage(((ProductPromoContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromoContent from the database
	 * 
	 * @param productPromoContentId:
	 *            the id of the ProductPromoContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoContentById(@RequestParam(value = "productPromoContentId") String productPromoContentId) {

		DeleteProductPromoContent com = new DeleteProductPromoContent(productPromoContentId);

		int usedTicketId;

		synchronized (ProductPromoContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoContentDeleted.class,
				event -> sendProductPromoContentChangedMessage(((ProductPromoContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromoContent/\" plus one of the following: "
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
