import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.itgroup.evo.fn.Struct;
import ru.itgroup.evo.jsonclass.CommandFN;

public class GsonTest {
    public static void main(String args[]) {

        GsonTest tester = new GsonTest();
        try {
            CommandFN commandFN = new CommandFN();
            ArrayList<Struct> structs = new ArrayList<>();

            ArrayList<Struct> s1059 = new ArrayList<>();
            s1059.add(new Struct("1030","товар 1"));
            s1059.add(new Struct("1079",50));
            structs.add(new Struct("1059",s1059));

            s1059 = new ArrayList<>();
            s1059.add(new Struct("1030","товар 2"));
            s1059.add(new Struct("1079",62));
            structs.add(new Struct("1059",s1059));

           // commandFN.setReceiptBody(structs);
            tester.writeJSON(commandFN);
            CommandFN commandFN1 = tester.readJSON();
            System.out.println(commandFN1);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void writeJSON(CommandFN student) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        FileWriter writer = new FileWriter("student.json");
        System.out.println(gson.toJson(student));
        writer.write(gson.toJson(student));
        writer.close();
    }

    private CommandFN readJSON() throws FileNotFoundException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader("student.json"));

        CommandFN student = gson.fromJson(bufferedReader, CommandFN.class);
        return student;
    }
}

