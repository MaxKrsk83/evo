package ru.itgroup.evo.fn;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static java.lang.Integer.parseInt;

public class DataFN {
    public static final String CRC = "crc";
    public static final String SIZE = "size";
    public static final String RESULT = "result";
    public static final String DATA = "data";
    public static final String BEGIN = "begin";
    public static final String TLV_TEG = "tlv_teg";
    public static final String COMMAND = "command";
    private FiscalAcc.commands currentCommand;
    private boolean isTlv;
    private boolean hasWarningFlags;

    enum SNO{
        OB(1,"Общая"),
        UD(2,"Упрощенная доход"),
        UD_R(4, "Упрощенная доход минус расход"),
        ENVD(8,"Единый налог на вмененный доход"),
        ESN(16,"Единый сельскохозяйственный налог"),
        PAT(32,"Патентная система налогообложения"),
        NONE(-1,"Не удалось определить СНО");
        private final int sno;
        private final String name;
        SNO(int sno, String name){
            this.sno = sno;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(sno);
        }
        public int getSno() {
            return sno;
        }
        static SNO define(String[] snoHex){
            return define(Integer.parseInt(parseArrStrHexToStr(snoHex)));
        }
        static SNO define(String sno){
            return define(Integer.parseInt(sno));
        }
        static SNO define(int sno){
            SNO[] types = SNO.values();
            for (SNO s : types) {
                if (s.getSno()==sno){
                    return s;
                }
            }
            return NONE;
        }
        @Override
        public String toString() {
            return String.format("%s (%d)",name,sno);
        }
    }
    enum VAT_RATE{
        V_20(1,"Ставка НДС 20%"),
        V_10(2,"Ставка НДС 10%"),
        V_20_120(3, "Ставка НДС расч. 20/120"),
        V_10_120(4,"Ставка НДС расч. 10/110"),
        V_0(5,"Ставка НДС 0%"),
        V_NONE(6,"Без НДС"),
        NONE(-1,"Не удалось определить ставку");
        private final int kod;
        private final String name;
        VAT_RATE(int sno, String name){
            this.kod = sno;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static VAT_RATE define(String rateHex){
            return define(Integer.parseInt(rateHex,16));
        }
        static VAT_RATE define(int rate){
            VAT_RATE[] types = VAT_RATE.values();
            for (VAT_RATE s : types) {
                if (s.getKod()==rate){
                    return s;
                }
            }
            return NONE;
        }
        @Override
        public String toString() {
            return String.format("%s (%d)",name, kod);
        }
    }
    enum SETTLEMENT_ATTRIBUTE{
        S_INCOMING(1,"приход"),
        S_RETURN(2,"возврат прихода"),
        S_EXPENSES(3, "расход"),
        S_RETURN_EXPENSES(4,"возврат расхода"),
        NONE(-1,"Не удалось определить ставку");
        private final int kod;
        private final String name;
        SETTLEMENT_ATTRIBUTE(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static SETTLEMENT_ATTRIBUTE define(String kod) {
           return define(Integer.parseInt(kod,16));
        }
        static SETTLEMENT_ATTRIBUTE define(int kod){
            SETTLEMENT_ATTRIBUTE[] types = SETTLEMENT_ATTRIBUTE.values();
            for (SETTLEMENT_ATTRIBUTE s : types) {
                if (s.getKod()==kod){
                    return s;
                }
            }
            return NONE;
        }
        @Override
        public String toString() {
            return String.format("%s (%d)",name, kod);
        }
    }
    enum PAYMENT_METHOD{
        P_1(1,"предоплата 100%"),
        P_2(2,"предоплата"),
        P_3(3,"аванс"),
        P_4(4,"полный расчет"),
        P_5(5,"частичный расчет и кредит"),
        P_6(6,"передача в кредит"),
        P_7(7,"оплата кредита"),
        NONE(-1,"не удалось определить способ расчета");
        private final int kod;
        private final String name;
        PAYMENT_METHOD(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static PAYMENT_METHOD define(String kodHex) {
            return define(Integer.parseInt(kodHex,16));
        }
        static PAYMENT_METHOD define(int kod){
            PAYMENT_METHOD[] types = PAYMENT_METHOD.values();
            for (PAYMENT_METHOD s : types) {
                if (s.getKod()==kod){
                    return s;
                }
            }
            return NONE;
        }
        @Override
        public String toString() {
            return String.format("%s (%d)",name, kod);
        }
    }
    public enum CODE_RESPONSE{
        C_0(0x00,"Ошибок нет"),
        C_1(0x01,"Неизвестная команда, неверный формат посылки или неизвестные параметры"),
        C_2(0x02,"Неверное состояние ФН"),
        C_3(0x03,"Ошибка ФН"),
        C_4(0x04,"Ошибка КС"),
        C_5(0x05,"Закончен срок эксплуатации ФН"),
        C_6(0x06,"Архив ФН переполнен"),
        C_7(0x07,"Неверные дата и/или время"),
        C_8(0x08,"Нет запрошенных данных"),
        C_9(0x09,"Некорректное значение параметров команды"),
        C_10(0x0A,"Неверная команда"),
        C_11(0x0B,"Неразрешенные реквизиты"),
        C_12(0x0C,"Дублирование данных"),
        C_13(0x0D,"Отсутствуют данные, необходимые для корректного учета в ФН"),
        C_14(0x0E,"Количество позиций, подлежащих учету в документе, превысило разрешенный лимит"),
        C_15(0x0F,"Отсутствуют данные в команде"),
        C_16(0x10,"Превышение размеров TLV данных"),
        C_17(0x11,"Нет транспортного соединения"),
        C_18(0x12,"Исчерпан ресурс КС"),
        C_20(0x14,"Исчерпан ресурс хранения"),
        C_21(0x15,"Исчерпан ресурс Ожидания передачи сообщения"),
        C_22(0x16,"Продолжительность смены более 24 часов"),
        C_23(0x17,"Неверная разница во времени между 2 операциями"),
        C_24(0x18,"В данном реквизите параметры не соответствуют форматам данных"),
        C_25(0x19,"Продажа подакцизного товара"),
        C_32(0x20,"Сообщение от ОФД не может быть принято"),
        C_128(0x80,"Флаг предупреждения"),
        NONE(-1,"Не удалось определить код ошибки");
        private final int kod;
        private final String name;
        CODE_RESPONSE(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        public static CODE_RESPONSE define(String kodHex){
            return define(Integer.parseInt(kodHex,16));
        }
        public static ArrayList<Struct> getError(String errStr) {
            return getError(Integer.parseInt(errStr,16));
        }
        public static boolean hasWarningFlags(final String code) {
            return hasWarningFlags(Integer.parseInt(code,16));
        }
        public static boolean hasWarningFlags(final int code){
            return code>=CODE_RESPONSE.C_128.getKod();
        }
        public Struct getStruct(){
            return new Struct(name,kod);
        }
        public static ArrayList<Struct> getError(final int err){
            ArrayList<Struct> ret = new ArrayList<>();
            int errTmp = err;
            if (hasWarningFlags(errTmp)){
                CODE_RESPONSE flag = CODE_RESPONSE.C_128;
                ret.add(flag.getStruct());
                errTmp = errTmp - flag.getKod();
            }
            ret.add(CODE_RESPONSE.define(errTmp).getStruct());
            return ret;
        }
        public static CODE_RESPONSE define(int kod){
            CODE_RESPONSE[] types = CODE_RESPONSE.values();
            for (CODE_RESPONSE s : types) {
                if (s.getKod()==kod){
                    return s;
                }
            }
            return NONE;
        }
        public static boolean isSuccessExec(String code){
            return isSuccessExec(Integer.parseInt(code,16));
        }
        public static boolean isSuccessExec(int code){
            if (hasWarningFlags(code)){
                return CODE_RESPONSE.define(code)==CODE_RESPONSE.C_128;
            }else{
                return CODE_RESPONSE.define(code)==CODE_RESPONSE.C_0;
            }
        }
        @Override
        public String toString() {
            return getStruct().toString();
        }
    }
    enum REASON_REGISTERING{
        R_REPLACE_FN(1,"Замена ФН"),
        R_REPLACE_OFD(2,"Замена ОФД"),
        R_CHANGE_REQUISITES(3, "Изменение реквизитов"),
        R_CHANGE_SETTING_KKT(4,"Изменение настроек ККТ"),
        NONE(-1,"Не удалось определить причину перерегистрации");
        private final int kod;
        private final String name;
        REASON_REGISTERING(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static REASON_REGISTERING define(String [] kodHex){
            int kod= Integer.parseInt(parseArrStrHexToStrLe(kodHex));
            REASON_REGISTERING[] types = REASON_REGISTERING.values();
            for (REASON_REGISTERING s : types) {
                if (s.getKod()==kod){
                    return s;
                }
            }
            return NONE;
        }
        @Override
        public String toString() {
            return String.format("%s (%d)",name, kod);
        }
    }
    enum FFD{
        FFD_NONE(0, "Не определен"),
        FFD_1_0(1, "ФФД 1.0"),
        FFD_1_05(2,"ФФД 1.05"),
        FFD_1_1(3,"ФФД 1.1");
        private final int ffd;
        private final String name;
        FFD(int ffd,String name){
            this.ffd = ffd;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(ffd);
        }
        static FFD define(String[] ffdHex){
            return define(Integer.parseInt(parseArrStrHexToStr(ffdHex)));
        }
        static FFD define(String ffd){
            return define(Integer.parseInt(ffd));
        }
        static FFD define(int ffd){
            FFD[] types = FFD.values();
            for (FFD s : types) {
                if (s.getFfd()==ffd){
                    return s;
                }
            }
            return FFD_NONE;
        }
        public int getFfd() {
            return ffd;
        }
        @Override
        public String toString() {
            return String.format("%s (%d)",name,ffd);
        }
    }
    public enum TEG {
        NT_REQUEST_PARAM_FISC(-6,""),
        NT_BYTE(-5,"Байт"),
        NT_INT(-4,"Целое число"),
        NT_DATE(-1,"дата"),
        NT_SA(-2,"Предмет расчета"),
        NT_SUM(-3,"Сумма"),
        T_0(0,"Тег не определен"),
        T_1000(1000,"Наименование документа"),
        T_1001(1001,"Признак автоматического режима"),
        T_1002(1002,"Признак автономного режима"),
        T_1005(1005,"Адрес оператора перевода"),
        T_1008(1008,"Телефон или электронный адрес покупателя"),
        T_1009(1009,"Адрес расчетов"),
        T_1012(1012,"Дата, время"),
        T_1013(1013,"Заводской номер ККТ"),
        T_1016(1016,"ИНН оператора перевода"),
        T_1017(1017,"ИНН ОФД"),
        T_1018(1018,"ИНН пользователя"),
        T_1020(1020,"Сумма расчета, указанного в чеке (БСО)"),
        T_1021(1021,"Кассир"),
        T_1022(1022,"Код ответа ОФД"),
        T_1023(1023,"Количество предмета расчета"),
        T_1026(1026,"Наименование оператора перевода"),
        T_1030(1030,"Наименование предмета расчета"),
        T_1031(1031,"Сумма по чеку (БСО) наличными"),
        T_1036(1036,"Номер автомата"),
        T_1037(1037,"Регистрационный номер ККТ"),
        T_1038(1038,"Номер смены"),
        T_1040(1040,"Номер ФД"),
        T_1041(1041,"Номер ФН"),
        T_1042(1042,"Номер чека за смену"),
        T_1043(1043,"Стоимость предмета расчета с учетом скидок и наценок"),
        T_1044(1044,"Операция банковского платежного агента"),
        T_1046(1046,"Наименование ОФД"),
        T_1048(1048,"Наименование пользователя"),
        T_1050(1050,"Признак исчерпания ресурса ФН"),
        T_1051(1051,"Признак необходимости срочной замены ФН"),
        T_1052(1052,"Признак заполнения памяти ФН"),
        T_1053(1053,"Признак превышения времени ожидания ответа ОФД"),
        T_1054(1054,"Признак расчета"),
        T_1055(1055,"Применяемая система налогообложения"),
        T_1056(1056,"Признак шифрования"),
        T_1057(1057,"Признак агента"),
        T_1059(1059,"Предмет расчета"),
        T_1060(1060,"Адрес сайта ФНС"),
        T_1062(1062,"Системы налогообложения"),
        T_1068(1068,"Сообщение оператора для ФН"),
        T_1073(1073,"Телефон платежного агента"),
        T_1074(1074,"Телефон оператора по приему платежей"),
        T_1075(1075,"Телефон оператора перевода"),
        T_1077(1077,"ФПД"),
        T_1078(1078,"ФПО"),
        T_1079(1079,"Цена за единицу предмета расчета с учетом скидок и наценок"),
        T_1081(1081,"Сумма по чеку (БСО) безналичными"),
        T_1084(1084,"Дополнительный реквизит пользователя"),
        T_1085(1085,"Наименование дополнительного реквизита пользователя"),
        T_1086(1086,"Значение дополнительного реквизита пользователя"),
        T_1097(1097,"Количество непереданных ФД"),
        T_1098(1098,"Дата первого из непереданных ФД"),
        T_1101(1101,"Код причины перерегистрации"),
        T_1102(1102,"Сумма НДС чека по ставке 20%"),
        T_1103(1103,"Сумма НДС чека по ставке 10%"),
        T_1104(1104,"Сумма расчета по чеку с НДС по ставке 0%"),
        T_1105(1105,"Сумма расчета по чеку без НДС"),
        T_1106(1106,"Сумма НДС чека по расч. ставке 20/120"),
        T_1107(1107,"Сумма НДС чека по расч. ставке 10/110"),
        T_1108(1108,"Признак ККТ для расчетов только в Интернет"),
        T_1109(1109,"Признак расчетов за услуги"),
        T_1110(1110,"Признак АС БСО"),
        T_1111(1111,"Общее количество ФД за смену"),
        T_1116(1116,"Номер первого непереданного документа"),
        T_1117(1117,"Адрес электронной почты отправителя чека"),
        T_1118(1118,"Количество кассовых чеков (БСО) за смену"),
        T_1126(1126,"Признак проведения лотереи"),
        T_1129(1129,"Счетчики операций \"приход\""),
        T_1130(1130,"Счетчики операций \"возврат прихода\""),
        T_1131(1131,"Счетчики операций \"расход\""),
        T_1132(1132,"Счетчики операций \"возврат расхода\""),
        T_1133(1133,"Счетчики операций по чекам коррекции (БСО коррекции)"),
        T_1134(1134,"Количество чеков (БСО) и чеков коррекции (БСО коррекции) со всеми признаками расчетов"),
        T_1135(1135,"Количество чеков (БСО) по признаку расчетов"),
        T_1136(1136,"Итоговая сумма в чеках (БСО) наличными денежными средствами"),
        T_1138(1138,"Итоговая сумма в чеках (БСО) безналичными"),
        T_1139(1139,"Сумма НДС по ставке 20%"),
        T_1140(1140,"Сумма НДС по ставке 10%"),
        T_1141(1141,"Сумма НДС по расч. ставке 20/120"),
        T_1142(1142,"Сумма НДС по расч. ставке 10/110"),
        T_1143(1143,"Сумма расчетов с НДС по ставке 0%"),
        T_1144(1144,"Количество чеков коррекции (БСО коррекции) или непереданных чеков (БСО) и чеков коррекции (БСО коррекции)"),
        T_1145(1145,"Счетчики по признаку \"приход\""),
        T_1146(1146,"Счетчики по признаку \"расход\""),
        T_1157(1157,"Счетчики итогов ФН"),
        T_1158(1158,"Счетчики итогов непереданных ФД"),
        T_1171(1171,"Телефон поставщика"),
        T_1173(1173,"Тип коррекции"),
        T_1174(1174,"Основание для коррекции"),
        T_1177(1177,"Наименование основания коррекции"),
        T_1178(1178,"Дата совершения корректируемого расчета"),
        T_1179(1179,"Номер предписания налогового органа/Номер документа основания"),
        T_1183(1183,"Сумма расчетов без НДС"),
        T_1187(1187,"Место расчетов"),
        T_1189(1189,"Версия ФФД ККТ"),
        T_1190(1190,"Версия ФФД ФН"),
        T_1191(1191,"Дополнительный реквизит предмета расчета"),
        T_1192(1192,"Дополнительный реквизит чека (БСО)"),
        T_1193(1193,"Признак проведения азартных игр"),
        T_1194(1194,"Счетчики итогов смены"),
        T_1196(1196,"QR-код"),
        T_1197(1197,"Единица измерения предмета расчета"),
        T_1198(1198,"Размер НДС за единицу предмета расчета"),
        T_1199(1199,"Ставка НДС"),
        T_1200(1200,"Сумма НДС за предмет расчета"),
        T_1201(1201,"Общая итоговая сумма в чеках (БСО)"),
        T_1203(1203,"ИНН кассира"),
        T_1205(1205,"Коды причин изменения сведений о ККТ"),
        T_1206(1206,"Сообщение оператора"),
        T_1207(1207,"Признак торговли подакцизными товарами"),
        T_1208(1208,"Сайт для получения чека"),
        T_1209(1209,"Номер версии ФФД"),
        T_1212(1212,"Признак предмета расчета"),
        T_1213(1213,"Ресурс ключей ФП"),
        T_1214(1214,"Признак способа расчета"),
        T_1215(1215,"Сумма по чеку (БСО) предоплатой (зачетом аванса и (или) предыдущих платежей)"),
        T_1216(1216,"Сумма по чеку (БСО) постоплатой (в кредит)"),
        T_1217(1217,"Сумма по чеку (БСО) встречным предоставлением"),
        T_1218(1218,"Итоговая сумма в чеках (БСО) предоплатами (авансами)"),
        T_1219(1219,"Итоговая сумма в чеках (БСО) постоплатами (кредитами)"),
        T_1220(1220,"Итоговая сумма в чеках (БСО) встречными предоставлениями"),
        T_1221(1221,"Признак установки принтера в автомате"),
        T_1222(1222,"Признак агента по предмету расчета"),
        T_1223(1223,"Данные агента"),
        T_1224(1224,"Данные поставщика"),
        T_1225(1225,"Наименование поставщика"),
        T_1226(1226,"ИНН поставщика"),
        T_1228(1228,"ИНН покупателя (клиента)"),
        T_1229(1229,"Акциз"),
        T_1230(1230,"Код страны происхождения товара"),
        T_1231(1231,"Номер декларации на товар"),
        T_1232(1232,"Счетчики по признаку \"возврат прихода\""),
        T_1233(1233,"Счетчики по признаку \"возврат расхода\""),
        T_1243(1243,"Дата рождения покупателя (клиента)"),
        T_1244(1244,"Гражданство"),
        T_1245(1245,"Код вида документа, удостоверяющего личность"),
        T_1246(1246,"Данные документа, удостоверяющего личность"),
        T_1254(1254,"Адрес покупателя (клиента)"),
        T_1256(1256,"Сведения о покупателе (клиенте)"),
        T_1260(1260,"Отраслевой реквизит предмета расчета"),
        T_1261(1261,"Отраслевой реквизит чека"),
        T_1262(1262,"Идентификатор ФОИВ"),
        T_1263(1263,"Дата документа основания"),
        T_1264(1264,"Номер документа основания"),
        T_1265(1265,"Значение отраслевого реквизита"),
        T_1270(1270,"Операционный реквизит чека"),
        T_1271(1271,"Идентификатор операции"),
        T_1272(1272,"Данные операции"),
        T_1273(1273,"Дата, время операции"),
        T_1274(1274,"Дополнительный реквизит OP"),
        T_1275(1275,"Дополнительные данные ОР"),
        T_1276(1276,"Дополнительный реквизит ООС"),
        T_1277(1277,"Дополнительные данные ООС"),
        T_1278(1278,"Дополнительный реквизит ОЗС"),
        T_1279(1279,"Дополнительные данные ОЗС"),
        T_1280(1280,"Дополнительный реквизит ОТР"),
        T_1281(1281,"Дополнительные данные ОТР"),
        T_1282(1282,"Дополнительный реквизит ОЗФН"),
        T_1283(1283,"Дополнительные данные ОЗФН"),
        T_1290(1290,"Признаки условий применения ККТ"),
        T_1291(1291,"Дробное количество маркированно го товара"),
        T_1292(1292,"Дробная часть"),
        T_1293(1293,"Числитель"),
        T_1294(1294,"Знаменатель"),
        T_1300(1300,"КТ Н"),
        T_1301(1301,"KT EAN-8"),
        T_1302(1302,"КТ ЕAN-13"),
        T_1303(1303,"КТ ITF-14"),
        T_1304(1304,"КТ GS1.0"),
        T_1305(1305,"KT GS1.M"),
        T_1306(1306,"КТК МК"),
        T_1307(1307,"КТ МИ"),
        T_1308(1308,"КТ ЕГАИС-2.0"),
        T_1309(1309,"КТ ЕГАИС-3.0"),
        T_1320(1320,"КТ Ф.1"),
        T_1321(1321,"КТ Ф.2"),
        T_1322(1322,"КТ Ф.3"),
        T_1323(1323,"КТ Ф.4"),
        T_1324(1324,"КТ Ф.5"),
        T_1325(1325,"КТ Ф.6"),
        T_2000(2000,"Код маркировки"),
        T_2001(2001,"Номер запроса"),
        T_2002(2002,"Номер уведомления"),
        T_2003(2003,"Планируемый статус товара"),
        T_2004(2004,"Результат проверки КМ"),
        T_2005(2005,"Результаты обработки запроса"),
        T_2006(2006,"Результаты обработки уведомления"),
        T_2007(2007,"Данные о маркирование м товаре"),
        T_2100(2100,"Тип кода маркировки"),
        T_2101(2101,"Идентификатор товара"),
        T_2102(2102,"Режим обработки кода маркировки"),
        T_2104(2104,"Количество непереданных уведомлений"),
        T_2105(2105,"Коды обработки запроса"),
        T_2106(2106,"Результат проверки сведений о товаре"),
        T_2107(2107,"Результаты проверки маркированны х товаров"),
        T_2108(2108,"Мера количества предмета расчета"),
        T_2109(2109,"Ответ ОИСМ о статусе товара"),
        T_2110(2110,"Присвоенный статус товара"),
        T_2111(2111,"Коды обработки уведомления"),
        T_2112(2112,"Признак некорректных кодов маркировки"),
        T_2113(2113,"Признак некорректных запросов и уведомлений"),
        T_2114(2114,"Дата и время запроса"),
        T_2115(2115,"Контрольный код КМ"),
        T_2116(2116,"Вид операции");
        private final int teg;
        private final String name;
        TEG(int teg, String nameTeg){
            this.teg = teg;
            this.name = nameTeg;
        }
        public static TEG T_(int tlv){
            TEG[] types = TEG.values();
            for (TEG s : types) {
                if (s.getTeg()==tlv){
                    return s;
                }
            }
            return T_0;
        }
        public static TEG T_(String tlv){
            return T_(Integer.parseInt(tlv));
        }
        public String getName(){
            return name;
        }
        public int getTeg() {
            return teg;
        }
        public String [] getHex(){
            String [] ret = new String[0];
            if (teg > 0){
                ret = toHexStringArrLe(teg);
            }
            return ret;
        }
        @Override
        public String toString() {
            return String.format("%s (%d)",name,teg);
        }
        public static int getCountTeg(){
            return (int)Arrays.stream(values()).count();
        }
        public String [] transformData(Object dataIn) throws FiscalAccException {
            String [] data = new String[0];
            if (teg==-1){
                if (((String)dataIn).length()==0){
                    data = toHexArrDate();
                }else {
                    for (int i = 0; i < ((String)dataIn).length(); i++) {
                        if (i%2==0){
                            data = bothArray(data,toHexStringArrLe(Integer.parseInt("" + ((String)dataIn).charAt(i) + ((String)dataIn).charAt(i+1))));
                        }
                    }
                }
            }else if(teg==-3){
                data = toHexStringArrLe((int)((double) dataIn*100),5,"00");
            }else if(teg==-2){
                SETTLEMENT_ATTRIBUTE sa;
                if (dataIn instanceof String){
                    sa = SETTLEMENT_ATTRIBUTE.define((String) dataIn);
                } else if (dataIn instanceof Integer || dataIn instanceof Double){
                    int i = (int)Math.round((double) dataIn);
                    sa = SETTLEMENT_ATTRIBUTE.define(i);
                } else{
                    sa = ((SETTLEMENT_ATTRIBUTE)dataIn);
                }
                data = sa.getHex();
            }else if(teg==-4){
                data = toHexStringArrLe((int)(dataIn),4,"00");
            }else if(teg==-5){
                data = toHexStringArrLe((int)(dataIn),1,"00");
            }else if(teg==-6){// запрос параметров регистрации
                Struct [] structs = (Struct[]) dataIn;
                for (Struct struct : structs) {
                    if (struct.getKey() == "num_reg") {
                        int val = (int) struct.getValue();
                        if (val != 0) data = bothArray(data, NT_BYTE.transformData(val));
                    } else if (struct.getKey() == "num_teg") {
                        String val = (String) struct.getValue();
                        if (val == null || val.equals("")) {
                            data = bothArray(data, new String[]{"FF", "FF"});
                        } else {
                            data = bothArray(data, toHexStringArrLe(Integer.parseInt(val), 2, "00"));
                        }
                    }
                }
            }else if(teg==0){
                throw new FiscalAccException(new Struct("Ошибка", "Токен не определен").toString());
            }else if(teg==1030){
                data = toArrStrHex(((String)dataIn));
            }else if(teg==1054){
                SETTLEMENT_ATTRIBUTE sa;
                if (dataIn instanceof String){
                    sa = SETTLEMENT_ATTRIBUTE.define((String) dataIn);
                } else if (dataIn instanceof Integer || dataIn instanceof Double){
                    sa = SETTLEMENT_ATTRIBUTE.define(((Double) dataIn).intValue());
                } else{
                    sa = ((SETTLEMENT_ATTRIBUTE)dataIn);
                }
                data = sa.getHex();
            }else if(teg==1209){
                FFD ffd;
                if (dataIn instanceof String){
                    ffd = FFD.define((String) dataIn);
                } else if (dataIn instanceof Integer || dataIn instanceof Double){
                    ffd = FFD.define(((Double) dataIn).intValue());
                } else{
                    ffd = ((FFD)dataIn);
                }
                data = ffd.getHex();
            }else if(teg==1055){
                SNO sno;
                if (dataIn instanceof String){
                    sno = SNO.define((String) dataIn);
                } else if (dataIn instanceof Integer || dataIn instanceof Double){
                    sno = SNO.define(((Double) dataIn).intValue());
                } else{
                    sno = ((SNO)dataIn);
                }
                data = sno.getHex();
            }else if(teg==1021 || teg==1177 || teg==1179){
                data = toArrStrHex((String) dataIn);
            }else if(teg==1178){
                long l = Long.parseLong((String)dataIn);
                data = toHexStringArrLe((int)(l),4,"00");
            }else if(teg==1059 || teg==1174){
                ArrayList<Struct> d = (ArrayList<Struct>)dataIn;
                for (Struct struct : d) {
                    TEG teg = (TEG) struct.getKey();
                    Object val = struct.getValue();
                    data = bothArray(data, teg.transformData(val));
                }
            }else if(teg==1199){
                VAT_RATE vr;
                if (dataIn instanceof String){
                    vr = VAT_RATE.define((String) dataIn);
                } else if (dataIn instanceof Integer || dataIn instanceof Double){
                    vr = VAT_RATE.define(((Double) dataIn).intValue());
                } else{
                    vr = ((VAT_RATE)dataIn);
                }
                data = vr.getHex();
            }else if(teg==1200||teg==1105||teg==1102||teg==1031||teg==1081||teg==1215||teg==1216||teg==1217){
                data = toHexStringArrLe((int)((double) dataIn*100));
            }else if(teg==1079||teg==1043){
                data = toHexStringArrLe((int)((double) dataIn*100),2,"00");
            }else if(teg==1173){
                int i = (int)Math.round((double) dataIn);
                data = toHexStringArrLe(i);
            }else if(teg==1212){
                SETTLEMENT_ITEM_ATTRIBUTE vr;
                if (dataIn instanceof String){
                    vr = SETTLEMENT_ITEM_ATTRIBUTE.define((String) dataIn);
                } else if (dataIn instanceof Integer || dataIn instanceof Double){
                    vr = SETTLEMENT_ITEM_ATTRIBUTE.define(((Double) dataIn).intValue());
                } else{
                    vr = ((SETTLEMENT_ITEM_ATTRIBUTE)dataIn);
                }
                data = vr.getHex();
            }else if(teg==1214){
                PAYMENT_METHOD vr;
                if (dataIn instanceof String){
                    vr = PAYMENT_METHOD.define((String) dataIn);
                } else if (dataIn instanceof Integer || dataIn instanceof Double){
                    vr = PAYMENT_METHOD.define(((Double) dataIn).intValue());
                } else{
                    vr = ((PAYMENT_METHOD)dataIn);
                }
                data = vr.getHex();
            }else if(teg==1023){
                String[] splitter = String.valueOf(dataIn).split("\\.");
                int count;
                String dig1 = splitter[0];
                String dig2 = "";
                if (splitter.length==2){
                    count = splitter[1].length();
                    dig2 = splitter[1];
                } else{
                    count = 0;
                }
                data = bothArray(toHexStringArrLe(count),toHexStringArrLe(Integer.parseInt(dig1+dig2)));
            }

            String [] arrSize = new String[0];
            if (teg > 0){
                int size = data.length;
                arrSize = toHexStringArrLe(size,2,"00");
            }
            return bothArray(bothArray(getHex(),arrSize),data);
        }
    }
    enum WARNING_FLAGS{
        F_0(0,"Нет предупреждений"),
        F_3(1,"Срочная замена КС (до окончания срока действия 3 дня)"),
        F_30(2, "Исчерпание ресурса КС (до окончания срока действия 30 дней)"),
        F_MEMORY_OVERFLOW(4,"Переполнение памяти ФН (Архив ФН заполнен на 90 %)"),
        F_RESPONSE_TIMED_OUT(8,"Превышено время ожидания ответа ОФД"),
        F_CRITICAL(64,"Критическая ошибка ФН");
        private final int kod;
        private final String name;
        WARNING_FLAGS(int warF, String name){
            this.kod = warF;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getFlag() {
            return kod;
        }
        static WARNING_FLAGS define(String warFHex){
            int war = Integer.parseInt(warFHex,16);
            WARNING_FLAGS[] types = WARNING_FLAGS.values();
            for (WARNING_FLAGS s : types) {
                if (s.getFlag()==war){
                    return s;
                }
            }
            return F_0;
        }
        public Struct getStruct(){
            return new Struct("Флаги предупреждения",new Struct(name,kod));
        }

        @Override
        public String toString() {
            return getStruct().toString();
        }
    }
    enum PHASE_LIFE{
        P_SETTINGS(0,"Настройка"),
        P_READY_FISCALIZATION(1,"Готовность к фискализации"),
        P_FISCAL_MODE(3,"Фискальный режим"),
        P_POST_FISCAL_MODE(7,"Постфискальный режим, идет передача ФД в ОФД"),
        P_READING_DATA(15,"Чтение данных из Архива ФН"),
        NONE(-1,"Не удалось определить");
        private final int kod;
        private final String name;
        PHASE_LIFE(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static PHASE_LIFE define(String kodHex){
            int war = Integer.parseInt(kodHex,16);
            PHASE_LIFE[] types = PHASE_LIFE.values();
            for (PHASE_LIFE s : types) {
                if (s.getKod()==war){
                    return s;
                }
            }
            return NONE;
        }
        public Struct getStruct(){
            return new Struct("Фаза жизни",new Struct(name,kod));
        }

        @Override
        public String toString() {
            return getStruct().toString();
        }
    }
    enum SHIFT_STATUS{
        S_CLOSE_SHIFT(0,"Смена закрыта"),
        S_OPEN_SHIFT(1,"Смена открыта"),
        NONE(-1,"Не удалось определить");
        private final int kod;
        private final String name;
        SHIFT_STATUS(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static SHIFT_STATUS define(String kodHex){
            int war = Integer.parseInt(kodHex,16);
            SHIFT_STATUS[] types = SHIFT_STATUS.values();
            for (SHIFT_STATUS s : types) {
                if (s.getKod()==war){
                    return s;
                }
            }
            return NONE;
        }
        public Struct getStruct(){
            return new Struct("Статус смены",new Struct(name,kod));
        }
        @Override
        public String toString() {
            return getStruct().toString();
        }
    }
    enum SETTLEMENT_ITEM_ATTRIBUTE {
        S_1(1,"товар"),
        S_2(2,"подакцизный товар"),
        S_3(3,"работа"),
        S_4(4,"услуга"),
        S_5(5,"ставка азартной игры"),
        S_6(6,"выигрыш азартной игры"),
        S_7(7,"лотерейный билет"),
        S_8(8,"выигрыш лотереи"),
        S_9(9,"предоставление рид"),
        S_10(10,"платеж"),
        S_11(11,"агентское вознаграждение"),
        S_12(12,"выплата"),
        S_13(13,"иной предмет расчета"),
        S_14(14,"имущественное право"),
        S_15(15,"внереализационный доход"),
        S_16(16,"страховые взносы"),
        S_17(17,"торговый сбор"),
        S_18(18,"курортный сбор"),
        S_19(19,"залог"),
        S_20(20,"расход"),
        S_21(21,"взносы на обязательное пенсионное страхование ип"),
        S_22(22,"взносы на обязательное пенсионное страхование"),
        S_23(23,"взносы на обязательное медицинское страхование ип"),
        S_24(24,"взносы на обязательное медицинское страхование"),
        S_25(25,"взносы на обязательное социальное страхование"),
        S_26(26,"платеж казино"),
        NONE(-1,"Не удалось определить");
        private final int kod;
        private final String name;
        SETTLEMENT_ITEM_ATTRIBUTE(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static SETTLEMENT_ITEM_ATTRIBUTE define(String kodHex) {
            return define(Integer.parseInt(kodHex,16));
        }
        static SETTLEMENT_ITEM_ATTRIBUTE define(int kod){
            SETTLEMENT_ITEM_ATTRIBUTE[] types = SETTLEMENT_ITEM_ATTRIBUTE.values();
            for (SETTLEMENT_ITEM_ATTRIBUTE s : types) {
                if (s.getKod()==kod){
                    return s;
                }
            }
            return NONE;
        }
        @Override
        public String toString() {
            return String.format("%s (%d)",name,kod);
        }
    }
    enum DATA_DOCUMENT{
        D_NO_DOCUMENT_DATA(0,"Нет данных документа"),
        D_RECEIVED_DOCUMENT_DATA(1,"Получены данные документа"),
        NONE(-1,"Не удалось определить");
        private final int kod;
        private final String name;
        DATA_DOCUMENT(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static DATA_DOCUMENT define(String kodHex){
            int war = Integer.parseInt(kodHex,16);
            DATA_DOCUMENT[] types = DATA_DOCUMENT.values();
            for (DATA_DOCUMENT s : types) {
                if (s.getKod()==war){
                    return s;
                }
            }
            return NONE;
        }
        public Struct getStruct(){
            return new Struct("Данные документа",new Struct(name,kod));
        }
        @Override
        public String toString() {
            return getStruct().toString();
        }
    }
    enum CURRENT_DOCUMENT{
        C_NONE_OPEN_DOCUMENT(0,"Нет открытого документа"),
        C_CRE_REGISTRATION_REPORT(1,"Отчёт о регистрации ККТ"),
        C_SHIFT_OPENING_REPORT(2,"Отчёт об открытии смены"),
        C_CASHIER_CHECK(4,"Кассовый чек"),
        C_SHIFT_CLOSING_REPORT(8,"Отчёт о закрытии смены"),
        C_FISCAL_CLOSURE_REPORT(10,"Отчёт о закрытии фискального режима"),
        C_REPORTING_LINE_FORM(11,"Бланк строкой отчетности"),
        C_REGISTRATION_CHANGE_WITH_REPLACEMENT(12,"Отчет об изменении параметров регистрации ККТ в связи с заменой ФН"),
        C_REGISTRATION_CHANGE(13,"Отчет об изменении параметров регистрации ККТ"),
        C_CASH_RECEIPT_CORRECTION(14,"Кассовый чек коррекции"),
        C_BSO_CORRECTION(15,"БСО коррекции"),
        C_REPORT_CURRENT_STATUS(17,"Отчет о текущем состоянии расчетов"),
        NONE(-1,"Не удалось определить");
        private final int kod;
        private final String name;
        CURRENT_DOCUMENT(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static CURRENT_DOCUMENT define(String kodHex){
            int war = Integer.parseInt(kodHex,16);
            CURRENT_DOCUMENT[] types = CURRENT_DOCUMENT.values();
            for (CURRENT_DOCUMENT s : types) {
                if (s.getKod()==war){
                    return s;
                }
            }
            return NONE;
        }
        public Struct getStruct(){
            return new Struct("Текущий документ",new Struct(name,kod));
        }

        @Override
        public String toString() {
            return getStruct().toString();
        }
    }
    enum DOCUMENT{
        D_CRE_REGISTRATION_REPORT(1,"Отчёт о регистрации ККТ"),
        D_SHIFT_OPENING_REPORT(2,"Отчёт об открытии смены"),
        D_CASHIER_CHECK(3,"Кассовый чек"),
        D_REPORTING_LINE_FORM(4,"Бланк строкой отчетности"),
        D_SHIFT_CLOSING_REPORT(5,"Отчёт о закрытии смены"),
        D_FISCAL_CLOSURE_REPORT(6,"Отчёт о закрытии фискального накопителя"),
        D_OPERATOR_CONFIRMATION(7,"Подтверждение оператора"),
        D_REGISTRATION_CHANGE(11,"Отчет об изменении параметров регистрации ККТ"),
        D_REPORT_CURRENT_STATUS(21,"Отчет о текущем состоянии расчетов"),
        D_CASH_RECEIPT_CORRECTION(31,"Кассовый чек коррекции"),
        D_BSO_CORRECTION(41,"Бланк строгой отчетности коррекции"),
        NONE(-1,"Не удалось определить");
        private final int kod;
        private final String name;
        DOCUMENT(int kod, String name){
            this.kod = kod;
            this.name = name;
        }
        String [] getHex(){
            return toHexStringArrLe(kod);
        }
        public int getKod() {
            return kod;
        }
        static DOCUMENT define(String [] kodHex){
            int war = Integer.parseInt(parseArrStrHexToStrLe(kodHex),16);
            DOCUMENT[] types = DOCUMENT.values();
            for (DOCUMENT s : types) {
                if (s.getKod()==war){
                    return s;
                }
            }
            return NONE;
        }
        public Struct getStruct(){
            return new Struct("Документ",new Struct(name,kod));
        }

