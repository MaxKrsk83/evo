package ru.itgroup.evo;

import com.google.gson.Gson;
import ru.itgroup.evo.fn.ManagerFN;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/info_ports")
public class InfoPorts {
    @GET
    @Produces(MediaType.APPLICATION_JSON )
    public String getInfoPorts() {
        String [] arrPort = ManagerFN.getListPort();
        return new Gson().toJson(arrPort);
    }

    @OPTIONS
    @Produces("text/plain;charset=UTF-8")
    public String getSupportedOperations(){
        return "GET";
    }

}
