package ru.itgroup.evo;

import com.google.gson.Gson;
import ru.itgroup.evo.fn.DataFN;
import ru.itgroup.evo.fn.Struct;
import ru.itgroup.evo.jsonclass.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("/fn_serial")
public class SerialFn {
    @POST
    @Produces(MediaType.APPLICATION_JSON +";charset=UTF-8")
    public String go(String jsonRequest) {
        CommandFN commandFN = new Gson().fromJson(jsonRequest,CommandFN.class);
        ArrayList<Struct> ret = commandFN.getSerialFN();
        return new Gson().toJson(ret);
    }

    @OPTIONS
    @Produces("text/plain;charset=UTF-8")
    public String getSupportedOperations(){
        return "GET";
    }

}
