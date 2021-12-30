package ru.itgroup.evo;

import com.google.gson.Gson;
import ru.itgroup.evo.fn.Struct;
import ru.itgroup.evo.jsonclass.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("/info_server")
public class Info {
    @GET
    @Produces(MediaType.APPLICATION_JSON )
    public String getInfoServer() {
        ArrayList<Struct> ret = new CommandFN().getInfoServer();
        return new Gson().toJson(ret);
    }

    @OPTIONS
    @Produces("text/plain;charset=UTF-8")
    public String getSupportedOperations(){
        return "GET";
    }

}
