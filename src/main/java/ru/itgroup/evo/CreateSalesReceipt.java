package ru.itgroup.evo;

import com.google.gson.Gson;
import ru.itgroup.evo.fn.DataFN;
import ru.itgroup.evo.fn.FiscalAccException;
import ru.itgroup.evo.fn.Struct;
import ru.itgroup.evo.jsonclass.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;


@Path("/fn_create_sales_receipt")
public class CreateSalesReceipt {
    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String go(String jsonRequest) {
        CommandFN commandFN = new Gson().fromJson(jsonRequest,CommandFN.class);
        ArrayList<Struct> ret;
        try {
            ret = commandFN.createSalesReceipt();
        } catch (FiscalAccException e) {
            try {
                commandFN.cancelDocument();
            } catch (FiscalAccException ex) {
                return new Gson().toJson(e.getMessage());
            }
            return new Gson().toJson(e.getMessage());
        }
        return new Gson().toJson(ret);
    }

    @OPTIONS
    @Produces("text/plain;charset=UTF-8")
    public String getSupportedOperations(){
        return "POST";
    }

}
