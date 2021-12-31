package ru.itgroup.evo.fn;

import jssc.*;
import java.util.ArrayList;
import java.util.Arrays;


public class FiscalAcc{
    private final SerialPort serialPort;
    private final SerialEvent serialEvent;
    private DataFN data;
    private boolean answerReceived;
    private int timeOut = 5000;
    private commands currentTask;

    @Override
    public String toString() {
        String ret = "";
        //try {
            ret = String.format("%nНомер: %s%nПорт: %s","getData(commands.FN_SERIAL_NUMBER).toString()",getSerialPort());
        //} catch (FiscalAccException e) {
        //    ret  = e.getMessage();
       // }
        return ret;
    }

    public ArrayList<Struct> getFnNumber(){
        ArrayList<Struct> ret = new ArrayList<>();
        try {
            ret.addAll(getData(commands.FN_SERIAL_NUMBER,new String[0]).getArrStruct());
        } catch (FiscalAccException e) {
            ret.add( new Struct("Ошибка", e.getMessage()));
        }
        return ret;
    }

    public String getSerialPort() {
        return serialPort.getPortName();
    }

    public FiscalAcc(String port) {
        serialPort = new SerialPort(port);
        serialEvent = new SerialEvent();
        currentTask = commands.FN_NONE;
        data = new DataFN(currentTask);
    }

    public FiscalAcc(String port, int timeOut) {
        this(port);
        this.timeOut = timeOut;
    }
//    public DataFN getData(commands comm) throws FiscalAccException{
//        return getData(comm,new String[0]);
//    }

//    private ArrayList<Integer> getHexTlvParamLE(String param){
//        ArrayList<Integer> ret = new ArrayList<>();
//        String hexString = toHexString(Integer.parseInt(param));
//        for (int i = hexString.length(); i >=0 ; i--) {
//            if (i%2!=0) {
//                ret.add(Integer.parseInt(""+hexString.charAt(i-1) + hexString.charAt(i),16));
//            }
//        }
//        return ret;
//    }
    public DataFN getData(commands comm,String [] param) throws FiscalAccException {
        data = new DataFN(comm);
        try {
            if (!init()){
                throw new FiscalAccException(new Struct("Ошибка","Не удалось инициализировать порт").toString());
            } else{
                currentTask = comm;
                DataFN dataFN = new DataFN(comm);
                dataFN.setData(param);
                Protocol.add(false,dataFN.getDataToArrStr());
                writeToPort(dataFN.getDataToArrInt());
            }
        } catch (SerialPortException e) {
            throw new FiscalAccException(new Struct("Ошибка",e.getMessage()).toString());// FiscalAccException("Не удалось открыть порт: ");
        }
        currentTask=commands.FN_NONE;
        return data;
    }
    private void writeToPort(int [] dataBuf) throws FiscalAccException{
        try {
            answerReceived = false;
            if (serialPort.writeIntArray(dataBuf)){
                // запихать в поток
                int time = 0;
                while (!answerReceived){
                    if (time>=timeOut){
                        throw new FiscalAccException(new Struct("Ошибка","Время чтения вышло").toString());
                    }else{
                        time++;
                    }
                    Thread.sleep(1);
                }
            }else{
                throw new FiscalAccException(new Struct("Ошибка","Не удалось записать данные в порт").toString());
            }

        } catch (SerialPortException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private String toHexString(int d){
        String ret = Integer.toHexString(d);
        if (ret.length()%2!=0){
            ret = "0" + ret;
        }
        return ret;
    }
    public enum commands {
        FN_SERIAL_NUMBER(0x31),
        FN_TIME(0x66),
        FN_REQUEST_TOTALS_UNSENT_DOCUMENTS(0x39),
        FN_COUNT_UNSENT_DOCUMENTS(0x42),
        FN_REGISTRATION_OPTIONS(0x7E),
        FN_STATUS(0x30),
        FN_VERSION(0x33),
        FN_VALIDITY(0x32),
        FN_RECENT_ERRORS(0x35),
        FN_NONE(0x00),
        FN_REQUEST_PARAM_CUR_SHIFT(0x10),
        FN_CLOSE_SHIFT(0x14),
        FN_BEGIN_CLOSE_SHIFT(0x13),
        FN_OPEN_SHIFT(0x12),
        FN_BEGIN_OPEN_SHIFT(0x11),
        FN_CANCEL_DOCUMENT(0x06),
        FN_BEGIN_CHECK(0x15),
        FN_END_CHECK(0x16),
        FN_BEGIN_CHECK_CORRECTION(0x17),
        FN_DOC(0x40),
        FN_DOC_TLV(0x45),
        FN_READ_DOC_TLV(0x46),
        FN_TRANSFER_DOCUMENT(0x07),
        FN_OFD_GET_INFORMATION_EXCHANGE_STATUS(0x20),
        FN_OFD_ESTABLISH_TRANSPORT_CONNECTION(0x21),
        FN_OFD_START_READING_MESSAGES_OFD_SERVER(0x22),
        FN_OFD_READ_MESSAGE_BLOCK_OFD(0x23),
        FN_OFD_FINISH_READING_MESSAGE_OFD_SERVER(0x25),
        FN_OFD_TRANSFER_TO_FN_OF_RECEIPTS_FROM_OFD(0x26),
        //параметры регистрации
        FN_REQUEST_PARAM_FISC(0x44),
        FN_READ_REG_TLV(0x47);
        private final int command;
        commands(int command) {
            this.command = command;
        }
        public int getCod() {
            return command;
        }
    }
    private boolean init() throws SerialPortException {
        serialPort.openPort();
        serialPort.setParams(SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.addEventListener(serialEvent);
        return true;
    }
    private class SerialEvent implements SerialPortEventListener {
        private String [] readFromPort(){
            String [] ret = new String[0];
            try {
                String [] buf = serialPort.readHexStringArray(3);
                ret = DataFN.bothArray(ret,buf);
                int size = DataFN.getSize(buf);
                buf = serialPort.readHexStringArray(size + 2);
                ret = DataFN.bothArray(ret,buf);
                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
            return ret;
        }
        @Override
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){
                String [] dataIn = readFromPort();
                Protocol.add(true, dataIn);
                data = new DataFN(currentTask);
                data.transformInputData(dataIn,false);
                try {
                    if (!data.isBeginData()){
                        throw new FiscalAccException(new Struct("Ошибка","Не является потоком данных").toString());
                    } else {
                        if (!data.isValid())
                        {
                            throw new FiscalAccException(new Struct("Ошибка","Данные не валидны!" + Arrays.toString(data.getValue(DataFN.CRC))).toString());
                        }
                    }
                } catch (FiscalAccException e) {
                    System.out.println(e.getMessage());
                }
                answerReceived = true;
            }
        }
    }
}
