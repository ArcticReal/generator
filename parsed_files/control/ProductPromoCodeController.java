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
import com.skytala.eCommerce.command.AddProductPromoCode;
import com.skytala.eCommerce.command.DeleteProductPromoCode;
import com.skytala.eCommerce.command.UpdateProductPromoCode;
import com.skytala.eCommerce.entity.ProductPromoCode;
import com.skytala.eCommerce.entity.ProductPromoCodeMapper;
import com.skytala.eCommerce.event.ProductPromoCodeAdded;
import com.skytala.eCommerce.event.ProductPromoCodeDeleted;
import com.skytala.eCommerce.event.ProductPromoCodeFound;
import com.skytala.eCommerce.event.ProductPromoCodeUpdated;
import com.skytala.eCommerce.query.FindProductPromoCodesBy;

@RestController
@RequestMapping("/api/productPromoCode")
public class ProductPromoCodeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPromoCode>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPromoCodeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPromoCode
	 * @return a List with the ProductPromoCodes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPromoCode> findProductPromoCodesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPromoCodesBy query = new FindProductPromoCodesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPromoCodeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodeFound.class,
				event -> sendProductPromoCodesFoundMessage(((ProductPromoCodeFound) event).getProductPromoCodes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPromoCodesFoundMessage(List<ProductPromoCode> productPromoCodes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPromoCodes);
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
	public boolean createProductPromoCode(HttpServletRequest request) {

		ProductPromoCode productPromoCodeToBeAdded = new ProductPromoCode();
		try {
			productPromoCodeToBeAdded = ProductPromoCodeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPromoCode(productPromoCodeToBeAdded);

	}

	/**
	 * creates a new ProductPromoCode entry in the ofbiz database
	 * 
	 * @param productPromoCodeToBeAdded
	 *            the ProductPromoCode thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPromoCode(ProductPromoCode productPromoCodeToBeAdded) {

		AddProductPromoCode com = new AddProductPromoCode(productPromoCodeToBeAdded);
		int usedTicketId;

		synchronized (ProductPromoCodeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodeAdded.class,
				event -> sendProductPromoCodeChangedMessage(((ProductPromoCodeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPromoCode(HttpServletRequest request) {

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

		ProductPromoCode productPromoCodeToBeUpdated = new ProductPromoCode();

		try {
			productPromoCodeToBeUpdated = ProductPromoCodeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPromoCode(productPromoCodeToBeUpdated);

	}

	/**
	 * Updates the ProductPromoCode with the specific Id
	 * 
	 * @param productPromoCodeToBeUpdated the ProductPromoCode thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPromoCode(ProductPromoCode productPromoCodeToBeUpdated) {

		UpdateProductPromoCode com = new UpdateProductPromoCode(productPromoCodeToBeUpdated);

		int usedTicketId;

		synchronized (ProductPromoCodeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodeUpdated.class,
				event -> sendProductPromoCodeChangedMessage(((ProductPromoCodeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPromoCode from the database
	 * 
	 * @param productPromoCodeId:
	 *            the id of the ProductPromoCode thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPromoCodeById(@RequestParam(value = "productPromoCodeId") String productPromoCodeId) {

		DeleteProductPromoCode com = new DeleteProductPromoCode(productPromoCodeId);

		int usedTicketId;

		synchronized (ProductPromoCodeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPromoCodeDeleted.class,
				event -> sendProductPromoCodeChangedMessage(((ProductPromoCodeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPromoCodeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPromoCode/\" plus one of the following: "
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
