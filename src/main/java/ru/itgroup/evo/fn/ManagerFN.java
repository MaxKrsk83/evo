package ru.itgroup.evo.fn;

import jssc.SerialPortList;

import java.util.ArrayList;

public class ManagerFN {
    private DataFN dataFN = new DataFN();
    public ManagerFN(FiscalAcc fn, Struct param) throws FiscalAccException {

        if (fn==null){
            throw new FiscalAccException(new Struct("Ошибка","Не инициализирован ФН").toString());
        }
        Object ob = param.getKey();
        if (!(ob instanceof FiscalAcc.commands)){
            throw new FiscalAccException(new Struct("Ошибка","Не является командой").toString());
        }
        FiscalAcc.commands command = (FiscalAcc.commands) ob;
        Object obj = param.getValue();
        // тут уже подготовленные данные
        String [] paramToSend = getArrayFromParameters(obj);

        dataFN = fn.getData(command, paramToSend);
        //Protocol.printProtocol();////****
        if (!dataFN.isSuccessfulCommandExecution()) throw new FiscalAccException(new Struct("Ошибка",dataFN.getError()).toString());

    }
    public DataFN getDataFN(){
        return dataFN;
    }
    public static String [] getListPort() {
        return SerialPortList.getPortNames();
    }
    public static FiscalAcc findFnByFNumber(String number) throws FiscalAccException {
        FiscalAcc fn;
        String [] listPort = getListPort();
            for (int i = 0; i < listPort.length; i++) {
                fn = new FiscalAcc(listPort[i]);
                DataFN dataFN = fn.getData(FiscalAcc.commands.FN_SERIAL_NUMBER, new String[0]);
                if (dataFN!=null) {
                    String stB = new String(dataFN.toChar().getBytes());
                    if (stB.equals(number)){
                        return fn;
                    }
                }
            }
        return null;
    }
    private String [] p_(String [] arrToSend, Struct param) throws FiscalAccException {
        Object val = param.getValue();
        Object key = param.getKey();
        if (key instanceof DataFN.TEG){
            return DataFN.bothArray(arrToSend, ((DataFN.TEG) key).transformData(val)).clone();
        } else return null;
    }
    private String [] getArrayFromParameters(Object obj) throws FiscalAccException {
        String [] paramToSend = new String[0];
        if (obj instanceof Struct){//tlv
            Struct str= ((Struct) obj);
            paramToSend = p_(paramToSend,str);
        } else if (obj instanceof Struct[]) {//массив tlv
            for (int j = 0; j < ((Struct[]) obj).length; j++) {
                Struct str= ((Struct[]) obj)[j];
                paramToSend = p_(paramToSend,str);
            }
        } else if(obj instanceof ArrayList){
            for (int i = 0; i < ((ArrayList) obj).size(); i++) {
                Struct str= ((Struct) ((ArrayList) obj).get(i));
                paramToSend = p_(paramToSend,str);
            }
        } else if (obj instanceof String[]){//параметры не структурированные
            paramToSend = ((String[]) obj).clone();
        }
        return  paramToSend;
    }
    public ArrayList<Struct> getData() throws FiscalAccException {
        return dataFN.getArrStruct();
    }
//    private DataFN requestParameter(FiscalAcc fn, String tlv) throws FiscalAccException{
//        return fn.getData(FiscalAcc.commands.FN_REQUEST_PARAM_FISC, new String[]{tlv});
//    }
}
