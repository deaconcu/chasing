package com.prosper.chasing.tool.controll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prosper.chasing.tool.bean.ActionData;
import com.prosper.chasing.tool.service.ActionDataService;
import com.prosper.chasing.tool.validation.Validation;

@RestController
public class ActionDataController {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private Validation validation;
    @Autowired
    private ActionDataService actionDataService;

    @RequestMapping(value="/actionData",method=RequestMethod.GET)
    public Object getActionDataX(
            @RequestParam(value="pageLength", defaultValue="1000") int pageLength,
            HttpServletResponse response){
        response.addHeader("Access-Control-Allow-Origin", "*");
        Map<String, List<? extends Object>> dataMap = new HashMap<>();
        dataMap.put("time", actionDataService.getActionDataTimes(pageLength));
        dataMap.put("x", actionDataService.getActionDataX(pageLength));
        dataMap.put("y", actionDataService.getActionDataY(pageLength));
        dataMap.put("z", actionDataService.getActionDataZ(pageLength));
        dataMap.put("a", actionDataService.getActionDataA(pageLength));
        
        return dataMap;
    }
    
    @RequestMapping(value="/actionData",method=RequestMethod.POST)
    public Object postActionData(HttpServletRequest request, @RequestBody String body){
        ActionData actionData = validation.getObject(body, ActionData.class, null);
        log.info("recieve action data" + actionData);
        actionDataService.addActionData(actionData);
        return "ok";
    }
    
}
