package ru.itgroup.evo.jsonclass;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ru.itgroup.evo.fn.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class CommandFN {
    private final String comPort;
    private final String [] innList = new String[0];
    private final String zNumber;
    private final String regNum;
    private final int docNumber;
    private final int num_reg;
    private final String num_teg;
    private final ArrayList<Struct> checkBegin;
    private final ArrayList<Struct> checkBody;
    private final ArrayList<Struct> checkEnd;

    public CommandFN(String comPort) {
        this.comPort = comPort;
        zNumber = "";
        regNum = "";
        docNumber = 0;
        num_reg = 0;
        num_teg = "";
        checkBegin = new ArrayList<>();
        checkBody = new ArrayList<>();
        checkEnd = new ArrayList<>();
    }
    public CommandFN() {
        this("");
    }
    public ArrayList<Struct> getListFN() {
        ArrayList<Struct> ret = new ArrayList<>();
        String [] arrPort = ManagerFN.getListPort();
        for (String s : arrPort) {
            FiscalAcc fn = new FiscalAcc(s);
            ret.add(new Struct(s, fn.getFnNumber()));
        }
        return ret;
    }
    public ArrayList<Struct> getCurShift(){
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;
        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        Struct param = new Struct(FiscalAcc.commands.FN_REQUEST_PARAM_CUR_SHIFT,"");
        try {
            ret.addAll(new ManagerFN(fiscalAcc,param).getData());
        } catch (FiscalAccException e) {
            ret.add(new Struct("Ошибка",e.getMessage()));
        }
        return ret;
    }
    public ArrayList<Struct> getInfoServer() {
        ArrayList<Struct> ret = new ArrayList<>();
        try {
            ret.add(new Struct("hostName",InetAddress.getLocalHost().getHostName()));
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iFace = interfaces.nextElement();
                if (iFace.isLoopback() || !iFace.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iFace.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    ret.add(new Struct("ipAddress",inetAddress.getHostAddress()));
                }
            }
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public String getInn(){

        String ret = "";
        if (innList==null) return ret;
        if (innList.length==0 || Objects.equals(zNumber, "") || Objects.equals(regNum, ""))return ret;
        String indexNum = regNum.substring(0,10);
        int crcInt = Integer.parseInt(regNum.substring(10));
        String zNum20Dig = "00000000000000000000".substring(zNumber.length()) + zNumber;
        try {
            for (String s : innList) {
                byte[] tmp ;
                tmp = (indexNum + s + zNum20Dig).getBytes("cp866");
                int crc16 = CRC16CCITT(tmp);
                if (crcInt == crc16) {
                    ret = s;
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }
    private int CRC16CCITT(byte[] arr){
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        for (int b : arr) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        crc &= 0xffff;
        return crc;
    }
    public ArrayList<Struct> getSerialFN(){
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;
        FiscalAcc fiscalAcc = new FiscalAcc(comPort);

        Struct param = new Struct(FiscalAcc.commands.FN_SERIAL_NUMBER,"");
        try {
            ret.addAll(new ManagerFN(fiscalAcc,param).getData());
        } catch (FiscalAccException e) {
            ret.add(new Struct("Ошибка",e.getMessage()));
        }

        return ret;
    }
    public ArrayList<Struct> getStatusFN(){
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;
        FiscalAcc fiscalAcc = new FiscalAcc(comPort);

        Struct param = new Struct(FiscalAcc.commands.FN_STATUS,"");
        try {
            ret.addAll(new ManagerFN(fiscalAcc,param).getData());
        } catch (FiscalAccException e) {
            ret.add(new Struct("Ошибка",e.getMessage()));
        }

        return ret;
    }
    public ArrayList<Struct> getVersionFN(){
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;
        FiscalAcc fiscalAcc = new FiscalAcc(comPort);

        Struct param = new Struct(FiscalAcc.commands.FN_VERSION,"");
        try {
            ret.addAll(new ManagerFN(fiscalAcc,param).getData());
        } catch (FiscalAccException e) {
            ret.add(new Struct("Ошибка",e.getMessage()));
        }

        return ret;
    }
    public ArrayList<Struct> getValidFN(){
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;
        FiscalAcc fiscalAcc = new FiscalAcc(comPort);

        Struct param = new Struct(FiscalAcc.commands.FN_VALIDITY,"");
        try {
            ret.addAll(new ManagerFN(fiscalAcc,param).getData());
        } catch (FiscalAccException e) {
            ret.add(new Struct("Ошибка",e.getMessage()));
        }

        return ret;
    }
    public ArrayList<Struct> getErrorFN(){
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        Struct param = new Struct(FiscalAcc.commands.FN_RECENT_ERRORS,"");
        try {
            ret.addAll(new ManagerFN(fiscalAcc,param).getData());
        } catch (FiscalAccException e) {
            ret.add(new Struct("Ошибка",e.getMessage()));
        }

        return ret;
    }

    public ArrayList<Struct> closeShift() throws FiscalAccException {
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        Struct param= new Struct(FiscalAcc.commands.FN_BEGIN_CLOSE_SHIFT,"");

        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();

        if (dataFN.isSuccessfulCommandExecution()){
            param = new Struct(FiscalAcc.commands.FN_CLOSE_SHIFT,"");
            dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
            if (dataFN.isSuccessfulCommandExecution()){
                ret = dataFN.getArrStruct();
            }else{
                throw new FiscalAccException(new Struct("Ошибка","Не удалось закрыть смену " + dataFN.getError()).toString());
            }
        } else{
            throw new FiscalAccException(new Struct("Ошибка","Не удалось закрыть смену "+ dataFN.getError()).toString());
        }
        return ret;
    }
    public ArrayList<Struct> openShift() throws FiscalAccException {
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        Struct param= new Struct(FiscalAcc.commands.FN_BEGIN_OPEN_SHIFT,"");
        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
        if (dataFN.isSuccessfulCommandExecution()){
            param = new Struct(FiscalAcc.commands.FN_OPEN_SHIFT,"");
            dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
            if (dataFN.isSuccessfulCommandExecution()){
                ret = dataFN.getArrStruct();
            }else{
                throw new FiscalAccException(new Struct("Ошибка","Не удалось открыть смену " + dataFN.getError()).toString());
            }
        } else{
            throw new FiscalAccException(new Struct("Ошибка","Не удалось открыть смену "+ dataFN.getError()).toString());
        }
        return ret;
    }

    public ArrayList<Struct> getTimeFN() throws FiscalAccException {
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        Struct param= new Struct(FiscalAcc.commands.FN_TIME,"");

        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
        if (dataFN.isSuccessfulCommandExecution()){
            ret = dataFN.getArrStruct();
        } else{
            throw new FiscalAccException(new Struct("Ошибка","Не удалось получить время ФН "+ dataFN.getError()).toString());
        }

        return ret;
    }
    public ArrayList<Struct> getTotalUnsent() throws FiscalAccException {
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        Struct param= new Struct(FiscalAcc.commands.FN_REQUEST_TOTALS_UNSENT_DOCUMENTS,"");

        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
        if (dataFN.isSuccessfulCommandExecution()){
            ret = dataFN.getArrStruct();
        } else{
            throw new FiscalAccException(new Struct("Ошибка","Не удалось получить итоги "+ dataFN.getError()).toString());
        }

        return ret;
    }
    public ArrayList<Struct> getRegOptions() throws FiscalAccException {
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        Struct param= new Struct(FiscalAcc.commands.FN_REGISTRATION_OPTIONS,"");

        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
        if (dataFN.isSuccessfulCommandExecution()){
            ret = dataFN.getArrStruct();
        } else{
            throw new FiscalAccException(new Struct("Ошибка","Не удалось получить итоги "+ dataFN.getError()).toString());
        }

        return ret;
    }
    public ArrayList<Struct> getCountUnSent() throws FiscalAccException {
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) return ret;

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        Struct param= new Struct(FiscalAcc.commands.FN_COUNT_UNSENT_DOCUMENTS,"");

        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
        if (dataFN.isSuccessfulCommandExecution()){
            ret = dataFN.getArrStruct();
        } else{
            throw new FiscalAccException(new Struct("Ошибка","Не удалось получить итоги "+ dataFN.getError()).toString());
        }

        return ret;
    }
    public ArrayList<Struct> getInfoDoc() throws FiscalAccException {
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null || docNumber==0) throw new FiscalAccException(new Struct("Ошибка","Не верно переданные параметры").toString());

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);

        Struct param= new Struct(FiscalAcc.commands.FN_DOC,new Struct(DataFN.TEG.NT_INT,docNumber));

        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
        if (dataFN.isSuccessfulCommandExecution()){
            ret = dataFN.getArrStruct();
        } else{
            throw new FiscalAccException(new Struct("Ошибка","Не удалось получить документ по номеру "+ dataFN.getError()).toString());
        }

        return ret;
    }

    public ArrayList<Struct> createSalesReceipt() throws FiscalAccException {
        if (comPort==null || checkBegin ==null || checkBody==null || checkEnd ==null ) throw new FiscalAccException(new Struct("Ошибка","Не верно переданные параметры").toString());
        else return createCheck(FiscalAcc.commands.FN_BEGIN_CHECK);
    }
    private ArrayList<Struct> createCheck(FiscalAcc.commands commands) throws FiscalAccException {
        ArrayList<Struct> ret;
        Struct param;
        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        param = new Struct(commands,convertInputDataToTag(checkBegin));
        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
        if (!dataFN.isSuccessfulCommandExecution())
            throw new FiscalAccException(new Struct("Ошибка","Не удалось передать заголовок чека: " + dataFN.getError()).toString());

        for (Struct struct : checkBody) {
            param = new Struct(FiscalAcc.commands.FN_TRANSFER_DOCUMENT, convertInputDataToTag(struct));
            dataFN = new ManagerFN(fiscalAcc, param).getDataFN();
            if (!dataFN.isSuccessfulCommandExecution())
                throw new FiscalAccException(new Struct("Ошибка", "Не удалось передать тело чека: " + dataFN.getError()).toString());
        }
        param = new Struct(FiscalAcc.commands.FN_END_CHECK,convertInputDataToTag(checkEnd));
        dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
        if (dataFN.isSuccessfulCommandExecution()){
            ret = new ArrayList<>(dataFN.getArrStruct());
        }else throw new FiscalAccException(new Struct("Ошибка","Не удалось передать результат чека: " + dataFN.getError()).toString());
        return ret;
    }
    public ArrayList<Struct> createCorrectionCheck() throws FiscalAccException {
        if (comPort==null || checkBegin ==null || checkBody==null || checkEnd ==null ) throw new FiscalAccException(new Struct("Ошибка","Не верно переданные параметры").toString());
        else return createCheck(FiscalAcc.commands.FN_BEGIN_CHECK_CORRECTION);
    }
    private ArrayList<Struct> convertInputDataToTag(Object dataIn){
        ArrayList<Struct> r = new ArrayList<>();
        if (dataIn instanceof ArrayList){
            Struct[] map = new Gson().fromJson(new Gson().toJson(dataIn),Struct[].class);

            for (Struct struct : map) {
                DataFN.TEG teg = DataFN.TEG.T_((String) struct.getKey());
                r.add(new Struct(teg, struct.getValue()));
            }

        } else if (dataIn instanceof Struct){
            Struct str = ((Struct)dataIn);
            DataFN.TEG teg = DataFN.TEG.T_((String)str.getKey());
            Object val = str.getValue();
            if (val instanceof ArrayList){
                for (int i = 0; i < ((ArrayList<Struct>) val).size(); i++) {
                    r.add(new Struct(teg, convertInputDataToTag(((ArrayList<Struct>) val).get(i))));
                }
            } else{
                r.add(new Struct(teg, val));
            }
        }
        return r;
    }
    public ArrayList<Struct> cancelDocument() throws FiscalAccException {
        ArrayList<Struct> ret;
        if (comPort==null ) throw new FiscalAccException(new Struct("Ошибка","Не верно переданные параметры").toString());

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);

        Struct param= new Struct(FiscalAcc.commands.FN_CANCEL_DOCUMENT,"");

        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
        if (dataFN.isSuccessfulCommandExecution()){
            ret = dataFN.getArrStruct();
        } else{
            throw new FiscalAccException(new Struct("Ошибка","Не удалось отменить документ "+ dataFN.getError()).toString());
        }

        return ret;
    }
    public ArrayList<Struct> getRegOptionTlv() throws FiscalAccException {
        ArrayList<Struct> ret = new ArrayList<>();
        if (comPort==null) throw new FiscalAccException(new Struct("Ошибка","Не верно переданные параметры").toString());

        FiscalAcc fiscalAcc = new FiscalAcc(comPort);
        Struct [] structs = new Struct[]{new Struct("num_reg",num_reg),new Struct("num_teg",num_teg)};


        Struct param= new Struct(FiscalAcc.commands.FN_REQUEST_PARAM_FISC,new Struct(DataFN.TEG.NT_REQUEST_PARAM_FISC,structs));

        if(num_reg==0){
            if (num_teg.equals("")){
                throw new FiscalAccException(new Struct("Ошибка","Не верные параметры").toString());
            } else{
                //запрос тлв параметра с последней регистрации без цикла
            }
        } else {
            if (num_teg.equals("")){
                //запрос всех параметров тлв в цикле с регистрации num_reg
            } else{
                //запрос тлв параметра с регистрации num_reg без цикла
            }
        }

//        DataFN dataFN = new ManagerFN(fiscalAcc,param).getDataFN();
//        if (dataFN.isSuccessfulCommandExecution()){
//            ret = dataFN.getArrStruct();
//        } else{
//            throw new FiscalAccException(new Struct("Ошибка","Не удалось отменить документ "+ dataFN.getError()).toString());
//        }

        return ret;
    }
}