        @Override
        public String toString() {
            return getStruct().toString();
        }
    }

    private final ArrayList<Struct> dataStruct;
    public DataFN() {
        hasWarningFlags = false;
        dataStruct = new ArrayList<>();
    }
    public DataFN(FiscalAcc.commands currentCommand) {
        this();
        this.currentCommand = currentCommand;
    }
    @Override
    public String toString() {
        String ret = "";
        if (currentCommand== FiscalAcc.commands.FN_READ_REG_TLV
                ||currentCommand== FiscalAcc.commands.FN_REQUEST_PARAM_FISC
                || currentCommand== FiscalAcc.commands.FN_READ_DOC_TLV){
            String [] d = getValue(DATA);
            if (d.length!=0){
                DataFN dataFN = new DataFN();
                dataFN.transformInputData(d,true);
                ret = String.format("  %-60s: %s", dataFN.getTlvName(), dataFN.getTlvData(dataFN.getTlvTeg()));
            }
        } else {
            ret = getArrStruct().toString();
        }
        return ret;
    }
    public void transformInputData(String [] inputData,boolean isTlv){
        this.isTlv = isTlv;
        if (inputData.length==0) return;
        if (isTlv){
            dataStruct.add(new Struct(TLV_TEG,Arrays.copyOfRange(inputData,0,2)));
            dataStruct.add(new Struct(SIZE,Arrays.copyOfRange(inputData,2,4)));
            int size = getSize();
            dataStruct.add(new Struct(DATA,Arrays.copyOfRange(inputData,4,size+4)));
        } else {
            dataStruct.add(new Struct(BEGIN,Arrays.copyOfRange(inputData,0,1)));
            dataStruct.add(new Struct(SIZE,Arrays.copyOfRange(inputData,1,3)));
            int size = getSize();
            dataStruct.add(new Struct(RESULT,Arrays.copyOfRange(inputData,3,4)));
            dataStruct.add(new Struct(DATA,Arrays.copyOfRange(inputData,4,size+3)));
            dataStruct.add(new Struct(CRC,Arrays.copyOfRange(inputData,size+3,size+5)));
            hasWarningFlags = CODE_RESPONSE.hasWarningFlags(getValue(RESULT)[0]);
        }
    }
//    private String [] transformParam(FiscalAcc.commands comm, String [] param){
//        String [] data = new String[0];
//        if (comm == FiscalAcc.commands.FN_REQUEST_PARAM_FISC) {
//            for (String s : param) {
//                data = bothArray(data, toHexStringArrLe(Integer.parseInt(s)));
//            }
//            if (param.length == 1) {
//                if (param[0].length() <= 2) {
//                    data = bothArray(data, new String[]{"FF", "FF"});
//                }
//            }
//        } else if (comm == FiscalAcc.commands.FN_DOC_TLV){
//            for (String s : param) {
//                data = bothArray(data, toHexStringArrLe(Integer.parseInt(s)));
//            }
//            String [] tmp = new String[4 - data.length];
//            Arrays.fill(tmp,"00");
//            data = bothArray(data,tmp);
//        } else if (comm== FiscalAcc.commands.FN_BEGIN_CLOSE_SHIFT){
//            data = toHexArrDate();
//        } else if (comm== FiscalAcc.commands.FN_BEGIN_OPEN_SHIFT){
//            data = toHexArrDate();
//        } else if (comm== FiscalAcc.commands.FN_TRANSFER_DOCUMENT){
//            data = param;
//        } else if (comm== FiscalAcc.commands.FN_CLOSE_SHIFT){
//            //нет параметров для этой команды
//        } else if (comm== FiscalAcc.commands.FN_REQUEST_TOTALS_UNSENT_DOCUMENTS){
//            //нет параметров для этой команды
//        } else if (comm== FiscalAcc.commands.FN_REGISTRATION_OPTIONS){
//            //нет параметров для этой команды
//        } else if (comm== FiscalAcc.commands.FN_OPEN_SHIFT){
//            //нет параметров для этой команды
//        } else if (comm== FiscalAcc.commands.FN_REQUEST_PARAM_CUR_SHIFT){
//            //нет параметров для этой команды
//        } else if (comm== FiscalAcc.commands.FN_COUNT_UNSENT_DOCUMENTS){
//            //data = param;
//        } else if (comm== FiscalAcc.commands.FN_TIME){
//            //нет параметров для этой команды
//        } else if (comm== FiscalAcc.commands.FN_BEGIN_CHECK){
//            data = param;
//        } else if (comm== FiscalAcc.commands.FN_END_CHECK){
//            data = param;
//        } else if (comm== FiscalAcc.commands.FN_OFD_ESTABLISH_TRANSPORT_CONNECTION){
//            data = param;
//        } else if (comm== FiscalAcc.commands.FN_OFD_START_READING_MESSAGES_OFD_SERVER){
//            data = param;
//        } else if (comm== FiscalAcc.commands.FN_OFD_READ_MESSAGE_BLOCK_OFD){
//            data = param;
//        } else if (comm== FiscalAcc.commands.FN_OFD_FINISH_READING_MESSAGE_OFD_SERVER){
//            data = param;
//        } else if (comm== FiscalAcc.commands.FN_OFD_TRANSFER_TO_FN_OF_RECEIPTS_FROM_OFD){
//            data = param;
//        } else if (comm== FiscalAcc.commands.FN_DOC){
//            data = param;
//        }
//        return data;
//    }
    public void setTlv(TEG tlv){
        isTlv = true;
        dataStruct.add(new Struct(TLV_TEG,tlv.getHex()));
    }
    private static String [] toArrStrHex(String data){
        byte [] bArr = data.getBytes(Charset.forName("CP866"));
        String [] sArr = new String[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            sArr[i] = Integer.toHexString(Byte.toUnsignedInt(bArr[i])).toUpperCase();
        }
        return sArr;
    }
    public void setTlvData(String dataTlv){
        int tlvTeg = getTlvTeg();
        String [] sArr = new String[0];
        String [] sizeArr = new String[0];
        if (tlvTeg==1030){
            sArr = toArrStrHex(dataTlv);
            int size = sArr.length;
            sizeArr = toHexStringArrLe(size,2,"00");
        }
        dataStruct.add(new Struct(SIZE,sizeArr));
        dataStruct.add(new Struct(DATA,sArr));
    }
    public void setTlvData(String [] dataTlv){
        int size = dataTlv.length;
        String [] sizeArr = toHexStringArrLe(size,2,"00");
        dataStruct.add(new Struct(SIZE,sizeArr));
        dataStruct.add(new Struct(DATA,dataTlv));
    }
    private static String [] toHexArrStr(String data){
        String [] ret = new String[data.length()/2];
        int q = 0;
        for (int i = 0; i < data.length(); i++) {
            ret[q] = String.format("%02X",data.charAt(i));
            q++;
        }
        return ret;
    }
    public void setData(String [] param){
        String [] beginMessage = new String[]{"04"};
        String [] size;
        String [] command = new String[1];
        String [] data = param;
        String [] crc;
        //получаем данные для отправки
        //data = transformParam(currentCommand,param);
        command[0] = toHexString(currentCommand.getCod());
        size = toHexStringArrLe(data.length+1,2,"00");
        int crcToSend = CRC16CCITT(parseArrToInt(bothArray(bothArray(size,command),data)));
        crc = toHexStringArrLe(crcToSend);
        dataStruct.add(new Struct(BEGIN,beginMessage));
        dataStruct.add(new Struct(SIZE,size));
        dataStruct.add(new Struct(COMMAND,command));
        dataStruct.add(new Struct(DATA,data));
        dataStruct.add(new Struct(CRC,crc));
    }
    private String toHexString(int d){
        String ret = Integer.toHexString(d);
        if (ret.length()%2!=0){
            ret = "0" + ret;
        }
        return ret;
    }
    private static String toDate(String [] arr){
        StringBuilder ret = new StringBuilder();
        for (int i = arr.length-1; i >= 0; i--) {
            ret.append(arr[i]);
        }
        return Instant.ofEpochSecond(Integer.parseInt(ret.toString(),16)).toString();
    }

