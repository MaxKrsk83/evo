package ru.itgroup.evo.fn;

import java.util.ArrayList;

public class Protocol {
    private static ArrayList<Struct> result = new ArrayList<>();
    private Protocol() {
    }
    public static void add(boolean isIn, String[] data) {
        if (isIn){
            result.add(new Struct("Входящие данные <-----",data));
        }else{
            result.add(new Struct("Исходящие данные ---->",data));
        }
    }
    public static void printProtocol() {
        System.out.println("Начало протокола-----------------------------------");
        for (Struct struct : result) {
            System.out.printf("%-23s:[",struct.getKey());
            String [] val = (String[])struct.getValue();
            for (int i = 0; i < val.length; i++) {
                if (i%24==0 & i!=0){
                    System.out.printf("]%n  %-23S","");
                }
                System.out.printf(" %S",val[i]);
            }
            System.out.println(" ]");
        }
        System.out.println("Конец протокола------------------------------------");
    }
    public static void clearProtocol(){
        result.clear();
    }
}
