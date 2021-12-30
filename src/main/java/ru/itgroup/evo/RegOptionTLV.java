package ru.itgroup.evo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.itgroup.evo.fn.FiscalAccException;
import ru.itgroup.evo.fn.Struct;
import ru.itgroup.evo.jsonclass.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("/fn_reg_option_tlv")
public class RegOptionTLV {
    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String go() {

        CommandFN commandFN = new CommandFN();
        ArrayList<Struct> str = null;
        try {
            str = commandFN.getRegOptionTlv();
        } catch (FiscalAccException e) {
            return new Gson().toJson(e.getMessage());
        }

        return new Gson().toJson(str);
    }

    @OPTIONS
    @Produces("text/plain;charset=UTF-8")
    public String getSupportedOperations(){
        return "GET";
    }

}
