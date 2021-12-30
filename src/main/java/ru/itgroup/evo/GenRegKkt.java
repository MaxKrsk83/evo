package ru.itgroup.evo;

import com.google.gson.Gson;
import ru.itgroup.evo.jsonclass.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/reg_gen_kkt")
public class GenRegKkt {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getReg(String jsonRequest){
        CommandFN commandFN = new Gson().fromJson(jsonRequest, CommandFN.class);
        return commandFN.getInn();
    }
    @OPTIONS
    @Produces("text/plain;charset=UTF-8")
    public String getSupportedOperations(){
        return "POST";
    }

}
