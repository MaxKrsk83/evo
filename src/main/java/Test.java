import com.google.gson.Gson;
import ru.itgroup.evo.fn.*;
import ru.itgroup.evo.jsonclass.CommandFN;

import java.util.ArrayList;

public class Test {
    public static void main(String[] args) throws FiscalAccException {
//        int hex = 0x80;
//        int err = 0x01;
//        System.out.println(hex & err);
        CommandFN t = t();
//        String r = "{\n" +
//                "\t\"comPort\": \"COM100\",\n" +
//                "\t\"num_teg\":\"1017\"" +
//                "}";
//        CommandFN t = new Gson().fromJson(r,CommandFN.class);
        ArrayList<Struct> ret =  t.createCorrectionCheck();
        System.out.println(ret);
//        Struct [] param = new Struct[1];
//            Struct [] tov1 = new Struct[3];
//            tov1[0] = new Struct(DataFN.TEG.T_1030,"test 1");
//            tov1[1] = new Struct(DataFN.TEG.T_1079,7);
//            tov1[2] = new Struct(DataFN.TEG.T_1023,1);
//
//            Struct [] tov2 = new Struct[3];
//            tov2[0] = new Struct(DataFN.TEG.T_1030,"test 2");
//            tov2[1] = new Struct(DataFN.TEG.T_1079,13);
//            tov2[2] = new Struct(DataFN.TEG.T_1023,1.5);
//
//            ArrayList<Struct[]> p = new ArrayList<>();
//            p.add(tov1);
//            p.add(tov2);
//            param[0] = new Struct(DataFN.TEG.T_1059,p);
//
//            String str = new Gson().toJson(param);
//            Struct[] ret = new Gson().fromJson(str,Struct[].class);
        //System.out.println(str);
    }
    public static CommandFN t(){
        String jsonRequest = "{\n" +
                "\"comPort\": \"COM76\",\n" +
                "\"checkBegin\": [\n" +
                "{\n" +
                "\"key\": \"-1\",\n" +
                "\"value\": \"\"\n" +
                "}\n" +
                "],\n" +
                "\"checkBody\": [\n" +
                "{\n" +
                "\"key\": \"1173\",\n" +
                "\"value\": 0\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1174\",\n" +
                "\"value\": [\n" +
                "[\n" +
                "{\n" +
                "\"key\": \"1177\",\n" +
                "\"value\": \"впрыяваыа\"\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1178\",\n" +
                "\"value\": \"1640822400\"\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1179\",\n" +
                "\"value\": \"123\"\n" +
                "}\n" +
                "]\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1031\",\n" +
                "\"value\": 0\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1081\",\n" +
                "\"value\": 150\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1215\",\n" +
                "\"value\": 0\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1216\",\n" +
                "\"value\": 0\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1217\",\n" +
                "\"value\": 0\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1021\",\n" +
                "\"value\": \"Кассир\"\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1055\",\n" +
                "\"value\": 4\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1209\",\n" +
                "\"value\": 2\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"1105\",\n" +
                "\"value\": 150\n" +
                "}\n" +
                "],\n" +
                "\"checkEnd\": [\n" +
                "{\n" +
                "\"key\": \"-1\",\n" +
                "\"value\": \"\"\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"-2\",\n" +
                "\"value\": 1\n" +
                "},\n" +
                "{\n" +
                "\"key\": \"-3\",\n" +
                "\"value\": 150\n" +
                "}\n" +
                "]\n" +
                "}";
        return new Gson().fromJson(jsonRequest,CommandFN.class);

    }
}
