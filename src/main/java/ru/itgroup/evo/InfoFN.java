package ru.itgroup.evo;

import com.google.gson.Gson;
import ru.itgroup.evo.fn.Struct;
import ru.itgroup.evo.jsonclass.CommandFN;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("/fn_info")
public class InfoFN {
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String getInfoFN() {

        CommandFN commandFN = new CommandFN();
        ArrayList<Struct> str = commandFN.getListFN();

        return new Gson().toJson(str);
    }

    @OPTIONS
    @Produces("text/plain;charset=UTF-8")
    public String getSupportedOperations(){
        return "GET";
    }

}