    private static String [] toHexStringArrLe(int d){
        return toHexStringArrLe(d,0, "");
    }
    public int [] getDataToArrInt(){
        return parseArrToInt(getDataToArrStr());
    }
    public String [] getDataToArrStr(){
        String [] arrRetString = new String[0];
        for (Struct struct : dataStruct) {
            arrRetString = bothArray(arrRetString, (String[]) struct.getValue());
        }
        return arrRetString;
    }
    public static String [] toHexArrDate(){
        return toHexArrDate(0);
    }
    public static String [] toHexArrDate(long timestamp){
        String [] ret;
        if (timestamp==0){
            timestamp = new Date().getTime();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String timeString = new SimpleDateFormat("yyMMddHHmm").format(cal.getTime());
        ret = toArrStr(timeString);
        for (int i = 0; i < ret.length; i++) {
            ret[i] = String.format("%02x",Integer.parseInt(ret[i]));
        }
        return ret;
    }

    private static String [] toArrStr(String data){
        String [] ret = new String[data.length()/2];
        int c = 0;
        for (int i = 0; i < data.length(); i++) {
            if (i%2!=0){
                ret[c] = String.format("%s%s", data.charAt(i - 1), data.charAt(i));
                c++;
            }
        }
        return ret;
    }
    private static String [] toHexStringArrLe(int d,int toSize, String fill){
        String ret = Integer.toHexString(d);
        if (ret.length()%2!=0){
            ret = "0" + ret;
        }
        String [] arr = new String[ret.length()/2];
        //Arrays.fill(arr,fill);
        int g = 0;
        for (int i = ret.length()-1; i >=0 ; i--) {
            if (i%2==0){
                arr[g] = "" + ret.charAt(i)+ret.charAt(i+1);
                g++;
            }
        }
        if (toSize!=0 & !fill.equals("")){
            String [] tmp = new String[toSize-arr.length];
            Arrays.fill(tmp, fill);
            arr = bothArray(arr,tmp);
        }
        return arr;
    }
    public ArrayList<Struct> getArrStruct(){
        ArrayList<Struct> ret = new ArrayList<>();
        if (hasWarningFlags) ret.add(new Struct("Внимание","Есть флаги предупреждения!"));
        if (isTlv){
            int tlv = getTlvTeg();
            if (tlv == 1059){
                ArrayList<DataFN> arrayList = getStructure(getValue());
                for (DataFN f : arrayList) {
                    ret.add(new Struct(f.getTlvName(), f.getTlvData(f.getTlvTeg())));
                }
            } else{
                ret.add(new Struct(getTlvName(),getTlvData(tlv)));
            }
        } else {
            String [] val = getValue(DATA);
            if (!isSuccessfulCommandExecution()){
                ret.addAll(getError());
                return ret;
            }
            if (val.length==0)return ret;

            if (FiscalAcc.commands.FN_SERIAL_NUMBER == currentCommand){
                ret.add(new Struct("Номер ФН",toChar()));
            } else if (FiscalAcc.commands.FN_REQUEST_PARAM_CUR_SHIFT == currentCommand){
                ret.add(SHIFT_STATUS.define(val[0]).getStruct());
                ret.add(new Struct("Номер смены",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,1,3)))));
                ret.add(new Struct("Номер чека",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,3,val.length)))));
            } else if (FiscalAcc.commands.FN_TIME == currentCommand){
                ret.add(new Struct("Дата/время ФН",toDate(val)));
            } else if (FiscalAcc.commands.FN_REQUEST_PARAM_FISC == currentCommand){
                if (val.length!=0){
                    DataFN dataFN = new DataFN();
                    dataFN.transformInputData(val,true);
                    ret.add(new Struct(dataFN.getTlvName(), dataFN.getTlvData(dataFN.getTlvTeg())));
                }
            } else if (FiscalAcc.commands.FN_COUNT_UNSENT_DOCUMENTS == currentCommand){
                ret.add(new Struct("Количество ФД",Long.parseLong(parseArrStrHexToStrLe(val))));
            } else if (FiscalAcc.commands.FN_REGISTRATION_OPTIONS == currentCommand){
                Calendar calendar = new GregorianCalendar(Integer.parseInt(val[0],16),Integer.parseInt(val[1],16)-1,Integer.parseInt(val[2],16),Integer.parseInt(val[3],16),Integer.parseInt(val[4],16));
                ret.add(new Struct("Дата/время регистрации",new SimpleDateFormat("dd-MM-yy HH:mm").format(calendar.getTime())));
                ret.add(new Struct("ИНН пользователя",toChar(Arrays.copyOfRange(val,5,17))));
                ret.add(new Struct("ИНН ОФД",toChar(Arrays.copyOfRange(val,17,29))));
                ret.add(new Struct("Регистрационный номер",toChar(Arrays.copyOfRange(val,29,val.length-1))));
            } else if (FiscalAcc.commands.FN_REQUEST_TOTALS_UNSENT_DOCUMENTS == currentCommand){
                ret.add(new Struct("Общее количество неотправленных чеков",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,0,4)))));
                ret.add(new Struct("Количество чеков приход",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,4,8)))));
                ret.add(new Struct("Сумма чеков приход",toPrice(Arrays.copyOfRange(val,8,14))/100d));
                ret.add(new Struct("Количество чеков возврат прихода",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,14,18)))));
                ret.add(new Struct("Сумма чеков возврат прихода",toPrice(Arrays.copyOfRange(val,18,24))/100d));
                ret.add(new Struct("Количество чеков расход",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,24,28)))));
                ret.add(new Struct("Сумма чеков расход",toPrice(Arrays.copyOfRange(val,28,34))/100d));
                ret.add(new Struct("Количество чеков возврат расхода",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,34,38)))));
                ret.add(new Struct("Сумма чеков возврат расхода",toPrice(Arrays.copyOfRange(val,38,43))/100d));
            } else if (FiscalAcc.commands.FN_VERSION == currentCommand){
                String [] arr = Arrays.copyOfRange(val,0,15);
                String versionPO = toChar(arr);
                String typePO = "";
                if(val[16].equals("00")) typePO = "Отладочная версия";
                else typePO = "Серийная версия";
                ret.add(new Struct("Версия ПО ФН",new Struct(versionPO,arr)));
                ret.add(new Struct("Тип ПО ФН",new Struct(typePO,val[16])));
            } else if (FiscalAcc.commands.FN_RECENT_ERRORS == currentCommand){
                ret.add(new Struct("Ошибки",val));
            } else if (FiscalAcc.commands.FN_END_CHECK == currentCommand){
                ret.add(new Struct("Номер чека",Integer.parseInt(parseArrStrHexToStrLe(Arrays.copyOfRange(val,0,2)))));
                ret.add(new Struct("Номер ФД",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,2,6)))));
                ret.add(new Struct("Фискальный признак",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,6,10)))));
            } else if (FiscalAcc.commands.FN_VALIDITY == currentCommand){
                Calendar calendar = new GregorianCalendar(Integer.parseInt(val[0],16),Integer.parseInt(val[1],16)-1,Integer.parseInt(val[2],16));
                String dt = new SimpleDateFormat("dd-MM-yy").format(calendar.getTime());
                int remainingQuantity = Integer.parseInt(val[3],16);
                int numReg = Integer.parseInt(val[4],16);
                ret.add(new Struct("Срок действия ФН",dt));
                ret.add(new Struct("Оставшееся количество возможности сделать отчет о Регистрации (перерегистрации) ККТ",remainingQuantity));
                ret.add(new Struct("Кол-во уже сделанных отчетов о регистрации (перерегистрации) ККТ",numReg));
            } else if (FiscalAcc.commands.FN_OPEN_SHIFT == currentCommand){
                ArrayList<Struct> tmp = new ArrayList<>();
                tmp.add(new Struct("Номер смены",Integer.parseInt(parseArrStrHexToStrLe(Arrays.copyOfRange(val,0,2)))));
                tmp.add(new Struct("Номер ФД",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,2,6)))));
                tmp.add(new Struct("Фискальный признак",parseArrStrHexToStrLe(Arrays.copyOfRange(val,6,val.length))));
                ret.add(new Struct("Смена успешно открыта",tmp));
            } else if (FiscalAcc.commands.FN_STATUS == currentCommand){
                ret.add(PHASE_LIFE.define(val[0]).getStruct());
                ret.add(CURRENT_DOCUMENT.define(val[1]).getStruct());
                ret.add(DATA_DOCUMENT.define(val[2]).getStruct());
                ret.add(SHIFT_STATUS.define(val[3]).getStruct());
                ret.add(WARNING_FLAGS.define(val[4]).getStruct());
                if(Integer.parseInt(val[5],16)!=0){
                    Calendar calendar = new GregorianCalendar(Integer.parseInt(val[5],16),Integer.parseInt(val[6],16)-1,Integer.parseInt(val[7],16),Integer.parseInt(val[8],16),Integer.parseInt(val[9],16));
                    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm");
                    ret.add(new Struct("Дата и время последнего документа",df.format(calendar.getTime())));
                } else {
                    ret.add(new Struct("Дата и время последнего документа","Нет документов"));
                }
                ret.add(new Struct("Номер ФН",toChar(Arrays.copyOfRange(val,10,26))));
                ret.add(new Struct("Номер последнего ФД",parseArrStrHexToStrLe(Arrays.copyOfRange(val,26,val.length))));
            } else if (FiscalAcc.commands.FN_CLOSE_SHIFT == currentCommand){
                ret.add(new Struct("Смена успешно закрыта",""));
                ret.add(new Struct("Номер смены",Integer.parseInt(parseArrStrHexToStrLe(Arrays.copyOfRange(val,0,2)))));
                ret.add(new Struct("Номер ФД",Long.parseLong(parseArrStrHexToStrLe(Arrays.copyOfRange(val,2,6)))));
                ret.add(new Struct("Фискальный признак",parseArrStrHexToStrLe(Arrays.copyOfRange(val,6,val.length))));
            } else if (FiscalAcc.commands.FN_DOC_TLV == currentCommand){
                int size = Integer.parseInt(parseArrStrHexToStrLe(Arrays.copyOfRange(val,2,4)));
                ret.add(DOCUMENT.define(Arrays.copyOfRange(val,0,2)).getStruct());
                ret.add(new Struct("Длина документа",size));
            } else if (FiscalAcc.commands.FN_DOC == currentCommand){
                int docType = Integer.parseInt(val[0],16);
                ret.add(DOCUMENT.define(Arrays.copyOfRange(val,0,1)).getStruct());
                if (Integer.parseInt(val[1],16)==1){
                    ret.add(new Struct("Получена квитанция из ОФД","Да"));
                } else {
                    ret.add(new Struct("Получена квитанция из ОФД","Нет"));
                }
                Calendar calendar = new GregorianCalendar(Integer.parseInt(val[2],16),Integer.parseInt(val[3],16)-1,Integer.parseInt(val[4],16),Integer.parseInt(val[5],16),Integer.parseInt(val[6],16));
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm");
                ret.add(new Struct("Дата/время",df.format(calendar.getTime())));
                ret.add(new Struct("Номер ФД чека",parseArrStrHexToStrLe(Arrays.copyOfRange(val,7,11))));
                ret.add(new Struct("Фискальный признак",parseArrStrHexToStrLe(Arrays.copyOfRange(val,11,15))));
                if (docType==5 || docType==2) {
                    ret.add(new Struct("Номер смены", parseArrStrHexToStrLe(Arrays.copyOfRange(val, 15, 17))));
                } else if(docType==3){
                    if (Integer.parseInt(val[15])==1){
                        ret.add(new Struct("Тип операции","Приход"));
                    } else {
                        ret.add(new Struct("Тип операции","Расход"));
                    }
                    ret.add(new Struct("Сумма", toPrice(Arrays.copyOfRange(val, 16, 21))/100f));
                } else if(docType==6 || docType==11|| docType==1){
                    ret.add(new Struct("ИНН", toChar(Arrays.copyOfRange(val, 15, 27))));
                    ret.add(new Struct("Регистрационный номер ККТ", toChar(Arrays.copyOfRange(val, 27, 47))));
                    if (docType==11 || docType==1){
                        ret.add(new Struct("Код налогообложения", toChar(Arrays.copyOfRange(val, 47, 48))));
                        ret.add(new Struct("Режим работы", toChar(Arrays.copyOfRange(val, 48, 49))));
                        if(docType==11){
                            ret.add(new Struct("Код причины перерегистрации", toChar(Arrays.copyOfRange(val, 49, 50))));
                        }
                    }
                }
            }else{
                ret.add(new Struct("Какие то не понятные данные",val));
            }
        }
        return ret;
    }
    private String toChar(String [] arr){
        return toChar(arr,StandardCharsets.ISO_8859_1);
    }
    private String toChar(String [] arr, Charset ch){
        StringBuilder ret = new StringBuilder();
        for (String s : arr) {
            ret.append((char) Integer.parseInt(s, 16));
        }
        return new String(ret.toString().getBytes(ch),Charset.forName("866"));
    }
    public String toChar(){
        return toChar(getValue(DATA));
    }
    private long toPrice(String [] arr){
        StringBuilder dig = new StringBuilder();
        for (int i = arr.length-1; i >=0 ; i--) {
            dig.append(arr[i]);
        }
        return Long.parseLong(dig.toString(),16);
    }
    public String getTlvData(int teg){
        String ret = "";
        String [] data = getValue(DATA);
        if(teg==1030 || teg==1041 || teg==1021 || teg==1017 || teg==1187 || teg==1009 || teg==1048 || teg==1018 || teg==1037 || teg==1013 || teg==1046|| teg==1060|| teg==1117|| teg==1188) {
            ret = toChar(data);
        } else if(teg==1079||teg==1043|| teg==1200 || teg==1020 || teg==1105|| teg==1102|| teg==1031|| teg==1081|| teg==1215|| teg==1216|| teg==1217) {
            ret = ((Float)(toPrice(data)/100f)).toString();
        } else if(teg==1199) {
            ret = VAT_RATE.define(data[0]).toString();
        } else if(teg==1212) {
            ret = SETTLEMENT_ITEM_ATTRIBUTE.define(data[0]).toString();
        } else if(teg==1023) {
            String [] arr = Arrays.copyOfRange(data,1,data.length);
            ret = String.valueOf(toPrice(arr)/Math.pow(10,Integer.parseInt(data[0],16)));
        } else if(teg==1012) {
            ret = toDate(data);
        } else if(teg==1101) {
            ret = REASON_REGISTERING.define(data).toString();
        } else if(teg==1221||teg==1207||teg==1056||teg==1001||teg==1002||teg==1109||teg==1110||teg==1108||teg==1193||teg==1126) {
            ret = toBool(parseArrStrHexToStrLe(data));
        } else if(teg==1055 || teg==1062) {
            ret = SNO.define(data).toString();
        } else if(teg==1209||teg==1189) {
            ret = FFD.define(data).toString();
        } else if(teg==1054) {
            ret = SETTLEMENT_ATTRIBUTE.define(data[0]).toString();
        } else if(teg==1077) {
            ret = parseArrStrHexToStr(Arrays.copyOfRange(data,2,data.length));
        } else if(teg==1040||teg==1038 ||teg==1042) {
            ret = parseArrStrHexToStrLe(data);
        } else if(teg==1059) {
            ArrayList<DataFN> dArrStr = getStructure(getValue(DATA));
            for (DataFN dataFN : dArrStr) {
                ret += dataFN;
            }
        } else if(teg==1214) {
            ret = PAYMENT_METHOD.define(data[0]).toString();
        } else {
            ret = "Обработчик не найден:" + Arrays.toString(data);
        }
        return ret;
    }
    public static String toBool(String b){
        if (b.equals("0"))return "нет";
        return "да";
    }
    public static String parseArrStrHexToStrLe(String [] arr){
        StringBuilder ret = new StringBuilder();
        for (int i = arr.length-1; i >=0 ; i--) {
            ret.append(arr[i]);
        }
        return String.valueOf(Long.parseLong(ret.toString(),16));
    }
    public static String parseArrStrHexToStr(String [] arr){
        StringBuilder ret = new StringBuilder();
        for (String s : arr) {
            ret.append(s);
        }
        return String.valueOf(Long.parseLong(ret.toString(),16));
    }
    public static ArrayList<DataFN> getStructure(String [] dataIn){
        ArrayList<DataFN> ret = new ArrayList<>();
        int size = dataIn.length;
        for (int i = 0; i < size; ) {
            DataFN dAll = new DataFN();
            dAll.transformInputData(Arrays.copyOfRange(dataIn,i,size),true);
            int s= i+dAll.getSize() + 4;
            DataFN dRes = new DataFN();
            dRes.transformInputData(Arrays.copyOfRange(dataIn,i,s),true);
            i=s;
            ret.add(dRes);
        }
        return ret;
    }
    public boolean isValid(){
        if (isTlv)return false;
        String [] crc = getValue(CRC);
        String [] d = bothArray(getValue(SIZE), getValue(RESULT));
        d= bothArray(d, getValue(DATA));
        return CRC16CCITT(parseArrToInt(d))== parseInt(crc[1] + crc[0],16);
    }
    public int [] parseArrToInt(String [] arr){
        int [] ret = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ret[i]= parseInt(arr[i],16);
        }
        return ret;
    }
    public int [] parseArrToIntLe(String [] arr){
        int [] ret = new int[arr.length];
        int d = 0;
        for (int i = arr.length - 1; i >=0 ; i--) {
            ret[d]= parseInt(arr[i],16);
            d++;
        }
        return ret;
    }
    public static String [] bothArray(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String [] c = (String[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
    public int getTlvTeg(){
        String [] val = getValue(DataFN.TLV_TEG);
        return Integer.parseInt(val[1] + val[0],16);
    }
    public String getTlvName(){
        int tlv = getTlvTeg();
        return TEG.T_(tlv).getName();
    }
    public String [] getValue(String key){
        for (Struct struct : dataStruct) {
            if (key.toLowerCase().equals(struct.getKey())) {
                return (String[]) struct.getValue();
            }
        }
        return null;
    }
    public String [] getValue(){
        String[] ret = new String[0];
        for (Struct struct : dataStruct) {
            ret = bothArray(ret, (String[]) struct.getValue());
        }
        return ret;
    }
    public boolean isSuccessfulCommandExecution(){
        return CODE_RESPONSE.isSuccessExec(getValue(RESULT)[0]);
    }
    private int CRC16CCITT(int [] arr){
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

    public ArrayList<Struct> getError() {
        ArrayList<Struct> ret = new ArrayList<>();
        if (isTlv){
           ret.add(new Struct("Ошибка","Входящие данные содержат структуру"));
           return ret;
        }else return DataFN.CODE_RESPONSE.getError(getValue(RESULT)[0]);
    }

    public boolean isBeginData(){
        if (isTlv)return false;
        return getValue(BEGIN)[0].equals("04");
    }
//    private String codeResponse(String dataC){
//        if (isTlv)return "Эта структура без результата выполнения";
//        return CODE_RESPONSE.define(dataC).toString();
//    }

    public int getSize(){
        String [] sizeArr = getValue(SIZE);
        return Integer.parseInt(sizeArr[1] + sizeArr[0],16);
    }
    public static int getSize(String [] arr){
        return Integer.parseInt(arr[2] + arr[1],16);
    }

    public static byte [] toByteArr(String [] arr){
        byte[] ret = new byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ret[i] = (byte)Integer.parseInt(arr[i],16);
        }
        return ret;
    }
    public static String [] toStringArr(byte [] arr){
        String [] ret = new String[arr.length];

        for (int i = 0; i < arr.length; i++) {
            ret[i] = String.format("%02X", Byte.toUnsignedInt(arr[i]));
        }
        return ret;
    }
}
