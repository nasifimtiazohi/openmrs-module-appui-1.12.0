package org.openmrs.module.appui.rest;



import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/appui/session")
public class SessionController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private ProviderService providerService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object get(HttpServletRequest request, HttpServletResponse response){
        RequestContext requestContext = RestUtil.getRequestContext(request, response);
        Representation rep = requestContext.getRepresentation();

        UiSessionContext uiSessionContext = new UiSessionContext(locationService, providerService, request);
        SimpleObject ret = new SimpleObject()
                .add("authenticated", uiSessionContext.isAuthenticated())
                .add("locale", uiSessionContext.getLocale().toString())
                .add("currentProvider", ConversionUtil.convertToRepresentation(uiSessionContext.getCurrentProvider(), rep))
                .add("sessionLocation", ConversionUtil.convertToRepresentation(uiSessionContext.getSessionLocation(), rep))
                .add("user", ConversionUtil.convertToRepresentation(uiSessionContext.getCurrentUser(), rep));
        return ret;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Object set(@RequestBody Map<String, Object> requestBody,
                      HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(requestBody.get("location") != null && StringUtils.isNotBlank(requestBody.get("location").toString())){
            UiSessionContext uiSessionContext = new UiSessionContext(locationService, providerService, request);
            Location location = locationService.getLocationByUuid(requestBody.get("location").toString());
            if (location != null) {
                uiSessionContext.setSessionLocation(location);
            }
        }


        return get(request, response);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Object delete() {
        Context.logout();
        return new SimpleObject().add("authenticated", Context.isAuthenticated());
    }

}
