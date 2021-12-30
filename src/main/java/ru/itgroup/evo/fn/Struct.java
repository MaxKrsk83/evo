package ru.itgroup.evo.fn;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

public class Struct {
    private Object key;
    private Object value;

    @Override
    public String toString() {

        StringBuilder ret = new StringBuilder(key + " : ");
        if (value instanceof int []){
            for (int i = 0; i < ((int[]) value).length; i++) {
                ret.append(Integer.toHexString(((int[]) value)[i])).append(" ");
            }
        }else if(value instanceof ArrayList){
            ret = new StringBuilder(String.format("%s:%n", key));
            for (int i = 0; i < ((ArrayList)value).size(); i++) {
                ret.append(String.format("   %-60s", ((ArrayList<Struct>) value).get(i).toString()));
            }
        }else if(value instanceof String []){
            ret = new StringBuilder(String.format("%-20s:%s", key, Arrays.toString(((String[]) value))));
        }
        else{
            ret = new StringBuilder(String.format("%n%-60s: %s", key, value));
        }
        return ret.toString();
    }
    public Struct(Object key, Object value) {
        this.key = key;
        this.value = value;
    }
    public Struct() {
    }
    public Object getKey() {
        return key;
    }
    public Object getValue() {
        return value;
    }
    public void setKey(Object key) {
        this.key = key;
    }
    public void setValue(Object value,Charset ch){
        byte [] b = value.toString().getBytes(ch);
        setValue(new String(b,Charset.forName("866")));
    }
    public void setValue(Object value) {
        this.value = value;
    }
}
