import java.io.*;

public class Parser {
    int count = 0;
    public Parser(String arg) throws IOException {
        parse(arg);
    }

    private void parse(String fileIn) throws IOException {
        BufferedReader stream = new BufferedReader(new FileReader(fileIn));

        BufferedWriter writer = new BufferedWriter(new FileWriter("parsed" + count + ".txt"));
        String line = "";
        while((line = stream.readLine()) != null)
        {
            String[] split = line.split(":\\s*");
            writer.write(split[1] + ",");
        }
        stream.close();
        writer.close();
    }

    public static void main(String args[]) throws IOException {
        Parser parser = new Parser(args[0]);

    }
}
